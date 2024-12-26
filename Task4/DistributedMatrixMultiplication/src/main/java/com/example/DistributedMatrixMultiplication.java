package com.example;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.config.Config;
import com.hazelcast.cluster.Member;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class DistributedMatrixMultiplication {
    static class MatrixBlock implements Serializable {
        double[][] data;
        int rowStart, colStart;
        public MatrixBlock(double[][] data, int rowStart, int colStart) {
            this.data = data;
            this.rowStart = rowStart;
            this.colStart = colStart;
        }
        public double[][] getData() {
            return this.data;
        }
        public int getRowStart() {
            return this.rowStart;
        }
        public int getColStart() {
            return this.colStart;
        }

    }

    static class MatrixMultiplicationTask implements Callable<MatrixBlock>, Serializable {
        private MatrixBlock aBlock;
        private MatrixBlock bBlock;
        private int commonSize;
        private int resultRowStart;
        private int resultColStart;
        public MatrixMultiplicationTask(MatrixBlock aBlock, MatrixBlock bBlock, int commonSize, int resultRowStart, int resultColStart) {
            this.aBlock = aBlock;
            this.bBlock = bBlock;
            this.commonSize = commonSize;
            this.resultRowStart = resultRowStart;
            this.resultColStart = resultColStart;
        }

        @Override
        public MatrixBlock call() {
            int aRows = aBlock.data.length;
            int aCols = aBlock.data[0].length;
            int bRows = bBlock.data.length;
            int bCols = bBlock.data[0].length;
            double[][] result = new double[aRows][bCols];

            if (aCols != bRows) {
                throw new IllegalArgumentException("Invalid matrix dimensions: A cols and B rows need to be equal.");
            }


            for (int i = 0; i < aRows; i++) {
                for (int j = 0; j < bCols; j++) {
                    for (int k = 0; k < aCols; k++) {
                        result[i][j] += aBlock.data[i][k] * bBlock.data[k][j];
                    }
                }
            }

            return new MatrixBlock(result, resultRowStart, resultColStart);
        }
    }
    public static double[][] multiplyMatricesDistributed(double[][] a, double[][] b, int blockSize, IExecutorService executorService, HazelcastInstance instance) throws ExecutionException, InterruptedException {

        int rowsA = a.length;
        int colsA = a[0].length;
        int rowsB = b.length;
        int colsB = b[0].length;

        if (colsA != rowsB) {
            throw new IllegalArgumentException("Invalid matrix dimensions: A cols and B rows need to be equal.");
        }

        int resultRows = rowsA;
        int resultCols = colsB;
        double[][] result = new double[resultRows][resultCols];
        List<Future<MatrixBlock>> futures = new ArrayList<>();

        for (int i = 0; i < rowsA; i += blockSize) {
            for (int j = 0; j < colsB; j += blockSize) {
                for(int k = 0; k < colsA; k+= blockSize){
                    int aBlockRowsEnd = Math.min(i + blockSize, rowsA);
                    int aBlockColsEnd = Math.min(k + blockSize, colsA);
                    int bBlockRowsEnd = Math.min(k + blockSize, rowsB);
                    int bBlockColsEnd = Math.min(j + blockSize, colsB);


                    double[][] aBlockData = getSubMatrix(a, i, k, aBlockRowsEnd, aBlockColsEnd);
                    double[][] bBlockData = getSubMatrix(b, k, j, bBlockRowsEnd, bBlockColsEnd);
                    int commonSize = bBlockData.length;

                    MatrixBlock aBlock = new MatrixBlock(aBlockData, i, k);
                    MatrixBlock bBlock = new MatrixBlock(bBlockData, k, j);

                    // Send tasks to all members
                    MatrixMultiplicationTask task = new MatrixMultiplicationTask(aBlock, bBlock, commonSize, i, j);
                    for(Member member: instance.getCluster().getMembers()){
                        Future<MatrixBlock> future = executorService.submitToMember(task, member);
                        futures.add(future);
                    }
                }
            }
        }

        for (Future<MatrixBlock> future : futures) {
            MatrixBlock blockResult = future.get();
            double[][] blockData = blockResult.getData();
            int resultRowStart = blockResult.getRowStart();
            int resultColStart = blockResult.getColStart();

            for (int i = 0; i < blockData.length; i++) {
                for (int j = 0; j < blockData[0].length; j++) {
                    result[resultRowStart + i][resultColStart + j] += blockData[i][j];
                }
            }
        }

        return result;
    }

    static double[][] getSubMatrix(double[][] matrix, int rowStart, int colStart, int rowEnd, int colEnd){
        int rows = rowEnd - rowStart;
        int cols = colEnd - colStart;
        double[][] subMatrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                subMatrix[i][j] = matrix[rowStart + i][colStart + j];
            }
        }
        return subMatrix;
    }
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        String localAddress = System.getProperty("hazelcast.local.localAddress");
        if (localAddress == null || localAddress.isEmpty()) {
            System.setProperty("hazelcast.local.localAddress", "192.168.1.194");
        }
        Config config = new Config();
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true)
                .addMember("192.168.1.194")
                .addMember("192.168.1.253");
        config.getNetworkConfig().setPortAutoIncrement(false);
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
        IExecutorService executorService = instance.getExecutorService("default");
        System.out.println("Node Started: " + instance.getCluster().getLocalMember().getAddress());

        // Check if this is the first member of the cluster
        if (instance.getCluster().getMembers().stream().findFirst().get().localMember()) {
            System.out.println("This node is the first member of the cluster. Starting distributed calculations.");

            // Sample matrices
            int rows = 1000;
            int cols = 1000;

            double[][] matrixA = new double[rows][cols];
            double[][] matrixB = new double[rows][cols];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    matrixA[i][j] = Math.random();
                    matrixB[i][j] = Math.random();
                }
            }


            int blockSize = 100;

            long startTime = System.currentTimeMillis();
            double[][] resultMatrix = multiplyMatricesDistributed(matrixA, matrixB, blockSize, executorService, instance);
            long endTime = System.currentTimeMillis();
            System.out.println("Distributed Multiplication Time: " + (endTime - startTime) + "ms");
        } else {
            System.out.println("This node is a cluster member, waiting for the main node to trigger calculation.  Address: " + instance.getCluster().getLocalMember().getAddress());
        }

    }
}
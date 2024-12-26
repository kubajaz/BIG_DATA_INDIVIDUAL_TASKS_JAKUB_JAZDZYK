package com.example;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.config.Config;
import com.hazelcast.cluster.Member;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class DistributedMatrixMultiplication {


    static class MatrixMultiplicationTask implements Callable<double[][]>, Serializable {
        private int commonSize;
        private int resultRowStart;
        private int resultColStart;
        private String taskId;
        private int aRows;
        private int bCols;
        private int colsA;


        public MatrixMultiplicationTask(int commonSize, int resultRowStart, int resultColStart, String taskId, int aRows, int bCols, int colsA) {
            this.commonSize = commonSize;
            this.resultRowStart = resultRowStart;
            this.resultColStart = resultColStart;
            this.taskId = taskId;
            this.aRows = aRows;
            this.bCols = bCols;
            this.colsA = colsA;
        }

        @Override
        public double[][] call() {
            HazelcastInstance instance = Hazelcast.getHazelcastInstanceByName("dev");
            String address = instance.getCluster().getLocalMember().getAddress().toString();
            System.out.println("Obliczam mnożenie bloku macierzy, " + taskId + " na węźle: " + address);
            double[][] matrixA = generateMatrix(aRows, colsA, resultRowStart, 0);
            double[][] matrixB = generateMatrix(colsA, bCols, 0, resultColStart);


            int aRows = matrixA.length;
            int aCols = matrixA[0].length;
            int bRows = matrixB.length;
            int bCols = matrixB[0].length;
            double[][] result = new double[aRows][bCols];

            if (aCols != bRows) {
                throw new IllegalArgumentException("Invalid matrix dimensions: A cols and B rows need to be equal.");
            }

            for (int i = 0; i < aRows; i++) {
                for (int j = 0; j < bCols; j++) {
                    for (int k = 0; k < aCols; k++) {
                        result[i][j] += matrixA[i][k] * matrixB[k][j];
                    }
                }
            }
            return result;
        }
        private double[][] generateMatrix(int rows, int cols, int rowStart, int colStart){
            double[][] matrix = new double[rows][cols];
            for(int i = 0; i< rows; i++){
                for(int j= 0; j < cols; j++){
                    matrix[i][j] = Math.random();
                }
            }
            return matrix;
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
        List<Future<double[][]>> futures = new ArrayList<>();
        List<Member> members = new ArrayList<>(instance.getCluster().getMembers());
        int numMembers = members.size();
        int taskCounter = 0;


        for (int i = 0; i < rowsA; i += blockSize) {
            for(int j = 0; j < colsB; j += blockSize) {
                int aBlockRowsEnd = Math.min(i + blockSize, rowsA);
                int bBlockColsEnd = Math.min(j + blockSize, colsB);

                String taskId = String.valueOf(taskCounter++);

                MatrixMultiplicationTask task = new MatrixMultiplicationTask(colsA, i, j, taskId, aBlockRowsEnd - i, bBlockColsEnd - j, colsA );
                //Distribute evenly between members
                Future<double[][]> future = executorService.submitToMember(task, members.get(taskCounter % numMembers));
                futures.add(future);
            }
        }

        for (Future<double[][]> future : futures) {
            double[][] blockResult = future.get();
            int rows = blockResult.length;
            int cols = blockResult[0].length;
            int resultRowStart = Integer.parseInt(future.toString().split("resultRowStart=")[1].split(",")[0]);
            int resultColStart = Integer.parseInt(future.toString().split("resultColStart=")[1].split(",")[0]);
            for(int i= 0; i < rows; i++){
                for(int j =0; j< cols; j++){
                    result[resultRowStart+i][resultColStart+j] += blockResult[i][j];
                }
            }
        }

        return result;
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

        // Wait for the cluster to have at least two members
        while (instance.getCluster().getMembers().size() < 2) {
            System.out.println("Waiting for the second member to join the cluster...");
            Thread.sleep(1000);
        }
        if (instance.getCluster().getMembers().stream().findFirst().get().localMember()) {
            System.out.println("This node is the first member of the cluster. Starting distributed calculations.");

            // Sample matrices
            int rows = 2000;
            int cols = 2000;

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
            System.out.println("This node is a cluster member, waiting for the main node to trigger calculation. Address: " + instance.getCluster().getLocalMember().getAddress());
        }
    }
}
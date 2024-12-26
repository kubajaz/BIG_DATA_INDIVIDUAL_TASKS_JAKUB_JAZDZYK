package com.example;

import com.hazelcast.core.*;
import com.hazelcast.config.Config;
import com.hazelcast.cluster.Member;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class DistributedMatrixMultiplication {

    static class MatrixMultiplicationTask implements Callable<double[][]>, Serializable {
        private final double[][] matrixA;
        private final double[][] matrixB;
        private final int rowStart;
        private final int rowEnd;
        private final int colStart;
        private final int colEnd;

        public MatrixMultiplicationTask(double[][] matrixA, double[][] matrixB, int rowStart, int rowEnd, int colStart, int colEnd) {
            this.matrixA = matrixA;
            this.matrixB = matrixB;
            this.rowStart = rowStart;
            this.rowEnd = rowEnd;
            this.colStart = colStart;
            this.colEnd = colEnd;
        }

        @Override
        public double[][] call() {
            int rows = rowEnd - rowStart;
            int cols = colEnd - colStart;
            int commonSize = matrixA[0].length;

            double[][] result = new double[rows][cols];

            for (int i = rowStart; i < rowEnd; i++) {
                for (int j = colStart; j < colEnd; j++) {
                    for (int k = 0; k < commonSize; k++) {
                        result[i - rowStart][j - colStart] += matrixA[i][k] * matrixB[k][j];
                    }
                }
            }

            return result;
        }
    }

    public static double[][] multiplyMatricesDistributed(double[][] a, double[][] b, int blockSize, IExecutorService executorService, List<Member> members) throws ExecutionException, InterruptedException {
        int rowsA = a.length;
        int colsA = a[0].length;
        int rowsB = b.length;
        int colsB = b[0].length;

        if (colsA != rowsB) {
            throw new IllegalArgumentException("Matrix dimensions do not match for multiplication.");
        }

        double[][] result = new double[rowsA][colsB];
        List<Future<double[][]>> futures = new ArrayList<>();
        int numMembers = members.size();
        int taskCounter = 0;

        for (int i = 0; i < rowsA; i += blockSize) {
            for (int j = 0; j < colsB; j += blockSize) {
                int rowEnd = Math.min(i + blockSize, rowsA);
                int colEnd = Math.min(j + blockSize, colsB);

                MatrixMultiplicationTask task = new MatrixMultiplicationTask(a, b, i, rowEnd, j, colEnd);
                Member targetMember = members.get(taskCounter % numMembers);
                futures.add(executorService.submitToMember(task, targetMember));
                taskCounter++;
            }
        }

        for (int f = 0; f < futures.size(); f++) {
            Future<double[][]> future = futures.get(f);
            double[][] partialResult = future.get();

            int i = (f / (colsB / blockSize)) * blockSize;
            int j = (f % (colsB / blockSize)) * blockSize;
            int rows = partialResult.length;
            int cols = partialResult[0].length;

            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    result[i + row][j + col] = partialResult[row][col];
                }
            }
        }

        return result;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Config config = new Config();
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true)
                .addMember("192.168.1.194")
                .addMember("192.168.1.253");
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
        IExecutorService executorService = instance.getExecutorService("default");

        while (instance.getCluster().getMembers().size() < 2) {
            System.out.println("Waiting for other members...");
            Thread.sleep(1000);
        }

        int size = 1000;
        double[][] matrixA = new double[size][size];
        double[][] matrixB = new double[size][size];

        Random rand = new Random();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrixA[i][j] = rand.nextDouble();
                matrixB[i][j] = rand.nextDouble();
            }
        }

        long startTime = System.currentTimeMillis();
        double[][] result = multiplyMatricesDistributed(matrixA, matrixB, 100, executorService, new ArrayList<>(instance.getCluster().getMembers()));
        long endTime = System.currentTimeMillis();

        System.out.println("Distributed multiplication completed in: " + (endTime - startTime) + "ms");
    }
}

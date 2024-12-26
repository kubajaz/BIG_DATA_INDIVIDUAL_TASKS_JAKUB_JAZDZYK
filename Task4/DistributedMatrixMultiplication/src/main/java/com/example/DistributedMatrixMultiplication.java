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
        private final int rowStart;
        private final int rowEnd;
        private final int colStart;
        private final int colEnd;
        private final int colsA;

        public MatrixMultiplicationTask(int rowStart, int rowEnd, int colStart, int colEnd, int colsA) {
            this.rowStart = rowStart;
            this.rowEnd = rowEnd;
            this.colStart = colStart;
            this.colEnd = colEnd;
            this.colsA = colsA;
        }
        @Override
        public double[][] call() {
            HazelcastInstance instance = null;
            while (instance == null) {
                instance = Hazelcast.getHazelcastInstanceByName("dev");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            double[][] matrixA = generateMatrix(rowEnd - rowStart, colsA, rowStart, 0);
            double[][] matrixB = generateMatrix(colsA, colEnd - colStart, 0, colStart);
            int rows = rowEnd - rowStart;
            int cols = colEnd - colStart;
            int commonSize = colsA;

            double[][] result = new double[rows][cols];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    for (int k = 0; k < commonSize; k++) {
                        result[i][j] += matrixA[i][k] * matrixB[k][j];
                    }
                }
            }
            return result;
        }
        private double[][] generateMatrix(int rows, int cols, int rowStart, int colStart){
            double[][] matrix = new double[rows][cols];
            for(int i= 0; i < rows; i++){
                for(int j= 0; j< cols; j++){
                    matrix[i][j] = Math.random();
                }
            }
            return matrix;
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
        int numMembers = members.size();
        int taskCounter = 0;


        for (int i = 0; i < rowsA; i += blockSize) {
            for (int j = 0; j < colsB; j += blockSize) {
                int rowEnd = Math.min(i + blockSize, rowsA);
                int colEnd = Math.min(j + blockSize, colsB);
                MatrixMultiplicationTask task = new MatrixMultiplicationTask(i, rowEnd, j, colEnd, colsA);
                Member targetMember = members.get(taskCounter % numMembers);
                Future<double[][]> future = executorService.submitToMember(task, targetMember);
                double[][] partialResult = future.get();
                for (int row = 0; row < partialResult.length; row++) {
                    for (int col = 0; col < partialResult[0].length; col++) {
                        result[i + row][j + col] = partialResult[row][col];
                    }
                }
                taskCounter++;
            }
        }
        return result;
    }

    // Metoda główna
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Config config = new Config();
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true)
                .addMember("192.168.1.194")
                .addMember("192.168.1.44");
        config.setProperty("hazelcast.operation.call.timeout.millis", "500000"); // Zwiększenie limitu czasu operacji
        config.setProperty("hazelcast.operation.response.timeout.millis", "500000"); // Zwiększenie czasu odpowiedzi
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);

        IExecutorService executorService = instance.getExecutorService("default");

        // Oczekiwanie na dołączenie węzłów
        while (instance.getCluster().getMembers().size() < 2) {
            System.out.println("Waiting for other members...");
            Thread.sleep(1000);
        }

        try {
            int size = 1200; // Rozmiar macierzy
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
            double[][] result = multiplyMatricesDistributed(matrixA, matrixB, 300, executorService, new ArrayList<>(instance.getCluster().getMembers()));
            long endTime = System.currentTimeMillis();

            System.out.println("Distributed multiplication completed in: " + (endTime - startTime) + "ms");
        } catch (Exception e) {
            System.err.println("An error occurred during matrix multiplication: " + e.getMessage());
            e.printStackTrace();
        } finally {
            instance.shutdown();
        }
    }
}
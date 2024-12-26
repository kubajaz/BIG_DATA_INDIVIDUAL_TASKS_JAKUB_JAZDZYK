package com.example;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.config.Config;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MatrixMultiplication {


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
    public static double[][] multiplyMatricesSingleNode(double[][] a, double[][] b, int blockSize, IExecutorService executorService) throws ExecutionException, InterruptedException {
        int rowsA = a.length;
        int colsA = a[0].length;
        int rowsB = b.length;
        int colsB = b[0].length;

        if (colsA != rowsB) {
            throw new IllegalArgumentException("Matrix dimensions do not match for multiplication.");
        }

        double[][] result = new double[rowsA][colsB];
        List<Future<double[][]>> futures = new ArrayList<>();
        int taskCounter = 0;

        for (int i = 0; i < rowsA; i += blockSize) {
            for (int j = 0; j < colsB; j += blockSize) {
                int rowEnd = Math.min(i + blockSize, rowsA);
                int colEnd = Math.min(j + blockSize, colsB);

                MatrixMultiplicationTask task = new MatrixMultiplicationTask(a, b, i, rowEnd, j, colEnd);
                futures.add(executorService.submit(task));
                taskCounter++;
            }
        }

        // Pobieranie wyników z zadań
        for (int f = 0; f < futures.size(); f++) {
            Future<double[][]> future = futures.get(f);
            double[][] partialResult = future.get();
            int i = (f / (colsB / blockSize)) * blockSize;
            int j = (f % (colsB / blockSize)) * blockSize;
            int rows = partialResult.length;
            int cols = partialResult[0].length;
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    result[i + row][j + col] += partialResult[row][col];
                }
            }
        }
        return result;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Config config = new Config();
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);

        IExecutorService executorService = instance.getExecutorService("default");


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
            double[][] result = multiplyMatricesSingleNode(matrixA, matrixB, 300, executorService);
            long endTime = System.currentTimeMillis();

            System.out.println("Single-node multiplication completed in: " + (endTime - startTime) + "ms");
        } catch (Exception e) {
            System.err.println("An error occurred during matrix multiplication: " + e.getMessage());
            e.printStackTrace();
        } finally {
            instance.shutdown();
        }
    }
}
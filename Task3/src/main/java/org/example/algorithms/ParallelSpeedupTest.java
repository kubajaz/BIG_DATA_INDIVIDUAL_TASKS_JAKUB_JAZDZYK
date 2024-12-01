package org.example.algorithms;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ParallelSpeedupTest {

    public static void main(String[] args) {
        int matrixSize = 100;
        int[][] matrixA = new int[matrixSize][matrixSize];
        int[][] matrixB = new int[matrixSize][matrixSize];
        fillMatrix(matrixA);
        fillMatrix(matrixB);

        int[] threadCounts = {1, 2, 4, 8, 16};

        long serialTime = measureSerialExecutionTime(matrixA, matrixB);

        System.out.println("Serial Execution Time: " + serialTime + " ns");

        for (int threads : threadCounts) {
            long parallelTime = measureParallelExecutionTime(matrixA, matrixB, threads);
            double speedupPerThread = ((double) serialTime / parallelTime) * threads;

            System.out.printf("Threads: %d, Parallel Execution Time: %d ns, Speedup per Thread: %.2f\n",
                    threads, parallelTime, speedupPerThread);
        }
    }

    private static void fillMatrix(int[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                matrix[i][j] = (int) (Math.random() * 10);
            }
        }
    }

    private static long measureSerialExecutionTime(int[][] matrixA, int[][] matrixB) {
        long startTime = System.nanoTime();
        MatrixMultiplicationNormal.multiply(matrixA, matrixB);
        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    private static long measureParallelExecutionTime(int[][] matrixA, int[][] matrixB, int threadCount) {
        long startTime = System.nanoTime();

        int size = matrixA.length;
        int[][] result = new int[size][size];
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < size; i++) {
            final int row = i;
            executor.execute(() -> {
                for (int j = 0; j < size; j++) {
                    for (int k = 0; k < size; k++) {
                        result[row][j] += matrixA[row][k] * matrixB[k][j];
                    }
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long endTime = System.nanoTime();
        return endTime - startTime;
    }
}

package org.example.algorithms;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        int[][] matrixA = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };

        int[][] matrixB = {
                {9, 8, 7},
                {6, 5, 4},
                {3, 2, 1}
        };

        System.out.println("Normal Matrix Multiplication:");
        int[][] normalResult = MatrixMultiplicationNormal.multiply(matrixA, matrixB);
        printMatrix(normalResult);

        System.out.println("\nParallel Matrix Multiplication:");
        int[][] parallelResult = MatrixMultiplicationParallel.multiply(matrixA, matrixB);
        printMatrix(parallelResult);

        System.out.println("\nVectorized Matrix Multiplication:");
        int[][] vectorizedResult = MatrixMultiplicationVextorized.multiply(matrixA, matrixB);
        printMatrix(vectorizedResult);
    }

    private static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            System.out.println(Arrays.toString(row));
        }
    }
}

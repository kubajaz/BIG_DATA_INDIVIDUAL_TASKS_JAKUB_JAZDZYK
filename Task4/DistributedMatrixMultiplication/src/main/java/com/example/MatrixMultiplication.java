package com.example;

import java.util.Random;

class MatrixMultiplication {

    public static double[][] multiplyMatrices(double[][] a, double[][] b) {
        int rowsA = a.length;
        int colsA = a[0].length;
        int rowsB = b.length;
        int colsB = b[0].length;

        if (colsA != rowsB) {
            throw new IllegalArgumentException("Matrix dimensions do not match for multiplication.");
        }

        double[][] result = new double[rowsA][colsB];

        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                for (int k = 0; k < colsA; k++) {
                    result[i][j] += a[i][k] * b[k][j];
                }
            }
        }

        return result;
    }

    public static void main(String[] args) {
        int size = 2400; // Rozmiar macierzy
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
        double[][] result = multiplyMatrices(matrixA, matrixB);
        long endTime = System.currentTimeMillis();

        System.out.println("Sequential multiplication completed in: " + (endTime - startTime) + "ms");
    }
}
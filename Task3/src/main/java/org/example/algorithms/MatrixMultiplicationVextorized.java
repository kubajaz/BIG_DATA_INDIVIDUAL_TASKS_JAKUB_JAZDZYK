package org.example.algorithms;

public class MatrixMultiplicationVextorized {

    public static int[][] multiply(int[][] matrixA, int[][] matrixB) {
        // Check if multiplication is possible
        if (matrixA[0].length != matrixB.length) {
            throw new IllegalArgumentException("Matrix dimensions do not match for multiplication.");
        }

        int[][] result = new int[matrixA.length][matrixB[0].length];

        for (int i = 0; i < matrixA.length; i++) {
            for (int j = 0; j < matrixB[0].length; j++) {
                result[i][j] = vectorizedDotProduct(matrixA[i], getColumn(matrixB, j));
            }
        }

        return result;
    }

    /**
     * Compute the dot product of two vectors
     * @param row the row vector from matrix A
     * @param column the column vector from matrix B
     * @return the dot product
     */
    private static int vectorizedDotProduct(int[] row, int[] column) {
        int sum = 0;
        for (int k = 0; k < row.length; k++) {
            sum += row[k] * column[k];
        }
        return sum;
    }

    /**
     * Extract a column from a matrix
     * @param matrix the matrix to extract the column from
     * @param colIndex the index of the column to extract
     * @return the column as an array
     */
    private static int[] getColumn(int[][] matrix, int colIndex) {
        int[] column = new int[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            column[i] = matrix[i][colIndex];
        }
        return column;
    }
}

package org.example.algorithms;

public class StandardMatrixMultiplication {
    public static int[][] multiply(int[][] A, int[][] B) {
        int n = A.length;
        int m = B[0].length;
        int p = B.length;
        int[][] result = new int[n][m];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                for (int k = 0; k < p; k++) {
                    result[i][j] += A[i][k] * B[k][j];
                }
            }
        }
        return result;
    }
}

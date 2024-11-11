package org.example.algorithms;

public class UnrolledMatrixMultiplication {
    public static int[][] multiply(int[][] A, int[][] B) {
        int n = A.length;
        int m = B[0].length;
        int p = B.length;
        int[][] result = new int[n][m];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                int sum = 0;
                for (int k = 0; k < p; k += 4) { // Loop unrolling by 4
                    sum += A[i][k] * B[k][j];
                    if (k + 1 < p) sum += A[i][k + 1] * B[k + 1][j];
                    if (k + 2 < p) sum += A[i][k + 2] * B[k + 2][j];
                    if (k + 3 < p) sum += A[i][k + 3] * B[k + 3][j];
                }
                result[i][j] = sum;
            }
        }
        return result;
    }
}

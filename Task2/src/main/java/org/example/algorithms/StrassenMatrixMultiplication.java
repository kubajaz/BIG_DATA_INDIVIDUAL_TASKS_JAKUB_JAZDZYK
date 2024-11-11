package org.example.algorithms;

public class StrassenMatrixMultiplication {

    // Main method to call matrix multiplication
    public static int[][] multiply(int[][] A, int[][] B) {
        int n = A.length;
        int[][] R = new int[n][n];
        strassen(A, B, R);
        return R;
    }

    private static void strassen(int[][] A, int[][] B, int[][] R) {
        int n = A.length;

        // Base condition for 1x1 matrices
        if (n == 1) {
            R[0][0] = A[0][0] * B[0][0];
            return;
        }

        // Create sub-matrices
        int[][] A11 = new int[n / 2][n / 2];
        int[][] A12 = new int[n / 2][n / 2];
        int[][] A21 = new int[n / 2][n / 2];
        int[][] A22 = new int[n / 2][n / 2];
        int[][] B11 = new int[n / 2][n / 2];
        int[][] B12 = new int[n / 2][n / 2];
        int[][] B21 = new int[n / 2][n / 2];
        int[][] B22 = new int[n / 2][n / 2];

        // Split matrices A and B into sub-matrices
        split(A, A11, 0, 0);
        split(A, A12, 0, n / 2);
        split(A, A21, n / 2, 0);
        split(A, A22, n / 2, n / 2);

        split(B, B11, 0, 0);
        split(B, B12, 0, n / 2);
        split(B, B21, n / 2, 0);
        split(B, B22, n / 2, n / 2);

        // Compute seven auxiliary matrices M1-M7
        int[][] M1 = multiply(add(A11, A22), add(B11, B22));
        int[][] M2 = multiply(add(A21, A22), B11);
        int[][] M3 = multiply(A11, sub(B12, B22));
        int[][] M4 = multiply(A22, sub(B21, B11));
        int[][] M5 = multiply(add(A11, A12), B22);
        int[][] M6 = multiply(sub(A21, A11), add(B11, B12));
        int[][] M7 = multiply(sub(A12, A22), add(B21, B22));

        // Combine results from M1-M7 into the resulting matrix C
        int[][] C11 = add(sub(add(M1, M4), M5), M7);
        int[][] C12 = add(M3, M5);
        int[][] C21 = add(M2, M4);
        int[][] C22 = add(sub(add(M1, M3), M2), M6);

        // Combine the four sub-matrices into the result matrix R
        join(C11, R, 0, 0);
        join(C12, R, 0, n / 2);
        join(C21, R, n / 2, 0);
        join(C22, R, n / 2, n / 2);
    }

    // Method for adding matrices
    private static int[][] add(int[][] A, int[][] B) {
        int n = A.length;
        int[][] C = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j] + B[i][j];
            }
        }
        return C;
    }

    // Method for subtracting matrices
    private static int[][] sub(int[][] A, int[][] B) {
        int n = A.length;
        int[][] C = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j] - B[i][j];
            }
        }
        return C;
    }

    // Method for splitting a matrix into sub-matrices
    private static void split(int[][] P, int[][] C, int iB, int jB) {
        for (int i = 0; i < C.length; i++) {
            System.arraycopy(P[iB + i], jB, C[i], 0, C.length);
        }
    }

    // Method for joining sub-matrices into the result matrix
    private static void join(int[][] C, int[][] P, int iB, int jB) {
        for (int i = 0; i < C.length; i++) {
            System.arraycopy(C[i], 0, P[iB + i], jB, C.length);
        }
    }

    // Main function for testing
    public static void main(String[] args) {
        System.out.println("Strassen algorithm implementation for matrix multiplication:");

        int N = 4;

        int[][] A = {
                {1, 2, 3, 4},
                {4, 3, 0, 1},
                {5, 6, 1, 1},
                {0, 2, 5, 6}
        };

        int[][] B = {
                {1, 0, 5, 1},
                {1, 2, 0, 2},
                {0, 3, 2, 3},
                {1, 2, 1, 2}
        };

        int[][] C = multiply(A, B);

        System.out.println("Result of matrix multiplication of A and B:");
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                System.out.print(C[i][j] + " ");
            }
            System.out.println();
        }
    }
}

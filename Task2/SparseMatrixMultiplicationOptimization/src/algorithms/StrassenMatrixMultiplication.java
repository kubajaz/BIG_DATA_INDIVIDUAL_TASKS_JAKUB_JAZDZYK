package algorithms;

public class StrassenMatrixMultiplication {
    public static int[][] multiply(int[][] A, int[][] B) {
        int n = A.length;
        return strassen(A, B, n);
    }

    private static int[][] strassen(int[][] A, int[][] B, int size) {
        if (size == 1) {
            return new int[][]{{A[0][0] * B[0][0]}};
        }

        int newSize = size / 2;
        int[][] result = new int[size][size];

        // Initialize submatrices
        int[][] a11 = new int[newSize][newSize];
        int[][] a12 = new int[newSize][newSize];
        int[][] a21 = new int[newSize][newSize];
        int[][] a22 = new int[newSize][newSize];
        int[][] b11 = new int[newSize][newSize];
        int[][] b12 = new int[newSize][newSize];
        int[][] b21 = new int[newSize][newSize];
        int[][] b22 = new int[newSize][newSize];

        // Divide A and B into submatrices
        split(A, a11, 0, 0);
        split(A, a12, 0, newSize);
        split(A, a21, newSize, 0);
        split(A, a22, newSize, newSize);

        split(B, b11, 0, 0);
        split(B, b12, 0, newSize);
        split(B, b21, newSize, 0);
        split(B, b22, newSize, newSize);

        // Calculate the seven products (Strassen's method)
        int[][] m1 = strassen(add(a11, a22), add(b11, b22), newSize);
        int[][] m2 = strassen(add(a21, a22), b11, newSize);
        int[][] m3 = strassen(a11, subtract(b12, b22), newSize);
        int[][] m4 = strassen(a22, subtract(b21, b11), newSize);
        int[][] m5 = strassen(add(a11, a12), b22, newSize);
        int[][] m6 = strassen(subtract(a21, a11), add(b11, b12), newSize);
        int[][] m7 = strassen(subtract(a12, a22), add(b21, b22), newSize);

        // Combine results into the resulting matrix
        int[][] c11 = add(subtract(add(m1, m4), m5), m7);
        int[][] c12 = add(m3, m5);
        int[][] c21 = add(m2, m4);
        int[][] c22 = add(subtract(add(m1, m3), m2), m6);

        // Combine submatrices into one
        join(c11, result, 0, 0);
        join(c12, result, 0, newSize);
        join(c21, result, newSize, 0);
        join(c22, result, newSize, newSize);

        return result;
    }

    // Helper method to add two matrices
    private static int[][] add(int[][] A, int[][] B) {
        int n = A.length;
        int[][] result = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = A[i][j] + B[i][j];
            }
        }
        return result;
    }

    // Helper method to subtract two matrices
    private static int[][] subtract(int[][] A, int[][] B) {
        int n = A.length;
        int[][] result = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = A[i][j] - B[i][j];
            }
        }
        return result;
    }

    // Helper method to split parent matrix into child matrices
    private static void split(int[][] parent, int[][] child, int row, int col) {
        for (int i = 0; i < child.length; i++) {
            System.arraycopy(parent[row + i], col, child[i], 0, child.length);
        }
    }

    // Helper method to join child matrices into the parent matrix
    private static void join(int[][] child, int[][] parent, int row, int col) {
        for (int i = 0; i < child.length; i++) {
            System.arraycopy(child[i], 0, parent[row + i], col, child.length);
        }
    }
}

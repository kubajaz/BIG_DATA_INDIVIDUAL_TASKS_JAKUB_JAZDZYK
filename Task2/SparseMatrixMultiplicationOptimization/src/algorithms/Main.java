package algorithms;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // Define test matrices
        int[][] matrixA = {{1, 2}, {3, 4}};
        int[][] matrixB = {{5, 6}, {7, 8}};

        // Test Standard Matrix Multiplication
        int[][] standardResult = StandardMatrixMultiplication.multiply(matrixA, matrixB);
        printMatrix(standardResult, "Standard Matrix Multiplication");

        // Test Unrolled Matrix Multiplication
        int[][] unrolledResult = UnrolledMatrixMultiplication.multiply(matrixA, matrixB);
        printMatrix(unrolledResult, "Unrolled Matrix Multiplication");

        // Test Sparse Matrix Multiplication
        Map<String, Integer> sparseA = new HashMap<>();
        sparseA.put("0,1", 2);
        sparseA.put("1,0", 3);
        sparseA.put("1,1", 4);

        Map<String, Integer> sparseB = new HashMap<>();
        sparseB.put("0,0", 5);
        sparseB.put("0,1", 6);
        sparseB.put("1,0", 7);
        sparseB.put("1,1", 8);

        Map<String, Integer> sparseResult = SparseMatrixMultiplication.multiply(sparseA, sparseB, 2, 2, 2);
        printSparseMatrix(sparseResult, "Sparse Matrix Multiplication");

        // Test Unrolled Sparse Matrix Multiplication
        Map<String, Integer> unrolledSparseResult = UnrolledSparseMatrixMultiplication.multiply(sparseA, sparseB, 2, 2, 2);
        printSparseMatrix(unrolledSparseResult, "Unrolled Sparse Matrix Multiplication");

        // Test Strassen's Algorithm
        int[][] strassenResult = StrassenMatrixMultiplication.multiply(matrixA, matrixB);
        printMatrix(strassenResult, "Strassen's Matrix Multiplication");
    }

    private static void printMatrix(int[][] matrix, String label) {
        System.out.println(label);
        for (int[] row : matrix) {
            for (int value : row) {
                System.out.print(value + " ");
            }
            System.out.println();
        }
    }

    private static void printSparseMatrix(Map<String, Integer> matrix, String label) {
        System.out.println(label);
        for (Map.Entry<String, Integer> entry : matrix.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
}

package org.example.algorithms;

import java.io.*;
import java.util.*;

public class Mc2depiComparison {

    public static void main(String[] args) throws IOException {
        String filePath = "/home/kuba/Documents/ERASMUS/BIG_DATA_INDIVIDUAL_TASKS_JAKUB_JAZDZYK/Task2/mc2depi/mc2depi.mtx";
        Map<String, Integer> matrix = loadSparseMatrixFromFile(filePath);

        testSparseAlgorithm("SparseMatrixMultiplication", matrix, matrix);
        testSparseAlgorithm("UnrolledSparseMatrixMultiplication", matrix, matrix);
    }

    private static Map<String, Integer> loadSparseMatrixFromFile(String filePath) throws IOException {
        Map<String, Integer> matrix = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(filePath));

        String line;
        boolean header = true;

        while ((line = br.readLine()) != null) {
            if (header) {
                if (line.startsWith("%")) continue;
                header = false;
            }

            String[] parts = line.split("\\s+");
            if (parts.length == 3) {
                int row = Integer.parseInt(parts[0]) - 1; 
                int col = Integer.parseInt(parts[1]) - 1;
                int value = Integer.parseInt(parts[2]);

                matrix.put(row + "," + col, value);
            }
        }

        br.close();
        return matrix;
    }

    private static void testSparseAlgorithm(String algorithm, Map<String, Integer> A, Map<String, Integer> B) {
        long startTime = System.nanoTime();
        long startMemory = getMemoryUsage();

        Map<String, Integer> result = null;

        switch (algorithm) {
            case "SparseMatrixMultiplication":
                result = SparseMatrixMultiplication.multiply(A, B, A.size(), B.size(), B.size());
                break;
            case "UnrolledSparseMatrixMultiplication":
                result = UnrolledSparseMatrixMultiplication.multiply(A, B, A.size(), B.size(), B.size());
                break;
        }

        long endTime = System.nanoTime();
        long endMemory = getMemoryUsage();

        System.out.println(algorithm + " Results:");
        System.out.println("Time taken: " + (endTime - startTime) + " nanoseconds");
        System.out.println("Memory used: " + (endMemory - startMemory) + " bytes");
        System.out.println();
    }

    private static long getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        return runtime.totalMemory() - runtime.freeMemory();
    }
}

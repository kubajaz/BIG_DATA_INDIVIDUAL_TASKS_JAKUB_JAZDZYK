package org.example.algorithms;

import org.knowm.xchart.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class SparseMatrixComparison {

    public static void main(String[] args) {
        int matrixSize = 200;
        double[] sparsityLevels = {0.1, 0.3, 0.5, 0.7, 0.9};

        Map<String, List<Long>> timeResults = new HashMap<>();
        Map<String, List<Long>> memoryResults = new HashMap<>();

        for (double sparsity : sparsityLevels) {
            Map<String, Integer> A = generateSparseMatrix(matrixSize, sparsity);
            Map<String, Integer> B = generateSparseMatrix(matrixSize, sparsity);

            testSparseAlgorithm("SparseMatrixMultiplication", A, B, matrixSize, matrixSize, timeResults, memoryResults);
            testSparseAlgorithm("UnrolledSparseMatrixMultiplication", A, B, matrixSize, matrixSize, timeResults, memoryResults);
        }

        generateChart("Execution Time for Sparse Matrices", timeResults, sparsityLevels, "Time (nanoseconds)");

        generateChart("Memory Usage for Sparse Matrices", memoryResults, sparsityLevels, "Memory Usage (bytes)");
    }

    private static void testSparseAlgorithm(String algorithm, Map<String, Integer> A, Map<String, Integer> B,
                                            int n, int m, Map<String, List<Long>> timeResults,
                                            Map<String, List<Long>> memoryResults) {
        long startTime = System.nanoTime();
        long startMemory = getMemoryUsage();

        Map<String, Integer> result = null;

        switch (algorithm) {
            case "SparseMatrixMultiplication":
                result = SparseMatrixMultiplication.multiply(A, B, n, m, m);
                break;
            case "UnrolledSparseMatrixMultiplication":
                result = UnrolledSparseMatrixMultiplication.multiply(A, B, n, m, m);
                break;
        }

        long endTime = System.nanoTime();
        long endMemory = getMemoryUsage();

        timeResults.computeIfAbsent(algorithm, k -> new ArrayList<>()).add(endTime - startTime);
        memoryResults.computeIfAbsent(algorithm, k -> new ArrayList<>()).add(endMemory - startMemory);
    }

    private static long getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private static Map<String, Integer> generateSparseMatrix(int size, double sparsity) {
        Map<String, Integer> sparseMatrix = new HashMap<>();
        Random random = new Random();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (random.nextDouble() > sparsity) {
                    sparseMatrix.put(i + "," + j, random.nextInt(10));
                }
            }
        }
        return sparseMatrix;
    }

    private static void generateChart(String title, Map<String, List<Long>> results, double[] sparsityLevels, String yAxisLabel) {
        XYChart chart = new XYChartBuilder().width(800).height(600).title(title).xAxisTitle("Sparsity").yAxisTitle(yAxisLabel).build();

        for (Map.Entry<String, List<Long>> entry : results.entrySet()) {
            String algorithm = entry.getKey();
            List<Long> values = entry.getValue();

            double[] xData = Arrays.stream(sparsityLevels).toArray();
            double[] yData = new double[sparsityLevels.length];

            for (int i = 0; i < sparsityLevels.length; i++) {
                yData[i] = values.get(i);
            }

            chart.addSeries(algorithm, xData, yData);
        }

        new SwingWrapper<>(chart).displayChart();
    }
}

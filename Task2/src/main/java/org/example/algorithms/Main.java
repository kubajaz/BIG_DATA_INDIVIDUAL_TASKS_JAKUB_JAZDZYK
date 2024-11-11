package org.example.algorithms;

import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {
        int[] matrixSizes = {5, 10, 50, 200, 500};
        double sparsity = 0.9;

        Map<String, List<Long>> timeResultsNormal = new HashMap<>();
        Map<String, List<Long>> memoryResultsNormal = new HashMap<>();
        Map<String, List<Long>> timeResultsSparse = new HashMap<>();
        Map<String, List<Long>> memoryResultsSparse = new HashMap<>();

        for (int size : matrixSizes) {
            int[][] A = generateNormalMatrix(size);
            int[][] B = generateNormalMatrix(size);

            testAlgorithm("StandardMatrixMultiplication", A, B, timeResultsNormal, memoryResultsNormal);
            testAlgorithm("UnrolledMatrixMultiplication", A, B, timeResultsNormal, memoryResultsNormal);
            testAlgorithm("StrassenMatrixMultiplication", A, B, timeResultsNormal, memoryResultsNormal);
        }

        for (int size : matrixSizes) {
            Map<String, Integer> A = generateSparseMatrix(size, sparsity);
            Map<String, Integer> B = generateSparseMatrix(size, sparsity);

            testSparseAlgorithm("SparseMatrixMultiplication", A, B, size, size, timeResultsSparse, memoryResultsSparse);
            testSparseAlgorithm("UnrolledSparseMatrixMultiplication", A, B, size, size, timeResultsSparse, memoryResultsSparse);
        }

        generateChart("Execution Time for Normal Matrices", timeResultsNormal);
        generateChart("Memory Usage for Normal Matrices", memoryResultsNormal);

        generateChart("Execution Time for Sparse Matrices", timeResultsSparse);
        generateChart("Memory Usage for Sparse Matrices", memoryResultsSparse);
    }

    private static void testAlgorithm(String algorithm, int[][] A, int[][] B,
                                      Map<String, List<Long>> timeResults, Map<String, List<Long>> memoryResults) {
        long startTime = System.nanoTime();
        long startMemory = getMemoryUsage();

        int[][] result = null;

        switch (algorithm) {
            case "StandardMatrixMultiplication":
                result = StandardMatrixMultiplication.multiply(A, B);
                break;
            case "UnrolledMatrixMultiplication":
                result = UnrolledMatrixMultiplication.multiply(A, B);
                break;
            case "StrassenMatrixMultiplication":
                result = StrassenMatrixMultiplication.multiply(A, B);
                break;
        }

        long endTime = System.nanoTime();
        long endMemory = getMemoryUsage();

        timeResults.computeIfAbsent(algorithm, k -> new ArrayList<>()).add(endTime - startTime);
        memoryResults.computeIfAbsent(algorithm, k -> new ArrayList<>()).add(endMemory - startMemory);
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

    private static int[][] generateNormalMatrix(int size) {
        int[][] matrix = new int[size][size];
        Random random = new Random();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = random.nextInt(10);
            }
        }
        return matrix;
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

    private static void generateChart(String chartTitle, Map<String, List<Long>> results) {
        List<String> algorithms = new ArrayList<>(results.keySet());
        List<List<Long>> times = new ArrayList<>();

        for (String algorithm : algorithms) {
            times.add(results.get(algorithm));
        }

        CategoryChart chart = new CategoryChartBuilder()
                .width(800)
                .height(600)
                .title(chartTitle)
                .xAxisTitle("Matrix Size")
                .yAxisTitle("Time (ns) or Memory (bytes)")
                .build();

        for (int i = 0; i < algorithms.size(); i++) {
            chart.addSeries(algorithms.get(i), Arrays.asList(5, 10, 50, 200, 500), times.get(i));
        }

        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setToolTipsEnabled(true);
        chart.getStyler().setDefaultSeriesRenderStyle(CategorySeries.CategorySeriesRenderStyle.Line);

        new SwingWrapper<>(chart).displayChart();
    }
}

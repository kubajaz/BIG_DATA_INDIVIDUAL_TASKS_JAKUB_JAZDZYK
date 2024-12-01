package org.example.algorithms;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;

public class TestDifferentSizes {

    public static void main(String[] args) {
        int[][][] matricesA = {
                new int[10][10],
                new int[100][100],
                new int[1000][1000],
                new int[2000][2000]
        };
        int[][][] matricesB = {
                new int[10][10],
                new int[100][100],
                new int[1000][1000],
                new int[2000][2000]
        };

        fillMatrix(matricesA[0]);
        fillMatrix(matricesB[0]);
        fillMatrix(matricesA[1]);
        fillMatrix(matricesB[1]);
        fillMatrix(matricesA[2]);
        fillMatrix(matricesB[2]);
        fillMatrix(matricesA[3]);
        fillMatrix(matricesB[3]);

        long[] normalTimes = new long[4];
        long[] parallelTimes = new long[4];
        long[] vectorizedTimes = new long[4];

        double[] normalCpuUsage = new double[4];
        double[] parallelCpuUsage = new double[4];
        double[] vectorizedCpuUsage = new double[4];

        long[] normalMemoryUsage = new long[4];
        long[] parallelMemoryUsage = new long[4];
        long[] vectorizedMemoryUsage = new long[4];

        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        for (int i = 0; i < matricesA.length; i++) {
            System.out.println("Testing matrix of size " + matricesA[i].length + "x" + matricesA[i][0].length);

            normalTimes[i] = measureExecutionTime(matricesA[i], matricesB[i], "Normal", osBean, normalCpuUsage, normalMemoryUsage, i);
            parallelTimes[i] = measureExecutionTime(matricesA[i], matricesB[i], "Parallel", osBean, parallelCpuUsage, parallelMemoryUsage, i);
            vectorizedTimes[i] = measureExecutionTime(matricesA[i], matricesB[i], "Vectorized", osBean, vectorizedCpuUsage, vectorizedMemoryUsage, i);
        }

        createAndDisplayLongChart(normalTimes, parallelTimes, vectorizedTimes, "Time (ns)", "Matrix Size", "Matrix Multiplication Performance Comparison", "Time");
        createAndDisplayDoubleChart(normalCpuUsage, parallelCpuUsage, vectorizedCpuUsage, "CPU Usage (%)", "Matrix Size", "CPU Usage Comparison", "CPU Usage");
        createAndDisplayLongChart(normalMemoryUsage, parallelMemoryUsage, vectorizedMemoryUsage, "Memory Usage (bytes)", "Matrix Size", "Memory Usage Comparison", "Memory Usage");
    }

    private static void fillMatrix(int[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                matrix[i][j] = (int) (Math.random() * 10);
            }
        }
    }

    private static long measureExecutionTime(int[][] matrixA, int[][] matrixB, String algorithm, OperatingSystemMXBean osBean, double[] cpuUsage, long[] memoryUsage, int index) {
        System.gc();

        long startTime = System.nanoTime();
        long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        double startCpuLoad = osBean.getSystemCpuLoad();

        if (algorithm.equals("Normal")) {
            MatrixMultiplicationNormal.multiply(matrixA, matrixB);
        } else if (algorithm.equals("Parallel")) {
            MatrixMultiplicationParallel.multiply(matrixA, matrixB);
        } else if (algorithm.equals("Vectorized")) {
            MatrixMultiplicationVextorized.multiply(matrixA, matrixB);
        }

        long endTime = System.nanoTime();
        long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        double endCpuLoad = osBean.getSystemCpuLoad();

        long executionTime = endTime - startTime;
        long memoryUsageInBytes = Math.max(0, endMemory - startMemory);
        double cpuUsagePercentage = Math.max(0, (endCpuLoad - startCpuLoad) * 100);

        cpuUsage[index] = cpuUsagePercentage;
        memoryUsage[index] = memoryUsageInBytes;

        return executionTime;
    }

    private static DefaultCategoryDataset createDataset(long[] normalData, long[] parallelData, long[] vectorizedData) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        dataset.addValue(normalData[0], "Normal", "10x10");
        dataset.addValue(parallelData[0], "Parallel", "10x10");
        dataset.addValue(vectorizedData[0], "Vectorized", "10x10");

        dataset.addValue(normalData[1], "Normal", "100x100");
        dataset.addValue(parallelData[1], "Parallel", "100x100");
        dataset.addValue(vectorizedData[1], "Vectorized", "100x100");

        dataset.addValue(normalData[2], "Normal", "1000x1000");
        dataset.addValue(parallelData[2], "Parallel", "1000x1000");
        dataset.addValue(vectorizedData[2], "Vectorized", "1000x1000");

        dataset.addValue(normalData[3], "Normal", "2000x2000");
        dataset.addValue(parallelData[3], "Parallel", "2000x2000");
        dataset.addValue(vectorizedData[3], "Vectorized", "2000x2000");

        return dataset;
    }

    private static DefaultCategoryDataset createDataset(double[] normalData, double[] parallelData, double[] vectorizedData) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        dataset.addValue(normalData[0], "Normal", "10x10");
        dataset.addValue(parallelData[0], "Parallel", "10x10");
        dataset.addValue(vectorizedData[0], "Vectorized", "10x10");

        dataset.addValue(normalData[1], "Normal", "100x100");
        dataset.addValue(parallelData[1], "Parallel", "100x100");
        dataset.addValue(vectorizedData[1], "Vectorized", "100x100");

        dataset.addValue(normalData[2], "Normal", "1000x1000");
        dataset.addValue(parallelData[2], "Parallel", "1000x1000");
        dataset.addValue(vectorizedData[2], "Vectorized", "1000x1000");

        dataset.addValue(normalData[3], "Normal", "2000x2000");
        dataset.addValue(parallelData[3], "Parallel", "2000x2000");
        dataset.addValue(vectorizedData[3], "Vectorized", "2000x2000");

        return dataset;
    }

    private static void createAndDisplayLongChart(long[] normalData, long[] parallelData, long[] vectorizedData, String yLabel, String xLabel, String chartTitle, String dataType) {
        DefaultCategoryDataset dataset = createDataset(normalData, parallelData, vectorizedData);

        JFreeChart chart = ChartFactory.createBarChart(
                chartTitle,
                xLabel,
                yLabel,
                dataset,
                PlotOrientation.VERTICAL, true, true, false);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));

        javax.swing.JFrame frame = new javax.swing.JFrame(dataType + " Comparison");
        frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(chartPanel);
        frame.pack();
        frame.setVisible(true);
    }

    private static void createAndDisplayDoubleChart(double[] normalData, double[] parallelData, double[] vectorizedData, String yLabel, String xLabel, String chartTitle, String dataType) {
        DefaultCategoryDataset dataset = createDataset(normalData, parallelData, vectorizedData);

        JFreeChart chart = ChartFactory.createBarChart(
                chartTitle,
                xLabel,
                yLabel,
                dataset,
                PlotOrientation.VERTICAL, true, true, false);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));

        javax.swing.JFrame frame = new javax.swing.JFrame(dataType + " Comparison");
        frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(chartPanel);
        frame.pack();
        frame.setVisible(true);
    }
}

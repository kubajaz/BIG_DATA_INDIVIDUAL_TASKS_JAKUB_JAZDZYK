package tests.java;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import production.java.Matrix;

public class Benchmark {

    public static BenchmarkResult runBenchmark(int n) {
        long startTime, endTime;

        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

        MemoryUsage beforeMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        long beforeCpuTime = threadMXBean.getCurrentThreadCpuTime();

        startTime = System.currentTimeMillis();
        Matrix.multiplyMatrices(n);
        endTime = System.currentTimeMillis();

        MemoryUsage afterMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        long afterCpuTime = threadMXBean.getCurrentThreadCpuTime();

        long executionTime = endTime - startTime;
        long memoryUsed = afterMemoryUsage.getUsed() - beforeMemoryUsage.getUsed();
        long cpuTimeUsed = afterCpuTime - beforeCpuTime;

        double cpuUsagePercent = (double) cpuTimeUsed / (executionTime * 1_000_000) * 100;

        return new BenchmarkResult(n, executionTime, memoryUsed, cpuUsagePercent);
    }

    public static void saveResultsToCSV(String filename, BenchmarkResult[] results) throws IOException {
        try (FileWriter fileWriter = new FileWriter(filename);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {

            printWriter.println("Matrix Size,Execution Time (s),Memory Used (MB),CPU Usage (%)");

            for (BenchmarkResult result : results) {
                printWriter.printf("%d,%.6f,%.2f,%.2f%n",
                        result.getMatrixSize(),
                        result.getExecutionTimeInSeconds(),
                        result.getMemoryUsedInMB(),
                        result.getCpuUsagePercent());
            }
        }
        System.out.println("Results saved to " + filename);
    }

    public static void main(String[] args) {
        int[] sizes = {10, 100, 300, 600};
        int numTests = 3;
        BenchmarkResult[] results = new BenchmarkResult[sizes.length];

        for (int i = 0; i < sizes.length; i++) {
            long totalExecutionTime = 0;
            long totalMemoryUsed = 0;
            double totalCpuUsage = 0;

            for (int j = 0; j < numTests; j++) {
                BenchmarkResult result = runBenchmark(sizes[i]);
                totalExecutionTime += result.getExecutionTime();
                totalMemoryUsed += result.getMemoryUsed();
                totalCpuUsage += result.getCpuUsagePercent();
            }

            // Average the results
            results[i] = new BenchmarkResult(sizes[i],
                totalExecutionTime / numTests,
                totalMemoryUsed / numTests,
                totalCpuUsage / numTests);
        }

        try {
            saveResultsToCSV("benchmark_results_java.csv", results);
        } catch (IOException e) {
            System.out.println("Error saving results: " + e.getMessage());
        }
    }
}

class BenchmarkResult {
    private final int matrixSize;
    private final long executionTime;  // in milliseconds
    private final long memoryUsed;  // in bytes
    private final double cpuUsage;    // in percentage

    public BenchmarkResult(int matrixSize, long executionTime, long memoryUsed, double cpuUsage) {
        this.matrixSize = matrixSize;
        this.executionTime = executionTime;
        this.memoryUsed = memoryUsed;
        this.cpuUsage = cpuUsage;
    }

    public int getMatrixSize() {
        return matrixSize;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public double getExecutionTimeInSeconds() {
        return executionTime / 1000.0;
    }

    public long getMemoryUsed() {
        return memoryUsed;
    }

    public double getMemoryUsedInMB() {
        return memoryUsed / (1024.0 * 1024.0);
    }

    public double getCpuUsagePercent() {
        return cpuUsage;
    }
}

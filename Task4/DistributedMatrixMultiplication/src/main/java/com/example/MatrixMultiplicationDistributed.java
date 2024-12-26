package com.example;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class MatrixMultiplicationDistributed {

    private final HazelcastInstance hazelcastInstance;
    private final ExecutorService executorService;

    public MatrixMultiplicationDistributed(String thisNodeAddress, String otherNodeAddress) {
        Config config = new Config();
        config.getNetworkConfig().getJoin().getTcpIpConfig().addMember(thisNodeAddress).addMember(otherNodeAddress).setEnabled(true);
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        System.setProperty("hazelcast.local.localAddress", thisNodeAddress);
        this.hazelcastInstance = Hazelcast.newHazelcastInstance(config);
        this.executorService = hazelcastInstance.getExecutorService("matrix-multiply");
    }

    public static class MatrixSlice implements Serializable {
        public int[][] matrix;
        public int startRow;
        public int endRow;

        public MatrixSlice(int[][] matrix, int startRow, int endRow) {
            this.matrix = matrix;
            this.startRow = startRow;
            this.endRow = endRow;
        }

        public MatrixSlice() {}
    }


    public static class MatrixMultiplicationTask implements Callable<MatrixSlice>, Serializable {

        private final MatrixSlice matrixASlice;
        private final int[][] matrixB;
        private final int colsB;
        private final int rowsA;


        public MatrixMultiplicationTask(MatrixSlice matrixASlice, int[][] matrixB) {
            this.matrixASlice = matrixASlice;
            this.matrixB = matrixB;
            this.colsB = matrixB[0].length;
            this.rowsA = matrixASlice.endRow - matrixASlice.startRow;
        }

        @Override
        public MatrixSlice call() {
            int[][] result = new int[rowsA][colsB];
            int rowsSliceA = matrixASlice.endRow - matrixASlice.startRow;

            for (int i = 0; i < rowsSliceA; i++) {
                for (int j = 0; j < colsB; j++) {
                    for (int k = 0; k < matrixASlice.matrix[0].length; k++) {
                        result[i][j] += matrixASlice.matrix[i][k] * matrixB[k][j];
                    }
                }
            }
            return new MatrixSlice(result,matrixASlice.startRow,matrixASlice.endRow);
        }
    }

    public int[][] multiply(int[][] matrixA, int[][] matrixB) throws ExecutionException, InterruptedException {
        int rowsA = matrixA.length;
        int colsB = matrixB[0].length;
        int numberOfSlices = hazelcastInstance.getCluster().getMembers().size();
        int sliceSize = rowsA / numberOfSlices;
        List<Future<MatrixSlice>> futures = new ArrayList<>();

        for(int i = 0; i < numberOfSlices; i++) {
            int startRow = i * sliceSize;
            int endRow = (i == numberOfSlices -1 ) ? rowsA: (i+1) * sliceSize;
            int sliceSizeRows = endRow - startRow;

            int[][] matrixSlice = new int[sliceSizeRows][matrixA[0].length];

            for(int x=0; x<sliceSizeRows; x++) {
                System.arraycopy(matrixA[startRow+x],0,matrixSlice[x],0,matrixA[0].length);
            }

            MatrixSlice matrixASlice = new MatrixSlice(matrixSlice,startRow,endRow);
            MatrixMultiplicationTask task = new MatrixMultiplicationTask(matrixASlice, matrixB);
            futures.add(executorService.submit(task));
        }


        int[][] result = new int[rowsA][colsB];

        for (Future<MatrixSlice> future : futures) {
            MatrixSlice resultSlice = future.get();
            int startRow = resultSlice.startRow;
            int endRow = resultSlice.endRow;
            int sliceSizeRows = endRow - startRow;

            for (int i = 0; i < sliceSizeRows; i++) {
                System.arraycopy(resultSlice.matrix[i],0,result[startRow+i],0,colsB);
            }
        }

        return result;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        int[][] matrixA = {
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        };

        int[][] matrixB = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9},
                {10, 11, 12}
        };

        String thisNodeAddress = "192.168.1.194"; // Dla komputera 1
        String otherNodeAddress = "192.168.1.44"; // Dla komputera 2

        MatrixMultiplicationDistributed distributedMultiply = new MatrixMultiplicationDistributed(thisNodeAddress, otherNodeAddress);

        long startTime = System.nanoTime(); // Rozpoczęcie pomiaru czasu
        int[][] resultDistributed = distributedMultiply.multiply(matrixA, matrixB);
        long endTime = System.nanoTime();   // Zakończenie pomiaru czasu

        long duration = (endTime - startTime) / 1_000_000; // Przeliczenie na milisekundy
        System.out.println("Wynik rozproszonego mnożenia macierzy:");
        // Wypisz wynik
        for (int[] row : resultDistributed) {
            for (int val : row) {
                System.out.print(val + " ");
            }
            System.out.println();
        }
        System.out.println("Czas wykonania: " + duration + " ms");
        distributedMultiply.hazelcastInstance.shutdown();
    }
}
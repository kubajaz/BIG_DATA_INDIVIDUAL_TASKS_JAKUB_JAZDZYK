package org.example.algorithms;

import java.util.HashMap;
import java.util.Map;

public class SparseMatrixMultiplication {
    public static Map<String, Integer> multiply(Map<String, Integer> A, Map<String, Integer> B, int n, int m, int p) {
        Map<String, Integer> result = new HashMap<>();

        for (String keyA : A.keySet()) {
            int i = Integer.parseInt(keyA.split(",")[0]);
            int k = Integer.parseInt(keyA.split(",")[1]);
            int valueA = A.get(keyA);

            for (int j = 0; j < m; j++) {
                String keyB = k + "," + j;
                if (B.containsKey(keyB)) {
                    int valueB = B.get(keyB);
                    String keyResult = i + "," + j;
                    result.put(keyResult, result.getOrDefault(keyResult, 0) + valueA * valueB);
                }
            }
        }
        return result;
    }
}

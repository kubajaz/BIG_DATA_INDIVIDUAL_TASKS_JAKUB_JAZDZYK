package algorithms;

import java.util.HashMap;
import java.util.Map;

public class UnrolledSparseMatrixMultiplication {
    public static Map<String, Integer> multiply(Map<String, Integer> A, Map<String, Integer> B, int n, int m, int p) {
        Map<String, Integer> result = new HashMap<>();

        for (String keyA : A.keySet()) {
            int i = Integer.parseInt(keyA.split(",")[0]);
            int k = Integer.parseInt(keyA.split(",")[1]);
            int valueA = A.get(keyA);

            // Loop unrolling by 4
            for (int j = 0; j < m; j += 4) {
                // Handle j
                String keyB = k + "," + j;
                if (B.containsKey(keyB)) {
                    int valueB = B.get(keyB);
                    String keyResult = i + "," + j;
                    result.put(keyResult, result.getOrDefault(keyResult, 0) + valueA * valueB);
                }

                // Handle j + 1
                if (j + 1 < m) {
                    String keyBNext1 = k + "," + (j + 1);
                    if (B.containsKey(keyBNext1)) {
                        int valueBNext1 = B.get(keyBNext1);
                        String keyResultNext1 = i + "," + (j + 1);
                        result.put(keyResultNext1, result.getOrDefault(keyResultNext1, 0) + valueA * valueBNext1);
                    }
                }

                // Handle j + 2
                if (j + 2 < m) {
                    String keyBNext2 = k + "," + (j + 2);
                    if (B.containsKey(keyBNext2)) {
                        int valueBNext2 = B.get(keyBNext2);
                        String keyResultNext2 = i + "," + (j + 2);
                        result.put(keyResultNext2, result.getOrDefault(keyResultNext2, 0) + valueA * valueBNext2);
                    }
                }

                // Handle j + 3
                if (j + 3 < m) {
                    String keyBNext3 = k + "," + (j + 3);
                    if (B.containsKey(keyBNext3)) {
                        int valueBNext3 = B.get(keyBNext3);
                        String keyResultNext3 = i + "," + (j + 3);
                        result.put(keyResultNext3, result.getOrDefault(keyResultNext3, 0) + valueA * valueBNext3);
                    }
                }
            }
        }
        return result;
    }
}

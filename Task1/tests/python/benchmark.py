import pytest
import psutil
import random
import pandas as pd

def multiply_matrices(n):
    A = [[random.random() for _ in range(n)] for _ in range(n)]
    B = [[random.random() for _ in range(n)] for _ in range(n)]
    C = [[0 for _ in range(n)] for _ in range(n)]

    for i in range(n):
        for j in range(n):
            for k in range(n):
                C[i][j] += A[i][k] * B[k][j]

    return C

@pytest.mark.benchmark
def test_multiply_matrices(benchmark):
    sizes = [10, 20, 30, 40]
    results = []

    for n in sizes:
        exec_time = benchmark(multiply_matrices, n)

        memory_usage = psutil.virtual_memory().used / (1024 ** 2)
        cpu_usage = psutil.cpu_percent(interval=None)

        results.append({
            "Matrix Size": n,
            "Execution Time (s)": exec_time,
            "Memory Usage (MB)": memory_usage,
            "CPU Usage (%)": cpu_usage
        })

        print(f"Size: {n}, Time: {exec_time:.6f}s, Memory: {memory_usage:.2f}MB, CPU: {cpu_usage:.2f}%")

    save_results_to_csv(results)

def save_results_to_csv(results, filename='benchmark_results.csv'):
    df = pd.DataFrame(results)
    df.to_csv(filename, index=False)
    print(f"Results saved to {filename}")

if __name__ == "__main__":
    pytest.main([__file__, "--benchmark-only"]) 

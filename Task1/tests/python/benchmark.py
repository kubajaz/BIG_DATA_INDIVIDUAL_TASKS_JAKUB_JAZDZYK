import random
import time
import psutil
import pandas as pd
from Task1.production.python.matrix import multiply_matrices

def benchmark(sizes, num_tests):
    results = []

    for n in sizes:
        total_time = 0.0
        total_memory = 0.0
        total_cpu = 0.0

        for _ in range(num_tests):
            memory_before = psutil.virtual_memory().used
            cpu_before = psutil.cpu_percent(interval=None)

            start_time = time.time()
            multiply_matrices(n)
            end_time = time.time()

            memory_after = psutil.virtual_memory().used
            cpu_after = psutil.cpu_percent(interval=None)

            time_spent = end_time - start_time
            memory_used = (memory_after - memory_before) / (1024 ** 2)
            cpu_usage = cpu_after - cpu_before

            total_time += time_spent
            total_memory += memory_used
            total_cpu += cpu_usage

        avg_time = total_time / num_tests
        avg_memory = total_memory / num_tests
        avg_cpu = total_cpu / num_tests

        results.append({
            "Matrix Size": n,
            "Avg Execution Time (s)": avg_time,
            "Avg Memory Usage (MB)": avg_memory,
            "Avg CPU Usage (%)": avg_cpu
        })

    return results

def save_to_csv(results, filename):
    df = pd.DataFrame(results)
    df.to_csv(filename, index=False)
    print(f"Results saved to {filename}")

if __name__ == "__main__":
    sizes = [10, 20, 30, 40]
    num_tests = 3
    results = benchmark(sizes, num_tests)
    save_to_csv(results, "benchmark_results.csv")

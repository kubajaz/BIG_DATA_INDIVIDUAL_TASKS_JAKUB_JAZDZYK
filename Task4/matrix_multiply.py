import numpy as np
import time
from multiprocessing import Pool
from memory_profiler import memory_usage

# Function for traditional matrix multiplication (using loops)
def multiply_matrices(A, B):
    m, n = A.shape
    n2, p = B.shape
    if n != n2:
        raise ValueError("The number of columns in matrix A must equal the number of rows in matrix B")
    
    C = np.zeros((m, p))
    
    for i in range(m):
        for j in range(p):
            for k in range(n):
                C[i, j] += A[i, k] * B[k, j]
                
    return C

# Map function - calculating a single element C[i, j]
def map_multiply_row_column(A, B, i, j):
    m, n = A.shape
    n2, p = B.shape
    value = sum(A[i, k] * B[k, j] for k in range(n))
    return (i, j, value)

# Reduce function - combining results into matrix C
def reduce_multiply(results, m, p):
    C = np.zeros((m, p))
    for i, j, value in results:
        C[i, j] = value
    return C

# Function for distributed matrix multiplication using MapReduce (multiprocessing)
def map_reduce_multiply(A, B):
    m, n = A.shape
    n2, p = B.shape
    tasks = [(i, j) for i in range(m) for j in range(p)]
    
    # Mapping
    with Pool() as pool:
        results = pool.starmap(map_multiply_row_column, [(A, B, i, j) for i, j in tasks])
    
    # Reducing
    C = reduce_multiply(results, m, p)
    
    return C

# Function for parallel matrix multiplication using threads
def parallel_multiply(A, B):
    m, n = A.shape
    n2, p = B.shape
    tasks = [(i, j) for i in range(m) for j in range(p)]
    
    with Pool() as pool:
        results = pool.starmap(map_multiply_row_column, [(A, B, i, j) for i, j in tasks])

    # Combining results into matrix
    C = np.zeros((m, p))
    for i, j, value in results:
        C[i, j] = value
    
    return C

# Function to measure time and memory usage for traditional multiplication
def test_traditional_multiply(A, B):
    start_time = time.time()
    result = multiply_matrices(A, B)
    end_time = time.time()
    elapsed_time = end_time - start_time
    return result, elapsed_time

# Function to measure time and memory usage for distributed multiplication (MapReduce)
def test_map_reduce_multiply(A, B):
    start_time = time.time()
    result = map_reduce_multiply(A, B)
    end_time = time.time()
    elapsed_time = end_time - start_time
    return result, elapsed_time

# Function to measure time and memory usage for parallel multiplication
def test_parallel_multiply(A, B):
    start_time = time.time()
    result = parallel_multiply(A, B)
    end_time = time.time()
    elapsed_time = end_time - start_time
    return result, elapsed_time

# Function to monitor memory usage
def memory_test(function, *args):
    mem_usage = memory_usage((function, args))
    return max(mem_usage)

# Example: Matrices A and B
A = np.random.rand(1000, 1000)  # 1000x1000 matrix
B = np.random.rand(1000, 1000)  # 1000x1000 matrix

# Traditional matrix multiplication (loop-based)
print("Testing traditional matrix multiplication...")
result_traditional, time_traditional = test_traditional_multiply(A, B)
print(f"Execution time for traditional multiplication: {time_traditional:.4f} seconds")
memory_traditional = memory_test(test_traditional_multiply, A, B)
print(f"Memory usage (traditional): {memory_traditional:.4f} MB")

# Distributed matrix multiplication (MapReduce)
print("\nTesting distributed matrix multiplication (MapReduce)...")
result_mapreduce, time_mapreduce = test_map_reduce_multiply(A, B)
print(f"Execution time for distributed multiplication: {time_mapreduce:.4f} seconds")
memory_mapreduce = memory_test(test_map_reduce_multiply, A, B)
print(f"Memory usage (MapReduce): {memory_mapreduce:.4f} MB")

# Parallel matrix multiplication
print("\nTesting parallel matrix multiplication...")
result_parallel, time_parallel = test_parallel_multiply(A, B)
print(f"Execution time for parallel multiplication: {time_parallel:.4f} seconds")
memory_parallel = memory_test(test_parallel_multiply, A, B)
print(f"Memory usage (parallel): {memory_parallel:.4f} MB")

# Results comparison
if np.allclose(result_traditional, result_mapreduce) and np.allclose(result_traditional, result_parallel):
    print("\nResults are consistent!")
else:
    print("\nResults differ.")

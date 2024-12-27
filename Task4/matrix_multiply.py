import numpy as np
import time
from multiprocessing import Pool
from memory_profiler import memory_usage

# Funkcja mnożenia macierzy w sposób tradycyjny (pętle)
def multiply_matrices(A, B):
    m, n = A.shape
    n2, p = B.shape
    if n != n2:
        raise ValueError("Liczba kolumn w macierzy A musi być równa liczbie wierszy w macierzy B")
    
    C = np.zeros((m, p))
    
    for i in range(m):
        for j in range(p):
            for k in range(n):
                C[i, j] += A[i, k] * B[k, j]
                
    return C

# Funkcja mapująca - obliczanie pojedynczego elementu C[i,j]
def map_multiply_row_column(A, B, i, j):
    m, n = A.shape
    n2, p = B.shape
    value = sum(A[i, k] * B[k, j] for k in range(n))
    return (i, j, value)

# Funkcja reduce - łączenie wyników w macierz C
def reduce_multiply(results, m, p):
    C = np.zeros((m, p))
    for i, j, value in results:
        C[i, j] = value
    return C

# Funkcja do rozproszonego mnożenia macierzy przy użyciu MapReduce (multiprocessing)
def map_reduce_multiply(A, B):
    m, n = A.shape
    n2, p = B.shape
    tasks = [(i, j) for i in range(m) for j in range(p)]
    
    # Mapowanie
    with Pool() as pool:
        results = pool.starmap(map_multiply_row_column, [(A, B, i, j) for i, j in tasks])
    
    # Redukcja
    C = reduce_multiply(results, m, p)
    
    return C

# Funkcja do równoległego mnożenia macierzy przy użyciu wątków
def parallel_multiply(A, B):
    m, n = A.shape
    n2, p = B.shape
    tasks = [(i, j) for i in range(m) for j in range(p)]
    
    with Pool() as pool:
        results = pool.starmap(map_multiply_row_column, [(A, B, i, j) for i, j in tasks])

    # Łączenie wyników w macierz
    C = np.zeros((m, p))
    for i, j, value in results:
        C[i, j] = value
    
    return C

# Funkcja do pomiaru czasu i pamięci dla zwykłego mnożenia
def test_traditional_multiply(A, B):
    start_time = time.time()
    result = multiply_matrices(A, B)
    end_time = time.time()
    elapsed_time = end_time - start_time
    return result, elapsed_time

# Funkcja do pomiaru czasu i pamięci dla rozproszonego mnożenia (MapReduce)
def test_map_reduce_multiply(A, B):
    start_time = time.time()
    result = map_reduce_multiply(A, B)
    end_time = time.time()
    elapsed_time = end_time - start_time
    return result, elapsed_time

# Funkcja do pomiaru czasu i pamięci dla równoległego mnożenia
def test_parallel_multiply(A, B):
    start_time = time.time()
    result = parallel_multiply(A, B)
    end_time = time.time()
    elapsed_time = end_time - start_time
    return result, elapsed_time

# Funkcja do monitorowania zużycia pamięci
def memory_test(function, *args):
    mem_usage = memory_usage((function, args))
    return max(mem_usage)

# Przykład: Macierze A i B
A = np.random.rand(100, 100)  # 100x100 macierz
B = np.random.rand(100, 100)  # 100x100 macierz

# Mnożenie macierzy tradycyjne (pętla)
print("Testowanie tradycyjnego mnożenia macierzy...")
result_traditional, time_traditional = test_traditional_multiply(A, B)
print(f"Czas wykonania tradycyjnego mnożenia: {time_traditional:.4f} sekund")
memory_traditional = memory_test(test_traditional_multiply, A, B)
print(f"Zużycie pamięci (tradycyjne): {memory_traditional:.4f} MB")

# Mnożenie macierzy z MapReduce (rozproszone)
print("\nTestowanie rozproszonego mnożenia macierzy (MapReduce)...")
result_mapreduce, time_mapreduce = test_map_reduce_multiply(A, B)
print(f"Czas wykonania rozproszonego mnożenia: {time_mapreduce:.4f} sekund")
memory_mapreduce = memory_test(test_map_reduce_multiply, A, B)
print(f"Zużycie pamięci (MapReduce): {memory_mapreduce:.4f} MB")

# Mnożenie macierzy z równoległym podejściem
print("\nTestowanie równoległego mnożenia macierzy...")
result_parallel, time_parallel = test_parallel_multiply(A, B)
print(f"Czas wykonania równoległego mnożenia: {time_parallel:.4f} sekund")
memory_parallel = memory_test(test_parallel_multiply, A, B)
print(f"Zużycie pamięci (Równoległe): {memory_parallel:.4f} MB")

# Porównanie wyników
if np.allclose(result_traditional, result_mapreduce) and np.allclose(result_traditional, result_parallel):
    print("\nWyniki są zgodne!")
else:
    print("\nWyniki się różnią.")

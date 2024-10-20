from time import time
from matrix import multiply_matrices

if __name__ == "__main__":
    n = 1024

    start = time()
    multiply_matrices(n)
    end = time()

    print(f"Execution time: {end - start:.6f} seconds")

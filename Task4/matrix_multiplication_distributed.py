import hazelcast
import random
import time
import numpy as np


class MatrixSlice:
    def __init__(self, matrix=None, start_row=None, end_row=None):
        self.matrix = matrix
        self.start_row = start_row
        self.end_row = end_row


class MatrixMultiplicationTask:
    def __init__(self, matrix_a_slice, matrix_b):
        self.matrix_a_slice = matrix_a_slice
        self.matrix_b = matrix_b
        self.cols_b = len(matrix_b[0])
        self.rows_a = self.matrix_a_slice.end_row - self.matrix_a_slice.start_row

    def __call__(self):
        rows_slice_a = self.matrix_a_slice.end_row - self.matrix_a_slice.start_row
        result = np.zeros((rows_slice_a, self.cols_b), dtype=int)

        for i in range(rows_slice_a):
            for j in range(self.cols_b):
                for k in range(len(self.matrix_a_slice.matrix[0])):
                    result[i][j] += self.matrix_a_slice.matrix[i][k] * self.matrix_b[k][j]

        return MatrixSlice(result.tolist(), self.matrix_a_slice.start_row, self.matrix_a_slice.end_row)


class MatrixMultiplicationDistributed:
    def __init__(self, this_node_address, other_node_address):
         self.client = hazelcast.HazelcastClient(
             cluster_name="dev",
             cluster_members=[
                this_node_address,
                other_node_address
            ],
            lifecycle_listeners=[
                lambda state: print("Lifecycle event >>>", state),
            ]
        )

         self.executor_service = self.client.get_executor_service("matrix-multiply")

    def multiply(self, matrix_a, matrix_b):
        rows_a = len(matrix_a)
        cols_b = len(matrix_b[0])
        number_of_slices = len(self.client.cluster.get_members())
        slice_size = rows_a // number_of_slices
        futures = []

        for i in range(number_of_slices):
            start_row = i * slice_size
            end_row = rows_a if i == number_of_slices - 1 else (i + 1) * slice_size
            slice_size_rows = end_row - start_row

            matrix_slice = [row[0:len(matrix_a[0])] for row in matrix_a[start_row:end_row]]

            matrix_a_slice = MatrixSlice(matrix_slice, start_row, end_row)
            task = MatrixMultiplicationTask(matrix_a_slice, matrix_b)
            futures.append(self.executor_service.submit(task))

        result = np.zeros((rows_a, cols_b), dtype=int)
        for future in futures:
            result_slice = future.result()
            start_row = result_slice.start_row
            end_row = result_slice.end_row
            slice_size_rows = end_row - start_row

            for i in range(slice_size_rows):
                result[start_row + i, :] = result_slice.matrix[i]

        return result.tolist()


def generate_random_matrix(rows, cols):
    matrix = np.random.randint(10, size=(rows, cols))
    return matrix.tolist()


if __name__ == '__main__':
    matrix_size = 2000
    matrix_a = generate_random_matrix(matrix_size, matrix_size)
    matrix_b = generate_random_matrix(matrix_size, matrix_size)

    this_node_address = "192.168.1.44:5701"  # Aktualizuj adresy IP
    other_node_address = "192.168.1.194:5701"  # Aktualizuj adresy IP

    distributed_multiply = MatrixMultiplicationDistributed(this_node_address, other_node_address)

    # Poczekaj na dołączenie węzłów
    while len(distributed_multiply.client.cluster.get_members()) < 2:
        print("Oczekiwanie na dołączenie drugiego węzła...")
        time.sleep(1)

    print("Klaster jest gotowy, rozpoczynam obliczenia.")

    start_time = time.time_ns()
    result_distributed = distributed_multiply.multiply(matrix_a, matrix_b)
    end_time = time.time_ns()

    duration = (end_time - start_time) // 1_000_000
    print("Wynik rozproszonego mnożenia macierzy:")
    print(f"Czas wykonania: {duration} ms")

    distributed_multiply.client.shutdown()
#include <stdio.h>
#include <stdlib.h>
#include <time.h>

extern void multiply_matrices(int n);

int main() {
    int n = 1024;

    clock_t start = clock();
    multiply_matrices(n);
    clock_t end = clock();

    double time_spent = (double)(end - start) / CLOCKS_PER_SEC;
    printf("Execution time: %.6f seconds\n", time_spent);
    return 0;
}

#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <sys/resource.h>

void multiply_matrices(int n) {
    double** A = (double**)malloc(n * sizeof(double*));
    double** B = (double**)malloc(n * sizeof(double*));
    double** C = (double**)malloc(n * sizeof(double*));
    for (int i = 0; i < n; i++) {
        A[i] = (double*)malloc(n * sizeof(double));
        B[i] = (double*)malloc(n * sizeof(double));
        C[i] = (double*)malloc(n * sizeof(double));
    }

    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            A[i][j] = (double)rand() / RAND_MAX;
            B[i][j] = (double)rand() / RAND_MAX;
            C[i][j] = 0;
        }
    }

    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            for (int k = 0; k < n; k++) {
                C[i][j] += A[i][k] * B[k][j];
            }
        }
    }

    for (int i = 0; i < n; i++) {
        free(A[i]);
        free(B[i]);
        free(C[i]);
    }
    free(A);
    free(B);
    free(C);
}

int main() {
    int sizes[] = {10, 20, 30, 40};
    FILE *fp = fopen("benchmark_results.csv", "w");
    
    fprintf(fp, "Matrix Size,Avg Execution Time (s),Avg Memory Usage (KB),Avg CPU Usage (%)\n");

    for (int i = 0; i < sizeof(sizes) / sizeof(sizes[0]); i++) {
        int n = sizes[i];
        double total_time = 0.0;
        long total_memory = 0;
        double total_cpu = 0.0;

        for (int j = 0; j < 3; j++) {
            struct rusage usage;
            clock_t start = clock();
            multiply_matrices(n);
            clock_t end = clock();

            double time_spent = (double)(end - start) / CLOCKS_PER_SEC;
            total_time += time_spent;

            getrusage(RUSAGE_SELF, &usage);
            total_memory += usage.ru_maxrss; 

            total_cpu += (double)(usage.ru_utime.tv_sec + usage.ru_utime.tv_usec / 1e6) / time_spent * 100.0; // CPU w %

            printf("Benchmark for n=%d (run %d): %.6f seconds, Memory: %ld KB, CPU Usage: %.2f%%\n", n, j + 1, time_spent, usage.ru_maxrss, (usage.ru_utime.tv_sec + usage.ru_utime.tv_usec / 1e6) / time_spent * 100.0);
        }

        double avg_time = total_time / 3;
        long avg_memory = total_memory / 3;
        double avg_cpu = total_cpu / 3;

        printf("Average for n=%d: Avg Time: %.6f seconds, Avg Memory: %ld KB, Avg CPU Usage: %.2f%%\n", n, avg_time, avg_memory, avg_cpu);

        fprintf(fp, "%d,%.6f,%ld,%.2f\n", n, avg_time, avg_memory, avg_cpu);
    }

    fclose(fp);
    printf("Results saved to benchmark_results.csv\n");
    return 0;
}

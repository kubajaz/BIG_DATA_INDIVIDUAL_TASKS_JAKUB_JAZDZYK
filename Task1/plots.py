import pandas as pd
import matplotlib.pyplot as plt

c_data = pd.read_csv('benchmark_results_c.csv')
python_data = pd.read_csv('benchmark_results_python.csv')
java_data = pd.read_csv('benchmark_results_java.csv')

c_data['Avg Memory Usage (MB)'] = c_data['Avg Memory Usage (KB)'] / 1024

plt.figure(figsize=(10, 6))
plt.plot(c_data['Matrix Size'], c_data['Avg Execution Time (s)'], label='C', marker='o')
plt.plot(python_data['Matrix Size'], python_data['Avg Execution Time (s)'], label='Python', marker='o')
plt.plot(java_data['Matrix Size'], java_data['Execution Time (s)'], label='Java', marker='o')
plt.title('Execution Time Comparison')
plt.xlabel('Matrix Size')
plt.ylabel('Execution Time (s)')
plt.xticks(c_data['Matrix Size'])
plt.legend()
plt.grid()
plt.savefig('execution_time_comparison.png')
plt.close()

plt.figure(figsize=(10, 6))
plt.plot(c_data['Matrix Size'], c_data['Avg Memory Usage (MB)'], label='C', marker='o')
plt.plot(python_data['Matrix Size'], python_data['Avg Memory Usage (MB)'], label='Python', marker='o')
plt.plot(java_data['Matrix Size'], java_data['Memory Used (MB)'], label='Java', marker='o')
plt.title('Memory Usage Comparison')
plt.xlabel('Matrix Size')
plt.ylabel('Memory Usage (MB)')
plt.xticks(c_data['Matrix Size'])
plt.legend()
plt.grid()
plt.savefig('memory_usage_comparison.png')
plt.close()

plt.figure(figsize=(10, 6))
plt.plot(c_data['Matrix Size'], c_data['Avg CPU Usage (%)'], label='C', marker='o')
plt.plot(python_data['Matrix Size'], python_data['Avg CPU Usage (%)'], label='Python', marker='o')
plt.plot(java_data['Matrix Size'], java_data['CPU Usage (%)'], label='Java', marker='o')
plt.title('CPU Usage Comparison')
plt.xlabel('Matrix Size')
plt.ylabel('CPU Usage (%)')
plt.xticks(c_data['Matrix Size'])
plt.legend()
plt.grid()
plt.savefig('cpu_usage_comparison.png')
plt.close()

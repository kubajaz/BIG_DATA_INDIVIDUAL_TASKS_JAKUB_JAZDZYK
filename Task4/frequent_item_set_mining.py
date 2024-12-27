from multiprocessing import Pool
from itertools import combinations

# Map step: Generate candidate itemsets from a transaction
def map_itemsets(transaction):
    items = transaction
    candidate_itemsets = []
    for size in range(1, len(items) + 1):  # Generate subsets of all sizes
        candidate_itemsets.extend(combinations(items, size))
    return candidate_itemsets

# Reduce step: Combine and count itemsets across all transactions
def reduce_itemsets(mapped_data):
    itemset_counts = {}
    for itemset in mapped_data:
        itemset_tuple = tuple(itemset)  # Convert list to tuple for hashing
        if itemset_tuple in itemset_counts:
            itemset_counts[itemset_tuple] += 1
        else:
            itemset_counts[itemset_tuple] = 1
    return itemset_counts

# Main function to find frequent itemsets
def find_frequent_itemsets(transactions, minsup):
    # Map step
    with Pool() as pool:
        results = pool.map(map_itemsets, transactions)
    
    # Flatten results
    flattened_results = [item for sublist in results for item in sublist]
    
    # Reduce step: Count itemsets
    itemset_counts = reduce_itemsets(flattened_results)
    
    # Filter by minimum support
    frequent_itemsets = {itemset: count for itemset, count in itemset_counts.items() if count / len(transactions) >= minsup}
    
    return frequent_itemsets

if __name__ == "__main__":
    # Example transactions dataset
    transactions = [
        ['milk', 'bread', 'butter'],
        ['bread', 'butter', 'jam'],
        ['milk', 'bread', 'butter', 'jam'],
        ['bread', 'jam']
    ]
    
    # Minimum support threshold
    minsup = 0.8

    # Find frequent itemsets
    frequent_itemsets = find_frequent_itemsets(transactions, minsup)
    
    # Print the results
    print("Frequent Itemsets:")
    for itemset, count in frequent_itemsets.items():
        print(f"{itemset}: {count}")

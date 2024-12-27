import hazelcast
import time
from hazelcast.config import ClientConfig

if __name__ == "__main__":
    this_node_address = "192.168.1.194"  # Adres IP komputera 1
    other_node_address = "192.168.1.44"  # Adres IP komputera 2

    config = ClientConfig()
    config.network_config.addresses.append(this_node_address + ":5701")
    config.network_config.addresses.append(other_node_address + ":5701")

    client = hazelcast.HazelcastClient(config)

    try:
         if len(client.cluster.get_members()) < 2:
            print("Oczekiwanie na dołączenie drugiego węzła...")
            while len(client.cluster.get_members()) < 2:
               time.sleep(1)
            print("Klaster jest gotowy!")
         else:
             print("Klaster jest gotowy!")

    finally:
        client.shutdown()
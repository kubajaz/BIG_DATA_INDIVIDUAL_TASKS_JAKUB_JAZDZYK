package com.example;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class SimpleHazelcastCluster {
    public static void main(String[] args) {
        String localAddress = System.getProperty("hazelcast.local.localAddress");
        if (localAddress == null || localAddress.isEmpty()) {
            System.setProperty("hazelcast.local.localAddress", "192.168.1.194");
        }
        Config config = new Config();
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true)
                .addMember("192.168.1.194")
                .addMember("192.168.1.253");
        config.getNetworkConfig().setPortAutoIncrement(false);
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);

        System.out.println("Hazelcast instance started. Cluster size: " + instance.getCluster().getMembers().size());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        instance.shutdown();
    }
}
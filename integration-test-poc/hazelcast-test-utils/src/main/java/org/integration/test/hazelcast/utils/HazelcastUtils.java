package org.integration.test.hazelcast.utils;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class HazelcastUtils {

    public static HazelcastInstance createHazelcastInstance(int hazelcastPort) {
        return Hazelcast.newHazelcastInstance(createHazelcastConfiguration(hazelcastPort));
    }

    // Bu config daha fazla arastirilmali.
    private static Config createHazelcastConfiguration(int hazelcastPort) {
        Config config = new Config();
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true);
        config.getNetworkConfig().setPort(hazelcastPort);
        config.getNetworkConfig().getJoin().getTcpIpConfig().addMember("localhost");
        return config;
    }
}

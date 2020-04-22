package org.integration.test.hazelcast.utils;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.util.SocketUtils;

public class HazelcastUtils {

    private int randomHazelcastPort;
    private HazelcastInstance hazelcastInstance;

    public HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }

    public void createHazelcastInstance(String IP) {
        setSystemProperty(IP);
        hazelcastInstance = Hazelcast.newHazelcastInstance(createHazelcastConfiguration(randomHazelcastPort));
    }

    // Bu config daha fazla arastirilmali.
    private Config createHazelcastConfiguration(int hazelcastPort) {
        Config config = new Config();
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true);
        config.getNetworkConfig().setPort(hazelcastPort);
        config.getNetworkConfig().getJoin().getTcpIpConfig().addMember("localhost");
        return config;
    }

    public void setSystemProperty(String IP) {
        randomHazelcastPort = SocketUtils.findAvailableTcpPort();
        System.setProperty("hazelcast.addresses", IP + ":" + randomHazelcastPort);
    }

    public void stop() {
        hazelcastInstance.getLifecycleService().shutdown();
    }
}

package org.integration.test.all.configuration;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class HazelcastConfiguration {

    @Value("${hazelcast.addresses}")
    private String addresses;

    @Bean
    public ClientConfig hazelcastClientConfig() {
        ClientConfig clientConfig = new ClientConfig();

        GroupConfig groupConfig = clientConfig.getGroupConfig();
        groupConfig.setName("dev");

        ClientNetworkConfig clientNetworkConfig = clientConfig.getNetworkConfig();

        List<String> addressList = new ArrayList<String>(Arrays.asList(addresses.split(",")));
        clientNetworkConfig.setAddresses(addressList);
        clientNetworkConfig.setSmartRouting(true);
        clientNetworkConfig.setRedoOperation(true);
        clientNetworkConfig.setConnectionAttemptLimit(Integer.MAX_VALUE);

        clientConfig.setExecutorPoolSize(40);

        return clientConfig;
    }

    @Bean
    HazelcastInstance hazelcastClient(@Autowired ClientConfig hazelcastClientConfig) {
        return HazelcastClient.newHazelcastClient(hazelcastClientConfig);
    }
}

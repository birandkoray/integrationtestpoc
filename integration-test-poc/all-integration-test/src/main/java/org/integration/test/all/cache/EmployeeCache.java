package org.integration.test.all.cache;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.spi.properties.ClientProperty;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.integration.test.all.data.Person;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EmployeeCache {
    HazelcastInstance instance = Hazelcast.newHazelcastInstance();
    HazelcastInstance client;

//    public void createClient() {
//        ClientConfig config = new ClientConfig();
//        config.setGroupConfig(new GroupConfig("YOUR_CLUSTER_NAME", "YOUR_CLUSTER_PASSWORD"));
//        config.setProperty("hazelcast.client.statistics.enabled","true");
//        config.setProperty(ClientProperty.HAZELCAST_CLOUD_DISCOVERY_TOKEN.getName(), "YOUR_CLUSTER_DISCOVERY_TOKEN");
//        client = HazelcastClient.newHazelcastClient(config);
//    }

    public HazelcastInstance getInstance() {
        return instance;
    }

    public HazelcastInstance getClient() {
        return client;
    }

    public Map<String, Person> getMap(){
        return getInstance().getMap("person");
//        return getClient().getMap("map");
    }
}

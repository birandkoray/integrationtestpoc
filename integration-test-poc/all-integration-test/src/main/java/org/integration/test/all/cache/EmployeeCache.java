package org.integration.test.all.cache;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.integration.test.all.data.Person;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EmployeeCache {
    HazelcastInstance instance = Hazelcast.newHazelcastInstance();

    public HazelcastInstance getInstance() {
        return instance;
    }

    public Map<String, Person> getMap(){
        return getInstance().getMap("person");
    }
}

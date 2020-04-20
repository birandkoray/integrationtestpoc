package org.integration.test.all.cacheAccessor;

import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Component
public class HazelcastMapAccessor {

    @Autowired
    private HazelcastInstance hazelcastClient;

    private final String MAP_NAME = "DEFAULT";

    public Object get(String key) {
        return this.get(MAP_NAME, key);
    }

    public Object get(String cacheName, Object key) {
        return this.hazelcastClient.getMap(cacheName).get(key);
    }

    public Map<Object, Object> get(String cacheName, Set<Object> keySet) {
        return this.hazelcastClient.getMap(cacheName).getAll(keySet);
    }

    public void put(Object key, Object object) {
        this.put(MAP_NAME, key, object);
    }

    public void put(String cacheName, Object key, Object object) {
        this.hazelcastClient.getMap(cacheName).put(key, object);
    }

    public void put(String cacheName, Map<Object, Object> objectMap) {
        this.hazelcastClient.getMap(cacheName).putAll(objectMap);
    }

    public Map getMap(String cacheName) {
        return this.hazelcastClient.getMap(cacheName);
    }

    public void putToList(String cacheName, Object item, int index) {
        this.hazelcastClient.getList(cacheName).add(index, item);
    }

    public void putAllToList(String cacheName, Collection collection) {
        this.hazelcastClient.getList(cacheName).addAll(collection);
    }

    public void putAll(Map<?, ?> map) {
        this.put(MAP_NAME, map);
    }

    public void delete(String key) {
        this.delete(MAP_NAME, key);
    }

    public void putAll(String cacheName, Map<?, ?> map) {
        this.hazelcastClient.getMap(cacheName).putAll(map);
    }

    public void delete(String cacheName, Object key) {
        this.hazelcastClient.getMap(cacheName).delete(key);
    }


    public Map getAllFromMap(String cacheName, Set keyList) {
        return hazelcastClient.getMap(cacheName).getAll(keyList);
    }

    public void clearCache(String cacheName) {
        hazelcastClient.getMap(cacheName).clear();
    }
}

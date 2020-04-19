package org.integration.test.hazelcast;

import com.hazelcast.cache.ICache;
import com.hazelcast.cache.impl.HazelcastServerCachingProvider;
import com.hazelcast.config.CacheConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.test.AssertTask;
import com.hazelcast.test.HazelcastSerialClassRunner;
import com.hazelcast.test.TestHazelcastInstanceFactory;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.event.*;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ModifiedExpiryPolicy;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.hazelcast.test.HazelcastTestSupport.*;
import static junit.framework.Assert.*;
import static org.junit.Assert.assertSame;

@RunWith(HazelcastSerialClassRunner.class)
@Category(QuickTest.class)
public class HazelcastIntegrationTest {
    private TestHazelcastInstanceFactory factory;
    private HazelcastInstance hz1;
    private HazelcastInstance hz2;

    private HazelcastServerCachingProvider cachingProvider1;
    private HazelcastServerCachingProvider cachingProvider2;


    @Before
    public void init() {
        factory = new TestHazelcastInstanceFactory(2);
        hz1 = factory.newHazelcastInstance();
        hz2 = factory.newHazelcastInstance();
        cachingProvider1 = HazelcastServerCachingProvider.createCachingProvider(hz1);
        cachingProvider2 = HazelcastServerCachingProvider.createCachingProvider(hz2);
    }

    @After
    public void tear() {
        cachingProvider1.close();
        cachingProvider2.close();
        factory.shutdownAll();
    }

    @Test
    public void testJSRExample1()
            throws InterruptedException {
        final String cacheName = "simpleCache";

        CacheManager cacheManager = cachingProvider1.getCacheManager();
        assertNotNull(cacheManager);

        assertNull(cacheManager.getCache(cacheName));

        CacheConfig<Integer, String> config = new CacheConfig<Integer, String>();
        Cache<Integer, String> cache = cacheManager.createCache(cacheName, config);
        assertNotNull(cache);

        assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                CacheManager cm2 = cachingProvider2.getCacheManager();
                assertNotNull(cm2.getCache(cacheName));
            }
        });

        Integer key = 1;
        String value1 = "value";
        cache.put(key, value1);

        String value2 = cache.get(key);
        assertEquals(value1, value2);
        cache.remove(key);
        assertNull(cache.get(key));

        Cache<Integer, String> cache2 = cacheManager.getCache(cacheName);
        assertNotNull(cache2);

        key = 1;
        value1 = "value";
        cache.put(key, value1);

        value2 = cache.get(key);
        assertEquals(value1, value2);
        cache.remove(key);
        assertNull(cache.get(key));

        cacheManager.destroyCache(cacheName);
        cacheManager.close();
    }

    @Test
    public void testRemoveAll()
            throws InterruptedException {
        String cacheName = "simpleCache";

        CacheManager cacheManager = cachingProvider1.getCacheManager();

        CacheConfig<Integer, String> config = new CacheConfig<Integer, String>();
        Cache<Integer, String> cache = cacheManager.createCache(cacheName, config);
        assertNotNull(cache);

        Integer key1 = 1;
        String value1 = "value1";
        cache.put(key1, value1);

        Integer key2 = 1;
        String value2 = "value2";
        cache.put(key2, value2);

        Set<Integer> keys = new HashSet<Integer>();
        keys.add(key1);
        keys.add(key2);
        cache.removeAll(keys);

        assertNull(cache.get(key1));
        assertNull(cache.get(key2));

        cacheManager.destroyCache(cacheName);
    }

    @Test
    public void testCompletionTest()
            throws InterruptedException {
        String cacheName = "simpleCache";

        CacheManager cacheManager = cachingProvider1.getCacheManager();

        CacheConfig<Integer, String> config = new CacheConfig<Integer, String>();
        final SimpleEntryListener<Integer, String> listener = new SimpleEntryListener<Integer, String>();
        MutableCacheEntryListenerConfiguration<Integer, String> listenerConfiguration =
                new MutableCacheEntryListenerConfiguration<Integer, String>(
                        FactoryBuilder.factoryOf(listener), null, true, true);

        config.addCacheEntryListenerConfiguration(listenerConfiguration);

        Cache<Integer, String> cache = cacheManager.createCache(cacheName, config);
        assertNotNull(cache);

        Integer key1 = 1;
        String value1 = "value1";
        cache.put(key1, value1);
        assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                assertEquals(1, listener.created.get());
            }
        });

        Integer key2 = 2;
        String value2 = "value2";
        cache.put(key2, value2);
        assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                assertEquals(2, listener.created.get());
            }
        });

        Set<Integer> keys = new HashSet<Integer>();
        keys.add(key1);
        keys.add(key2);
        cache.removeAll(keys);
        assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                assertEquals(2, listener.removed.get());
            }
        });
    }

    @Test
    public void testJSRCreateDestroyCreate()
            throws InterruptedException {
        String cacheName = "simpleCache";

        CacheManager cacheManager = cachingProvider1.getCacheManager();
        assertNotNull(cacheManager);

        assertNull(cacheManager.getCache(cacheName));

        CacheConfig<Integer, String> config = new CacheConfig<Integer, String>();
        Cache<Integer, String> cache = cacheManager.createCache(cacheName, config);
        assertNotNull(cache);

        Thread.sleep(1000);

        Integer key = 1;
        String value1 = "value";
        cache.put(key, value1);

        String value2 = cache.get(key);
        assertEquals(value1, value2);
        cache.remove(key);
        assertNull(cache.get(key));

        cacheManager.destroyCache(cacheName);
        assertNull(cacheManager.getCache(cacheName));

        Cache<Integer, String> cache1 = cacheManager.createCache(cacheName, config);
        assertNotNull(cache1);
    }

    @Test
    public void testCaches_NotEmpty() {
        CacheManager cacheManager = cachingProvider1.getCacheManager();

        ArrayList<String> caches1 = new ArrayList<String>();
        cacheManager.createCache("c1", new MutableConfiguration());
        cacheManager.createCache("c2", new MutableConfiguration());
        cacheManager.createCache("c3", new MutableConfiguration());
        caches1.add(cacheManager.getCache("c1").getName());
        caches1.add(cacheManager.getCache("c2").getName());
        caches1.add(cacheManager.getCache("c3").getName());

        Iterable<String> cacheNames = cacheManager.getCacheNames();
        Iterator<String> iterator = cacheNames.iterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }

    @Test
    public void testCachesDestroy() {
        CacheManager cacheManager = cachingProvider1.getCacheManager();
        CacheManager cacheManager2 = cachingProvider2.getCacheManager();
        MutableConfiguration configuration = new MutableConfiguration();
        final Cache c1 = cacheManager.createCache("c1", configuration);
        final Cache c2 = cacheManager2.getCache("c1");
        c1.put("key", "value");
        cacheManager.destroyCache("c1");
        assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                try {
                    c2.get("key");
                    throw new AssertionError("get should throw IllegalStateException");
                } catch (IllegalStateException e) {
                    //ignored as expected
                }
            }
        });
    }

    @Test
    public void testCachesDestroyFromOtherManagers() {
        CacheManager cacheManager = cachingProvider1.getCacheManager();
        CacheManager cacheManager2 = cachingProvider2.getCacheManager();
        MutableConfiguration configuration = new MutableConfiguration();
        final Cache c1 = cacheManager.createCache("c1", configuration);
        final Cache c2 = cacheManager2.createCache("c2", configuration);
        c1.put("key", "value");
        c2.put("key", "value");
        cacheManager.close();
        assertTrueAllTheTime(new AssertTask() {
            @Override
            public void run() throws Exception {
                c2.get("key");
            }
        }, 10);
    }

    @Test
    public void testIterator() {
        CacheManager cacheManager = cachingProvider1.getCacheManager();

        CacheConfig<Integer, String> config = new CacheConfig<Integer, String>();
        config.setName("SimpleCache");

        ICache<Integer, String> cache =
                (ICache<Integer, String>) cacheManager.createCache("simpleCache", config);

        int testSize = 1007;
        for (int i = 0; i < testSize; i++) {
            Integer key = i;
            String value1 = "value" + i;
            cache.put(key, value1);
        }

        assertEquals(testSize, cache.size());

        Iterator<Cache.Entry<Integer, String>> iterator = cache.iterator();
        assertNotNull(iterator);

        HashMap<Integer, String> resultMap = new HashMap<Integer, String>();

        int c = 0;
        while (iterator.hasNext()) {
            Cache.Entry<Integer, String> next = iterator.next();
            Integer key = next.getKey();
            String value = next.getValue();
            resultMap.put(key, value);
            c++;
        }
        assertEquals(testSize, c);
    }

    @Test
    public void testCachesTypedConfig() {
        CacheManager cacheManager = cachingProvider1.getCacheManager();

        CacheConfig<Integer, Long> config = new CacheConfig();
        String cacheName = "test";
        config.setName(cacheName);
        config.setTypes(Integer.class, Long.class);

        Cache<Integer, Long> cache = cacheManager.createCache(cacheName, config);
        Cache<Integer, Long> cache2 =
                cacheManager.getCache(cacheName, config.getKeyType(), config.getValueType());

        assertNotNull(cache);
        assertNotNull(cache2);
    }

    public static class SimpleEntryListener<K, V>
            implements CacheEntryListener<K, V>,
            CacheEntryCreatedListener<K, V>,
            CacheEntryUpdatedListener<K, V>,
            CacheEntryRemovedListener<K, V>,
            CacheEntryExpiredListener<K, V>,
            Serializable {

        public AtomicInteger created = new AtomicInteger();
        public AtomicInteger expired = new AtomicInteger();
        public AtomicInteger removed = new AtomicInteger();
        public AtomicInteger updated = new AtomicInteger();

        public SimpleEntryListener() {
        }

        @Override
        public void onCreated(Iterable<CacheEntryEvent<? extends K, ? extends V>> cacheEntryEvents)
                throws CacheEntryListenerException {
            for (CacheEntryEvent<? extends K, ? extends V> cacheEntryEvent : cacheEntryEvents) {
                created.incrementAndGet();
            }
        }

        @Override
        public void onExpired(Iterable<CacheEntryEvent<? extends K, ? extends V>> cacheEntryEvents)
                throws CacheEntryListenerException {
            for (CacheEntryEvent<? extends K, ? extends V> cacheEntryEvent : cacheEntryEvents) {
                expired.incrementAndGet();
            }
        }

        @Override
        public void onRemoved(Iterable<CacheEntryEvent<? extends K, ? extends V>> cacheEntryEvents)
                throws CacheEntryListenerException {
            for (CacheEntryEvent<? extends K, ? extends V> cacheEntryEvent : cacheEntryEvents) {
                removed.incrementAndGet();
            }
        }

        @Override
        public void onUpdated(Iterable<CacheEntryEvent<? extends K, ? extends V>> cacheEntryEvents)
                throws CacheEntryListenerException {
            for (CacheEntryEvent<? extends K, ? extends V> cacheEntryEvent : cacheEntryEvents) {
                updated.incrementAndGet();
            }
        }
    }

    @Test
    public void getAndOperateOnCacheAfterClose() {
        final String CACHE_NAME = "myCache";

        CacheManager cacheManager = cachingProvider1.getCacheManager();
        ICache<Object, Object> cache = (ICache<Object, Object>) cacheManager.createCache(CACHE_NAME, new CacheConfig());
        cache.close();
        assertTrue(cache.isClosed());
        assertFalse(cache.isDestroyed());

        Cache<Object, Object> cacheAfterClose = cacheManager.getCache(CACHE_NAME);
        assertNotNull(cacheAfterClose);
        assertFalse(cacheAfterClose.isClosed());

        cache.put(1, 1);
    }

    @Test
    public void testWithTtl(){
        CacheManager cacheManager = cachingProvider1.getCacheManager();
        CacheConfig<String, String> config = new CacheConfig<>();
        final ICache<String, String> cache = (ICache<String, String>) cacheManager.createCache("testCache", config);
        final String key = "key";
        cache.put(key, "value1", new ModifiedExpiryPolicy(new Duration(TimeUnit.SECONDS, 3)));
        assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                assertNull(cache.get(key));
            }
        });
    }

    @Test
    public void testInstance(){
        final String INSTANCE_NAME = "h1";
        Config config = new Config().setInstanceName(INSTANCE_NAME);
        TcpIpConfig tcpIpConfig = new TcpIpConfig();
        tcpIpConfig.addMember("127.0.0.1");
        NetworkConfig networkConfig = new NetworkConfig();
        networkConfig.getJoin().setTcpIpConfig(tcpIpConfig);
        config.setNetworkConfig(networkConfig);
        HazelcastInstance h1 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance h2 = Hazelcast.getHazelcastInstanceByName(INSTANCE_NAME);
        System.out.println(config.getNetworkConfig().getJoin().getTcpIpConfig().getMembers());
        assertSame(h1, h2);
        List<String> members = h2.getConfig().getNetworkConfig().getJoin().getTcpIpConfig().getMembers();
        assertEquals(1, members.size());
        assertEquals("127.0.0.1", members.get(0));
        h1.shutdown();
    }

    @Test
    public void testClusterSize(){
        Config config = new Config();
        TcpIpConfig tcpIpConfig = new TcpIpConfig();
        tcpIpConfig.addMember("127.0.0.1");
        NetworkConfig networkConfig = new NetworkConfig();
        networkConfig.setPort(9880);
        networkConfig.getJoin().setTcpIpConfig(tcpIpConfig);
        config.setNetworkConfig(networkConfig);
        HazelcastInstance h1 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance h2 = Hazelcast.newHazelcastInstance(config);
        assertClusterSize(2, h1);
        h1.shutdown();
        h2.shutdown();
    }
}

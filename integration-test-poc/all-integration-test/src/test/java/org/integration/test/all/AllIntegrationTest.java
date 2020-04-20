package org.integration.test.all;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;
import com.mongodb.client.MongoClients;
import org.integration.test.all.cacheAccessor.HazelcastMapAccessor;
import org.integration.test.all.cacheKeys.CacheKeys;
import org.integration.test.all.data.Employee;
import org.integration.test.all.data.Person;
import org.integration.test.all.document.EmployeeDocument;
import org.integration.test.hazelcast.utils.HazelcastUtils;
import org.integration.test.kafka.utils.KafkaUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.util.SocketUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EmbeddedKafka(partitions = 1)
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
public class AllIntegrationTest {

    private static final String IP = "localhost";
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    private static KafkaUtils kafkaUtils;
    private static final String INPUT_TOPIC = "employee-topic-test-";
    private static final String OUTPUT_TOPIC = "person-topic-test-";


    private MongoTemplate mongoTemplate;
    private static int randomMongoPort;

    @Autowired
    private HazelcastMapAccessor hazelcastMapAccessor;
    private static HazelcastInstance hazelcastInstance;
    private static int randomHazelcastPort;
    private static HazelcastUtils hazelcastUtils;

    @BeforeAll
    public static void setUpBeforeAll() {
        kafkaUtils = new KafkaUtils();
        hazelcastUtils = new HazelcastUtils();
        randomMongoPort = SocketUtils.findAvailableTcpPort();
        randomHazelcastPort = SocketUtils.findAvailableTcpPort();
        hazelcastInstance = hazelcastUtils.createHazelcastInstance(randomHazelcastPort);
        System.setProperty("hazelcast.addresses", IP + ":" + randomHazelcastPort);
        System.setProperty("spring.cloud.stream.kafka.binder.brokers", "${spring.embedded.kafka.brokers}");
        System.setProperty("spring.data.mongodb.port", String.valueOf(randomMongoPort));
        System.setProperty("spring.data.mongodb.host", IP);
        System.setProperty("spring.data.mongodb.auto-index-creation", "true");
    }

    @AfterAll
    public static void destroy(){
        hazelcastInstance.shutdown();
    }

    @Test
    @DisplayName("Kafka, Database ve Hazelcast Testi. Employee Add")
    public void addEmployeeTest() throws Exception {
        // mongoTemplate = new MongoTemplate(new MongoClient(IP, randomPort), "test");
        mongoTemplate = new MongoTemplate(MongoClients.create("mongodb://" + IP + ":" + randomMongoPort), "test");

        Person person = new Person("TestAd", "TestSoyad", 20);
        kafkaUtils.sendMessage(OUTPUT_TOPIC, objectMapper.writeValueAsString(person), KafkaTestUtils.producerProps(embeddedKafkaBroker));


        String comingMessage = kafkaUtils.consumeMessage(INPUT_TOPIC, KafkaTestUtils.consumerProps("consumer", "false", embeddedKafkaBroker));
        Employee employee = objectMapper.readValue(comingMessage, Employee.class);


        List<EmployeeDocument> employeeDocumentList = mongoTemplate.findAll(EmployeeDocument.class, "employee");

        Map<String, Employee> employeeMap1 = hazelcastInstance.getMap(CacheKeys.PERSON_MAP);
        Map<String, Employee> employeeMap2 = hazelcastMapAccessor.getMap(CacheKeys.PERSON_MAP);

        assertAll("Add Employee Test",
                () -> assertTrue(employee.getNickName().equals(person.getName() + "-" + person.getSurname()), "Kafka'ya verilen deger ile kafka'dan alınan deger beklendigi gibi degil"),
                () -> assertTrue(() -> employeeDocumentList != null && employeeDocumentList.size() == 1, "Db'den gelen employee Listesi beklenen deger degil..."),
                () -> assertTrue(() -> {
                    for (EmployeeDocument employeeDocument : employeeDocumentList) {
                        if (employeeDocument.getId() == null) {
                            return false;
                        }
                    }
                    return true;
                }, "Id atanmamis document'lar bulunmakta. DB'ye kaydedilmemis olabilir")
        );

        //hazelcast tests
        assertNotNull(hazelcastUtils, "Hazelcast Utils dolu degil");
        assertNotNull(hazelcastInstance, "Hazelcast Instance dolu degil");
        assertNotNull(employeeMap1, "HazelcastInstance uzerindeki map null");
        assertNotNull(employeeMap2, "HazelcastMapAccessor uzerindeki map null");

        assertTrue(employeeMap1.size() > 0, "Cache dolu degil");
        assertTrue(employeeMap2.size() > 0, "Cache dolu degil");

        assertAll("Check equality test",
                () -> assertEquals(employee.getNickName(), employeeMap1.get(employee.getObjectId()).getNickName()),
                () -> assertEquals(employee.getNickName(), employeeMap2.get(employee.getObjectId()).getNickName())
        );

        hazelcastInstance.getMap(CacheKeys.PERSON_MAP).clear();
        hazelcastMapAccessor.getMap(CacheKeys.PERSON_MAP).clear();

        assertTrue(employeeMap1.size() == 0, "HazelcastInstance uzerindeki map null degil");
        assertTrue(employeeMap2.size() == 0, "HazelcastMapAccessor uzerindeki map null degil");

        int testSize = 1000;
        for (int i = 0; i < testSize; i++) {
            //coklu ekleme icin key degistirildi
            hazelcastMapAccessor.put(CacheKeys.PERSON_MAP, i, employee);
        }

        assertEquals(employeeMap1.size(), testSize);
        assertEquals(employeeMap2.size(), testSize);

        hazelcastInstance.getMap(CacheKeys.PERSON_MAP).destroy();

        assertEquals(employeeMap1.size(), 0);
        assertEquals(employeeMap2.size(), 0);
    }

}


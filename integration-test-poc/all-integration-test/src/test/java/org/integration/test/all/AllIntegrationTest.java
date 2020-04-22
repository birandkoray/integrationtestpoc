package org.integration.test.all;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;
import com.mongodb.client.MongoClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.integration.test.all.cacheAccessor.HazelcastMapAccessor;
import org.integration.test.all.cacheKeys.CacheKeys;
import org.integration.test.all.data.Employee;
import org.integration.test.all.data.Person;
import org.integration.test.all.data.UpdateTypeEnum;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EmbeddedKafka(partitions = 1)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AllIntegrationTest {

    private static Logger logger = LogManager.getLogger(AllIntegrationTest.class);

    private static final String IP = "localhost";
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    private static KafkaUtils kafkaUtils;
    private static final String INPUT_TOPIC = "employee-topic-test--";
    private static final String OUTPUT_TOPIC = "person-topic-test--";


    private MongoTemplate mongoTemplate;
    private static int randomMongoPort;

    @Autowired
    private HazelcastMapAccessor hazelcastMapAccessor;
    private static HazelcastInstance hazelcastInstance;
    private static int randomHazelcastPort;
    private static HazelcastUtils hazelcastUtils;

    @BeforeAll
    public static void setUpBeforeAll() {
        logger.debug("START OF BEFORE ALL");
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
        logger.debug("END OF BEFORE ALL");
    }

    @AfterAll
    public static void destroy() {
        hazelcastInstance.shutdown();
    }

    @Test
    @DisplayName("Kafka, Database ve Hazelcast Testi. Employee Add")
    @Order(1)
    public void addEmployeeTest() throws Exception {
        // mongoTemplate = new MongoTemplate(new MongoClient(IP, randomPort), "test");

        mongoTemplate = new MongoTemplate(MongoClients.create("mongodb://" + IP + ":" + randomMongoPort), "test");

        Person person = new Person("TestAd", "TestSoyad", 20, UpdateTypeEnum.ADD_EMPLOYEE);
        kafkaUtils.sendMessage(OUTPUT_TOPIC, objectMapper.writeValueAsString(person), KafkaTestUtils.producerProps(embeddedKafkaBroker));


        String comingMessage = kafkaUtils.consumeMessage(INPUT_TOPIC, KafkaTestUtils.consumerProps("consumer", "true", embeddedKafkaBroker));
        Employee employee = objectMapper.readValue(comingMessage, Employee.class);

        List<EmployeeDocument> employeeDocumentList = mongoTemplate.findAll(EmployeeDocument.class, "employee");

        Map<String, Employee> employeeMap1 = hazelcastInstance.getMap(CacheKeys.PERSON_MAP);
        Map<String, Employee> employeeMap2 = hazelcastMapAccessor.getMap(CacheKeys.PERSON_MAP);

        assertAll("Add Employee Test",
                () -> {
                    assert employee != null;
                    assertEquals(employee.getNickName(), person.getName() + "-" + person.getSurname(), "Kafka'ya verilen deger ile kafka'dan alınan deger beklendigi gibi degil");
                },
                () -> assertTrue(() -> employeeDocumentList.size() == 1, "Db'den gelen employee Listesi beklenen deger degil..."),
                () -> assertFalse(() -> employeeDocumentList.stream().anyMatch(employeeDocument1 -> employeeDocument1.getId() == null)
                        , "Id atanmamis document'lar bulunmakta. DB'ye kaydedilmemis olabilir"),
                () -> assertNotNull(hazelcastUtils, "Hazelcast Utils dolu degil"),
                () -> assertNotNull(hazelcastInstance, "Hazelcast Instance dolu degil"),
                () -> assertNotNull(employeeMap1, "HazelcastInstance uzerindeki map null"),
                () -> assertNotNull(employeeMap2, "HazelcastMapAccessor uzerindeki map null"),
                () -> assertTrue(employeeMap1.size() > 0, "Cache dolu degil"),
                () -> assertTrue(employeeMap2.size() > 0, "Cache dolu degil")
        );

        assertAll("Check equality test",
                () -> assertEquals(employee.getNickName(), employeeMap1.get(employee.getObjectId()).getNickName()),
                () -> assertEquals(employee.getNickName(), employeeMap2.get(employee.getObjectId()).getNickName())
        );

    }


    @Test
    @DisplayName("Kafka, Database ve Hazelcast Testi. Employee Update")
    @Order(2)
    public void updateEmployeeTest() throws Exception {
        mongoTemplate = new MongoTemplate(MongoClients.create("mongodb://" + IP + ":" + randomMongoPort), "test");

        List<EmployeeDocument> employeeDocumentList = mongoTemplate.findAll(EmployeeDocument.class, "employee");
        EmployeeDocument employeeDocument = employeeDocumentList.stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Employee Document Bos"));

        Map<String, Employee> employeeMap = new HashMap<>(hazelcastMapAccessor.getMap(CacheKeys.PERSON_MAP));

        Person person = new Person("TestAdUpdate", "TestSoyadUpdate", 30, UpdateTypeEnum.UPDATE_EMPLOYEE);
        person.setObjectId(employeeDocument.getId());
        kafkaUtils.sendMessage(OUTPUT_TOPIC, objectMapper.writeValueAsString(person), KafkaTestUtils.producerProps(embeddedKafkaBroker));


        String comingMessage = kafkaUtils.consumeMessage(INPUT_TOPIC, KafkaTestUtils.consumerProps("consumer", "true", embeddedKafkaBroker));
        Employee employee = objectMapper.readValue(comingMessage, Employee.class);

        List<EmployeeDocument> lastEmployeeDocumentList = mongoTemplate.findAll(EmployeeDocument.class, "employee");

        Map<String, Employee> lastEmployeeMap1 = hazelcastInstance.getMap(CacheKeys.PERSON_MAP);
        Map<String, Employee> lastEmployeeMap2 = hazelcastMapAccessor.getMap(CacheKeys.PERSON_MAP);

        Employee employee1 = employeeMap.entrySet().stream().findFirst().get().getValue();
        Employee employee12 = lastEmployeeMap1.entrySet().stream().findFirst().get().getValue();

        System.out.println(employee1.equals(employee12));

        assertAll("Update Employee Test",
                () -> assertEquals(employee.getNickName(), person.getName() + "-" + person.getSurname(), "Kafka'ya verilen deger ile kafka'dan alınan deger beklendigi gibi degil"),
                () -> assertTrue(() -> lastEmployeeDocumentList.size() == 1, "Db'den gelen employee Listesi beklenen deger degil..."),
                () -> assertFalse(() -> lastEmployeeDocumentList.stream().anyMatch(employeeDocument1 -> employeeDocument1.getId() == null)
                        , "Id atanmamis document'lar bulunmakta. DB'ye kaydedilmemis olabilir"),
                () -> assertNotEquals(lastEmployeeDocumentList.get(0), employeeDocument, "Db'deki data update edilmemis"),
                () -> assertEquals(1, lastEmployeeMap1.size(), "Cache dolu degil"),
                () -> assertEquals(1, lastEmployeeMap2.size(), "Cache dolu degil"),
                () -> assertNotEquals(employeeMap.entrySet().stream().findFirst().get().getValue(),
                        lastEmployeeMap1.entrySet().stream().findFirst().get().getValue(), "Cache'deki data update edilmemis")
        );
    }


    @Test
    @DisplayName("Kafka, Database ve Hazelcast Testi. Delete Employee")
    @Order(3)
    public void deleteEmployeeTest() throws Exception {
        mongoTemplate = new MongoTemplate(MongoClients.create("mongodb://" + IP + ":" + randomMongoPort), "test");

        List<EmployeeDocument> employeeDocumentList = mongoTemplate.findAll(EmployeeDocument.class, "employee");
        EmployeeDocument employeeDocument = employeeDocumentList.stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Employee Document Bos"));


        Person person = new Person(null, null, null, UpdateTypeEnum.DELETE_EMPLOYEE);
        person.setObjectId(employeeDocument.getId());
        kafkaUtils.sendMessage(OUTPUT_TOPIC, objectMapper.writeValueAsString(person), KafkaTestUtils.producerProps(embeddedKafkaBroker));

        Thread.sleep(4000);

        List<EmployeeDocument> lastEmployeeDocumentList = mongoTemplate.findAll(EmployeeDocument.class, "employee");

        assertTrue(() -> employeeDocumentList.size() >= lastEmployeeDocumentList.size(), "Employee Silinmemis");


    }

    @Test
    @DisplayName("Kafka, Database ve Hazelcast Testi. Delete Employee")
    @Order(4)
    public void testHazelcastMethods() throws Exception {

        //hazelcast tests
        Person person = new Person("99999999999999999", "TestAd", "TestSoyad", 20);
        hazelcastMapAccessor.put(CacheKeys.PERSON_MAP, person.getObjectId(), person);

        Map<String, Employee> mapOfInstance = hazelcastInstance.getMap(CacheKeys.PERSON_MAP);
        Map<String, Employee> mapOfAccessor = hazelcastMapAccessor.getMap(CacheKeys.PERSON_MAP);
        assertNotNull(mapOfInstance, "HazelcastInstance uzerindeki map null");
        assertNotNull(mapOfAccessor, "HazelcastMapAccessor uzerindeki map null");

        hazelcastInstance.getMap(CacheKeys.PERSON_MAP).clear();
        hazelcastMapAccessor.clearCache(CacheKeys.PERSON_MAP);

        assertTrue(mapOfInstance.size() == 0, "HazelcastInstance uzerindeki map null degil");
        assertTrue(mapOfAccessor.size() == 0, "HazelcastMapAccessor uzerindeki map null degil");

        Person dummyPerson = new Person("TestAd", "TestSoyad", 20, UpdateTypeEnum.ADD_EMPLOYEE);
        int testSize = 1000;
        for (int i = 0; i < testSize; i++) {
            //coklu ekleme icin key degistirildi
            hazelcastMapAccessor.put(CacheKeys.PERSON_MAP, i, dummyPerson);
        }

        assertEquals(mapOfInstance.size(), testSize);
        assertEquals(mapOfAccessor.size(), testSize);

        hazelcastInstance.getMap(CacheKeys.PERSON_MAP).destroy();

        assertEquals(mapOfInstance.size(), 0);
        assertEquals(mapOfAccessor.size(), 0);

    }
}


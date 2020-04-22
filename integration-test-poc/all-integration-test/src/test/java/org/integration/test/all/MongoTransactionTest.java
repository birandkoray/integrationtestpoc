package org.integration.test.all;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.integration.test.all.cacheAccessor.HazelcastMapAccessor;
import org.integration.test.all.cacheKeys.CacheKeys;
import org.integration.test.all.data.Employee;
import org.integration.test.all.data.Person;
import org.integration.test.all.data.UpdateTypeEnum;
import org.integration.test.all.document.EmployeeDocument;
import org.integration.test.all.repository.EmployeeRepository;
import org.integration.test.hazelcast.utils.HazelcastUtils;
import org.integration.test.kafka.utils.KafkaUtils;
import org.integration.test.mongo.utils.DistributedModeMongoUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EmbeddedKafka(partitions = 1)
public class MongoTransactionTest {

    private static final String IP = "localhost";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    private static KafkaUtils kafkaUtils;
    private static final String INPUT_TOPIC_TRANSACTION = "employee-topic-test--";
    private static final String OUTPUT_TOPIC_TRANSACTION = "person-transaction-topic";

    @Autowired
    private HazelcastMapAccessor hazelcastMapAccessor;
    private static HazelcastUtils hazelcastUtils;

    private static DistributedModeMongoUtils distributedModeMongoUtils;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeAll
    public static void setUpBeforeAll() throws Exception {
        kafkaUtils = new KafkaUtils();
        distributedModeMongoUtils = new DistributedModeMongoUtils();
        hazelcastUtils = new HazelcastUtils();
        kafkaUtils.setSystemProperty();
        distributedModeMongoUtils.createReplicaMongo(IP);
        hazelcastUtils.createHazelcastInstance(IP);
    }

    @AfterAll
    public static void afterAll() {
        distributedModeMongoUtils.stop();
        hazelcastUtils.stop();
    }

    @BeforeEach
    public void setupBeforeEach() throws IOException, InterruptedException {
        // Multi document transaction
        employeeRepository.save(new EmployeeDocument());
        employeeRepository.deleteAll();
    }

    @Test
    @DisplayName("Transaction Employee Test")
    public void addEmployeeTransactionTest() throws Exception {
        IntStream.range(0, 10).forEach((number) -> {
            try {
                Person person = new Person("TransactionAd" + number, "TransactionSoyad" + number, 20 + number, UpdateTypeEnum.ADD_EMPLOYEE);
                kafkaUtils.sendMessage(OUTPUT_TOPIC_TRANSACTION, objectMapper.writeValueAsString(person), KafkaTestUtils.producerProps(embeddedKafkaBroker));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


        assertThrows(IllegalStateException.class, () -> {
            String message = kafkaUtils
                    .consumeTransactionalMessage(INPUT_TOPIC_TRANSACTION, KafkaTestUtils.consumerProps("consumer", "true", embeddedKafkaBroker));
            System.out.println(message);
        });

        List<EmployeeDocument> employeeDocumentList = mongoTemplate.findAll(EmployeeDocument.class, "employee");

        Map<String, Employee> employeeMap = hazelcastMapAccessor.getMap(CacheKeys.PERSON_MAP);

        assertAll("Add Employee Test",
                () -> assertTrue(() -> employeeDocumentList.size() == 0, "Db'den gelen employee Listesi beklenen deger degil..."),
                () -> assertEquals(0, employeeMap.size(), "Cache bos degil")
        );
    }


}

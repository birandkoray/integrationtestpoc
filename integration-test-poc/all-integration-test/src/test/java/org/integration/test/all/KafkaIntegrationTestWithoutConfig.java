package org.integration.test.all;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.integration.test.all.cacheAccessor.HazelcastMapAccessor;
import org.integration.test.all.cacheKeys.CacheKeys;
import org.integration.test.all.data.Employee;
import org.integration.test.all.data.Person;
import org.integration.test.all.data.UpdateTypeEnum;
import org.integration.test.all.document.EmployeeDocument;
import org.integration.test.all.stream.EmployeeStream;
import org.integration.test.all.stream.PersonStream;
import org.integration.test.hazelcast.utils.HazelcastUtils;
import org.integration.test.mongo.utils.SingleModeMongoUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KafkaIntegrationTestWithoutConfig {

    private static Logger logger = LogManager.getLogger(KafkaIntegrationTestWithoutConfig.class);

    private static final String IP = "localhost";

    @Autowired
    @Qualifier(PersonStream.INPUT)
    private MessageChannel personOutput;

    @Autowired
    @Qualifier(EmployeeStream.OUTPUT)
    private MessageChannel employeeInput;

    @Autowired
    private MessageCollector collector;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private HazelcastMapAccessor hazelcastMapAccessor;
    private static HazelcastUtils hazelcastUtils;

    @BeforeAll
    public static void setUpBeforeAll() {
        logger.debug("START OF BEFORE ALL");
        hazelcastUtils = new HazelcastUtils();
        SingleModeMongoUtils.startSingleMongo();
        hazelcastUtils.createHazelcastInstance(IP);
        logger.debug("END OF BEFORE ALL");
    }

    @AfterAll
    public static void destroy() {
        hazelcastUtils.stop();
    }

    @Test
    @DisplayName("Kafka, Database ve Hazelcast Testi. Employee Add")
    @Order(1)
    public void addEmployeeTest() throws Exception {
        Person person = new Person("TestAd", "TestSoyad", 20, UpdateTypeEnum.ADD_EMPLOYEE);
        personOutput.send(new GenericMessage<>(person));

        BlockingQueue<Message<?>> messages = collector.forChannel(this.employeeInput);
        Employee employee = new ObjectMapper().readValue(messages.take().getPayload().toString(), Employee.class);

        List<EmployeeDocument> employeeDocumentList = mongoTemplate.findAll(EmployeeDocument.class, "employee");

        Map<String, Employee> employeeMap1 = hazelcastUtils.getHazelcastInstance().getMap(CacheKeys.PERSON_MAP);
        Map<String, Employee> employeeMap2 = hazelcastMapAccessor.getMap(CacheKeys.PERSON_MAP);

        assertAll("Add Employee Test",
                () -> {
                    assertEquals(employee.getNickName(), person.getName() + "-" + person.getSurname(), "Kafka'ya verilen deger ile kafka'dan alınan deger beklendigi gibi degil");
                },
                () -> assertTrue(() -> employeeDocumentList.size() == 1, "Db'den gelen employee Listesi beklenen deger degil..."),
                () -> assertFalse(() -> employeeDocumentList.stream().anyMatch(employeeDocument1 -> employeeDocument1.getId() == null)
                        , "Id atanmamis document'lar bulunmakta. DB'ye kaydedilmemis olabilir"),
                () -> assertNotNull(hazelcastUtils, "Hazelcast Utils dolu degil"),
                () -> assertNotNull(hazelcastUtils.getHazelcastInstance(), "Hazelcast Instance dolu degil"),
                () -> assertNotNull(employeeMap1, "HazelcastInstance uzerindeki map null"),
                () -> assertNotNull(employeeMap2, "HazelcastMapAccessor uzerindeki map null"),
                () -> assertTrue(employeeMap1.size() > 0, "Cache dolu degil"),
                () -> assertTrue(employeeMap2.size() > 0, "Cache dolu degil")
        );

    }
}


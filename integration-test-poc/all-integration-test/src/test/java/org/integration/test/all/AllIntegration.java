package org.integration.test.all;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClients;
import org.integration.test.all.data.Employee;
import org.integration.test.all.data.Person;
import org.integration.test.all.document.EmployeeDocument;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@EmbeddedKafka(partitions = 1)
public class AllIntegration extends AbstractEmbeddedKafka {

    private static final String INPUT_TOPIC = "employee-topic-test";
    private static final String OUTPUT_TOPIC = "person-topic-test";
    private static final String GROUP_NAME = "embeddedKafkaApplication";

    private static int randomPort;
    private static String IP = "localhost";

    private MongoTemplate mongoTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @BeforeAll
    public static void setUpBeforeClass() {
        randomPort = SocketUtils.findAvailableTcpPort();
        System.setProperty("spring.cloud.stream.kafka.binder.brokers", "${spring.embedded.kafka.brokers}");
        System.setProperty("spring.data.mongodb.port", String.valueOf(randomPort));
        System.setProperty("spring.data.mongodb.host", "localhost");
        System.setProperty("spring.data.mongodb.auto-index-creation", "true");
    }

    @Test
    @DisplayName("Kafka, Database ve Hazelcast Testi")
    public void testSendReceive() throws Exception {
        // mongoTemplate = new MongoTemplate(new MongoClient(IP, randomPort), "test");
        mongoTemplate = new MongoTemplate(MongoClients.create("mongodb://" + IP + ":" + randomPort), "test");

        Person person = new Person("Omer", "Celik", 20);
        Map<String, Object> senderProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        sendMessage(OUTPUT_TOPIC, objectMapper.writeValueAsString(person), senderProps);


        Map<String, Object> consumerProps = new HashMap<>(KafkaTestUtils.consumerProps("consumer", "false", embeddedKafkaBroker));
        String comingMessage = consumeMessage(INPUT_TOPIC, consumerProps);
        Employee employee = objectMapper.readValue(comingMessage, Employee.class);

        List<EmployeeDocument> employeeDocumentList = mongoTemplate.findAll(EmployeeDocument.class, "employee");

        assertAll("Employee Test",
                () -> assertTrue(() -> employeeDocumentList != null && employeeDocumentList.size() == 1, "Employee Listesi beklenen deger degil..."),
                () -> assertTrue(() -> {
                    for (EmployeeDocument employeeDocument : employeeDocumentList) {
                        if (employeeDocument.getId() == null) {
                            return false;
                        }
                    }
                    return true;
                }, "Id atanmamis document'lar bulunmakta. DB'ye kaydedilmemis olabilir"),
                () -> assertTrue(employee.getNickName().equals(person.getName() + "-" + person.getSurname()), "Kafka'ya verilen deger ile kafka'dan alınan deger beklendigi gibi degil"));
    }

}


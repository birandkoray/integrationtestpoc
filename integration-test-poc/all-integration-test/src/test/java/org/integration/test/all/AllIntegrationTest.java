package org.integration.test.all;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.integration.test.all.cache.EmployeeCache;
import org.integration.test.all.data.Employee;
import org.integration.test.all.data.Person;
import org.integration.test.all.deserializer.EmployeeDeserializer;
import org.integration.test.all.deserializer.PersonSerializer;
import org.integration.test.all.service.EmployeeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.util.SocketUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EmbeddedKafka(partitions = 1)
public class AllIntegrationTest {

    private static final String INPUT_TOPIC = "employee-topic";
    private static final String OUTPUT_TOPIC = "person-topic";
    private static final String GROUP_NAME = "embeddedKafkaApplication";
    private static int randomPort;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeCache employeeCache;

    @BeforeAll
    public static void setUpBeforeClass() {
        randomPort = SocketUtils.findAvailableTcpPort();
        System.setProperty("spring.cloud.stream.kafka.binder.brokers", "${spring.embedded.kafka.brokers}");
        System.setProperty("spring.data.mongodb.port", String.valueOf(randomPort));
        System.setProperty("spring.data.mongodb.host", "localhost");
        System.setProperty("spring.data.mongodb.auto-index-creation", "true");
    }

    @Test
    public void testSendReceive() {
        Map<String, Object> senderProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        senderProps.put("key.serializer", StringSerializer.class);
        senderProps.put("value.serializer", PersonSerializer.class);
        DefaultKafkaProducerFactory<String, Person> pf = new DefaultKafkaProducerFactory<>(senderProps);

        Person person = new Person("Omer", "Celik", 20);

        KafkaTemplate<String, Person> template = new KafkaTemplate<String, Person>(pf, true);
        template.setDefaultTopic(OUTPUT_TOPIC);
        template.sendDefault(person);

        Map<String, Object> consumerProps = new HashMap<>(KafkaTestUtils.consumerProps("consumer", "false", embeddedKafkaBroker));
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put("key.deserializer", StringDeserializer.class);
        consumerProps.put("value.deserializer", EmployeeDeserializer.class);
        DefaultKafkaConsumerFactory<String, Employee> cf = new DefaultKafkaConsumerFactory<>(consumerProps);

        Consumer<String, Employee> consumer = cf.createConsumer();
        consumer.subscribe(Collections.singleton(INPUT_TOPIC));
        ConsumerRecords<String, Employee> records = consumer.poll(Duration.ofMillis(10000));
        consumer.commitSync();

        Employee employee = records.iterator().next().value();
        System.err.println(employee.getNickName());
        assertThat(employee.getNickName()).isEqualTo(person.getName() + "-" + person.getSurname());
    }

//    @Autowired
//    EmployeeService employeeService;
//
//    @Autowired
//    EmployeeCache employeeCache;
//
//    @Test
//    public void testMongoHazelcast(){
//        Person person = new Person("Muzaffer", "Öztürk", 25);
////        Mockito.when(employeeCache.getMap().put(anyString(), any(Person.class))).thenReturn(person);
//        employeeService.savePerson(person);
//        System.out.println(person.getSurname());
//        assertEquals(1, employeeCache.getMap().size());
//    }

    @Test
    public void testMongoWithHazelcast(){
        Person person = new Person("m", "m", 1);
        employeeService.savePerson(person);
        assertEquals(1, employeeCache.getMap().size());
    }

    @Test
    @AfterEach
    public void testCache(){
        System.out.println(employeeCache.getMap().size());
    }


}


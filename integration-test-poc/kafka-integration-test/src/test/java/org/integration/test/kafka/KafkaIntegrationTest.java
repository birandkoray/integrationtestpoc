package org.integration.test.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.integration.test.kafka.deserializer.PersonDeserializer;
import org.integration.test.kafka.deserializer.PersonSerializer;
import org.integration.test.kafka.model.Employee;
import org.integration.test.kafka.model.Person;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class KafkaIntegrationTest {

    private static final String INPUT_TOPIC = "employee-topic--";
    private static final String OUTPUT_TOPIC = "person-topic--";
    private static final String GROUP_NAME = "embeddedKafkaApplication";

    @ClassRule
    public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1, true);

    @BeforeClass
    public static void setup() {
        System.setProperty("spring.cloud.stream.kafka.binder.brokers", embeddedKafka.getEmbeddedKafka().getBrokersAsString());
    }

    @Test
    public void testSendReceive() {
        Map<String, Object> senderProps = KafkaTestUtils.producerProps(embeddedKafka.getEmbeddedKafka());
        senderProps.put("key.serializer", StringSerializer.class);
        senderProps.put("value.serializer", PersonSerializer.class);
        DefaultKafkaProducerFactory<String, Person> pf = new DefaultKafkaProducerFactory<>(senderProps);

        Person person = new Person("Omer", "Celik", 20);

        KafkaTemplate<String, Person> template = new KafkaTemplate<String, Person>(pf, true);
        template.setDefaultTopic(OUTPUT_TOPIC);
        template.sendDefault(person);

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(GROUP_NAME, "false", embeddedKafka.getEmbeddedKafka());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put("key.deserializer", StringDeserializer.class);
        consumerProps.put("value.deserializer", PersonDeserializer.class);
        DefaultKafkaConsumerFactory<String, Employee> cf = new DefaultKafkaConsumerFactory<>(consumerProps);

        Consumer<String, Employee> consumer = cf.createConsumer();
        consumer.subscribe(Collections.singleton(INPUT_TOPIC));
        ConsumerRecords<String, Employee> records = consumer.poll(Duration.ofMillis(10000));
        consumer.commitSync();

        Employee employee = records.iterator().next().value();
        assertThat(employee.getNickName()).isEqualTo(person.getName() + "-" + person.getSurname());
    }

}

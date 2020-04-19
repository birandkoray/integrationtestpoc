package org.integration.test.all;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.assertj.core.util.Lists;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;
import static org.apache.kafka.clients.producer.ProducerConfig.*;

abstract class AbstractEmbeddedKafka {

    protected void sendMessage(String topic, String message, Map<String, Object> producerConfiguration) throws Exception {
        try (KafkaProducer<String, String> kafkaProducer = createProducer(producerConfiguration)) {
            kafkaProducer.send(new ProducerRecord<>(topic, message)).get();
        }
    }

    protected void sendTransactionalMessage(String topic, String message, Map<String, Object> producerConfiguration) throws Exception {
        try (KafkaProducer<String, String> kafkaProducer = createTransactionalProducer(producerConfiguration)) {
            kafkaProducer.beginTransaction();
            kafkaProducer.send(new ProducerRecord<>(topic, message)).get();
            kafkaProducer.commitTransaction();
        }
    }

    protected String consumeMessage(String topic, Map<String, Object> consumerConfig) {
        return consumeMessages(topic, consumerConfig)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("no message received"));
    }

    protected String consumeTransactionalMessage(String topic, Map<String, Object> consumerConfig) {
        return consumeMessagesTransactional(topic, consumerConfig)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("no message received"));
    }

    protected List<String> consumeMessages(String topic, Map<String, Object> consumerConfig) {
        try (KafkaConsumer<String, String> consumer = createConsumer(topic, consumerConfig)) {
            return pollForRecords(consumer)
                    .stream()
                    .map(ConsumerRecord::value)
                    .collect(Collectors.toList());
        }
    }

    protected List<String> consumeMessagesTransactional(String topic, Map<String, Object> consumerConfig) {
        try (KafkaConsumer<String, String> consumer = createTransactionalConsumer(topic, consumerConfig)) {
            return pollForRecords(consumer)
                    .stream()
                    .map(ConsumerRecord::value)
                    .collect(Collectors.toList());
        }
    }

    protected KafkaProducer<String, String> createProducer(Map<String, Object> producerConfigs) {
        Map<String, Object> producerConfiguration = getKafkaProducerConfiguration(producerConfigs);
        return new KafkaProducer<>(producerConfiguration);
    }

    protected KafkaProducer<String, String> createTransactionalProducer(Map<String, Object> producerConfig) {
        Map<String, Object> producerConfiguration = getKafkaTransactionalProducerConfiguration(producerConfig);
        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(producerConfiguration);
        kafkaProducer.initTransactions();
        return kafkaProducer;
    }

    protected KafkaConsumer<String, String> createConsumer(String topic, Map<String, Object> consumerConfig) {
        Map<String, Object> consumerConfiguration = getKafkaConsumerConfiguration(consumerConfig);
        Properties properties = new Properties();
        properties.putAll(consumerConfiguration);
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(singleton(topic));
        return consumer;
    }

    protected KafkaConsumer<String, String> createTransactionalConsumer(String topic, Map<String, Object> consumerConfig) {
        Map<String, Object> consumerConfiguration = getKafkaTransactionalConsumerConfiguration(consumerConfig);
        Properties properties = new Properties();
        properties.putAll(consumerConfiguration);
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(singleton(topic));
        return consumer;
    }

    protected static <K, V> List<ConsumerRecord<K, V>> pollForRecords(KafkaConsumer<K, V> consumer) {
        ConsumerRecords<K, V> received = consumer.poll(Duration.ofSeconds(5000));
        return received == null ? emptyList() : Lists.newArrayList(received);
    }

    protected Map<String, Object> getKafkaProducerConfiguration(Map<String, Object> producerConfigs) {
        producerConfigs.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerConfigs.put(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerConfigs.put(RETRIES_CONFIG, 0);
        producerConfigs.put(BATCH_SIZE_CONFIG, 0);
        return producerConfigs;
    }

    protected Map<String, Object> getKafkaTransactionalProducerConfiguration(Map<String, Object> producerConfiguration) {
        producerConfiguration.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerConfiguration.put(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerConfiguration.put(BATCH_SIZE_CONFIG, 0);
        producerConfiguration.put(TRANSACTIONAL_ID_CONFIG, "tx-0");
        producerConfiguration.put(MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        producerConfiguration.put(ENABLE_IDEMPOTENCE_CONFIG, true);
        producerConfiguration.put(ACKS_CONFIG, "all");
        producerConfiguration.put(RETRIES_CONFIG, 10);
        producerConfiguration.put(DELIVERY_TIMEOUT_MS_CONFIG, 300000);
        producerConfiguration.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        return producerConfiguration;
    }

    protected Map<String, Object> getKafkaConsumerConfiguration(Map<String, Object> consumerConfig) {
        consumerConfig.put(GROUP_ID_CONFIG, "testGroup");
        consumerConfig.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerConfig.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerConfig.put(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        return consumerConfig;
    }

    protected Map<String, Object> getKafkaTransactionalConsumerConfiguration(Map<String, Object> consumerConfig) {
        consumerConfig.put(GROUP_ID_CONFIG, "testGroup");
        consumerConfig.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerConfig.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerConfig.put(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerConfig.put(ISOLATION_LEVEL_CONFIG, "read_committed");
        consumerConfig.put(ENABLE_AUTO_COMMIT_CONFIG, false);
        return consumerConfig;
    }
}

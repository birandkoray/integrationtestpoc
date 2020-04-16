package org.integration.test.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KafkaIntegrationTestServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaIntegrationTestServiceApplication.class, args);
    }
}

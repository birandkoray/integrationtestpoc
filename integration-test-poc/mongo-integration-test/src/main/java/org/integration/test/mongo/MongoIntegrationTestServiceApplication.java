package org.integration.test.mongo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
public class MongoIntegrationTestServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MongoIntegrationTestServiceApplication.class, args);
    }

}

package org.integration.test.all;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "org.integration.test.all.*")
public class AllIntegrationTestServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AllIntegrationTestServiceApplication.class, args);
    }

}

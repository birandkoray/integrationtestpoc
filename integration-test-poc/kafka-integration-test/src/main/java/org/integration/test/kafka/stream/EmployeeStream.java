package org.integration.test.kafka.stream;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface EmployeeStream {

    String OUTPUT = "employee-topic-out";

    @Output(OUTPUT)
    MessageChannel outboundEmployee();

}

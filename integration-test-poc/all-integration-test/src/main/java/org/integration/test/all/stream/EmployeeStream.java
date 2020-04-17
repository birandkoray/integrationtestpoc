package org.integration.test.all.stream;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface EmployeeStream {

    String OUTPUT = "employee-topic-out";

    @Output(OUTPUT)
    MessageChannel outboundEmployee();

}

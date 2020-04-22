package org.integration.test.all.stream;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface EmployeeStream {

    String OUTPUT = "employee-topic-out";
    String TRANSACTION_OUTPUT = "employee-transaction-topic-out";

    @Output(OUTPUT)
    MessageChannel outboundEmployee();

    @Output(TRANSACTION_OUTPUT)
    MessageChannel outboundTransactionEmployee();

}

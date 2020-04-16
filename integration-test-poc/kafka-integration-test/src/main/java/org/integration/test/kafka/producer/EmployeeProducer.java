package org.integration.test.kafka.producer;

import org.integration.test.kafka.model.Employee;
import org.integration.test.kafka.stream.EmployeeStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

@Service
public class EmployeeProducer {

    @Autowired
    private EmployeeStream employeeStream;

    public void publishStudent(Employee employee) {
        MessageChannel messageChannel = employeeStream.outboundEmployee();
        messageChannel.send(MessageBuilder.withPayload(employee).build());
    }
}

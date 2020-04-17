package org.integration.test.all.producer;

import org.integration.test.all.data.Employee;
import org.integration.test.all.stream.EmployeeStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

@Service
public class EmployeeProducer {

    @Autowired
    private EmployeeStream employeeStream;

    public void publishEmployee(Employee employee) {
        System.err.println("Employee basiliyor...");
        MessageChannel messageChannel = employeeStream.outboundEmployee();
        messageChannel.send(MessageBuilder.withPayload(employee).build());
    }
}

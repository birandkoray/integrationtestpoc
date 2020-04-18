package org.integration.test.all.listener;

import org.integration.test.all.data.Employee;
import org.integration.test.all.data.Person;
import org.integration.test.all.producer.EmployeeProducer;
import org.integration.test.all.service.EmployeeService;
import org.integration.test.all.stream.PersonStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Service;

@Service
public class PersonListener {

    @Autowired
    private EmployeeProducer employeeProducer;

    @Autowired
    private EmployeeService employeeService;

    @StreamListener(value = PersonStream.INPUT)
    private void personReceiverWithOutCondition(Person person) {
        employeeService.savePerson(person);
    }
}

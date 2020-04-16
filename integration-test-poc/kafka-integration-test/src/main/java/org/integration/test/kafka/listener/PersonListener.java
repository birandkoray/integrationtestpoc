package org.integration.test.kafka.listener;

import org.integration.test.kafka.model.Employee;
import org.integration.test.kafka.model.Person;
import org.integration.test.kafka.producer.EmployeeProducer;
import org.integration.test.kafka.stream.PersonStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Service;

@Service
public class PersonListener {

    @Autowired
    private EmployeeProducer employeeProducer;

    @StreamListener(value = PersonStream.INPUT)
    private void personReceiverWithOutCondition(Person person) {
        Long salary = (long) (person.getAge() * 1000);

        Employee employee = new Employee();
        employee.setNickName(person.getName() + "-" + person.getSurname());
        employee.setSalary(salary);
        employeeProducer.publishStudent(employee);
    }
}

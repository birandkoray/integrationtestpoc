package org.integration.test.all.listener;

import org.integration.test.all.data.Employee;
import org.integration.test.all.data.Person;
import org.integration.test.all.producer.EmployeeProducer;
import org.integration.test.all.stream.PersonStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Service;

@Service
public class PersonListener {

    @Autowired
    private EmployeeProducer employeeProducer;

    @StreamListener(value = PersonStream.INPUT)
    private void personReceiverWithOutCondition(Person person) {
        System.err.println("Person Topic'ine data geldi..");
        Long salary = (long) (person.getAge() * 1000);

        Employee employee = new Employee();
        employee.setNickName(person.getName() + "-" + person.getSurname());
        employee.setSalary(salary);
        employeeProducer.publishStudent(employee);
    }
}

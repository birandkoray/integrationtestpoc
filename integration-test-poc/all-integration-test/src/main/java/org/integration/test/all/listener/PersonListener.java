package org.integration.test.all.listener;

import org.integration.test.all.data.Person;
import org.integration.test.all.data.UpdateTypeEnum;
import org.integration.test.all.service.EmployeeService;
import org.integration.test.all.stream.PersonStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersonListener {

    @Autowired
    private EmployeeService employeeService;

    @StreamListener(value = PersonStream.INPUT)
    private void personReceiverWithOutCondition(Person person) {
        if (person.getUpdateTypeEnum().equals(UpdateTypeEnum.DELETE_EMPLOYEE)) {
            employeeService.deleteEmployee(person.getObjectId());
        } else {
            employeeService.saveOrUpdateEmployee(person);
        }
    }

    @StreamListener(value = PersonStream.TRANSACTION_INPUT)
    private void personReceiverWithTransaction(List<Person> personList) {
        employeeService.saveTransactionalEmployee(personList);
    }
}
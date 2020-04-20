package org.integration.test.all.factory;

import org.integration.test.all.data.Employee;
import org.integration.test.all.data.Person;
import org.integration.test.all.document.EmployeeDocument;
import org.springframework.stereotype.Component;

@Component
public class EmployeeFactory {

    public Employee convertPersonToEmployeeData(Person person) {
        Employee employee = new Employee();
        String nickName = person.getName() + "-" + person.getSurname();
        employee.setNickName(nickName);
        employee.setSalary((long) (person.getAge() * 1000));
        employee.setObjectId(person.getObjectId());
        return employee;
    }

    public EmployeeDocument convertEmployeeToEmployeeDocument(Employee employee) {
        EmployeeDocument employeeDocument = new EmployeeDocument();
        employeeDocument.setNickName(employee.getNickName());
        employeeDocument.setSalary(employee.getSalary());
        employeeDocument.setId(employee.getObjectId());
        return employeeDocument;
    }
}
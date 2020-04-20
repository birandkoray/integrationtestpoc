package org.integration.test.all.data;

import java.io.Serializable;
import java.util.Objects;

public class Employee implements Serializable {

    private String objectId;
    private Long salary;
    private String nickName;

    public Employee() {
    }

    public Employee(Long salary) {
        this.salary = salary;
    }

    public Employee(Long salary, String nickName) {
        this.salary = salary;
        this.nickName = nickName;
    }

    public Long getSalary() {
        return salary;
    }

    public void setSalary(Long salary) {
        this.salary = salary;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee)) return false;
        Employee employee = (Employee) o;
        return Objects.equals(getObjectId(), employee.getObjectId()) &&
                Objects.equals(getSalary(), employee.getSalary()) &&
                Objects.equals(getNickName(), employee.getNickName());
    }

    @Override
    public String toString() {
        return "Employee{" +
                "objectId='" + objectId + '\'' +
                ", salary=" + salary +
                ", nickName='" + nickName + '\'' +
                '}';
    }
}

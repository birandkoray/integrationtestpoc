package org.integration.test.kafka.model;

import java.io.Serializable;

public class Employee implements Serializable {
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
}

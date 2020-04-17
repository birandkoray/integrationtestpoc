package org.integration.test.all.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("employee")
public class EmployeeDocument {

    @Id
    private String id;
    private Long salary;
    private String nickName;

    public EmployeeDocument() {
    }

    public EmployeeDocument(Long salary) {
        this.salary = salary;
    }

    public EmployeeDocument(Long salary, String nickName) {
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

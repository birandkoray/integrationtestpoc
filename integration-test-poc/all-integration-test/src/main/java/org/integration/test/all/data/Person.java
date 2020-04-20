package org.integration.test.all.data;

import java.io.Serializable;

public class Person implements Serializable {

    private String objectId;
    private String name;
    private String surname;
    private UpdateTypeEnum updateTypeEnum;
    private Integer age;

    public Person() {
        super();
    }

    public Person(String name, String surname, Integer age) {
        this.name = name;
        this.surname = surname;
        this.age = age;
    }

    public Person(String name, String surname) {
        this.name = name;
        this.surname = surname;
    }

    public Person(String objectId, String name, String surname, Integer age) {
        this.objectId = objectId;
        this.name = name;
        this.surname = surname;
        this.age = age;
    }

    public Person(String name, String surname, UpdateTypeEnum updateTypeEnum, Integer age) {
        this.name = name;
        this.surname = surname;
        this.updateTypeEnum = updateTypeEnum;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public UpdateTypeEnum getUpdateTypeEnum() {
        return updateTypeEnum;
    }

    public void setUpdateTypeEnum(UpdateTypeEnum updateTypeEnum) {
        this.updateTypeEnum = updateTypeEnum;
    }

    @Override
    public String toString() {
        return "Person{" +
                "objectId='" + objectId + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", age=" + age +
                '}';
    }
}

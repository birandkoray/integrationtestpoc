package org.integration.test.mongo.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("person")
public class Person {

    @Id
    private String id;
    private String name;
    private String surname;
    private String age;

    public Person() {
    }

    public Person(String name, String surname, String age) {
        this.name = name;
        this.surname = surname;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}

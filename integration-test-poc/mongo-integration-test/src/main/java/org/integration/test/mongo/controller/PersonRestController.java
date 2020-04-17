package org.integration.test.mongo.controller;

import org.integration.test.mongo.document.Person;
import org.integration.test.mongo.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("person-service")
public class PersonRestController {

    @Autowired
    private PersonService personService;

    @GetMapping("/savePersonTest")
    public void savePersonForTest() {
        // Tarayicidan kolaylikla tetikleyebilmek icin yapildi
        Person person = new Person("Omer", "Celik", "27");
        personService.savePerson(person);
    }

    @PostMapping("/savePerson")
    public void savePerson(@RequestBody Person person) {
        personService.savePerson(person);
    }

    @PostMapping("/saveTransactionalPerson")
    public void saveTransactionalPerson(@RequestBody Person person) {
        personService.saveTransactionalPerson(person);
    }

    @PostMapping("/saveAllBulkPerson")
    public void saveBulkPerson() {
        personService.saveAllBulkPerson();
    }

    @PostMapping("/saveAllPersonMongoTemplate")
    public void saveAllPersonMongoTemplate() {
        personService.saveAllPersonMongoTemplate();
    }

    @PostMapping("/savePersonLongProcess")
    public void savePersonLongProcess() throws InterruptedException {
        personService.savePersonLongProcess();
    }

}
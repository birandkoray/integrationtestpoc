package org.integration.test.mongo.service;

import org.integration.test.mongo.document.Person;
import org.integration.test.mongo.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PersonService {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public void savePerson(Person person) {
        personRepository.save(person);
    }

    @Transactional
    public void saveTransactionalPerson(Person person) {
        personRepository.save(person);
        throw new RuntimeException("RollBack yapsin diye throw edildi..");
    }

    @Transactional
    public void saveAllBulkPerson() {
        List<Person> personList = createDummyPersonList();
        long firstTime = System.currentTimeMillis();
        personRepository.saveAll(personList);
        long lastTime = System.currentTimeMillis();
        long finalTime = lastTime - firstTime;
        System.out.println("saveAllBulkPerson : " + finalTime);
    }

    @Transactional
    public void saveAllPersonMongoTemplate() {
        List<Person> personList = createDummyPersonList();
        long firstTime = System.currentTimeMillis();
        BulkOperations bulkOperations = mongoTemplate.bulkOps(BulkOperations.BulkMode.ORDERED, Person.class);
        bulkOperations.insert(personList);
        bulkOperations.execute();
        long lastTime = System.currentTimeMillis();
        long finalTime = lastTime - firstTime;
        System.out.println("savePersonMongoTemplate : " + finalTime);
    }

    private List<Person> createDummyPersonList() {
        List<Person> personList = new ArrayList<>();
        for (int i = 0; i < 500000; i++) {
            personList.add(new Person("Omer " + i, "Celik " + i, "26"));
        }
        return personList;
    }


    // Boyle bir methodun sonucunda process uzun surdugu icin
    // transaction timeout'a ugrayip error throw eder.
    // time out suresini artirmak icin mongoya asagidaki ayar verilmelidir:
    // db.adminCommand( { setParameter: 1, transactionLifetimeLimitSeconds: 160 } )
    @Transactional
    public void savePersonLongProcess() throws InterruptedException {
        List<Person> personList = createDummyPersonList();
        BulkOperations bulkOperations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Person.class);
        bulkOperations.insert(personList);
        bulkOperations.execute();
        Thread.sleep(120000);
    }
}

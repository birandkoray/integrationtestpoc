package org.integration.test.all.service;

import org.integration.test.all.data.Employee;
import org.integration.test.all.data.Person;
import org.integration.test.all.document.EmployeeDocument;
import org.integration.test.all.factory.EmployeeFactory;
import org.integration.test.all.producer.EmployeeProducer;
import org.integration.test.all.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeFactory employeeFactory;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private EmployeeProducer employeeProducer;

    public void savePerson(Person person) {
        Employee employee = employeeFactory.convertPersonToEmployeeData(person);


        EmployeeDocument employeeDocument = employeeFactory.convertEmployeeToEmployeeDocument(employee);
        EmployeeDocument empDoc = employeeRepository.save(employeeDocument);

        employee.setObjectId(empDoc.getId());
        // employee datası cache'e yazilacak
        // cache.save(employee);

        employeeProducer.publishEmployee(employee);

    }

    /*@Transactional
    public void saveTransactionalPerson(Employee person) {
        employeeRepository.save(person);
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
    }*/

}

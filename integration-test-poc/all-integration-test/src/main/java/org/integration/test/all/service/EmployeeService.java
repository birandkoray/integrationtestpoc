package org.integration.test.all.service;

import org.integration.test.all.cacheAccessor.HazelcastMapAccessor;
import org.integration.test.all.cacheKeys.CacheKeys;
import org.integration.test.all.data.Employee;
import org.integration.test.all.data.Person;
import org.integration.test.all.document.EmployeeDocument;
import org.integration.test.all.factory.EmployeeFactory;
import org.integration.test.all.producer.EmployeeProducer;
import org.integration.test.all.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeFactory employeeFactory;

    @Autowired
    private EmployeeProducer employeeProducer;

    @Autowired
    private HazelcastMapAccessor hazelcastMapAccessor;

    @Autowired
    private MongoTemplate mongoTemplate;

    private BulkOperations employeeBulkOperations;

    public String saveOrUpdateEmployee(Person person) {
        Employee employee = employeeFactory.convertPersonToEmployeeData(person);

        EmployeeDocument employeeDocument = employeeFactory.convertEmployeeToEmployeeDocument(employee);
        EmployeeDocument empDoc = employeeRepository.save(employeeDocument);
        employee.setObjectId(empDoc.getId());

        hazelcastMapAccessor.put(CacheKeys.PERSON_MAP, employee.getObjectId(), employee);

        employeeProducer.publishEmployee(employee);
        return employeeDocument.getId();
    }

    public void deleteEmployee(String id) {
        employeeRepository.deleteById(id);
        hazelcastMapAccessor.delete(CacheKeys.PERSON_MAP, id);
    }

    @Transactional
    public void saveTransactionalEmployee(List<Person> personList) {
        employeeBulkOperations = mongoTemplate.bulkOps(BulkOperations.BulkMode.ORDERED, EmployeeDocument.class);
        Map<String, Employee> employeeMap = saveEmployeeListToDb(personList);
        hazelcastMapAccessor.putAll(CacheKeys.PERSON_MAP, employeeMap);
        employeeMap.entrySet().parallelStream().forEach(employee -> employeeProducer.publishEmployee(employee.getValue()));
    }


    private Map<String, Employee> saveEmployeeListToDb(List<Person> personList) {
        Map<String, Employee> employeeMap = new HashMap<>();
        personList.stream().forEach((person) -> {
            person.setObjectId(String.valueOf(new Random().nextInt(999999999)));
            Employee employee = employeeFactory.convertPersonToEmployeeData(person);
            EmployeeDocument employeeDocument = employeeFactory.convertEmployeeToEmployeeDocument(employee);
            employeeBulkOperations.insert(employeeDocument);
            employeeMap.put(employee.getObjectId(), employee);
        });
        employeeBulkOperations.execute();
        return employeeMap;
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

    @Transactional
    public void savePersonLongProcess() throws InterruptedException {
        List<Person> personList = createDummyPersonList();
        BulkOperations bulkOperations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Person.class);
        bulkOperations.insert(personList);
        bulkOperations.execute();
        Thread.sleep(120000);
    }*/

}

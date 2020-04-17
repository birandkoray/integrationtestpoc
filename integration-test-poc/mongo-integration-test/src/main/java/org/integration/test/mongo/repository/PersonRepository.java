package org.integration.test.mongo.repository;

import org.integration.test.mongo.document.Person;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonRepository extends MongoRepository<Person, String> {
    List<Person> findAll();
}

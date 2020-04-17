package org.integration.test.all.repository;

import org.integration.test.all.document.EmployeeDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends MongoRepository<EmployeeDocument, String> {
}

package org.integration.test.kafka.deserializer;

import org.integration.test.kafka.model.Employee;
import org.integration.test.kafka.model.Person;
import org.springframework.kafka.support.serializer.JsonDeserializer;

public class PersonDeserializer extends JsonDeserializer<Employee> {
}

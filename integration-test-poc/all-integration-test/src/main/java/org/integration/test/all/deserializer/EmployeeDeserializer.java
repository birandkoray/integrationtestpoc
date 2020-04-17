package org.integration.test.all.deserializer;

import org.integration.test.all.data.Employee;
import org.springframework.kafka.support.serializer.JsonDeserializer;

public class EmployeeDeserializer extends JsonDeserializer<Employee> {
}

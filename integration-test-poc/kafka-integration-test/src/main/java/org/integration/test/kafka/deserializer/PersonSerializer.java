package org.integration.test.kafka.deserializer;

import org.integration.test.kafka.model.Person;
import org.springframework.kafka.support.serializer.JsonSerializer;

public class PersonSerializer extends JsonSerializer<Person> {
}

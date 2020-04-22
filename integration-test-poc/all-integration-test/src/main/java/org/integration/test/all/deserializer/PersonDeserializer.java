package org.integration.test.all.deserializer;

import org.integration.test.all.data.Person;
import org.springframework.kafka.support.serializer.JsonDeserializer;

public class PersonDeserializer extends JsonDeserializer<Person> {
}

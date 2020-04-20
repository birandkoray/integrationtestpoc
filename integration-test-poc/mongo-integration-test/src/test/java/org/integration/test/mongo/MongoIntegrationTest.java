package org.integration.test.mongo;

import com.mongodb.MongoClient;
import org.integration.test.mongo.document.Person;
import org.integration.test.mongo.repository.PersonRepository;
import org.integration.test.mongo.service.PersonService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = "classpath:application.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MongoIntegrationTest {

    private MongoTemplate mongoTemplate;

    @Value("${spring.data.mongodb.host}")
    private String mongoServer;

    @Value("${spring.data.mongodb.port}")
    private int mongoPort;

    @Value("${server.port}")
    private int serverPort;

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonRepository personRepository;

    @BeforeAll
    public static void setUpBeforeClass() {
    }

    @Test
    @DisplayName("testRestSave")
    void testRestSave() throws Exception {
        // given
        mongoTemplate = new MongoTemplate(new MongoClient(mongoServer, mongoPort), "test");

        // when
        final String baseUrl = "http://localhost:" + serverPort + "/person-save/savePerson";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        Person person = new Person("Omer", "Celik", "30");

        HttpEntity<Person> request = new HttpEntity<>(person, headers);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForEntity(uri, request, Void.class);

        // then
        assertThat(mongoTemplate.findAll(Person.class, "person").size() == 1);
    }

    @Test
    public void testBulkData() {
        personService.saveAllBulkPerson();
        assertThat(personRepository.findAll().size() == PersonService.BULK_SIZE);
    }

    @Test
    public void testSave() {
        // given
        Person person = new Person("Omer2", "Celik2", "272");

        //when
        personService.savePerson(person);

        // then
        assertThat(personRepository.findAll().size() == 1);
    }
}

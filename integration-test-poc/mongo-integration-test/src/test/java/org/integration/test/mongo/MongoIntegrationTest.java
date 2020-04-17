package org.integration.test.mongo;

import com.mongodb.MongoClient;
import org.integration.test.mongo.document.Person;
import org.integration.test.mongo.repository.PersonRepository;
import org.integration.test.mongo.service.PersonService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.SocketUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MongoIntegrationTest {

    private MongoTemplate mongoTemplate;

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonRepository personRepository;

    private static int randomPort;

    private static String IP = "localhost";

    private static String REST_PORT = "8085";

    @BeforeAll
    public static void setUpBeforeClass() {
        randomPort = SocketUtils.findAvailableTcpPort();
        System.setProperty("server.port", REST_PORT);
        System.setProperty("spring.data.mongodb.port", String.valueOf(randomPort));
        System.setProperty("spring.data.mongodb.host", "localhost");
        System.setProperty("spring.data.mongodb.auto-index-creation", "true");
    }

    @Test
    @DisplayName("testRestSave")
    void testRestSave() throws Exception {
        // given
        mongoTemplate = new MongoTemplate(new MongoClient(IP, randomPort), "test");

        // when
        final String baseUrl = "http://localhost:" + REST_PORT + "/person-save/savePerson";
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

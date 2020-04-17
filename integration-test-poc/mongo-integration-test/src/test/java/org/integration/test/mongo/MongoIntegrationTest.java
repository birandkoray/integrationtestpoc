package org.integration.test.mongo;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mongodb.client.MongoClients;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.integration.test.mongo.document.Person;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class MongoIntegrationTest {


    private MongodExecutable mongodExecutable;
    private MongoTemplate mongoTemplate;

    @Mock
    private RestTemplate restTemplate;

    @AfterEach
    void clean() {
        mongodExecutable.stop();
    }

    @BeforeAll
    public static void setUpBeforeClass() {
        System.setProperty("spring.data.mongodb.uri", "mongodb://localhost:27010");
        System.setProperty("spring.data.mongodb.auto-index-creation", "true");
    }

    @BeforeEach
    void setup() throws Exception {
        String ip = "localhost";
        int port = 27010;

        IMongodConfig mongodConfig = new MongodConfigBuilder().version(Version.Main.PRODUCTION)
                .net(new Net(ip, port, Network.localhostIsIPv6()))
                .build();

        MongodStarter starter = MongodStarter.getDefaultInstance();
        mongodExecutable = starter.prepare(mongodConfig);
        mongodExecutable.start();
        mongoTemplate = new MongoTemplate(MongoClients.create("mongodb://" + ip + ":" + port), "test");
    }

    @Test
    void test() throws Exception {
        // given
        DBObject objectToSave = BasicDBObjectBuilder.start()
                .add("key", "value")
                .get();

        // when
        mongoTemplate.save(objectToSave, "collection");

        final String baseUrl = "http://localhost:8080" + "/person-service/savePerson";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        Person person = new Person("Omer", "Celik", "30");

        HttpEntity<Person> request = new HttpEntity<>(person, headers);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForEntity(uri, request, Void.class);

        // then
        assertThat(mongoTemplate.findAll(DBObject.class, "person")).extracting("key")
                .containsOnly("value");
    }

}

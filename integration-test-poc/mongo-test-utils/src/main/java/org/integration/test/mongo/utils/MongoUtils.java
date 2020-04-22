package org.integration.test.mongo.utils;

import com.mongodb.BasicDBList;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongoCmdOptionsBuilder;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.bson.Document;

import java.io.IOException;

public class MongoUtils {

    public void createReplicaMongo(int randomReplicaMongoPort, int randomReplicaMongoPort2) throws IOException {
        startNewMongoDb(randomReplicaMongoPort);
        startNewMongoDb(randomReplicaMongoPort2);
    }

    private void startNewMongoDb(int randomReplicaMongoPort) throws IOException {
        MongodStarter.getDefaultInstance().
                prepare(new MongodConfigBuilder()
                        .version(Version.Main.V4_0)
                        .withLaunchArgument("--replSet", "rs0")
                        .net(new Net(randomReplicaMongoPort, Network.localhostIsIPv6()))
                        .cmdOptions(new MongoCmdOptionsBuilder().useNoJournal(false).build())
                        .build()).start();
    }

    public void configureReplicaMongo(MongoClient mongoClient, int randomMongoPort, int randomMongoPort2) {
        MongoDatabase adminDatabase = mongoClient.getDatabase("admin");
        Document config = new Document("_id", "rs0");
        BasicDBList members = new BasicDBList();
        members.add(new Document("_id", 0)
                .append("host", "localhost:" + randomMongoPort));
        members.add(new Document("_id", 1)
                .append("host", "localhost:" + randomMongoPort2));
        config.put("members", members);
        adminDatabase.runCommand(new Document("replSetInitiate", config));
    }

}

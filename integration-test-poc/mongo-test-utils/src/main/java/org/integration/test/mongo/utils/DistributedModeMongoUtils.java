package org.integration.test.mongo.utils;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongoCmdOptionsBuilder;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DistributedModeMongoUtils {

    private Logger logger = LogManager.getLogger(DistributedModeMongoUtils.class);
    private List<MongodProcess> mongodProcessList = new ArrayList<>();
    private static List<Integer> randomReplicaMongoPortList = new ArrayList<>();
    private static final String ADMIN_DATABASE_NAME = "admin";


    public static final String REPLICA_SET_1_NAME = "shard_1_replicas";
    public static final String REPLICA_SET_2_NAME = "rs0";


    public void createReplicaMongo(String IP) throws Exception {
        setSystemProperty(IP);
        for (Integer randomReplicaMongoPort : randomReplicaMongoPortList) {
            startNewMongoDb(randomReplicaMongoPort);
        }
        configureReplicaMongo(IP);
        //configureSharded(IP);
    }

    private void startNewMongoDb(int randomReplicaMongoPort) throws IOException {
        MongodProcess mongodProcess = MongodStarter.getDefaultInstance().
                prepare(new MongodConfigBuilder()
                        .version(Version.Main.V4_0)
                        .withLaunchArgument("--replSet", "rs0")
                        .net(new Net(randomReplicaMongoPort, Network.localhostIsIPv6()))
                        .cmdOptions(new MongoCmdOptionsBuilder().useNoJournal(false).build())
                        .build()).start();
        mongodProcessList.add(mongodProcess);
    }

    private void configureReplicaMongo(String IP) throws InterruptedException {

        MongoDatabase adminDatabase = new MongoClient(new ServerAddress(IP, randomReplicaMongoPortList.get(2))).getDatabase(ADMIN_DATABASE_NAME);
        Document config = new Document("_id", "rs0");
        BasicDBList members = new BasicDBList();
        for (int i = 0; i < randomReplicaMongoPortList.size(); i++) {
            members.add(new Document("_id", i)
                    .append("host", IP + ":" + randomReplicaMongoPortList.get(i)));
        }
        config.put("members", members);

        Document resultDocument = adminDatabase.runCommand(new Document("replSetInitiate", config));

        while (!isReplicaSetStarted(resultDocument)) {
            logger.info("Waiting for 3 seconds...");
            Thread.sleep(1000);
            resultDocument = adminDatabase.runCommand(new BasicDBObject("replSetGetStatus", 1));
            logger.info("replSetGetStatus: {}", resultDocument);
        }
    }

    private boolean isReplicaSetStarted(Document setting) {
        if (setting.get("members") == null) {
            return false;
        }

        List members = (List) setting.get("members");
        for (Object m : members) {
            Document member = (Document) m;
            logger.info(member.toString());
            int state = member.getInteger("state");
            logger.info("state: {}", state);
            // 1 - PRIMARY, 2 - SECONDARY, 7 - ARBITER
            if (state != 1 && state != 2 && state != 7) {
                return false;
            }
        }
        return true;
    }

    private void setSystemProperty(String IP) throws IOException {
        int randomReplicaMongoPort = Network.getFreeServerPort();
        int randomReplicaMongoPort2 = Network.getFreeServerPort();
        int randomReplicaMongoPort3 = Network.getFreeServerPort();
        randomReplicaMongoPortList.add(randomReplicaMongoPort);
        randomReplicaMongoPortList.add(randomReplicaMongoPort2);
        randomReplicaMongoPortList.add(randomReplicaMongoPort3);
        System.setProperty("spring.mongodb.embedded.version", "4.0.2");
        System.setProperty("spring.data.mongodb.auto-index-creation", "true");
        System.setProperty("spring.data.mongodb.uri", "mongodb://" +
                IP + ":" + randomReplicaMongoPort + "," + IP + ":" + randomReplicaMongoPort2 +
                "," + IP + ":" + randomReplicaMongoPort3 + "/?replicaSet=rs0");
    }

    public void stop() {
        for (MongodProcess process : this.mongodProcessList) {
            process.stop();
        }
    }

    // YAPAMADIM UGRASIYORUM
    private void configureSharded(String IP) throws Exception {
        Thread.sleep(15000);
        Document documentResult;
        MongoClientOptions options = MongoClientOptions.builder()
                .connectTimeout(10)
                .build();

        try (MongoClient mongo = new MongoClient(
                new ServerAddress(IP, randomReplicaMongoPortList.get(1)))) {

            MongoDatabase mongoAdminDatabase = mongo.getDatabase(ADMIN_DATABASE_NAME);


            String command = "";
            for (Integer randomReplicaMongoPort : randomReplicaMongoPortList) {
                if (command.isEmpty()) {
                    command = REPLICA_SET_2_NAME + "/";
                } else {
                    command += ",";
                }
                command += IP
                        + ":" + randomReplicaMongoPort;
            }

            documentResult = mongoAdminDatabase.runCommand(new BasicDBObject("addShard", command));


            documentResult = mongoAdminDatabase.runCommand(new BasicDBObject("listShards", 1));

            // Enabled sharding at database level
            logger.info("Enabled sharding at database level");
            documentResult = mongoAdminDatabase.runCommand(new BasicDBObject("enableSharding",
                    "employee"));

            /*
            // Create index in sharded collection
            logger.info("Create index in sharded collection");
            MongoDatabase db = mongo.getDatabase("employee");
            db.getCollection("empCollection").createIndex("employee_id");

            // Shard the collection
            DBObject cmd = new BasicDBObject();
            cmd.put("shardCollection", this.shardDatabase + "." + this.shardCollection);
            cmd.put("key", new BasicDBObject(this.shardKey, 1));
            cr = mongoAdminDB.command(cmd);
            logger.info(cr.toString());

            logger.info("Get info from config/shards");
            DBCursor cursor = mongo.getDB("config").getCollection("shards").find();
            while (cursor.hasNext()) {
                DBObject item = cursor.next();
                logger.info(item.toString());
            }*/
        }

    }

}

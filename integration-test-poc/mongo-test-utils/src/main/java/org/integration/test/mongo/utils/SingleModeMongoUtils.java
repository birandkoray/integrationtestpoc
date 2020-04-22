package org.integration.test.mongo.utils;

import org.springframework.util.SocketUtils;

public class SingleModeMongoUtils {

    public static void startSingleMongo() {
        int randomMongoPort = SocketUtils.findAvailableTcpPort();
        System.setProperty("spring.data.mongodb.port", String.valueOf(randomMongoPort));
        System.setProperty("spring.data.mongodb.auto-index-creation", "true");
    }
}

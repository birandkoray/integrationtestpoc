package org.integration.test.kafka.stream;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface PersonStream {

    String INPUT = "person-topic-in";

    @Input(INPUT)
    SubscribableChannel inboundPerson();
}

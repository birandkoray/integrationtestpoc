package org.integration.test.all.stream;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface PersonStream {

    String INPUT = "person-topic-in";

    @Input(INPUT)
    SubscribableChannel inboundPerson();
}

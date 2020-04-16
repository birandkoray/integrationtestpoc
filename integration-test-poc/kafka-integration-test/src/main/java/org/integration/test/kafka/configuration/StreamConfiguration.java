package org.integration.test.kafka.configuration;

import org.integration.test.kafka.stream.EmployeeStream;
import org.integration.test.kafka.stream.PersonStream;
import org.springframework.cloud.stream.annotation.EnableBinding;

@EnableBinding({ PersonStream.class, EmployeeStream.class})
public class StreamConfiguration {
}

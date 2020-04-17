package org.integration.test.all.configuration;

import org.integration.test.all.stream.EmployeeStream;
import org.integration.test.all.stream.PersonStream;
import org.springframework.cloud.stream.annotation.EnableBinding;

@EnableBinding({ PersonStream.class, EmployeeStream.class})
public class StreamConfiguration {
}

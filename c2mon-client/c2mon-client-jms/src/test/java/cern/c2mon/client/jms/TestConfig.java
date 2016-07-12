package cern.c2mon.client.jms;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jms.annotation.EnableJms;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@EnableJms
@ImportResource({
    "classpath:config/c2mon-client-jms.xml",
    "classpath:test-config/c2mon-client-jms-properties.xml"
})
public class TestConfig {
}

package cern.c2mon.server.test.config;

import cern.c2mon.server.common.jms.EmbeddedBrokerAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@ComponentScan("cern.c2mon.server.test")
@Import({
    EmbeddedBrokerAutoConfiguration.class
})
public class TestConfig {}

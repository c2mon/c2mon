package cern.c2mon.server.dsl.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Martin Flamm
 */

@Configuration
@EnableConfigurationProperties(DslProperties.class)
@ComponentScan("cern.c2mon.server.dsl")
public class DslModule {
}
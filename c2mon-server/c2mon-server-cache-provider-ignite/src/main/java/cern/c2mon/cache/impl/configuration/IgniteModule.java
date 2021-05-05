package cern.c2mon.cache.impl.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * This class is responsible for configuring the Spring context for the
 * Ignite module.
 *
 */
@Configuration
@Import(C2monIgniteConfiguration.class)
@EnableConfigurationProperties(IgniteProperties.class)
@ComponentScan("cern.c2mon.cache.impl")
public class IgniteModule {
}

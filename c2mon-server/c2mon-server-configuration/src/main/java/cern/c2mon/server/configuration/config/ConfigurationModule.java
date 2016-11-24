package cern.c2mon.server.configuration.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@Import({
    ConfigDataSourceConfig.class
})
@EnableConfigurationProperties(ConfigurationProperties.class)
@ComponentScan("cern.c2mon.server.configuration")
public class ConfigurationModule {}

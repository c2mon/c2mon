package cern.c2mon.server.configuration.config;

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
@ComponentScan("cern.c2mon.server.configuration")
public class ConfigurationModule {}

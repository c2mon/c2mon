package cern.c2mon.server.history.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@Import({
    HistoryDataSourceConfig.class
})
@EnableConfigurationProperties(HistoryProperties.class)
@ComponentScan("cern.c2mon.server.history")
public class HistoryModule {}

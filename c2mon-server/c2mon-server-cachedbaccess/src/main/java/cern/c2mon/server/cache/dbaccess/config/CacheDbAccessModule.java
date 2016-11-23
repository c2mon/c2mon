package cern.c2mon.server.cache.dbaccess.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@ComponentScan("cern.c2mon.server.cache.dbaccess")
@Import({
    CacheDataSourceConfig.class
})
@EnableConfigurationProperties(CacheDbAccessProperties.class)
public class CacheDbAccessModule {}

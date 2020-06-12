package cern.c2mon.server.cache.dbaccess.config;

import cern.c2mon.server.common.config.ServerProperties;
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
public class CacheDbAccessModule {}

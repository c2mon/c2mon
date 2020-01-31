package cern.c2mon.server.cache.test.config;

import cern.c2mon.server.common.config.ServerProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@ComponentScan("cern.c2mon.server.cache.test")
@Import({
    ServerProperties.class,
})
public class CacheTestConfig {}

package cern.c2mon.server.supervision.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@Import({
  SupervisionCacheConfig.class
})
@ComponentScan("cern.c2mon.server.supervision")
public class SupervisionModule {}

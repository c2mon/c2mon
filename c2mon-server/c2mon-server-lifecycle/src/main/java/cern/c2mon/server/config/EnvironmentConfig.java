package cern.c2mon.server.config;

import cern.c2mon.server.client.config.ClientConfig;
import cern.c2mon.server.client.config.ClientJmsConfig;
import org.springframework.context.annotation.*;
import org.springframework.context.support.DefaultLifecycleProcessor;

/**
 * This class is responsible for configuring the C2MON server environment,
 * which involves loading the core modules and processing user-defined
 * property overrides.
 *
 * @author Justin Lewis Salmon
 */
@Configuration
@ImportResource({
    "classpath:config/server-cache.xml",
    "classpath:config/server-cachedbaccess.xml",
    "classpath:config/server-cacheloading.xml",
    "classpath:config/server-cachepersistence.xml",
    "classpath:config/server-supervision.xml",
    "classpath:config/server-daqcommunication-in.xml",
    "classpath:config/server-daqcommunication-out.xml",
    "classpath:config/server-rule.xml",
    "classpath:config/server-configuration.xml",
//    "classpath:config/server-client.xml",
    "classpath:config/server-alarm.xml",
    "classpath:config/server-command.xml",
})
@Import({
    ClientConfig.class
})
@PropertySources({
    @PropertySource("classpath:c2mon-server-default.properties"),
    @PropertySource(value = "${c2mon.server.properties}", ignoreResourceNotFound = true)}
)
public class EnvironmentConfig {

  @Bean
  public DefaultLifecycleProcessor lifecycleProcessor() {
    DefaultLifecycleProcessor lifecycleProcessor = new DefaultLifecycleProcessor();
    lifecycleProcessor.setTimeoutPerShutdownPhase(20000);
    return lifecycleProcessor;
  }
}

package cern.c2mon.server.config;

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
    "classpath:cern/c2mon/server/cache/config/server-cache.xml",
    "classpath:cern/c2mon/server/cache/dbaccess/config/server-cachedbaccess.xml",
    "classpath:cern/c2mon/server/cache/loading/config/server-cacheloading.xml",
    "classpath:cern/c2mon/server/cachepersistence/config/server-cachepersistence.xml",
    "classpath:cern/c2mon/server/supervision/config/server-supervision.xml",
    "classpath:cern/c2mon/server/daqcommunication/in/config/server-daqcommunication-in.xml",
    "classpath:cern/c2mon/server/daqcommunication/out/config/server-daqcommunication-out.xml",
    "classpath:cern/c2mon/server/rule/config/server-rule.xml",
    "classpath:cern/c2mon/server/configuration/config/server-configuration.xml",
    "classpath:cern/c2mon/server/client/config/server-client.xml",
    "classpath:cern/c2mon/server/alarm/config/server-alarm.xml",
    "classpath:cern/c2mon/server/command/config/server-command.xml",
})
@PropertySources({
    @PropertySource("classpath:c2mon.properties"),
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

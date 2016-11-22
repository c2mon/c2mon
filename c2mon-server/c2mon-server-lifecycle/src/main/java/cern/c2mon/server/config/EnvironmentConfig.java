package cern.c2mon.server.config;

import cern.c2mon.server.alarm.config.AlarmModule;
import cern.c2mon.server.cache.config.CacheModule;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModule;
import cern.c2mon.server.cachepersistence.config.CachePersistenceModule;
import cern.c2mon.server.client.config.ClientModule;
import cern.c2mon.server.command.config.CommandModule;
import cern.c2mon.server.configuration.config.ConfigurationModule;
import cern.c2mon.server.daqcommunication.in.config.DaqCommunicationInModule;
import cern.c2mon.server.daqcommunication.out.config.DaqCommunicationOutModule;
import cern.c2mon.server.elasticsearch.config.ElasticsearchModule;
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
    "classpath:config/server-supervision.xml",
    "classpath:config/server-rule.xml",
})
@Import({
    CacheModule.class,
    CacheDbAccessModule.class,
    CacheLoadingModule.class,
    CachePersistenceModule.class,
//    SupervisionModule.class,
    DaqCommunicationInModule.class,
    DaqCommunicationOutModule.class,
//    RuleModule.class,
    ConfigurationModule.class,
    ElasticsearchModule.class,
//    ShortTermLogModule.class,
    ClientModule.class,
    AlarmModule.class,
    CommandModule.class
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

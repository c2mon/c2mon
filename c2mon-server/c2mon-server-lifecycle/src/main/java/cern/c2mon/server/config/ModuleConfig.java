package cern.c2mon.server.config;

import cern.c2mon.cache.actions.CacheActionsModuleRef;
import cern.c2mon.cache.actions.alarm.AlarmModule;
import cern.c2mon.cache.config.CacheConfigModuleRef;
import cern.c2mon.cache.impl.configuration.C2monIgniteConfiguration;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModuleRef;
import cern.c2mon.server.cachepersistence.config.CachePersistenceModule;
import cern.c2mon.server.client.config.ClientModule;
import cern.c2mon.server.command.config.CommandModule;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.configuration.config.ConfigurationModule;
import cern.c2mon.server.daq.config.DaqModule;
import cern.c2mon.server.elasticsearch.config.ElasticsearchModule;
import cern.c2mon.server.history.config.HistoryModule;
import cern.c2mon.server.rule.config.RuleModule;
import cern.c2mon.server.supervision.config.SupervisionModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.DefaultLifecycleProcessor;

/**
 * This class is responsible for importing the core C2MON server modules.
 *
 * @author Justin Lewis Salmon
 */
@Configuration
@Import({
  CommonModule.class,
  CacheActionsModuleRef.class,
  CacheConfigModuleRef.class,
  CacheLoadingModuleRef.class,
  CacheDbAccessModule.class,
  CachePersistenceModule.class,
  C2monIgniteConfiguration.class,
  SupervisionModule.class,
  DaqModule.class,
  RuleModule.class,
  ConfigurationModule.class,
  ElasticsearchModule.class,
  HistoryModule.class,
  ClientModule.class,
  AlarmModule.class,
  CommandModule.class
})
public class ModuleConfig {

  @Bean
  public DefaultLifecycleProcessor lifecycleProcessor() {
    DefaultLifecycleProcessor lifecycleProcessor = new DefaultLifecycleProcessor();
    lifecycleProcessor.setTimeoutPerShutdownPhase(20000);
    return lifecycleProcessor;
  }
}

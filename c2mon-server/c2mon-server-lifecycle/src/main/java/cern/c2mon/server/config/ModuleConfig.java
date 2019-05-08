package cern.c2mon.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.DefaultLifecycleProcessor;

import cern.c2mon.server.cache.CacheModuleRef;
import cern.c2mon.server.cache.alarm.config.AlarmModule;
import cern.c2mon.server.cache.config.CacheModule;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loader.config.CacheLoaderModuleRef;
import cern.c2mon.server.cache.loading.config.CacheLoadingModule;
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

/**
 * This class is responsible for importing the core C2MON server modules.
 *
 * @author Justin Lewis Salmon
 */
@Configuration
@Import({
        CommonModule.class,
        CacheModule.class,
        CacheLoaderModuleRef.class,
        CacheModuleRef.class,
        CacheDbAccessModule.class,
        CacheLoadingModule.class,
        CachePersistenceModule.class,
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

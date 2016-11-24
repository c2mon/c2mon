package cern.c2mon.server.elasticsearch.config;

import cern.c2mon.pmanager.IAlarmListener;
import cern.c2mon.pmanager.IDBPersistenceHandler;
import cern.c2mon.pmanager.persistence.IPersistenceManager;
import cern.c2mon.pmanager.persistence.impl.PersistenceManager;
import cern.c2mon.server.elasticsearch.structure.types.EsAlarm;
import cern.c2mon.server.elasticsearch.structure.types.EsSupervisionEvent;
import cern.c2mon.server.elasticsearch.structure.types.tag.EsTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class ElasticsearchPersistenceConfig {

  @Autowired
  private ElasticsearchProperties properties;

  @Bean
  public IPersistenceManager<EsTag> esTagPersistenceManager(final IDBPersistenceHandler<EsTag> esTagIndexer,
      final IAlarmListener esAlarmListener) {
    String fallbackFile = properties.getTagFallbackFile();
    return new PersistenceManager<>(esTagIndexer, fallbackFile, esAlarmListener, new EsTag());
  }

  @Bean
  public IPersistenceManager<EsAlarm> esAlarmPersistenceManager(final IDBPersistenceHandler<EsAlarm> esAlarmIndexer,
      final IAlarmListener esAlarmListener) {
    String fallbackFile = properties.getAlarmFallbackFile();
    return new PersistenceManager<>(esAlarmIndexer, fallbackFile, esAlarmListener, new EsAlarm());
  }

  @Bean
  public IPersistenceManager<EsSupervisionEvent> esSupervisionEventPersistenceManager(
      final IDBPersistenceHandler<EsSupervisionEvent> esSupervisionEventIndexer, final IAlarmListener esAlarmListener) {
    String fallbackFile = properties.getSupervisionFallbackFile();
    return new PersistenceManager<>(esSupervisionEventIndexer, fallbackFile, esAlarmListener, new EsSupervisionEvent());
  }
}

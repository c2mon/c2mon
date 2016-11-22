package cern.c2mon.server.elasticsearch.config;

import cern.c2mon.pmanager.IAlarmListener;
import cern.c2mon.pmanager.IDBPersistenceHandler;
import cern.c2mon.pmanager.persistence.IPersistenceManager;
import cern.c2mon.pmanager.persistence.impl.PersistenceManager;
import cern.c2mon.server.elasticsearch.structure.types.EsAlarm;
import cern.c2mon.server.elasticsearch.structure.types.EsSupervisionEvent;
import cern.c2mon.server.elasticsearch.structure.types.tag.EsTag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class ElasticsearchPersistenceConfig {

  @Bean
  public IPersistenceManager<EsSupervisionEvent> esSupervisionEventPersistenceManager(
      @Qualifier("esSupervisionEventIndexer") final IDBPersistenceHandler<EsSupervisionEvent> persistenceHandler,
      @Value("/tmp/supervisionESfallback.txt") final String fallbackFile,
      @Qualifier("esAlarmListener") final IAlarmListener alarmListener) {

    return new PersistenceManager<>(persistenceHandler, fallbackFile, alarmListener, new EsSupervisionEvent());
  }

  @Bean
  public IPersistenceManager<EsAlarm> esAlarmPersistenceManager(
      @Qualifier("esAlarmIndexer") final IDBPersistenceHandler<EsAlarm> persistenceHandler,
      @Value("/tmp/alarmESfallback.txt") final String fallbackFile,
      @Qualifier("esAlarmListener") final IAlarmListener alarmListener) {

    return new PersistenceManager<>(persistenceHandler, fallbackFile, alarmListener, new EsAlarm());
  }


  @Bean
  public IPersistenceManager<EsTag> esTagPersistenceManager(
      @Qualifier("esTagIndexer") final IDBPersistenceHandler<EsTag> persistenceHandler,
      @Value("/tmp/tagESfallback.txt") final String fallbackFile,
      @Qualifier("esAlarmListener") final IAlarmListener alarmListener) {

    return new PersistenceManager<>(persistenceHandler, fallbackFile, alarmListener, new EsTag());
  }
}

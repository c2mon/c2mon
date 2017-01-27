package cern.c2mon.server.elasticsearch.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import cern.c2mon.pmanager.IAlarmListener;
import cern.c2mon.pmanager.IDBPersistenceHandler;
import cern.c2mon.pmanager.persistence.IPersistenceManager;
import cern.c2mon.pmanager.persistence.impl.PersistenceManager;
import cern.c2mon.server.elasticsearch.alarm.AlarmDocument;
import cern.c2mon.server.elasticsearch.supervision.SupervisionEventDocument;
import cern.c2mon.server.elasticsearch.tag.TagDocument;

/**
 * @author Justin Lewis Salmon
 */
public class ElasticsearchPersistenceConfig {

  @Autowired
  private ElasticsearchProperties properties;

  @Bean
  public IPersistenceManager<TagDocument> tagDocumentPersistenceManager(
      final IDBPersistenceHandler<TagDocument> esTagIndexer, final IAlarmListener fallbackActivationListener) {
    String fallbackFile = properties.getTagFallbackFile();
    return new PersistenceManager<>(esTagIndexer, fallbackFile, fallbackActivationListener, new TagDocument());
  }

  @Bean
  public IPersistenceManager<AlarmDocument> alarmDocumentPersistenceManager(
      final IDBPersistenceHandler<AlarmDocument> esAlarmIndexer, final IAlarmListener fallbackActivationListener) {
    String fallbackFile = properties.getAlarmFallbackFile();
    return new PersistenceManager<>(esAlarmIndexer, fallbackFile, fallbackActivationListener, new AlarmDocument());
  }

  @Bean
  public IPersistenceManager<SupervisionEventDocument> supervisionEventDocumentPersistenceManager(
      final IDBPersistenceHandler<SupervisionEventDocument> esSupervisionEventIndexer, final IAlarmListener fallbackActivationListener) {
    String fallbackFile = properties.getSupervisionFallbackFile();
    return new PersistenceManager<>(esSupervisionEventIndexer, fallbackFile, fallbackActivationListener, new SupervisionEventDocument());
  }
}

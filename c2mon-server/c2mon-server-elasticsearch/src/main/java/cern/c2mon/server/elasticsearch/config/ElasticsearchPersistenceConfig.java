package cern.c2mon.server.elasticsearch.config;

import cern.c2mon.pmanager.IAlarmListener;
import cern.c2mon.pmanager.IDBPersistenceHandler;
import cern.c2mon.pmanager.persistence.IPersistenceManager;
import cern.c2mon.pmanager.persistence.impl.PersistenceManager;
import cern.c2mon.server.elasticsearch.alarm.AlarmDocument;
import cern.c2mon.server.elasticsearch.supervision.SupervisionEventDocument;
import cern.c2mon.server.elasticsearch.tag.TagDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

/**
 * @author Justin Lewis Salmon
 */
public class ElasticsearchPersistenceConfig {

  @Autowired
  private ElasticsearchProperties properties;

  @Bean
  public IPersistenceManager<TagDocument> tagDocumentPersistenceManager(
      final IDBPersistenceHandler<TagDocument> esTagIndexer, final IAlarmListener esAlarmListener) {
    String fallbackFile = properties.getTagFallbackFile();
    return new PersistenceManager<>(esTagIndexer, fallbackFile, esAlarmListener, new TagDocument());
  }

  @Bean
  public IPersistenceManager<AlarmDocument> alarmDocumentPersistenceManager(
      final IDBPersistenceHandler<AlarmDocument> esAlarmIndexer, final IAlarmListener esAlarmListener) {
    String fallbackFile = properties.getAlarmFallbackFile();
    return new PersistenceManager<>(esAlarmIndexer, fallbackFile, esAlarmListener, new AlarmDocument());
  }

  @Bean
  public IPersistenceManager<SupervisionEventDocument> supervisionEventDocumentPersistenceManager(
      final IDBPersistenceHandler<SupervisionEventDocument> esSupervisionEventIndexer, final IAlarmListener esAlarmListener) {
    String fallbackFile = properties.getSupervisionFallbackFile();
    return new PersistenceManager<>(esSupervisionEventIndexer, fallbackFile, esAlarmListener, new SupervisionEventDocument());
  }
}

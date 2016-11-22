package cern.c2mon.server.cachepersistence.config;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.dbaccess.AlarmMapper;
import cern.c2mon.server.cachepersistence.CachePersistenceDAO;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManager;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManagerImpl;
import cern.c2mon.server.cachepersistence.impl.CachePersistenceDAOImpl;
import cern.c2mon.server.cachepersistence.listener.PersistenceSynchroListener;
import cern.c2mon.server.common.alarm.Alarm;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * @author Justin Lewis Salmon
 */
public class AlarmPersistenceConfig {

  @Bean
  public CachePersistenceDAO alarmPersistenceDAO(AlarmMapper alarmMapper, AlarmCache alarmCache) {
    return new CachePersistenceDAOImpl<>(alarmMapper, alarmCache);
  }

  @Bean
  public BatchPersistenceManager alarmPersistenceManager(CachePersistenceDAO<Alarm> alarmPersistenceDAO,
                                                         AlarmCache alarmCache,
                                                         Environment environment) {
    BatchPersistenceManagerImpl manager = new BatchPersistenceManagerImpl<>(alarmPersistenceDAO, alarmCache);
    manager.setTimeoutPerBatch(environment.getRequiredProperty("c2mon.server.cachepersistence.timeoutPerBatch", Integer.class));
    return manager;
  }

  @Bean
  public PersistenceSynchroListener alarmPersistenceSynchroListener(BatchPersistenceManager alarmPersistenceManager,
                                                                    AlarmCache alarmCache,
                                                                    Environment environment) {
    Integer pullFrequency = environment.getRequiredProperty("c2mon.server.cache.bufferedListenerPullFrequency", Integer.class);
    return new PersistenceSynchroListener(alarmCache, alarmPersistenceManager, pullFrequency);
  }
}

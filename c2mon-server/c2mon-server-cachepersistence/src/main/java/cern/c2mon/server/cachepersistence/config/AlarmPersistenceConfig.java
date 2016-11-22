package cern.c2mon.server.cachepersistence.config;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.dbaccess.AlarmMapper;
import cern.c2mon.server.cachepersistence.CachePersistenceDAO;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManager;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManagerImpl;
import cern.c2mon.server.cachepersistence.impl.CachePersistenceDAOImpl;
import cern.c2mon.server.cachepersistence.listener.PersistenceSynchroListener;
import cern.c2mon.server.common.alarm.Alarm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * @author Justin Lewis Salmon
 */
public class AlarmPersistenceConfig {

  @Autowired
  private Environment environment;

  @Autowired
  private AlarmMapper alarmMapper;

  @Autowired
  private AlarmCache alarmCache;

  @Bean
  public CachePersistenceDAO<Alarm> alarmPersistenceDAO() {
    return new CachePersistenceDAOImpl<>(alarmMapper, alarmCache);
  }

  @Bean
  public BatchPersistenceManager alarmPersistenceManager() {
    BatchPersistenceManagerImpl<Alarm> manager = new BatchPersistenceManagerImpl<>(alarmPersistenceDAO(), alarmCache);
    manager.setTimeoutPerBatch(environment.getRequiredProperty("c2mon.server.cachepersistence.timeoutPerBatch", Integer.class));
    return manager;
  }

  @Bean
  public PersistenceSynchroListener alarmPersistenceSynchroListener() {
    Integer pullFrequency = environment.getRequiredProperty("c2mon.server.cache.bufferedListenerPullFrequency", Integer.class);
    return new PersistenceSynchroListener(alarmCache, alarmPersistenceManager(), pullFrequency);
  }
}

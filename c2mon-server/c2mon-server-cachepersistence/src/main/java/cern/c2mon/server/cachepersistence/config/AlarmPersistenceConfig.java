package cern.c2mon.server.cachepersistence.config;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.dbaccess.AlarmMapper;
import cern.c2mon.server.cachepersistence.CachePersistenceDAO;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManager;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManagerImpl;
import cern.c2mon.server.cachepersistence.impl.CachePersistenceDAOImpl;
import cern.c2mon.server.cachepersistence.listener.PersistenceSynchroListener;
import cern.c2mon.server.common.alarm.Alarm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
public class AlarmPersistenceConfig extends AbstractPersistenceConfig {

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
    BatchPersistenceManagerImpl manager = new BatchPersistenceManagerImpl<>(alarmPersistenceDAO(), alarmCache,
        clusterCache, cachePersistenceThreadPoolTaskExecutor);
    manager.setTimeoutPerBatch(properties.getTimeoutPerBatch());
    return manager;
  }

  @Bean
  public PersistenceSynchroListener alarmPersistenceSynchroListener() {
    Integer pullFrequency = cacheProperties.getBufferedListenerPullFrequency();
    return new PersistenceSynchroListener(alarmCache, alarmPersistenceManager(), pullFrequency);
  }
}

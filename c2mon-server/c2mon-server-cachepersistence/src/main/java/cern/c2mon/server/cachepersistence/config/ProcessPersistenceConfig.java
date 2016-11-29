package cern.c2mon.server.cachepersistence.config;

import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.dbaccess.ProcessMapper;
import cern.c2mon.server.cachepersistence.CachePersistenceDAO;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManager;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManagerImpl;
import cern.c2mon.server.cachepersistence.impl.CachePersistenceDAOImpl;
import cern.c2mon.server.cachepersistence.listener.PersistenceSynchroListener;
import cern.c2mon.server.common.process.Process;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
public class ProcessPersistenceConfig extends AbstractPersistenceConfig {

  @Autowired
  private ProcessMapper processMapper;

  @Autowired
  private ProcessCache processCache;

  @Bean
  public CachePersistenceDAO<Process> processPersistenceDAO() {
    return new CachePersistenceDAOImpl<>(processMapper, processCache);
  }

  @Bean
  public BatchPersistenceManager processPersistenceManager() {
    BatchPersistenceManagerImpl manager = new BatchPersistenceManagerImpl<>(processPersistenceDAO(), processCache,
        clusterCache, cachePersistenceThreadPoolTaskExecutor);
    manager.setTimeoutPerBatch(properties.getTimeoutPerBatch());
    return manager;
  }

  @Bean
  public PersistenceSynchroListener processPersistenceSynchroListener() {
    Integer pullFrequency = cacheProperties.getBufferedListenerPullFrequency();
    return new PersistenceSynchroListener(processCache, processPersistenceManager(), pullFrequency);
  }
}

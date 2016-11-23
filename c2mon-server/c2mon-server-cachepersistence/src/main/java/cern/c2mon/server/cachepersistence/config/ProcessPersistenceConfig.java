package cern.c2mon.server.cachepersistence.config;

import cern.c2mon.server.cache.ClusterCache;
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
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author Justin Lewis Salmon
 */
public class ProcessPersistenceConfig {

  @Autowired
  private Environment environment;

  @Autowired
  private ClusterCache clusterCache;

  @Autowired
  private ThreadPoolTaskExecutor cachePersistenceThreadPoolTaskExecutor;

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
    manager.setTimeoutPerBatch(environment.getRequiredProperty("c2mon.server.cachepersistence.timeoutPerBatch", Integer.class));
    return manager;
  }

  @Bean
  public PersistenceSynchroListener processPersistenceSynchroListener() {
    Integer pullFrequency = environment.getRequiredProperty("c2mon.server.cache.bufferedListenerPullFrequency", Integer.class);
    return new PersistenceSynchroListener(processCache, processPersistenceManager(), pullFrequency);
  }
}

package cern.c2mon.server.cachepersistence.config;

import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.dbaccess.ProcessMapper;
import cern.c2mon.server.cachepersistence.CachePersistenceDAO;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManager;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManagerImpl;
import cern.c2mon.server.cachepersistence.impl.CachePersistenceDAOImpl;
import cern.c2mon.server.cachepersistence.listener.PersistenceSynchroListener;
import cern.c2mon.server.common.process.Process;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * @author Justin Lewis Salmon
 */
public class ProcessPersistenceConfig {

  @Bean
  public CachePersistenceDAO processPersistenceDAO(ProcessMapper processMapper, ProcessCache processCache) {
    return new CachePersistenceDAOImpl<>(processMapper, processCache);
  }

  @Bean
  public BatchPersistenceManager processPersistenceManager(CachePersistenceDAO<Process> processPersistenceDAO,
                                                           ProcessCache processCache,
                                                           Environment environment) {
    BatchPersistenceManagerImpl manager = new BatchPersistenceManagerImpl<>(processPersistenceDAO, processCache);
    manager.setTimeoutPerBatch(environment.getRequiredProperty("c2mon.server.cachepersistence.timeoutPerBatch", Integer.class));
    return manager;
  }

  @Bean
  public PersistenceSynchroListener processPersistenceSynchroListener(BatchPersistenceManager processPersistenceManager,
                                                                      ProcessCache processCache,
                                                                      Environment environment) {
    Integer pullFrequency = environment.getRequiredProperty("c2mon.server.cache.bufferedListenerPullFrequency", Integer.class);
    return new PersistenceSynchroListener(processCache, processPersistenceManager, pullFrequency);
  }
}

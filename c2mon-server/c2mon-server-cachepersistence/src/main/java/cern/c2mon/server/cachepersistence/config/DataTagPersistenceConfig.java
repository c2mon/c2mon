package cern.c2mon.server.cachepersistence.config;

import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.dbaccess.DataTagMapper;
import cern.c2mon.server.cachepersistence.CachePersistenceDAO;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManager;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManagerImpl;
import cern.c2mon.server.cachepersistence.impl.CachePersistenceDAOImpl;
import cern.c2mon.server.cachepersistence.listener.PersistenceSynchroListener;
import cern.c2mon.server.common.datatag.DataTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author Justin Lewis Salmon
 */
public class DataTagPersistenceConfig {

  @Autowired
  private Environment environment;

  @Autowired
  private ClusterCache clusterCache;

  @Autowired
  private ThreadPoolTaskExecutor cachePersistenceThreadPoolTaskExecutor;

  @Autowired
  private DataTagMapper dataTagMapper;

  @Autowired
  private DataTagCache dataTagCache;

  @Bean
  public CachePersistenceDAO<DataTag> dataTagPersistenceDAO() {
    return new CachePersistenceDAOImpl<>(dataTagMapper, dataTagCache);
  }

  @Bean
  public BatchPersistenceManager dataTagPersistenceManager() {
    BatchPersistenceManagerImpl manager = new BatchPersistenceManagerImpl<>(dataTagPersistenceDAO(), dataTagCache,
        clusterCache, cachePersistenceThreadPoolTaskExecutor);
    manager.setTimeoutPerBatch(environment.getRequiredProperty("c2mon.server.cachepersistence.timeoutPerBatch", Integer.class));
    return manager;
  }

  @Bean
  public PersistenceSynchroListener dataTagPersistenceSynchroListener() {
    Integer pullFrequency = environment.getRequiredProperty("c2mon.server.cache.bufferedListenerPullFrequency", Integer.class);
    return new PersistenceSynchroListener(dataTagCache, dataTagPersistenceManager(), pullFrequency);
  }
}

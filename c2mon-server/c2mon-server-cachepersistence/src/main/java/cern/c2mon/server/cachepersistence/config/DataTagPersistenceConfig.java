package cern.c2mon.server.cachepersistence.config;

import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.dbaccess.DataTagMapper;
import cern.c2mon.server.cachepersistence.CachePersistenceDAO;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManager;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManagerImpl;
import cern.c2mon.server.cachepersistence.impl.CachePersistenceDAOImpl;
import cern.c2mon.server.cachepersistence.listener.PersistenceSynchroListener;
import cern.c2mon.server.common.datatag.DataTag;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * @author Justin Lewis Salmon
 */
public class DataTagPersistenceConfig {

  @Bean
  public CachePersistenceDAO dataTagPersistenceDAO(DataTagMapper dataTagMapper, DataTagCache dataTagCache) {
    return new CachePersistenceDAOImpl<>(dataTagMapper, dataTagCache);
  }

  @Bean
  public BatchPersistenceManager dataTagPersistenceManager(CachePersistenceDAO<DataTag> dataTagPersistenceDAO,
                                                           DataTagCache dataTagCache,
                                                           Environment environment) {
    BatchPersistenceManagerImpl manager = new BatchPersistenceManagerImpl<>(dataTagPersistenceDAO, dataTagCache);
    manager.setTimeoutPerBatch(environment.getRequiredProperty("c2mon.server.cachepersistence.timeoutPerBatch", Integer.class));
    return manager;
  }

  @Bean
  public PersistenceSynchroListener dataTagPersistenceSynchroListener(BatchPersistenceManager dataTagPersistenceManager,
                                                                      DataTagCache dataTagCache,
                                                                      Environment environment) {
    Integer pullFrequency = environment.getRequiredProperty("c2mon.server.cache.bufferedListenerPullFrequency", Integer.class);
    return new PersistenceSynchroListener(dataTagCache, dataTagPersistenceManager, pullFrequency);
  }
}

package cern.c2mon.server.cachepersistence.config;

import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.cache.dbaccess.ControlTagMapper;
import cern.c2mon.server.cachepersistence.CachePersistenceDAO;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManager;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManagerImpl;
import cern.c2mon.server.cachepersistence.impl.CachePersistenceDAOImpl;
import cern.c2mon.server.cachepersistence.listener.PersistenceSynchroListener;
import cern.c2mon.server.common.control.ControlTag;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * @author Justin Lewis Salmon
 */
public class ControlTagPersistenceConfig {

  @Bean
  public CachePersistenceDAO controlTagPersistenceDAO(ControlTagMapper controlTagMapper, ControlTagCache controlTagCache) {
    return new CachePersistenceDAOImpl<>(controlTagMapper, controlTagCache);
  }

  @Bean
  public BatchPersistenceManager controlTagPersistenceManager(CachePersistenceDAO<ControlTag> controlTagPersistenceDAO,
                                                              ControlTagCache controlTagCache) {
    return new BatchPersistenceManagerImpl<>(controlTagPersistenceDAO, controlTagCache);
  }

  @Bean
  public PersistenceSynchroListener controlTagPersistenceSynchroListener(BatchPersistenceManager controlTagPersistenceManager,
                                                                         ControlTagCache controlTagCache,
                                                                         Environment environment) {
    Integer pullFrequency = environment.getRequiredProperty("c2mon.server.cache.bufferedListenerPullFrequency", Integer.class);
    return new PersistenceSynchroListener(controlTagCache, controlTagPersistenceManager, pullFrequency);
  }
}

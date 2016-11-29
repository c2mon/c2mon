package cern.c2mon.server.cachepersistence.config;

import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.cache.dbaccess.ControlTagMapper;
import cern.c2mon.server.cachepersistence.CachePersistenceDAO;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManager;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManagerImpl;
import cern.c2mon.server.cachepersistence.impl.CachePersistenceDAOImpl;
import cern.c2mon.server.cachepersistence.listener.PersistenceSynchroListener;
import cern.c2mon.server.common.control.ControlTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
public class ControlTagPersistenceConfig extends AbstractPersistenceConfig {

  @Autowired
  private ControlTagMapper controlTagMapper;

  @Autowired
  private ControlTagCache controlTagCache;

  @Bean
  public CachePersistenceDAO<ControlTag> controlTagPersistenceDAO() {
    return new CachePersistenceDAOImpl<>(controlTagMapper, controlTagCache);
  }

  @Bean
  public BatchPersistenceManager controlTagPersistenceManager() {
    return new BatchPersistenceManagerImpl<>(controlTagPersistenceDAO(), controlTagCache, clusterCache, cachePersistenceThreadPoolTaskExecutor);
  }

  @Bean
  public PersistenceSynchroListener controlTagPersistenceSynchroListener() {
    Integer pullFrequency = cacheProperties.getBufferedListenerPullFrequency();
    return new PersistenceSynchroListener(controlTagCache, controlTagPersistenceManager(), pullFrequency);
  }
}

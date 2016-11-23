package cern.c2mon.server.cachepersistence.config;

import cern.c2mon.server.cache.ClusterCache;
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
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author Justin Lewis Salmon
 */
public class ControlTagPersistenceConfig {

  @Autowired
  private Environment environment;

  @Autowired
  private ClusterCache clusterCache;

  @Autowired
  private ThreadPoolTaskExecutor cachePersistenceThreadPoolTaskExecutor;

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
    Integer pullFrequency = environment.getRequiredProperty("c2mon.server.cache.bufferedListenerPullFrequency", Integer.class);
    return new PersistenceSynchroListener(controlTagCache, controlTagPersistenceManager(), pullFrequency);
  }
}

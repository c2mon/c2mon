package cern.c2mon.server.cachepersistence.config;

import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.config.CacheProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author Justin Lewis Salmon
 */
public abstract class AbstractPersistenceConfig {

  @Autowired
  protected CacheProperties cacheProperties;

  @Autowired
  protected CachePersistenceProperties properties;

  @Autowired
  protected ClusterCache clusterCache;

  @Autowired
  protected ThreadPoolTaskExecutor cachePersistenceThreadPoolTaskExecutor;
}

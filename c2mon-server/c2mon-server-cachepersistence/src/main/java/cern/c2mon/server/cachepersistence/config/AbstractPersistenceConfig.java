package cern.c2mon.server.cachepersistence.config;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.config.CacheProperties;
import cern.c2mon.server.cachepersistence.CachePersistenceDAO;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManager;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManagerImpl;
import cern.c2mon.server.cachepersistence.listener.PersistenceSynchroListener;
import cern.c2mon.shared.common.Cacheable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * @author Alexandros Papageorgiou, Justin Lewis Salmon
 */
public abstract class AbstractPersistenceConfig<CACHEABLE extends Cacheable> {

  @Inject
  private CacheProperties cacheProperties;

  @Inject
  private CachePersistenceProperties properties;

  @Inject
  private ThreadPoolTaskExecutor cachePersistenceThreadPoolTaskExecutor;

  private CachePersistenceDAO<CACHEABLE> persistenceDAO;

  private C2monCache<CACHEABLE> cache;

  protected AbstractPersistenceConfig(final C2monCache<CACHEABLE> cache,
                                      final CachePersistenceDAO<CACHEABLE> persistenceDAO) {
    this.cache = cache;
    this.persistenceDAO = persistenceDAO;
  }

  @PostConstruct
  public void init() {
    int pullFrequency = cacheProperties.getBufferedListenerPullFrequency();

    final PersistenceSynchroListener<CACHEABLE> persistenceListener =
      new PersistenceSynchroListener<>(persistenceManager());

    cache.getCacheListenerManager().registerBufferedListener(persistenceListener);
  }

  private BatchPersistenceManager persistenceManager() {
    BatchPersistenceManagerImpl manager = new BatchPersistenceManagerImpl<>(persistenceDAO, cache, cachePersistenceThreadPoolTaskExecutor);
    manager.setTimeoutPerBatch(properties.getTimeoutPerBatch());
    return manager;
  }
}

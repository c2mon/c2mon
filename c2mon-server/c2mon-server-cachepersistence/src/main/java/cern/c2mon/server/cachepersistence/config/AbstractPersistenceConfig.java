package cern.c2mon.server.cachepersistence.config;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cachepersistence.CachePersistenceDAO;
import cern.c2mon.server.cachepersistence.common.BatchPersistenceManagerImpl;
import cern.c2mon.server.cachepersistence.listener.PersistenceSynchroListener;
import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.Cacheable;
import lombok.Getter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * @author Alexandros Papageorgiou, Justin Lewis Salmon
 */
public abstract class AbstractPersistenceConfig<CACHEABLE extends Cacheable> {

  @Inject
  private CachePersistenceProperties properties;

  @Inject
  private ThreadPoolTaskExecutor cachePersistenceThreadPoolTaskExecutor;

  @Getter
  private CachePersistenceDAO<CACHEABLE> persistenceDAO;

  private C2monCache<CACHEABLE> cache;

  @Getter
  private BatchPersistenceManagerImpl<CACHEABLE> batchPersistenceManager;

  protected AbstractPersistenceConfig(final C2monCache<CACHEABLE> cache,
                                      final CachePersistenceDAO<CACHEABLE> persistenceDAO) {
    this.cache = cache;
    this.persistenceDAO = persistenceDAO;
  }

  @PostConstruct
  public void init() {
    batchPersistenceManager = new BatchPersistenceManagerImpl<>(persistenceDAO, cache, cachePersistenceThreadPoolTaskExecutor);
    batchPersistenceManager.setTimeoutPerBatch(properties.getTimeoutPerBatch());
    cache.getCacheListenerManager().registerBatchListener(new PersistenceSynchroListener<>(batchPersistenceManager), CacheEvent.UPDATE_ACCEPTED);
  }
}

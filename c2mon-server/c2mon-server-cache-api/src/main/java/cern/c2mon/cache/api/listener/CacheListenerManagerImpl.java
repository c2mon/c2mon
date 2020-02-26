package cern.c2mon.cache.api.listener;

import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.Cacheable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/**
 * Implements a {@link CacheListener} that uses an {@link ExecutorService} to manage incoming events
 * <p>
 * Thread safety of this class:
 * <ul>
 *   <li>A "frozen copy" clone of the original object is always passed. Mutating it will have no effect on others.
 *   <li>Putting the object back in the cache is an unsafe operation, as the object may have changed in the meanwhile.
 *       Ensure that the object you are attempting to update is what you expect. Using
 *       {@link cern.c2mon.cache.api.C2monCache#compute(long, java.util.function.Consumer)} is strongly advised, as
 *       it will allow you to delta the object with your desired changes, regardless of previous state.
 * </ul>
 * <p>
 * The buffered event handler will be run during a fixed period (by default 10 seconds), or when the pending
 * updates exceed the maximum limit specified (default 100.000)
 * <p>
 * Will attempt to shut down cleanly if possible. As soon as a shutdown signal {@link CacheListenerManagerImpl#close()}
 * is received, new incoming events will be rejected.
 *
 * @param <CACHEABLE> the type of objects received during events.
 * @author Alexandros Papageorgiou Koufidis
 */
public class CacheListenerManagerImpl<CACHEABLE extends Cacheable> implements CacheListenerManager<CACHEABLE> {
  private static final Logger log = LoggerFactory.getLogger(CacheListenerManager.class);

  private final ExecutorService centralizedExecutorService;
  //
  private final CacheListenerProperties cacheListenerProperties;

  // Is and should remain concurrent both in the map level and the set level!
  private final Map<CacheEvent, Set<CacheListener<? super CACHEABLE>>> eventListeners;

  public CacheListenerManagerImpl() {
    cacheListenerProperties = new CacheListenerProperties();
    eventListeners = new ConcurrentHashMap<>();
    // Initialize each list using a "ConcurrentSet"
    for (CacheEvent event : CacheEvent.values()) {
      eventListeners.put(event, ConcurrentHashMap.newKeySet());
    }
    centralizedExecutorService = Executors.newFixedThreadPool(cacheListenerProperties.getConcurrency());
  }

  @Override
  public void notifyListenersOf(CacheEvent event, CACHEABLE source) {
    if (!cacheListenerProperties.isEnabled()) {
      log.debug("Dropping update received as cache listeners are disabled");
      return;
    }

    eventListeners.get(event)
      .forEach(listener -> {
        try {
          // The clones here may end up bottlenecking, though I doubt it. Keep an eye on them though
          centralizedExecutorService.submit(() -> listener.apply((CACHEABLE) source.clone()));
        } catch (RejectedExecutionException rejected) {
          log.info("Rejected execution of {} #{} for source event {}. Details: {}",
            source.getClass(), source.getId(), event, rejected);
        }
      });
  }

  @Override
  public void registerListener(CacheListener<CACHEABLE> listener, CacheEvent baseEvent, CacheEvent... events) {
    eventListeners.get(baseEvent).add(listener);
    for (CacheEvent cacheEvent : events) {
      eventListeners.get(cacheEvent).add(listener);
    }
  }

  @Override
  public void registerBatchListener(BatchConsumer<CACHEABLE> listener, CacheEvent baseEvent, CacheEvent... events) {
    registerBatchListener(new BatchCacheListener<>(listener), baseEvent, events);
  }

  @Override
  public void registerBatchListener(BatchCacheListener<CACHEABLE> listener, CacheEvent baseEvent, CacheEvent... events) {
    // By adding to the normal event listener list, we get the items in the buffered listener list
    eventListeners.get(baseEvent).add(listener);
    for (CacheEvent cacheEvent : events) {
      eventListeners.get(cacheEvent).add(listener);
    }
  }

  /**
   * The executor service will attempt to shut down cleanly. If this fails, it force quits, which can lead to some
   * event handlers being interrupted. By default the handlers should be operating on copies of Cache Objects, but
   * for any critical operation, there may be value in designing with potential interruptions in mind.
   */
  @Override
  public void close() {
    try {
      // TODO (Alex) Process pending items?

      // Give the executor a chance to exit gracefully
      centralizedExecutorService.shutdown();

      centralizedExecutorService.awaitTermination(cacheListenerProperties.getShutdownWait(), cacheListenerProperties.getShutdownWaitUnits());
    } catch (InterruptedException e) {
      log.warn("Executor service interrupted while shutting down", e);
    }
  }
}

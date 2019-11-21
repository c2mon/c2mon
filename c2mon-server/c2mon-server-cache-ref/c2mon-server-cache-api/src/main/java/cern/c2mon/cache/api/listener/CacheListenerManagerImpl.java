package cern.c2mon.cache.api.listener;

import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.Cacheable;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;


/**
 * A base implementation of {@link CacheListener} that uses an {@link ExecutorService} to manage incoming events
 * <p>
 * Will attempt to shut down cleanly if possible. As soon as a shutdown signal {@link CacheListenerManagerImpl#close()}
 * is received, new incoming events will be rejected.
 * <p>
 * Thread safety of this class:
 * <ul>
 *   <li>A "frozen copy" clone of the original object is always passed. Mutating it will have no effect on others.
 *   <li>Putting the object back in the cache is an unsafe operation, as the object may have changed in the meanwhile.
 *       Ensure that the object you are attempting to update is what you expect. Using
 *       {@link cern.c2mon.cache.api.C2monCache#compute(long, java.util.function.Consumer)} is strongly advised, as
 *       it will allow you to delta the object with your desired changes, regardless of previous state.
 * </ul>
 *
 * The buffered event handler will be run during a fixed period (by default 10 seconds), or when the pending
 * updates exceed the maximum limit specified (default 100.000)
 *
 * @param <CACHEABLE> the type of objects received during events.
 * @author Alexandros Papageorgiou Koufidis
 */
@Slf4j
public class CacheListenerManagerImpl<CACHEABLE extends Cacheable> implements CacheListenerManager<CACHEABLE> {
  private static final long DEFAULT_SHUTDOWN_WAIT = 1;
  private static final TimeUnit DEFAULT_SHUTDOWN_WAIT_UNITS = TimeUnit.SECONDS;
  private static final long DEFAULT_PERIOD = 10;

  private static final ExecutorService centralizedExecutorService = Executors.newFixedThreadPool(32);
  private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);

  // Is and should remain concurrent both in the map level and the set level!
  private final Map<CacheEvent, Set<CacheListener<CACHEABLE>>> eventListeners;

  public CacheListenerManagerImpl(int concurrency){
    eventListeners = new ConcurrentHashMap<>();
    // Initialize each list using a "ConcurrentSet"
    for (CacheEvent event : CacheEvent.values()) {
      eventListeners.put(event, Collections.newSetFromMap(new ConcurrentHashMap<>()));
    }
  }

  //  TODO (Alex) Should this switch to try-catch for safety? We should be ok with Executor threads blowing up

  @Override
  public void notifyListenersOf(CacheEvent event, CACHEABLE source) {
    eventListeners.get(event) // TODO (Alex) Do we want this many clones?
      .forEach(listener -> centralizedExecutorService.submit(() -> listener.apply((CACHEABLE) source.clone())));
  }

  @Override
  public void registerListener(CacheListener<CACHEABLE> listener, CacheEvent... events) {
    for (CacheEvent cacheEvent : events) {
      eventListeners.get(cacheEvent).add(listener);
    }
  }

  @Override
  public void registerBufferedListener(BufferedCacheListener<CACHEABLE> listener, CacheEvent... events) {
    BufferedCacheListenerImpl<CACHEABLE> runnableListener = new BufferedCacheListenerImpl<>(centralizedExecutorService, listener);

    // By adding to the normal event listener list, we get the items in the buffered listener list
    for (CacheEvent cacheEvent : events) {
      eventListeners.get(cacheEvent).add(runnableListener);
    }

    // TODO (Alex) Where do we want to run this?
    scheduledExecutorService.scheduleAtFixedRate(runnableListener, 0, DEFAULT_PERIOD, TimeUnit.SECONDS);
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
      scheduledExecutorService.shutdown();
      centralizedExecutorService.shutdown();
      scheduledExecutorService.awaitTermination(DEFAULT_SHUTDOWN_WAIT, DEFAULT_SHUTDOWN_WAIT_UNITS);
      centralizedExecutorService.awaitTermination(DEFAULT_SHUTDOWN_WAIT, DEFAULT_SHUTDOWN_WAIT_UNITS);
    } catch (InterruptedException e) {
      log.warn("Executor service interrupted while shutting down");
      e.printStackTrace();
    }
  }
}

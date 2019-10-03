package cern.c2mon.cache.api.listener.impl;

import cern.c2mon.cache.api.listener.CacheListener;
import cern.c2mon.cache.api.transactions.TransactionalCallable;
import cern.c2mon.shared.common.Cacheable;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A base implementation of {@link CacheListener} that uses an {@link ExecutorService} to manage incoming events
 * <p>
 * Will attempt to shut down cleanly if possible. As soon as a shutdown signal {@link AbstractCacheListener#close()}
 * is received, new incoming events will be rejected.
 * <p>
 * Thread safety of this class (and implementations):
 * <ul>
 *   <li>All implementations are running in a different thread than the event thread.
 *   <li>A "frozen copy" clone of the original object is always passed. Mutating it will have no effect on others.
 *   <li>Putting the object back in the cache is an unsafe operation, as the object may have changed in the meanwhile.
 *       Ensure that the object you are attempting to update is what you expect. Using {@link cern.c2mon.cache.api.C2monCache#executeTransaction(TransactionalCallable)}
 *       is strongly advised, as it will allow you to verify and update the object without it changing in between.
 * </ul>
 *
 * @param <V> the cache object type we are getting updates on
 * @author Alexandros Papageorgiou Koufidis
 */
@Slf4j
public abstract class AbstractCacheListener<V extends Cacheable> implements CacheListener<V>, Closeable {
  private static final long DEFAULT_SHUTDOWN_WAIT = 1;
  private static final TimeUnit DEFAULT_SHUTDOWN_WAIT_UNITS = TimeUnit.SECONDS;
  protected final ExecutorService executorService;

  protected AbstractCacheListener(ExecutorService executorService) {
    this.executorService = executorService;
  }

  /**
   * The executor service will attempt to shut down cleanly. If this fails, it force quits, which can lead to some
   * event handlers being interrupted. By default the handlers should be operating on copies of Cache Objects, but
   * for any critical operation, there may be value in designing with potential interruptions in mind.
   */
  @Override
  public void close() {
    try {
      // Give the executor a chance to exit gracefully
      executorService.shutdown();
      executorService.awaitTermination(DEFAULT_SHUTDOWN_WAIT, DEFAULT_SHUTDOWN_WAIT_UNITS);
    } catch (InterruptedException e) {
      log.warn("Executor service interrupted while shutting down");
      e.printStackTrace();
    }
  }
}

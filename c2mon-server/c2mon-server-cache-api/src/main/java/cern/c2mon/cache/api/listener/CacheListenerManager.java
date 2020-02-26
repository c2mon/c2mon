package cern.c2mon.cache.api.listener;

import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.Cacheable;

import java.io.Closeable;

/**
 * Controls the listeners for a given cache and propagates events.
 *
 * This class is highly asynchronous by design. No business logic
 * is contained here, only simple ops.
 *
 * @param <CACHEABLE> the type of objects received during events.
 * @author Alexandros Papageorgiou Koufidis
 * @see CacheListenerManagerImpl for more implementation details
 */
public interface CacheListenerManager<CACHEABLE extends Cacheable> extends Closeable {

  /**
   * Will send out a {@link CacheEvent} notification to all registered listeners. The
   * object is cloned before sending to each listener - they are free to mutate it
   * if they require to do so.
   *
   * The method returns when all the notifications have been submitted to the pool,
   * and there are no guarantees about how many of them may have been executed!
   *
   * @param event the {@code CacheEvent} to notify listeners of
   * @param source the {@code Cacheable} that caused this event
   */
  void notifyListenersOf(CacheEvent event, CACHEABLE source);

  /**
   * Create an action that will be run when any of {@code requiredEvent}, {@code events} occur.
   *
   * The parameter received inside the {@code listener} lambda is a clone - mutating
   * it is allowed (though generally not advised)
   *
   * @param listener the action to execute, can use the original object that caused this
   * @param requiredEvent an event to listen to (specified outside of varargs as at least 1 required)
   * @param events additional events to listen to
   */
  void registerListener(CacheListener<CACHEABLE> listener, CacheEvent requiredEvent, CacheEvent... events);

  /**
   * An alternative cache listener, that collects all incoming updates to a list, then periodically runs an event handler
   * on that list
   * <p>
   * Use buffered listeners to increase performance for very busy caches with loads of updates,
   * or for operations optimized for {@code Collection}s
   *
   * This is the simpler lambda based constructor.
   *
   * @see CacheListenerManager#registerBufferedListener(BatchCacheListener, CacheEvent, CacheEvent...)
   *      If you need more customization over the listener, such as parameterized scheduling
   */
  void registerBufferedListener(BatchConsumer<CACHEABLE> listener, CacheEvent requiredEvent, CacheEvent... events);

  /**
   * An alternative cache listener, that collects all incoming updates to a list, then periodically runs an event handler
   * on that list
   * <p>
   * Use buffered listeners to increase performance for very busy caches with loads of updates,
   * or for operations optimized for {@code Collection}s
   */
  void registerBufferedListener(BatchCacheListener<CACHEABLE> listener, CacheEvent requiredEvent, CacheEvent... events);

  /**
   * Stops accepting any future events and terminates the underlying pool
   *
   * Method returns when the pool has been shutdown.
   * @see CacheListenerProperties for controlling the default shutdown timers
   */
  @Override
  void close();
}

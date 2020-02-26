package cern.c2mon.cache.api.listener;

import cern.c2mon.shared.common.Cacheable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

/**
 * An alternative cache listener, that collects all incoming updates to a list,
 * then periodically runs an event handler on that list
 *
 * Eliminates duplicates based on object equality (two objects with the same id
 * could both exist in the resultset: for example, an {@code Alarm} with id 1
 * could be present twice in the results if it has different {@code Alarm#triggerTimestamp}).
 *
 * If between updates it collects a large amount of items (more than {@code CacheListenerProperties.batchSize}),
 * it will create a task to handle those items proactively.
 *
 * @param <CACHEABLE> the type of {@link Cacheable}s handled by this listener
 */
final class BatchCacheListener<CACHEABLE extends Cacheable> implements CacheListener<CACHEABLE>, Runnable, Closeable {
  private static final Logger log = LoggerFactory.getLogger(BatchCacheListener.class);
  /**
   * Executor for *emergency* offloading tasks when the primary one is full
   */
  private static final ExecutorService overflowTaskExecutor = Executors.newFixedThreadPool(2);

  /**
   * Executor for normal scheduling, shared
   */
  private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);

  private final Set<CACHEABLE> items = ConcurrentHashMap.newKeySet();
  private BatchConsumer<CACHEABLE> eventHandler;
  private CacheListenerProperties properties;

  /**
   *
   * @param eventHandler what action to take on the list of pending items when the task runs
   */
  public BatchCacheListener(BatchConsumer<CACHEABLE> eventHandler) {
    this(eventHandler, new CacheListenerProperties());
  }

  /**
   * Creates the listener and registers the task in the internal scheduler to run on the default schedule
   *
   * No guarantees are made about tasks running in parallel or not - take all
   * reasonable precautions to ensure a thread-safe result
   *
   * @param eventHandler what action to take on the list of pending items when the task runs
   * @param properties the properties to configure this listener
   */
  public BatchCacheListener(BatchConsumer<CACHEABLE> eventHandler, CacheListenerProperties properties) {
    this.eventHandler = eventHandler;
    this.properties = properties;
    scheduledExecutorService.scheduleAtFixedRate(this, 0, properties.getBatchSchedulePeriodMillis(), TimeUnit.MILLISECONDS);
  }

  /**
   * Accepts an object and adds it to the internal buffer list, to process collectively later
   *
   * @param cacheable the item, after whatever event happened that has triggered this action
   */
  @Override
  public final void apply(CACHEABLE cacheable) {
    items.add(cacheable);
    if (items.size() > properties.getBatchSize()) {
      overflowTaskExecutor.submit(this);
    }
  }

  /**
   * The actual operation that we'll run every X seconds
   * <p>
   * Creates a copy list to avoid the original being modified during iteration
   */
  @Override
  public final void run() {
    if (items.isEmpty()) {
      return;
    }

    Set<CACHEABLE> itemsCopy = new HashSet<>(items);
    // We're not doing a clear here, as other items may have joined the list
    // This relies on our Cacheables having a proper equals implementation!
    items.removeAll(itemsCopy);
    eventHandler.apply(itemsCopy);
  }

  @Override
  public void close() {
    try {
      // Give the executor a chance to exit gracefully
      scheduledExecutorService.shutdown();

      if (!overflowTaskExecutor.isShutdown()) {
        overflowTaskExecutor.shutdown();
      }

      overflowTaskExecutor.awaitTermination(properties.getShutdownWait(), properties.getShutdownWaitUnits());
      scheduledExecutorService.awaitTermination(properties.getShutdownWait(), properties.getShutdownWaitUnits());
    } catch (InterruptedException e) {
      log.warn("Executor service interrupted while shutting down", e);
    }
  }
}

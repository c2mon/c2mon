package cern.c2mon.cache.api.listener.impl;

import cern.c2mon.shared.common.Cacheable;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An alternative cache listener, that collects all incoming updates to a list, then periodically runs an event handler
 * on that list
 * <p>
 * Use this to increase performance for very busy caches with loads of updates, or for operations optimized for {@code Collection}s
 * <p>
 * The event handler will be run during a fixed period (by default 1 second), or when the pending updates exceed the
 * maximum limit specified (default 100.000)
 *
 * @param <V> the type of objects received during events.
 * @param <T> the type of objects collected in the buffer queue. May or may not be the same as V - consumers may
 *            choose to override the transformer function applied using the full constructor:
 *            {@link BufferedListener#BufferedListener(Consumer, long, int, Function)}
 */
@Slf4j
public class BufferedListener<V extends Cacheable, T> extends AbstractCacheListener<V> {
  private static final long DEFAULT_PERIOD = 1;
  private static final int DEFAULT_MAX_SIZE = 100_000;
  private final List<T> items = Collections.synchronizedList(new ArrayList<>());
  private final int maxQueueSize;
  private final Function<V, T> transformer;
  private Consumer<List<T>> eventHandler;

  /**
   * The actual operation that we'll run every X seconds
   * <p>
   * Creates a copy list to avoid the original being modified during iteration
   */
  private Runnable batchProcessor = () -> {
    if (items.isEmpty())
      return;
    List<T> copyList = new ArrayList<>(items);
    // We're not doing a clear here, as other items may have joined the list
    // This relies on our Cacheables having a proper equals implementation!
    items.removeAll(copyList);
    eventHandler.accept(copyList);
  };

  /**
   * Create a BatchListener with user defined period, max queue size and transformer function
   *
   * @param eventHandler the action to perform on the transformed objects collected between updates
   * @param period       the period between subsequent runs of the eventHandler
   * @param maxQueueSize the max size allowed for buffered items queue, before an emergency task is run to clear it
   * @param transformer  a transform function on the original elements received, e.g to keep only the id
   *                     {@code Cacheable::getId}. If your V and T are the same, use {@code Function.identity()}
   */
  protected BufferedListener(Consumer<List<T>> eventHandler, long period, int maxQueueSize, Function<V, T> transformer) {
    super(Executors.newScheduledThreadPool(1));
    this.eventHandler = eventHandler;
    this.maxQueueSize = maxQueueSize;
    this.transformer = transformer;
    ((ScheduledExecutorService) executorService).scheduleAtFixedRate(batchProcessor, 0, period, TimeUnit.SECONDS);
  }

  /**
   * Create a BatchListener with default period.
   * <p>
   * Assumes V and T are the same, which means the objects you receive events on, are the same type
   * <p>
   * Uses an unchecked cast, so it will blow up *loudly* if attempts to cast illegally are made
   *
   * @param eventHandler
   */
  protected BufferedListener(Consumer<List<T>> eventHandler) {
    this(eventHandler, DEFAULT_PERIOD, DEFAULT_MAX_SIZE, i -> (T) i);
  }

  @Override
  public void apply(V cacheable) {
    items.add(transformer.apply(cacheable));
    if (items.size() > maxQueueSize)
      executorService.submit(batchProcessor);
  }

  @Override
  public void close() {
    // Scheduling this first, so we can attempt to flush, then we initiate shutdown
    executorService.submit(batchProcessor);
    super.close();
  }
}

package cern.c2mon.cache.api.listener;

import cern.c2mon.shared.common.Cacheable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

final class BufferedCacheListenerImpl<CACHEABLE extends Cacheable> implements CacheListener<CACHEABLE>, Runnable {

  private static final int DEFAULT_MAX_SIZE = 100_000;
  //  TODO (Alex) This could be a set, to eliminate duplicates
  private final List<CACHEABLE> items = Collections.synchronizedList(new ArrayList<>());
  private final ExecutorService centralizedExecutorService;
  private BufferedCacheListener<CACHEABLE> eventHandler;

  BufferedCacheListenerImpl(ExecutorService executorService, BufferedCacheListener<CACHEABLE> eventHandler) {
    this.centralizedExecutorService = executorService;
    this.eventHandler = eventHandler;
  }

  /**
   * Accepts an object and adds it to the internal buffer list, to process collectively later
   *
   * @param cacheable the item, after whatever event happened that has triggered this action
   */
  @Override
  public final void apply(CACHEABLE cacheable) {
    items.add(cacheable);
    if (items.size() > DEFAULT_MAX_SIZE)
      centralizedExecutorService.submit(this);
  }

  /**
   * The actual operation that we'll run every X seconds
   * <p>
   * Creates a copy list to avoid the original being modified during iteration
   */
  @Override
  public final void run() {
    if (items.isEmpty())
      return;
    List<CACHEABLE> copyList = new ArrayList<>(items);
    // We're not doing a clear here, as other items may have joined the list
    // This relies on our Cacheables having a proper equals implementation!
    items.removeAll(copyList);
    eventHandler.apply(copyList);
  }
}

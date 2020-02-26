package cern.c2mon.cache.api.listener;

import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.Cacheable;

import java.io.Closeable;

public interface CacheListenerManager<CACHEABLE extends Cacheable> extends Closeable {

  void notifyListenersOf(CacheEvent event, CACHEABLE source);

  void registerListener(CacheListener<CACHEABLE> listener, CacheEvent baseEvent, CacheEvent... events);

  void registerBufferedListener(BufferedCacheListenerImpl<CACHEABLE> listener, CacheEvent... events);

  /**
   * An alternative cache listener, that collects all incoming updates to a list, then periodically runs an event handler
   * on that list
   *
   * TODO Should use the CacheProperties and get the
   *
   * Use buffered listeners to increase performance for very busy caches with loads of updates,
   * or for operations optimized for {@code Collection}s
   */
  void registerBufferedListener(BatchConsumer<CACHEABLE> listener, CacheEvent... events);

  @Override
  void close();
}

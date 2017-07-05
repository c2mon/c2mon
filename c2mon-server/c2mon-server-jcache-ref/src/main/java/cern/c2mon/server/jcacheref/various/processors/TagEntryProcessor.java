package cern.c2mon.server.jcacheref.various.processors;

import javax.cache.Cache;
import javax.cache.Caching;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.MutableEntry;

/**
 * A javax.cache.processor.EntryProcessor is an invocable function, much like a
 * java.util.concurrent.Callable, that applications may use to efficiently perform compound
 * Cache operations, including access, update and removal atomically on a Cache Entry, without
 * requiring explicit locking or transactions.
 * When invoked using either the Cache#invoke or Cache#invokeAll methods, an
 * EntryProcessor is provided with a MutableEntry, that of which allows an application to
 * exclusively have access to the entry.
 *
 * @author Szymon Halastra
 */
public class TagEntryProcessor<K, V, T> implements EntryProcessor<K, V, T> {

  @Override
  public T process(MutableEntry<K, V> entry, Object... arguments) throws EntryProcessorException {
    return null;
  }


  public void example() {
    Cache cache = Caching.getCachingProvider().getCacheManager().createCache("cache", null);

    cache.invoke(1L, (entry, arguments) -> {
      if(entry.exists()) {
        entry.setValue(entry.getValue());
      }

      return null;
    });
  }

  /**
   * “EntryProcessors execute on the partition thread in a member. Multiple operations on the same partition are queued.
   While executing partition migrations are not allowed. Any migrations are queued on the partition thread.“
   */
}

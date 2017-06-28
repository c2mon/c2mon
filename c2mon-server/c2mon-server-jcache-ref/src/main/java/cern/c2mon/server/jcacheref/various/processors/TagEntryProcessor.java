package cern.c2mon.server.jcacheref.various.processors;

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


}

package cern.c2mon.server.jcacheref.prototype.common;

import java.io.Serializable;

import javax.cache.processor.EntryProcessor;

/**
 * @author Szymon Halastra
 */

public interface SerializableEntryProcessor<K, V, T> extends EntryProcessor<K, V, T>, Serializable {
}

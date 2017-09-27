package cern.c2mon.cache.api.lock;

import java.util.concurrent.locks.Lock;

/**
 * @author Szymon Halastra
 */
public interface C2monLock<K> {

  void lockOnKey(K key);

  void unlockOnKey(K key);
}

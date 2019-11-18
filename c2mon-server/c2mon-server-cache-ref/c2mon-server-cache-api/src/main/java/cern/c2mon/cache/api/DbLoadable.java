package cern.c2mon.cache.api;

import cern.c2mon.shared.common.Cacheable;

/**
 * @author Szymon Halastra
 */
public interface DbLoadable<T extends Cacheable> {

  void doPostDbLoading(T object);
}

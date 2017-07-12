package cern.c2mon.server.jcacheref.prototype.common;

import java.util.Properties;

import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.daq.config.Change;

/**
 * Implemented by Cache object facades for caches that
 * allow reconfiguration during runtime.
 *
 * @author Szymon Halastra
 */
public interface RuntimeCacheReconfigurator<T extends Cacheable> {

  /**
   * Creates a cache object with the provided id from
   * the provided Properties object. Should also do some
   * validation of the cache object before it is inserted
   * into the system.
   * <p>
   * <p>Note this method does NOT insert the cache object into
   * the cache or the database. This should be done in
   * the configuration module.
   * <p>
   * <p>For tag objects, the cache timestamp is set to the time the object was created;
   * the DAQ and source timestamps are set as null (for Data/ControlTags). The quality is
   * set to UNINITIALISED.
   *
   * @param id         the id of the cache object created (should not exist already)
   * @param properties the map of properties necessary for creating
   *                   the object
   *
   * @return the configured cache object
   * @throws IllegalAccessException
   */
  T createCacheObject(Long id, Properties properties) throws IllegalAccessException;

  /**
   * Throws a {@link ConfigurationException} if update not permitted, in
   * which case the reconfiguration should be aborted (this method should leave
   * the cache in a consistent state in this case).
   *
   * @param cacheable
   * @param properties
   *
   * @return a Change event containing the changes needed to send to the DAQ
   * (may contain no changes)
   * @throws IllegalAccessException
   */
  Change updateConfig(T cacheable, Properties properties) throws IllegalAccessException;
}

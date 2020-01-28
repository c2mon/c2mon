package cern.c2mon.cache.api.factory;

import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.daq.config.Change;

import java.util.Properties;

/**
 * @author Szymon Halastra
 */
public abstract class AbstractCacheObjectFactory<T extends Cacheable> {

  /**
   * Creates CacheObject wrapped with properties
   *
   * @return CacheObject
   */
  public T createCacheObject(Long id, Properties properties) {
    T cacheable = createCacheObject(id);
    configureCacheObject(cacheable, properties);
    validateConfig(cacheable);
    return cacheable;
  }

  public Change updateConfig(T cacheable, Properties properties) {
    Change changeEvent = configureCacheObject(cacheable, properties);
    validateConfig(cacheable);
    return changeEvent;
  }

  /**
   * Creates basic object with only id set
   *
   * @param id
   * @return CacheObject
   */
  public abstract T createCacheObject(Long id);

  /**
   * Given an alarm object, reset some of its fields according to the passed properties.
   *
   * @param properties the properties object containing the fields
   * @param cacheable  the object to modify (is modified by this method)
   * @return always returns null, as no CacheObject change needs propagating to the DAQ layer
   * @throws ConfigurationException if cannot configure the CacheObject from the properties
   */
  protected abstract Change configureCacheObject(T cacheable, Properties properties);

  /**
   * Perform a series of consistency checks on the CacheObject. This method
   * should be invoked if an CacheObject was created from a list of named
   * properties.
   *
   * @param cacheable the cache object to validate
   * @throws ConfigurationException if one of the consistency checks fails
   */
  protected abstract void validateConfig(T cacheable) throws ConfigurationException;

  /**
   * If the String is "null", then set the return value
   * to null, else return the original value.
   * <p>
   * <p>Should be used on fields that accept configurations
   * resetting them to null.
   *
   * @param fieldValue the field value, may be "null"
   * @return the fieldValue, or null if the fieldValue was "null"
   */
  protected String checkAndSetNull(String fieldValue) {
    return fieldValue.equals("null") ? null : fieldValue;
  }
}

package cern.c2mon.cache.api.factory;

import java.util.Properties;

import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.daq.config.Change;

/**
 * @author Szymon Halastra
 */
public abstract class AbstractCacheObjectFactory<T extends Cacheable> {

  /**
   * Creates CacheObject wrapped with properties, use this one
   * @param id
   * @param properties
   * @return CacheObject
   * @throws IllegalAccessException
   */
  public T createCacheObject(Long id, Properties properties) throws IllegalAccessException {
    T cacheable = createCacheObject(id);
    configureCacheObject(cacheable, properties);
    validateConfig(cacheable);

    return cacheable;
  }

  public Change updateConfig(T cacheable, Properties properties) throws IllegalAccessException {
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

  public abstract Change configureCacheObject(T cacheable, Properties properties)throws IllegalAccessException;

  /**
   * Checks all fields of a Command Tag satisfy requirements.
   *
   * @param commandTag
   *
   * @throws ConfigurationException
   */
  public abstract void validateConfig(T cacheable) throws ConfigurationException;

  protected Integer parseInt(String integerInString, String paramName) {
    try {
      return Integer.parseInt(integerInString);
    }
    catch (NumberFormatException e) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
              "NumberFormatException: Unable to convert parameter \" " + paramName + " \" to int: " + integerInString);
    }
  }

  protected Short parseShort(String shortInString, String paramName) {
    try {
      return Short.parseShort(shortInString);
    }
    catch (NumberFormatException e) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
              "NumberFormatException: Unable to convert parameter \"" + paramName + "\" to short: " + shortInString);
    }
  }

  protected Long parseLong(String longInString, String paramName) {
    try {
      return Long.parseLong(longInString);
    }
    catch (NumberFormatException e) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
              "NumberFormatException: Unable to convert parameter \"" + paramName + "\" to short: " + longInString);
    }
  }

  /**
   * If the String is "null", then set the return value
   * to null, else return the original value.
   *
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

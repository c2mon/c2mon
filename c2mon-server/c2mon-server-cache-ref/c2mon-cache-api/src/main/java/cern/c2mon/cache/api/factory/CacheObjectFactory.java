package cern.c2mon.cache.api.factory;

import java.util.Properties;

import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.daq.config.Change;

/**
 * @author Szymon Halastra
 */
public abstract class CacheObjectFactory<T extends Cacheable> {

  public Cacheable createCacheObject(T cacheable, Properties properties) throws IllegalAccessException {
    configureCacheObject(cacheable, properties);
    validateConfig(cacheable);

    return cacheable;
  }

  public Change updateConfig(T cacheable, Properties properties) throws IllegalAccessException {
    Change changeEvent = configureCacheObject(cacheable, properties);
    validateConfig(cacheable);
    return changeEvent;
  }

  public abstract Change configureCacheObject(T cacheable, Properties properties);

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
}

package cern.c2mon.cache.api.factory;

import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.daq.config.Change;

import java.util.Properties;
import java.util.function.Function;

/**
 * @author Szymon Halastra
 */
public abstract class AbstractCacheObjectFactory<T extends Cacheable> {

  /**
   * Creates CacheObject wrapped with properties, use this one
   *
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
   *
   * @return CacheObject
   */
  public abstract T createCacheObject(Long id);

  /**
   * Given an alarm object, reset some of its fields according to the passed properties.
   *
   * @param properties the properties object containing the fields
   * @param cacheable  the object to modify (is modified by this method)
   *
   * @return always returns null, as no CacheObject change needs propagating to the DAQ layer
   * @throws ConfigurationException if cannot configure the CacheObject from the properties
   */
  public abstract Change configureCacheObject(T cacheable, Properties properties) throws IllegalAccessException;

  /**
   * Perform a series of consistency checks on the CacheObject. This method
   * should be invoked if an CacheObject was created from a list of named
   * properties.
   *
   * @param cacheable the cache object to validate
   *
   * @throws ConfigurationException if one of the consistency checks fails
   */
  public abstract void validateConfig(T cacheable) throws ConfigurationException;

  protected Integer parseInt(String integerInString, String paramName) {
    return safeParse(Integer::parseInt, integerInString, paramName);
  }

  protected Short parseShort(String shortInString, String paramName) {
    return safeParse(Short::parseShort, shortInString,paramName);
  }

  protected Long parseLong(String longInString, String paramName) {
    return safeParse(Long::parseLong, longInString, paramName);
  }

  protected <V> V safeParse(Function<String,V> parser, String element, String paramName){
    try {
      return parser.apply(element);
    }
    catch (NumberFormatException e) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
        "NumberFormatException: Unable to convert parameter \"" + paramName + "\" using " + parser.getClass() + " parser : " + element);
    }
  }

  /**
   * If the String is "null", then set the return value
   * to null, else return the original value.
   * <p>
   * <p>Should be used on fields that accept configurations
   * resetting them to null.
   *
   * @param fieldValue the field value, may be "null"
   *
   * @return the fieldValue, or null if the fieldValue was "null"
   */
  protected String checkAndSetNull(String fieldValue) {
    return fieldValue.equals("null") ? null : fieldValue;
  }
}

package cern.c2mon.cache.api.factory;

import cern.c2mon.cache.api.C2monCache;

/**
 * @author Szymon Halastra
 */
public abstract class AbstractFactory {

  public AbstractFactory getFactory() {
    AbstractFactory abstractFactory = null;

    abstractFactory = getCachingFactory();

    return abstractFactory;
  }

  public abstract C2monCache createCache(String name, Class<?> keyType, Class<?> valueType);

  public abstract AbstractFactory getCachingFactory();
}

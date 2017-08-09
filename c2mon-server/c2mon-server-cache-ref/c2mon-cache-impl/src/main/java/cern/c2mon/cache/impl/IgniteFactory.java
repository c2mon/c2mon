package cern.c2mon.cache.impl;

import cern.c2mon.cache.api.AbstractFactory;
import cern.c2mon.cache.api.C2monCache;

/**
 * @author Szymon Halastra
 */
public class IgniteFactory extends AbstractFactory {

  @Override
  public C2monCache createCache(String name, Class<?> keyType, Class<?> valueType) {
    return null;
  }

  @Override
  public AbstractFactory getCachingFactory() {
    return this;
  }
}

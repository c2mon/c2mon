package cern.c2mon.cache.impl;

import java.util.List;

import cern.c2mon.cache.api.C2monCache;

/**
 * @author Szymon Halastra
 */
public class IgniteCache extends C2monCache {

  @Override
  protected Object get(Object key) {
    return null;
  }

  @Override
  protected boolean containsKey(Object key) {
    return false;
  }

  @Override
  protected void put(Object key, Object value) {

  }

  @Override
  protected boolean remove(Object key) {
    return false;
  }

  @Override
  protected String getName() {
    return null;
  }

  @Override
  protected List getKeys() {
    return null;
  }
}

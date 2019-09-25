package cern.c2mon.cache;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.shared.common.Cacheable;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @param <V> the type of cache object being tested, e.g {@code Alarm}
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou
 */
public abstract class CacheCRUDTest<V extends Cacheable> extends AbstractCacheLoaderTest {

  private C2monCache<V> cache;

  @Before
  public void init() {
    this.cache = getCache();
    cache.clear();
    assertEquals(0, cache.getAll(cache.getKeys()).size());
    cache.init();
  }

  protected abstract C2monCache<V> getCache();

  protected abstract Long getExistingKey();

  protected abstract V getSample();

  @Test
  public void containsKey() {
    assertTrue("Have you provided a correct key? Check the cache-data-insert.sql file",
      cache.containsKey(getExistingKey()));
  }

  @Test
  public void get() {
    assertNotNull(cache.get(getExistingKey()));
  }

  /**
   * If null is used as a key, an exception should be thrown.
   */
  @Test(expected = IllegalArgumentException.class)
  public void getNull() {
    cache.get(null);
  }

  @Test
  public void put() {
    cache.put(1337L, getSample());
  }

  @Test
  public void updateExisting() {
    cache.put(getExistingKey(), getSample());
    assertEquals(getSample(), cache.get(getExistingKey()));
  }

  /**
   * If null is used as a value, an exception should be thrown.
   */
  @Test(expected = IllegalArgumentException.class)
  public void putNull() {
    cache.put(1L, null);
  }

  /**
   * If null is used as a value, an exception should be thrown.
   */
  @Test(expected = IllegalArgumentException.class)
  public void updateNullOnExisting() {
    cache.put(getExistingKey(), null);
  }

  @Test
  public void delete() {
    assertTrue(cache.remove(getExistingKey()));
    assertFalse(cache.remove(getExistingKey()));
  }
}

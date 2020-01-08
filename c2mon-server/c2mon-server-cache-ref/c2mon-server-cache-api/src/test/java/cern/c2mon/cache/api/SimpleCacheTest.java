package cern.c2mon.cache.api;

import cern.c2mon.cache.api.impl.SimpleC2monCache;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.*;

/**
 * Unit tests for basic implementation of C2monCache interface
 *
 * @author Alexandros Papageorgiou, Szymon Halastra
 */
public class SimpleCacheTest {

  private static final long MAP_SIZE = 100;
  private static final int GENERATOR_SIZE = 10;
  private static final Map<Long, DataTag> map = new HashMap<>();
  private static final Set<Long> keys = new TreeSet<>();

  @BeforeClass
  public static void init() {
    for (long i = 0; i < MAP_SIZE; i++) {
      map.put(i, new DataTagCacheObject(i));
    }
    while (keys.size() < GENERATOR_SIZE) {
      keys.add(getRandom());
    }
  }

  @Test
  public void createCache() {
    C2monCache<DataTag> c2monCache = new SimpleC2monCache<>("simple-c2monCache");
    assertNotNull("C2monCache should be not null", c2monCache);
  }

  @Test
  public void populateCache() {
    C2monCache<DataTag> c2monCache = new SimpleC2monCache<>("simple-c2monCache");

    c2monCache.putAll(map);

    assertEquals("C2monCache should contain " + MAP_SIZE + " elements", MAP_SIZE, c2monCache.getKeys().size());
  }

  @Test
  public void getAllByKeys() {
    C2monCache<DataTag> c2monCache = new SimpleC2monCache<>("simple-c2monCache");
    c2monCache.putAll(map);

    Map<Long, DataTag> selectedMap = c2monCache.getAll(keys);
    assertEquals("Selected map should have " + GENERATOR_SIZE + " entries", GENERATOR_SIZE, selectedMap.size());

    for (Long key : keys) {
      assertNotNull(key + " key should return valid object", c2monCache.get(key));
    }
  }

  @Test
  public void getAllEntriesByProvidedKeySet() {
    C2monCache<DataTag> c2monCache = new SimpleC2monCache<>("simple-c2monCache");
    c2monCache.putAll(map);

    long removedId = getRandom();
    boolean isRemoved = c2monCache.remove(removedId);
    assertTrue("Status after entry removal should be true", isRemoved);
    assertEquals("C2monCache should have " + (MAP_SIZE - 1) + "entries after removal in transaction", MAP_SIZE - 1, c2monCache.getKeys().size());

    c2monCache.put(removedId, new DataTagCacheObject(removedId));

    assertEquals("C2monCache should have " + MAP_SIZE + " entries again", MAP_SIZE, c2monCache.getKeys().size());
    assertTrue("Removed entry should be accessible again", c2monCache.containsKey(removedId));
    assertNotNull("Removed entry should be available again", c2monCache.get(removedId));
  }

  private static Long getRandom() {
    return ThreadLocalRandom.current().nextLong(0L, MAP_SIZE + 1);
  }
}

package cern.c2mon.cache.api;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;

import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.cache.api.impl.SimpleC2monCache;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;

import static org.junit.Assert.*;

/**
 * Unit tests for basic implementation of C2monCache interface
 * <p>
 * ONLY FOR TESTING PURPOSES
 *
 * @author Szymon Halastra
 */
public class SimpleCacheTest {

  private static final long MAP_SIZE = 100;
  private static final int GENERATOR_SIZE = 10;

  @Test
  public void getAllEntriesByProvidedKeySet() {
    AbstractCache<Long, DataTag> c2monCache = new SimpleC2monCache<>("simple-c2monCache");
    assertNotNull("C2monCache should be not null", c2monCache);

    Map<Long, DataTag> map = new HashMap<>();

    for (long i = 0; i < MAP_SIZE; i++) {
      map.put(i, new DataTagCacheObject(i));
    }
    c2monCache.putAll(map);
    assertEquals("C2monCache should contain " + MAP_SIZE + " elements", MAP_SIZE, c2monCache.getKeys().size());

    Set<Long> keys = new TreeSet<>();

    while (keys.size() < GENERATOR_SIZE) {
      keys.add(getRandom());
    }
    assertEquals("Selected key set should have " + GENERATOR_SIZE + " keys", GENERATOR_SIZE, keys.size());

    Map<Long, DataTag> selectedMap = c2monCache.getAll(keys);
    assertEquals("Selected map should have " + GENERATOR_SIZE + " entries", GENERATOR_SIZE, selectedMap.size());

    for (Long key : keys) {
      assertNotNull(key + " key should return valid object", c2monCache.get(key));
    }

    long removedId = getRandom();
    Optional<Boolean> isRemoved = c2monCache.executeTransaction(() -> c2monCache.remove(removedId));
    Boolean removed = isRemoved.orElseThrow(CacheElementNotFoundException::new);
    assertTrue("Status after entry removal should be true", removed);
    assertEquals("C2monCache should have " + (MAP_SIZE - 1) + "entries after removal in transaction", MAP_SIZE - 1, c2monCache.getKeys().size());

    c2monCache.executeTransaction(() -> {
      c2monCache.put(removedId, new DataTagCacheObject(removedId));

      return null;
    });

    assertEquals("C2monCache should have " + MAP_SIZE + " entries again", MAP_SIZE, c2monCache.getKeys().size());
    assertTrue("Removed entry should be accessible again", c2monCache.containsKey(removedId));
    assertNotNull("Removed entry should be available again", c2monCache.get(removedId));
  }

  private Long getRandom() {
    return ThreadLocalRandom.current().nextLong(0L, MAP_SIZE + 1);
  }
}

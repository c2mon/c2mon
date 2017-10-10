package cern.c2mon.cache.api;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;

import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.cache.api.impl.SimpleCache;
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
    Cache<Long, DataTag> cache = new SimpleCache<>("simple-cache");
    assertNotNull("Cache should be not null", cache);

    Map<Long, DataTag> map = new HashMap<>();

    for (long i = 0; i < MAP_SIZE; i++) {
      map.put(i, new DataTagCacheObject(i));
    }
    cache.putAll(map);
    assertEquals("Cache should contain " + MAP_SIZE + " elements", MAP_SIZE, cache.getKeys().size());

    Set<Long> keys = new TreeSet<>();

    while (keys.size() < GENERATOR_SIZE) {
      keys.add(getRandom());
    }
    assertEquals("Selected key set should have " + GENERATOR_SIZE + " keys", GENERATOR_SIZE, keys.size());

    Map<Long, DataTag> selectedMap = cache.getAll(keys);
    assertEquals("Selected map should have " + GENERATOR_SIZE + " entries", GENERATOR_SIZE, selectedMap.size());

    for (Long key : keys) {
      assertNotNull(key + " key should return valid object", cache.get(key));
    }

    long removedId = getRandom();
    Optional<Boolean> isRemoved = cache.executeTransaction(() -> cache.remove(removedId));
    Boolean removed = isRemoved.orElseThrow(CacheElementNotFoundException::new);
    assertTrue("Status after entry removal should be true", removed);
    assertEquals("Cache should have " + (MAP_SIZE - 1) + "entries after removal in transaction", MAP_SIZE - 1, cache.getKeys().size());

    cache.executeTransaction(() -> {
      cache.put(removedId, new DataTagCacheObject(removedId));

      return null;
    });

    assertEquals("Cache should have " + MAP_SIZE + " entries again", MAP_SIZE, cache.getKeys().size());
    assertTrue("Removed entry should be accessible again", cache.containsKey(removedId));
    assertNotNull("Removed entry should be available again", cache.get(removedId));
  }

  private Long getRandom() {
    return ThreadLocalRandom.current().nextLong(0L, MAP_SIZE + 1);
  }
}

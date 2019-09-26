package cern.c2mon.cache;

import cern.c2mon.server.cache.dbaccess.LoaderMapper;
import cern.c2mon.shared.common.Cacheable;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 *
 * @author Alexandros Papageorgiou
 */
public abstract class AbstractCacheLoaderTest<V extends Cacheable> extends AbstractCacheCRUDTest<V> {

  protected LoaderMapper<V> mapper;

  @Before
  public void initMapper() {
    if (mapper == null)
      mapper = getMapper();
  }

  protected abstract LoaderMapper<V> getMapper();

  /**
   * Tests the get method retrieves an existing Alarm correctly across cache/mapper
   */
  @Test
  public void testGet() {
    V cacheObject = cache.get(existingKey);
    V objectInDb = mapper.getItem(existingKey);
    assertEquals(cacheObject, objectInDb);
  }

  @Test
  public void cacheIsPreloadedCorrectly() {
    assertNotNull("Cache should not be null", cache);
    assertNotNull("Mapper should not be null", mapper);

    List<V> dbItems = mapper.getAll();

    assertTrue("List of DB mapped objects should not be empty", dbItems.size() > 0);

    assertEquals("Size of cache and DB mapping should be equal", dbItems.size(), cache.getKeys().size());
    //compare all the objects from the cache and buffer
    try {
      for (V dbItem : dbItems) {
        assertTrue("Cache should include a key for object with id " + dbItem.getId(), cache.containsKey(dbItem.getId()));
        assertEquals("Object should be equal in DB and cache", dbItem, cache.get(dbItem.getId()));
      }
      customCompare(dbItems, cache.getAll(cache.getKeys()));
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      fail();
    }
  }

  protected abstract void customCompare(List<V> mapperList, Map<Long, V> cacheList) throws ClassNotFoundException;
}

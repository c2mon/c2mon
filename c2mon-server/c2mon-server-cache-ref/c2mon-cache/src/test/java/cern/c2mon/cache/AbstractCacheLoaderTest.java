package cern.c2mon.cache;

import cern.c2mon.server.cache.dbaccess.LoaderMapper;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.command.CommandTagCacheObject;
import cern.c2mon.server.common.control.ControlTagCacheObject;
import cern.c2mon.server.common.tag.AbstractTagCacheObject;
import cern.c2mon.server.test.CacheObjectComparison;
import cern.c2mon.shared.common.Cacheable;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;


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

    List<V> itemList = mapper.getAll();

    assertTrue("List of DB mapped objects should not be empty", itemList.size() > 0);

    assertEquals("Size of cache and DB mapping should be equal", itemList.size(), cache.getKeys().size());
    //compare all the objects from the cache and buffer
    try {
      compareLists(itemList, cache.getAll(cache.getKeys()));
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      fail();
    }
  }

  protected abstract void compareLists(List<V> mapperList, Map<Long, V> cacheList) throws ClassNotFoundException;
}

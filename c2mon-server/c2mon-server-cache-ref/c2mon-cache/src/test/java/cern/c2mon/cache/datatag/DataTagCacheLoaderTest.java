package cern.c2mon.cache.datatag;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.cache.AbstractCacheLoaderTest;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.DataTagMapper;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.datatag.DataTag;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Szymon Halastra
 */
public class DataTagCacheLoaderTest extends AbstractCacheLoaderTest {

  @Autowired
  private C2monCache<Long, DataTag> dataTagCacheRef;

  @Autowired
  private DataTagMapper dataTagMapper;

  @Before
  public void init() {
    dataTagCacheRef.init();
  }

  @Test
  public void preloadCache() {
    assertNotNull("DataTag Cache should not be null", dataTagCacheRef);

    List<DataTag> dataTagList = dataTagMapper.getAll();

    Set<Long> keySet = dataTagList.stream().map(DataTag::getId).collect(Collectors.toSet());
    assertTrue("List of data tags should not be empty", dataTagList.size() > 0);

    assertEquals("Size of cache and DB mapping should be equal", dataTagList.size(), dataTagCacheRef.getKeys().size());
    //compare all the objects from the cache and buffer
    Iterator<DataTag> it = dataTagList.iterator();
    while (it.hasNext()) {
      DataTag currentTag = it.next();
      //equality of DataTagCacheObjects => currently only compares names
      assertEquals("Cached DataTag should have the same name as in DB",
              currentTag.getName(), (dataTagCacheRef.get(currentTag.getId())).getName());
    }
  }
}
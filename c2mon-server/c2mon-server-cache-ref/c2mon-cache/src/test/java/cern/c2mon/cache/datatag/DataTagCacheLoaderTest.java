package cern.c2mon.cache.datatag;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.cache.AbstractCacheLoaderTest;
import cern.c2mon.cache.api.C2monCacheBase;
import cern.c2mon.server.cache.dbaccess.DataTagMapper;
import cern.c2mon.server.common.datatag.DataTag;

import static org.junit.Assert.*;

/**
 * @author Szymon Halastra
 */
public class DataTagCacheLoaderTest extends AbstractCacheLoaderTest {

  @Autowired
  private C2monCacheBase<DataTag> dataTagCacheRef;

  @Autowired
  private DataTagMapper dataTagMapper;

  @Before
  public void init() {
    dataTagCacheRef.init();
  }

  @Test
  @Ignore
  public void preloadCache() {
    assertNotNull("DataTag Cache should not be null", dataTagCacheRef);

    List<DataTag> dataTagList = dataTagMapper.getAll();

    assertTrue("List of data tags should not be empty", dataTagList.size() > 0);

    assertEquals("Size of cache and DB mapping should be equal", dataTagList.size(), dataTagCacheRef.getKeys().size());
    //compare all the objects from the cache and buffer
    for (DataTag currentTag : dataTagList) {
      //equality of DataTagCacheObjects => currently only compares names
      assertEquals("Cached DataTag should have the same name as in DB",
              currentTag.getName(), (dataTagCacheRef.get(currentTag.getId())).getName());
    }
  }
}

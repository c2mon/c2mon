package cern.c2mon.server.cache.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.cache.dbaccess.ControlTagMapper;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;

/**
 * Integration test of the ControlTagCache implementation
 * with the cache loading and cache DB access modules.
 * 
 * @author mbrightw
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/cache/config/server-cache-control-test.xml"})
@DirtiesContext  
public class ControlTagCacheTest {
    
  @Autowired
  @Qualifier("controlTagCache")
  private ControlTagCacheImpl controlTagCache;
  
  @Autowired
  private ControlTagMapper controlTagMapper;
  
  
  /**
   * Tests the cache was loaded from the DB by checking the same number of objects
   * are in each and that the names are identical.
   * Only compares the names of the cache and DB, so could be improved
   * (but to compare values need test server to be stopped!)
   */
  
  @Test
  @DirtiesContext
  public void testCacheLoading() {
    assertNotNull(controlTagCache);
    
    List<ControlTag> dataTagList = controlTagMapper.getAll();
    
    //test the cache was loaded correctly
    assertEquals(dataTagList.size(), controlTagCache.getCache().getKeys().size());
    //compare all the objects from the cache and buffer
    Iterator<ControlTag> it = dataTagList.iterator();
    while (it.hasNext()) {
      DataTag currentTag = (DataTag) it.next();
      //equality of DataTagCacheObjects => currently only compares names (as these don't change if the test server is running!)
      assertEquals(currentTag.getName(), ((DataTagCacheObject) controlTagCache.getCopy(currentTag.getId())).getName());
    }
  }
}

package cern.c2mon.server.cache.commfault;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.cache.dbaccess.CommFaultTagMapper;
import cern.c2mon.server.cache.dbaccess.test.TestDataHelper;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.commfault.CommFaultTagCacheObject;

/**
 * Integration test of the CommFaultTagCache with the
 * cache loading and cache DB access modules.
 * 
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration({"classpath:cern/c2mon/server/cache/config/server-cache-commfault-test.xml"})
public class CommFaultTagCacheTest {

  @Autowired
  private TestDataHelper testDataHelper;
  
  @Autowired
  private CommFaultTagCacheImpl commFaultTagCache;
  
  @Autowired
  private CommFaultTagMapper commFaultTagMapper;
  
  @Before
  public void clean() {
    testDataHelper.removeTestData();
  }
  
  @Test
  public void testCacheLoading() {
    assertNotNull(commFaultTagCache);
    
    List<CommFaultTag> commFaultList = commFaultTagMapper.getAll(); //IN FACT: GIVES TIME FOR CACHE TO FINISH LOADING ASYNCH BEFORE COMPARISON BELOW...
    try {
      Thread.sleep(10000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    //test the cache is the same size as in DB
    assertEquals(commFaultList.size(), commFaultTagCache.getCache().getKeys().size());
    //compare all the objects from the cache and buffer
    Iterator<CommFaultTag> it = commFaultList.iterator();
    while (it.hasNext()) {
      CommFaultTagCacheObject currentTag = (CommFaultTagCacheObject) it.next();
      //equality of DataTagCacheObjects => currently only compares names
      assertEquals(currentTag.getEquipmentId(), (((CommFaultTagCacheObject) commFaultTagCache.getCopy(currentTag.getId())).getEquipmentId()));
    }
  }
}

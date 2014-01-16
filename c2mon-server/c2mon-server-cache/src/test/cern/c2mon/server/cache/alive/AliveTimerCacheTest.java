package cern.c2mon.server.cache.alive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.cache.dbaccess.AliveTimerMapper;
import cern.c2mon.server.common.alive.AliveTimer;
import cern.c2mon.server.common.alive.AliveTimerCacheObject;

/**
 * Integration test checking that AliveTimer cache loads correctly from the database
 * (integration of cache, cache loading and cache DB access modules).
 * @author mbrightw
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/cache/config/server-cache-alive-test.xml"})
@DirtiesContext
public class AliveTimerCacheTest {

  @Autowired
  AliveTimerMapper aliveTimerMapper;
  
  @Autowired
  AliveTimerCacheImpl aliveTimerCache;
  
  @Test
  @DirtiesContext
  public void testCacheLoading() throws InterruptedException {
    assertNotNull(aliveTimerCache);
    
    List<AliveTimer> aliveList = aliveTimerMapper.getAll(); //IN FACT: GIVES TIME FOR CACHE TO FINISH LOADING ASYNCH BEFORE COMPARISON BELOW...
    
    //test the cache is the same size as in DB
    assertEquals(aliveList.size(), aliveTimerCache.getCache().getKeys().size());
    //compare all the objects from the cache and buffer
    Iterator<AliveTimer> it = aliveList.iterator();
    while (it.hasNext()) {
      AliveTimerCacheObject currentTimer = (AliveTimerCacheObject) it.next();
      //only compares one field so far
      assertEquals(currentTimer.getRelatedName(), (((AliveTimerCacheObject) aliveTimerCache.getCopy(currentTimer.getId())).getRelatedName()));
    }
  }
  
}

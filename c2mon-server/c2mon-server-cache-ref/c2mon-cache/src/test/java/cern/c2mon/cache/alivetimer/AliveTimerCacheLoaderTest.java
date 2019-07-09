package cern.c2mon.cache.alivetimer;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.cache.AbstractCacheLoaderTest;
import cern.c2mon.cache.api.C2monCacheBase;
import cern.c2mon.server.cache.dbaccess.AliveTimerMapper;
import cern.c2mon.server.common.alive.AliveTimer;
import cern.c2mon.server.common.alive.AliveTimerCacheObject;

import static org.junit.Assert.assertNotNull;
import static org.springframework.test.util.AssertionErrors.assertEquals;

/**
 * @author Szymon Halastra
 */
public class AliveTimerCacheLoaderTest extends AbstractCacheLoaderTest {

  @Autowired
  private C2monCacheBase<AliveTimer> aliveTimerCacheRef;

  @Autowired
  private AliveTimerMapper aliveTimerMapper;

  @Before
  public void prepare() {
    aliveTimerCacheRef.init();
  }

  @Test
  @Ignore
  public void preloadCache() {
    assertNotNull("AliveTimer cache should be not null", aliveTimerCacheRef);

    List<AliveTimer> aliveList = aliveTimerMapper.getAll(); //IN FACT: GIVES TIME FOR CACHE TO FINISH LOADING ASYNCH BEFORE COMPARISON BELOW...

    //test the cache is the same size as in DB
    assertEquals("Size of cache and DB mapping should be equal", aliveList.size(), aliveTimerCacheRef.getKeys().size());
    //compare all the objects from the cache and buffer
    for (AliveTimer anAliveList : aliveList) {
      AliveTimerCacheObject currentTimer = (AliveTimerCacheObject) anAliveList;
      //only compares one field so far
      assertEquals("Cached AliveTimer should have the same name as AliveTimer in DB", currentTimer.getRelatedName(), ((aliveTimerCacheRef.get(currentTimer.getId())).getRelatedName()));
    }
  }
}

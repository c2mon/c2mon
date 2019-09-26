package cern.c2mon.cache.alivetimer;

import cern.c2mon.cache.AbstractCacheLoaderTest;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.AliveTimerMapper;
import cern.c2mon.server.common.alive.AliveTimer;
import cern.c2mon.server.common.alive.AliveTimerCacheObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.springframework.test.util.AssertionErrors.assertEquals;

/**
 * @author Szymon Halastra
 */
public class AliveTimerCacheLoaderTest extends AbstractCacheLoaderTest<AliveTimer> {

  @Autowired
  private C2monCache<AliveTimer> aliveTimerCacheRef;

  @Autowired
  private AliveTimerMapper aliveTimerMapper;

  @Override
  protected AliveTimerMapper getMapper() {
    return aliveTimerMapper;
  }

  @Override
  protected void compareLists(List<AliveTimer> mapperList, Map<Long, AliveTimer> cacheList) {
    for (AliveTimer anAliveList : mapperList) {
      AliveTimerCacheObject currentTimer = (AliveTimerCacheObject) anAliveList;
      //only compares one field so far
      assertEquals("Cached AliveTimer should have the same name as AliveTimer in DB", currentTimer.getRelatedName(), ((aliveTimerCacheRef.get(currentTimer.getId())).getRelatedName()));
    }
  }

  @Override
  protected Long getExistingKey() {
    return 1224L;
  }

  @Override
  protected C2monCache<AliveTimer> getCache() {
    return aliveTimerCacheRef;
  }

  @Override
  protected AliveTimer getSample() {
    return new AliveTimerCacheObject(0L);
  }
}

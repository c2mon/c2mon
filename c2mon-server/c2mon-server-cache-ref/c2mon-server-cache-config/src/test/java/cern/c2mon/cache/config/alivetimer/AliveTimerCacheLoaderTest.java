package cern.c2mon.cache.config.alivetimer;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.config.AbstractCacheLoaderTest;
import cern.c2mon.server.cache.dbaccess.AliveTimerMapper;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.alive.AliveTagCacheObject;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static org.springframework.test.util.AssertionErrors.assertEquals;

/**
 * @author Alexandros Papageorgiou
 */
public class AliveTimerCacheLoaderTest extends AbstractCacheLoaderTest<AliveTag> {

  @Inject
  private C2monCache<AliveTag> aliveTimerCacheRef;

  @Inject
  private AliveTimerMapper aliveTimerMapper;

  @Override
  protected AliveTimerMapper getMapper() {
    return aliveTimerMapper;
  }

  @Override
  protected void customCompare(List<AliveTag> mapperList, Map<Long, AliveTag> cacheList) {
    for (AliveTag anAliveList : mapperList) {
      AliveTagCacheObject currentTimer = (AliveTagCacheObject) anAliveList;
      //only compares one field so far
      assertEquals("Cached AliveTimer should have the same name as AliveTimer in DB", currentTimer.getRelatedName(), ((aliveTimerCacheRef.get(currentTimer.getId())).getRelatedName()));
    }
  }

  @Override
  protected Long getExistingKey() {
    return 1224L;
  }

  @Override
  protected C2monCache<AliveTag> getCache() {
    return aliveTimerCacheRef;
  }

  @Override
  protected AliveTag getSample() {
    return new AliveTagCacheObject(0L);
  }
}

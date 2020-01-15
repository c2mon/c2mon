package cern.c2mon.cache.config.alivetimer;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.config.AbstractCacheLoaderTest;
import cern.c2mon.server.cache.dbaccess.AliveTagMapper;
import cern.c2mon.server.common.alive.AliveTag;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static org.springframework.test.util.AssertionErrors.assertEquals;

/**
 * @author Alexandros Papageorgiou
 */
public class AliveTagCacheLoaderTest extends AbstractCacheLoaderTest<AliveTag> {

  @Inject
  private C2monCache<AliveTag> aliveTimerCacheRef;

  @Inject
  private AliveTagMapper aliveTimerMapper;

  @Override
  protected AliveTagMapper getMapper() {
    return aliveTimerMapper;
  }

  @Override
  protected void customCompare(List<AliveTag> mapperList, Map<Long, AliveTag> cacheList) {
    for (AliveTag anAliveList : mapperList) {
      AliveTag currentTimer = (AliveTag) anAliveList;
      //only compares one field so far
      assertEquals("Cached AliveTimer should have the same name as AliveTimer in DB", currentTimer.getSupervisedName(), ((aliveTimerCacheRef.get(currentTimer.getId())).getSupervisedName()));
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
    return new AliveTag(0L, 100L, "Abc", "PROC", null, null, 10);
  }
}

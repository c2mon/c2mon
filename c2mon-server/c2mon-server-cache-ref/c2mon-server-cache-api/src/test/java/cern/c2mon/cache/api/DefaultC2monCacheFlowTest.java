package cern.c2mon.cache.api;

import cern.c2mon.cache.api.flow.DefaultCacheFlow;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.shared.common.CacheEvent;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class DefaultC2monCacheFlowTest {
  private DefaultCacheFlow<Alarm> cacheFlow = new DefaultCacheFlow<>();

  @Test
  public void preInsertValidateAcceptsNull() {
    // Should not throw - null will be passed at least once!
    cacheFlow.preInsertValidate(null, new AlarmCacheObject(0L));
    // Should almost always return true, otherwise the first item can never be inserted to the cache!
    // There are cases where this could reasonably be false - override this test in the corresponding
    // child class test to check for the custom logic!
    assertTrue(cacheFlow.preInsertValidate(null, new AlarmCacheObject(0L)));
  }

  @Test
  public void postInsertEventsAcceptsNull() {
    // Should not throw - null will be passed at least once!
    cacheFlow.postInsertEvents(null, new AlarmCacheObject(0L));
    // Should almost always be contained, unless an implementation for some reason doesn't want to
    // emit this event. Usually however this test failing means you didn't call super.postInsertEvents.
    // If you have a reasonable case, override this test in the corresponding child class test
    assertTrue(cacheFlow.postInsertEvents(null, new AlarmCacheObject(0L)).contains(CacheEvent.INSERTED));
  }
}

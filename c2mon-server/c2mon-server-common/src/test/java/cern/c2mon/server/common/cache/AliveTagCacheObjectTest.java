package cern.c2mon.server.common.cache;

import cern.c2mon.server.common.alive.AliveTag;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AliveTagCacheObjectTest extends CacheableTest<AliveTag> {

  private static AliveTag sample = new AliveTag(0L, 100L, "Abc", "PROC", null, null, 10);

  public AliveTagCacheObjectTest() {
    super(sample);
  }

  @Override
  protected void mutateObject(AliveTag cloneObject) {
    cloneObject.setAliveInterval(420);
    cloneObject.setLastUpdate(1337);
  }

  /**
   * Set active should return whether the object changed
   *
   * This is mainly used in the AliveTimerService
   */
  @Test
  public void setActiveReturnsDifferent() {
    assertTrue(sample.setValueAndGetDifferent(true));
    assertTrue(sample.setValueAndGetDifferent(false));
    assertFalse(sample.setValueAndGetDifferent(false));
    assertTrue(sample.setValueAndGetDifferent(true));
    assertFalse(sample.setValueAndGetDifferent(true));
  }
}

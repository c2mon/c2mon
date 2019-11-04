package cern.c2mon.cache.actions.alarm;

import cern.c2mon.server.test.cache.AlarmCacheObjectFactory;
import org.junit.BeforeClass;
import org.junit.Test;

public class AlarmCacheObjectControllerTest {

  private static AlarmCacheObjectController alarmCacheObjectController;
  private static AlarmCacheObjectFactory alarmCacheObjectFactory;

  @BeforeClass
  public static void setup() {
    alarmCacheObjectController = new AlarmCacheObjectController();
    alarmCacheObjectFactory = new AlarmCacheObjectFactory();
  }

  @Test
  public void evaluateAdditionalInfo() {

  }

  @Test
  public void updateThrowsWithNullTagTimestamp() {

  }

//  @Test(expected = NullPointerException.class)
//  public void updateWithNullTag() {
//    Alarm alarm = alarmCacheObjectFactory.sampleBase();
//
//    alarmCacheObjectController.updateAlarmBasedOnTag(alarm, null);
//  }
}

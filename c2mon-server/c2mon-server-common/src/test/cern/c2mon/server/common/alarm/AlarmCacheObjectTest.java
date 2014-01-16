package cern.c2mon.server.common.alarm;

import org.junit.Test;

/**
 * Test of AlarmCacheObject class.
 * @author Mark Brightwell
 *
 */
public class AlarmCacheObjectTest {

  @Test
  public void testClone() throws CloneNotSupportedException {
    AlarmCacheObject alarm = new AlarmCacheObject(10L);
    AlarmCacheObject cloneAlarm = (AlarmCacheObject) alarm.clone();    
  }
  
}

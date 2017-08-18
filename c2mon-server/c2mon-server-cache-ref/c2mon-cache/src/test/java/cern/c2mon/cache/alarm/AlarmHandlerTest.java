package cern.c2mon.cache.alarm;

import java.sql.Timestamp;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.TagLocationService;
import cern.c2mon.server.cache.alarm.AlarmHandler;
import cern.c2mon.server.cache.alarm.AlarmUpdateHandler;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.alarm.AlarmCondition;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.test.CacheObjectCreation;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.TagQualityStatus;

import static org.junit.Assert.*;

/**
 * @author Szymon Halastra
 */
public class AlarmHandlerTest {

  private AlarmHandler alarmHandler;

  private C2monCache<Long, Alarm> alarmCache;

  private TagLocationService tagLocationService;

  @Before
  public void setup() {
    alarmCache = EasyMock.createNiceMock(C2monCache.class);
    tagLocationService = EasyMock.createStrictMock(TagLocationService.class);
    alarmHandler = new AlarmUpdateHandler(alarmCache);
  }

  /**
   * Verifies that the alarm stays TERMINATED for tags that are invalid but which changes
   * still their values, so that an alarm would be ACTIVATED under valid conditions.
   */
  @Test
  public void testInvalidActivationFiltered() throws InterruptedException {
    Timestamp tagTime = new Timestamp(System.currentTimeMillis());

    DataTagCacheObject tag = CacheObjectCreation.createTestDataTag3();
    tag.setDataTagQuality(new DataTagQualityImpl(TagQualityStatus.UNKNOWN_REASON));
    AlarmCacheObject currentAlarmState = CacheObjectCreation.createTestAlarm1();
    Timestamp origTime = new Timestamp(System.currentTimeMillis() - 1000);
    currentAlarmState.setTimestamp(origTime);
    tag.setSourceTimestamp(tagTime);
    //check set as expected
    assertEquals(AlarmCondition.TERMINATE, currentAlarmState.getState());
    assertEquals(AlarmCondition.ACTIVE, currentAlarmState.getCondition().evaluateState(tag.getValue()));
    assertFalse(tag.isValid());
    assertFalse(currentAlarmState.isPublishedToLaser());
    currentAlarmState.hasBeenPublished(new Timestamp(System.currentTimeMillis()));


    alarmCache.lockOnKey(currentAlarmState.getId());
    EasyMock.expect(alarmCache.get(currentAlarmState.getId())).andReturn(currentAlarmState);
    alarmCache.unlockOnKey(currentAlarmState.getId());
    EasyMock.replay(alarmCache, tagLocationService);
    //(1)test update works
    AlarmCacheObject newAlarm = (AlarmCacheObject) alarmHandler.update(currentAlarmState.getId(), tag);
    EasyMock.verify(alarmCache, tagLocationService);

    assertEquals(AlarmCondition.TERMINATE, newAlarm.getState());
    assertTrue(newAlarm.getTimestamp().equals(tag.getCacheTimestamp()));
    assertEquals(AlarmCondition.TERMINATE, currentAlarmState.getState()); //also update alarm parameter object (usually in cache)
    assertTrue(currentAlarmState.getTimestamp().equals(tag.getCacheTimestamp()));
    assertTrue(currentAlarmState.isPublishedToLaser());
  }

  /**
   * Testing the change of an Alarm from ACTIVE to TERMINATE. Important is also that
   * the new alarm timestamp is set to the tag cache timestamp. This is currently the only
   * way to determine which tag event triggered which alarm evaluation.
   */
  @Test
  public void testUpdateTimestampIsSetToTagCacheTimestamp() {
    Timestamp tagTime = new Timestamp(System.currentTimeMillis() - 1000);

    DataTagCacheObject tag = CacheObjectCreation.createTestDataTag();
    AlarmCacheObject currentAlarmState = CacheObjectCreation.createTestAlarm2();
    Timestamp origTime = new Timestamp(System.currentTimeMillis() - 50000);
    currentAlarmState.setTimestamp(origTime);
    tag.setSourceTimestamp(tagTime);
    //check set as expected
    assertEquals(AlarmCondition.ACTIVE, currentAlarmState.getState());
    assertEquals(AlarmCondition.TERMINATE, currentAlarmState.getCondition().evaluateState(tag.getValue()));
    assertTrue(tag.isValid());
    assertFalse(currentAlarmState.isPublishedToLaser());
    currentAlarmState.hasBeenPublished(origTime);

    // Recording Mock calls
    alarmCache.lockOnKey(currentAlarmState.getId());
    EasyMock.expect(alarmCache.get(currentAlarmState.getId())).andReturn(currentAlarmState);
    alarmCache.put(currentAlarmState.getId(), currentAlarmState);
    alarmCache.unlockOnKey(currentAlarmState.getId());
    EasyMock.replay(alarmCache, tagLocationService);

    //(1) test update works
    AlarmCacheObject newAlarm = (AlarmCacheObject) alarmHandler.update(currentAlarmState.getId(), tag);
    EasyMock.verify(alarmCache, tagLocationService);

    assertEquals(AlarmCondition.TERMINATE, newAlarm.getState());
    assertTrue(newAlarm.getTimestamp().equals(tag.getCacheTimestamp()));
    assertEquals(AlarmCondition.TERMINATE, currentAlarmState.getState()); //also update alarm parameter object (usually in cache)
    assertTrue(currentAlarmState.getTimestamp().equals(tag.getCacheTimestamp()));
    assertFalse(currentAlarmState.isPublishedToLaser());
  }

  /**
   * Checks alarms are filtered if previous and new state are TERMINATE.
   *
   * @throws InterruptedException
   */
  @Test
  public void testUpdateFiltered() throws InterruptedException {
    Timestamp tagTime = new Timestamp(System.currentTimeMillis());

    DataTagCacheObject tag = CacheObjectCreation.createTestDataTag3();
    AlarmCacheObject alarm = CacheObjectCreation.createTestAlarm1();
    Timestamp origTime = new Timestamp(System.currentTimeMillis() - 1000);
    alarm.setTimestamp(origTime);
    tag.setSourceTimestamp(tagTime);
    //check set as expected
    assertEquals(AlarmCondition.TERMINATE, alarm.getState());
    assertEquals(AlarmCondition.ACTIVE, alarm.getCondition().evaluateState(tag.getValue()));
    assertFalse(alarm.isPublishedToLaser());
    alarm.hasBeenPublished(new Timestamp(System.currentTimeMillis()));

    alarmCache.lockOnKey(alarm.getId());
    EasyMock.expect(alarmCache.get(alarm.getId())).andReturn(alarm);
    // record expected notification call with EasyMock
    alarmCache.put(alarm.getId(), alarm);
    alarmCache.unlockOnKey(alarm.getId());
    EasyMock.replay(alarmCache, tagLocationService);

    //(1)test update works
    AlarmCacheObject newAlarm = (AlarmCacheObject) alarmHandler.update(alarm.getId(), tag);

    EasyMock.verify(alarmCache, tagLocationService);

    assertEquals(AlarmCondition.ACTIVE, newAlarm.getState());
    assertTrue(newAlarm.getTimestamp().after(origTime));
    assertEquals(AlarmCondition.ACTIVE, alarm.getState()); //also update alarm parameter object (usually in cache)
    assertTrue(alarm.getTimestamp().after(origTime));
    assertFalse(alarm.isPublishedToLaser());

    //(2)test terminate->terminate fails
    AlarmCacheObject alarm2 = CacheObjectCreation.createTestAlarm1(); //reset data
    origTime = alarm2.getTimestamp();
    Thread.sleep(10);
    assertEquals(AlarmCondition.TERMINATE, alarm2.getState()); //check is in correct start state
    tag.setValue("UP"); //alarm should be terminate

    EasyMock.reset(alarmCache, tagLocationService);
    alarmCache.lockOnKey(alarm2.getId());
    EasyMock.expect(alarmCache.get(alarm2.getId())).andReturn(alarm2);
    alarmCache.unlockOnKey(alarm2.getId());
    EasyMock.replay(alarmCache, tagLocationService);

    AlarmCacheObject newAlarm2 = (AlarmCacheObject) alarmHandler.update(alarm2.getId(), tag);

    EasyMock.verify(alarmCache, tagLocationService);

    assertEquals(AlarmCondition.TERMINATE, newAlarm2.getState()); //original TERMINATE!
    assertEquals(newAlarm2.getTimestamp(), origTime);
    assertEquals(AlarmCondition.TERMINATE, alarm2.getState()); //original TERMINATE!
    assertEquals(alarm2.getTimestamp(), origTime);
  }
}

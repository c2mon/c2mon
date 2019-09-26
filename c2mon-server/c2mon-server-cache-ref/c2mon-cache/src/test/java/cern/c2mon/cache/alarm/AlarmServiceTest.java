package cern.c2mon.cache.alarm;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.alarm.AlarmService;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

/**
 * @author Szymon Halastra
 */
public class AlarmServiceTest {

  private AlarmService alarmService;

  private C2monCache<Tag> tagCache;

  private C2monCache<Alarm> alarmCache;

  @Before
  public void setup() {
    alarmCache = EasyMock.createNiceMock(C2monCache.class);
    tagCache = EasyMock.createStrictMock(C2monCache.class);
//    alarmService = new AlarmService(alarmCache, tagCache);
  }

  @Test
  @Ignore
  public void evaluateAlarmWithNullTag() {
    AlarmCacheObject alarm = new AlarmCacheObject(1L);
//    alarm.setState(AlarmCondition.ACTIVE);
    alarm.setDataTagId(1L);

    DataTagCacheObject tag = new DataTagCacheObject(1L);
    tag.setValue(null);

    expect(alarmCache.get(1L)).andReturn(alarm);
    expect(tagCache.get(alarm.getDataTagId())).andReturn(tag);
    replay(alarmCache, tagCache);

//    alarmService.evaluateAlarm(alarm.getId());

//    assertEquals("Alarm should have the same state as before evaluation", true, alarm.getState().equals(AlarmCondition.ACTIVE));
  }

  @Test
  @Ignore
  public void evaluateAlarmWithUninitialisedTag() {
    AlarmCacheObject alarmCacheObject = new AlarmCacheObject(1L);
//    alarmCacheObject.setState(AlarmCondition.ACTIVE);
    alarmCacheObject.setDataTagId(1L);

    DataTagCacheObject tagCacheObject = new DataTagCacheObject(1L);
    DataTagQualityImpl dataTagQuality = new DataTagQualityImpl(TagQualityStatus.UNINITIALISED);
    tagCacheObject.setDataTagQuality(dataTagQuality);

    tagCacheObject.setValue("value");

    expect(alarmCache.get(1L)).andReturn(alarmCacheObject);
    expect(tagCache.get(1L)).andReturn(tagCacheObject);

    replay(alarmCache, tagCache);

//    alarmService.evaluateAlarm(alarmCacheObject.getId());

//    assertEquals("Alarm should have the same status as before evaluation", true, alarmCacheObject.getState().equals(AlarmCondition.ACTIVE));
  }

  /**
   * Verifies that the alarm stays TERMINATED for tags that are invalid but which changes
   * still their values, so that an alarm would be ACTIVATED under valid conditions.
   */
  @Test
  @Ignore
  public void testInvalidActivationFiltered() throws InterruptedException {
//    Timestamp tagTime = new Timestamp(System.currentTimeMillis());
//
//    DataTagCacheObject tag = CacheObjectCreation.createTestDataTag3();
//    tag.setDataTagQuality(new DataTagQualityImpl(TagQualityStatus.UNKNOWN_REASON));
//    AlarmCacheObject currentAlarmState = CacheObjectCreation.createTestAlarm1();
//    Timestamp origTime = new Timestamp(System.currentTimeMillis() - 1000);
//    currentAlarmState.setTimestamp(origTime);
//    tag.setSourceTimestamp(tagTime);
//    //check set as expected
//    assertEquals(AlarmCondition.TERMINATE, currentAlarmState.getState());
//    assertEquals(AlarmCondition.ACTIVE, currentAlarmState.getCondition().evaluateState(tag.getValue()));
//    assertFalse(tag.isValid());
//    assertFalse(currentAlarmState.isPublishedToLaser());
//    currentAlarmState.hasBeenPublished(new Timestamp(System.currentTimeMillis()));
//
//
//    alarmCache.lockOnKey(currentAlarmState.getId());
//    expect(alarmCache.get(currentAlarmState.getId())).andReturn(currentAlarmState);
//    alarmCache.unlockOnKey(currentAlarmState.getId());
//    EasyMock.replay(alarmCache, tagCache);
//    //(1)test update works
//    AlarmCacheObject newAlarm = (AlarmCacheObject) alarmService.update(currentAlarmState.getId(), tag);
//    EasyMock.verify(alarmCache, tagCache);
//
//    assertEquals(AlarmCondition.TERMINATE, newAlarm.getState());
//    assertTrue(newAlarm.getTimestamp().equals(tag.getCacheTimestamp()));
//    assertEquals(AlarmCondition.TERMINATE, currentAlarmState.getState()); //also update alarm parameter object (usually in cache)
//    assertTrue(currentAlarmState.getTimestamp().equals(tag.getCacheTimestamp()));
//    assertTrue(currentAlarmState.isPublishedToLaser());
  }

  /**
   * Testing the change of an Alarm from ACTIVE to TERMINATE. Important is also that
   * the new alarm timestamp is set to the tag cache timestamp. This is currently the only
   * way to determine which tag event triggered which alarm evaluation.
   */
  @Test
  @Ignore
  public void testUpdateTimestampIsSetToTagCacheTimestamp() {
//    Timestamp tagTime = new Timestamp(System.currentTimeMillis() - 1000);
//
//    DataTagCacheObject tag = CacheObjectCreation.createTestDataTag();
//    AlarmCacheObject currentAlarmState = CacheObjectCreation.createTestAlarm2();
//    Timestamp origTime = new Timestamp(System.currentTimeMillis() - 50000);
//    currentAlarmState.setTimestamp(origTime);
//    tag.setSourceTimestamp(tagTime);
//    //check set as expected
//    assertEquals(AlarmCondition.ACTIVE, currentAlarmState.getState());
//    assertEquals(AlarmCondition.TERMINATE, currentAlarmState.getCondition().evaluateState(tag.getValue()));
//    assertTrue(tag.isValid());
//    assertFalse(currentAlarmState.isPublishedToLaser());
//    currentAlarmState.hasBeenPublished(origTime);
//
//    // Recording Mock calls
//    alarmCache.lockOnKey(currentAlarmState.getId());
//    expect(alarmCache.get(currentAlarmState.getId())).andReturn(currentAlarmState);
//    alarmCache.put(currentAlarmState.getId(), currentAlarmState);
//    alarmCache.unlockOnKey(currentAlarmState.getId());
//    EasyMock.replay(alarmCache, tagCache);
//
//    //(1) test update works
//    AlarmCacheObject newAlarm = (AlarmCacheObject) alarmService.update(currentAlarmState.getId(), tag);
//    EasyMock.verify(alarmCache, tagCache);
//
//    assertEquals(AlarmCondition.TERMINATE, newAlarm.getState());
//    assertTrue(newAlarm.getTimestamp().equals(tag.getCacheTimestamp()));
//    assertEquals(AlarmCondition.TERMINATE, currentAlarmState.getState()); //also update alarm parameter object (usually in cache)
//    assertTrue(currentAlarmState.getTimestamp().equals(tag.getCacheTimestamp()));
//    assertFalse(currentAlarmState.isPublishedToLaser());
  }

  /**
   * Checks alarms are filtered if previous and new state are TERMINATE.
   *
   * @throws InterruptedException
   */
  @Test
  @Ignore
  public void testUpdateFiltered() throws InterruptedException {
//    Timestamp tagTime = new Timestamp(System.currentTimeMillis());
//
//    DataTagCacheObject tag = CacheObjectCreation.createTestDataTag3();
//    AlarmCacheObject alarm = CacheObjectCreation.createTestAlarm1();
//    Timestamp origTime = new Timestamp(System.currentTimeMillis() - 1000);
//    alarm.setTimestamp(origTime);
//    tag.setSourceTimestamp(tagTime);
//    //check set as expected
//    assertEquals(AlarmCondition.TERMINATE, alarm.getState());
//    assertEquals(AlarmCondition.ACTIVE, alarm.getCondition().evaluateState(tag.getValue()));
//    assertFalse(alarm.isPublishedToLaser());
//    alarm.hasBeenPublished(new Timestamp(System.currentTimeMillis()));
//
//    alarmCache.lockOnKey(alarm.getId());
//    expect(alarmCache.get(alarm.getId())).andReturn(alarm);
//    // record expected notification call with EasyMock
//    alarmCache.put(alarm.getId(), alarm);
//    alarmCache.unlockOnKey(alarm.getId());
//    EasyMock.replay(alarmCache, tagCache);
//
//    //(1)test update works
//    AlarmCacheObject newAlarm = (AlarmCacheObject) alarmService.update(alarm.getId(), tag);
//
//    EasyMock.verify(alarmCache, tagCache);
//
//    assertEquals(AlarmCondition.ACTIVE, newAlarm.getState());
//    assertTrue(newAlarm.getTimestamp().after(origTime));
//    assertEquals(AlarmCondition.ACTIVE, alarm.getState()); //also update alarm parameter object (usually in cache)
//    assertTrue(alarm.getTimestamp().after(origTime));
//    assertFalse(alarm.isPublishedToLaser());
//
//    //(2)test terminate->terminate fails
//    AlarmCacheObject alarm2 = CacheObjectCreation.createTestAlarm1(); //reset data
//    origTime = alarm2.getTimestamp();
//    Thread.sleep(10);
//    assertEquals(AlarmCondition.TERMINATE, alarm2.getState()); //check is in correct start state
//    tag.setValue("UP"); //alarm should be terminate
//
//    EasyMock.reset(alarmCache, tagCache);
//    alarmCache.lockOnKey(alarm2.getId());
//    expect(alarmCache.get(alarm2.getId())).andReturn(alarm2);
//    alarmCache.unlockOnKey(alarm2.getId());
//    EasyMock.replay(alarmCache, tagCache);
//
//    AlarmCacheObject newAlarm2 = (AlarmCacheObject) alarmService.update(alarm2.getId(), tag);
//
//    EasyMock.verify(alarmCache, tagCache);
//
//    assertEquals(AlarmCondition.TERMINATE, newAlarm2.getState()); //original TERMINATE!
//    assertEquals(newAlarm2.getTimestamp(), origTime);
//    assertEquals(AlarmCondition.TERMINATE, alarm2.getState()); //original TERMINATE!
//    assertEquals(alarm2.getTimestamp(), origTime);
  }
}

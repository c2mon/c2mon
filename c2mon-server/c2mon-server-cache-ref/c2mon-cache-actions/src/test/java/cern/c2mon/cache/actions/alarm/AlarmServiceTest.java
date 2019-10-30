package cern.c2mon.cache.actions.alarm;

import cern.c2mon.cache.actions.oscillation.OscillationUpdater;
import cern.c2mon.cache.actions.tag.UnifiedTagCacheFacade;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.impl.SimpleC2monCache;
import cern.c2mon.cache.config.tag.TagCacheFacade;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.test.CacheObjectCreation;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.Timestamp;

import static org.junit.Assert.*;

/**
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou
 */
public class AlarmServiceTest {

  private AlarmService alarmService;

  private C2monCache<Alarm> alarmCache;
  private C2monCache<DataTag> dataTagCache;

  @Before
  public void setup() {
    alarmCache = new SimpleC2monCache<>("alarm");
    C2monCache<RuleTag> ruleTagCache = new SimpleC2monCache<>("rules");
    C2monCache<ControlTag> controlTagCache = new SimpleC2monCache<>("control");
    dataTagCache = new SimpleC2monCache<>("data");
    TagCacheFacade tagCacheFacade = new TagCacheFacade(ruleTagCache, controlTagCache, dataTagCache);
    UnifiedTagCacheFacade unifiedTagCacheFacade = new UnifiedTagCacheFacade(ruleTagCache, controlTagCache, dataTagCache);
    AlarmCacheUpdaterImpl alarmCacheUpdater = new AlarmCacheUpdaterImpl();
    alarmCacheUpdater.setOscillationUpdater(new OscillationUpdater());
    alarmCacheUpdater.setAlarmCache(alarmCache);
    alarmService = new AlarmService(alarmCache, tagCacheFacade, alarmCacheUpdater, unifiedTagCacheFacade);
  }

  @Test
  public void evaluateAlarmWithNullTag() {
    AlarmCacheObject alarm = new AlarmCacheObject(1L);
    alarm.setActive(true);
    alarm.setDataTagId(1L);

    DataTagCacheObject tag = new DataTagCacheObject(1L);
    tag.setValue(null);

    dataTagCache.put(tag.getId(), tag);
    alarmCache.put(alarm.getId(), alarm);

    alarmService.evaluateAlarm(alarm.getId());

    assertTrue("Alarm should have the same state as before evaluation", alarm.isActive());
  }

  @Test
  public void evaluateAlarmWithUninitialisedTag() {
    AlarmCacheObject alarmCacheObject = new AlarmCacheObject(1L);
    alarmCacheObject.setActive(false);
    alarmCacheObject.setDataTagId(1L);

    DataTagCacheObject dataTagCacheObject = new DataTagCacheObject(1L);
    DataTagQualityImpl dataTagQuality = new DataTagQualityImpl(TagQualityStatus.UNINITIALISED);
    dataTagCacheObject.setDataTagQuality(dataTagQuality);

    dataTagCacheObject.setValue("value");

    dataTagCache.put(dataTagCacheObject.getId(), dataTagCacheObject);
    alarmCache.put(alarmCacheObject.getId(), alarmCacheObject);

    alarmService.evaluateAlarm(alarmCacheObject.getId());

    assertFalse("Alarm should have the same status as before evaluation", alarmCacheObject.isActive());
  }

  /**
   * Verifies that the alarm stays TERMINATED for tags that are invalid but which changes
   * still their values, so that an alarm would be ACTIVATED under valid conditions.
   */
  @Test
  public void testInvalidActivationFiltered() {
    Timestamp tagTime = new Timestamp(System.currentTimeMillis());

    DataTagCacheObject tag = CacheObjectCreation.createTestDataTag3();
    tag.setDataTagQuality(new DataTagQualityImpl(TagQualityStatus.UNKNOWN_REASON));
    AlarmCacheObject alarm = CacheObjectCreation.createTestAlarm1();
    Timestamp origTime = new Timestamp(System.currentTimeMillis() - 1000);
    alarm.setTriggerTimestamp(origTime);
    tag.setSourceTimestamp(tagTime);
    //check set as expected
    assertFalse(alarm.isActive());
    assertTrue(alarm.getCondition().evaluateState(tag.getValue()));
    assertFalse(tag.isValid());

    alarmCache.put(alarm.getId(), alarm);
    //(1)test update works
    AlarmCacheObject newAlarm = (AlarmCacheObject) alarmService.update(alarm.getId(), tag);

    assertFalse(alarm.isActive());
    assertFalse(alarm.isActive()); //also update alarm parameter object (usually in cache)
  }

  /**
   * Testing the change of an Alarm from ACTIVE to TERMINATE. Important is also that
   * the new alarm timestamp is set to the tag cache timestamp. This is currently the only
   * way to determine which tag event triggered which alarm evaluation.
   */
  @Test
  @Ignore
  public void testUpdateTimestampIsSetToTagCacheTimestamp() {
    Timestamp tagTime = new Timestamp(System.currentTimeMillis() - 1000);

    DataTagCacheObject tag = CacheObjectCreation.createTestDataTag();
    AlarmCacheObject currentAlarmState = CacheObjectCreation.createTestAlarm2();
    Timestamp origTime = new Timestamp(System.currentTimeMillis() - 50000);
    currentAlarmState.setTriggerTimestamp(origTime);
    tag.setSourceTimestamp(tagTime);
    //check set as expected
    assertTrue(currentAlarmState.isActive());
    assertFalse(currentAlarmState.getCondition().evaluateState(tag.getValue()));
    assertTrue(tag.isValid());

    dataTagCache.put(tag.getId(), tag);
    alarmCache.put(currentAlarmState.getId(), currentAlarmState);

    //(1) test update works
    AlarmCacheObject newAlarm = (AlarmCacheObject) alarmService.update(currentAlarmState.getId(), tag);

    assertFalse(newAlarm.isActive());
    assertTrue(newAlarm.getSourceTimestamp().equals(tag.getTimestamp()));
    assertFalse(currentAlarmState.isActive()); //also update alarm parameter object (usually in cache)
    assertTrue(currentAlarmState.getSourceTimestamp().equals(tag.getTimestamp()));
  }

  /**
   * Checks alarms are filtered if previous and new state are TERMINATE.
   *
   * @throws InterruptedException
   */
  @Test
  @Ignore
  public void testUpdateFiltered() throws InterruptedException {
    Timestamp tagTime = new Timestamp(System.currentTimeMillis());

    DataTagCacheObject tag = CacheObjectCreation.createTestDataTag3();
    AlarmCacheObject alarm = CacheObjectCreation.createTestAlarm1();
    Timestamp origTime = new Timestamp(System.currentTimeMillis() - 1000);
    alarm.setTriggerTimestamp(origTime);
    tag.setSourceTimestamp(tagTime);
    //check set as expected
    assertFalse(alarm.isActive());
    assertTrue(alarm.getCondition().evaluateState(tag.getValue()));

    dataTagCache.put(tag.getId(), tag);
    alarmCache.put(alarm.getId(), alarm);

    //(1)test update works
    AlarmCacheObject newAlarm = (AlarmCacheObject) alarmService.update(alarm.getId(), tag);

    assertTrue(newAlarm.isActive());
    assertTrue(newAlarm.getTriggerTimestamp().after(origTime));
    assertTrue(alarm.isActive()); //also update alarm parameter object (usually in cache)
    assertTrue(alarm.getTriggerTimestamp().after(origTime));

    //(2)test terminate->terminate fails
    AlarmCacheObject alarm2 = CacheObjectCreation.createTestAlarm1(); //reset data
    origTime = alarm2.getTriggerTimestamp();
    Thread.sleep(10);
    assertFalse(alarm2.isActive()); //check is in correct start state
    tag.setValue("UP"); //alarm should be terminate

    alarmCache.clear();
    dataTagCache.clear();

    dataTagCache.put(tag.getId(), tag);
    alarmCache.put(alarm.getId(), alarm);

    AlarmCacheObject newAlarm2 = (AlarmCacheObject) alarmService.update(alarm2.getId(), tag);

    assertFalse(newAlarm2.isActive()); //original TERMINATE!
    assertEquals(newAlarm2.getTriggerTimestamp(), origTime);
    assertFalse(alarm2.isActive()); //original TERMINATE!
    assertEquals(alarm2.getTriggerTimestamp(), origTime);
  }
}

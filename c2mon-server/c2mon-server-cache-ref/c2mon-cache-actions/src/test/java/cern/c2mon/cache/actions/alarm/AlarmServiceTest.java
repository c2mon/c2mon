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
import cern.c2mon.server.test.cache.AlarmCacheObjectFactory;
import cern.c2mon.server.test.cache.DataTagCacheObjectFactory;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.function.BiConsumer;

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
    alarmService = new AlarmService(alarmCache, tagCacheFacade, unifiedTagCacheFacade, new OscillationUpdater());
  }

  @Test
  public void evaluateAlarmWithNullTag() {
    insertAlarmAndDatatagThen(
      (alarm, __) -> alarm.setActive(true),
      (alarm, __) -> assertTrue("Alarm should have the same state as before evaluation", alarm.isActive()));
  }

  @Test
  public void updateIsConsistent() {
    insertAlarmAndDatatagThen((alarm, dataTag) -> {
      AlarmCacheObject preCache = alarm.clone();

      alarmService.update(alarm, dataTag, true);

      // Initially the old object should not be evaluated
      assertNotEquals(preCache, alarm);

      alarmService.update(preCache, dataTag, true);

      // The effect should be the same as calling the alarmCacheController update method
      assertEquals(preCache, alarm);
    });
  }

  @Test
  public void evaluateAlarmWithUninitialisedTag() {
    insertAlarmAndDatatagThen(
      (alarm, dataTag) -> {
        alarm.setActive(false);
        alarm.setDataTagId(1L);

        dataTag.setDataTagQuality(new DataTagQualityImpl(TagQualityStatus.UNINITIALISED));
        dataTag.setId(1L);
        dataTag.setValue("value");
      },
      (alarm, __) -> {
        alarmService.evaluateAlarm(alarm.getId());
        assertFalse("Alarm should have the same status as before evaluation", alarm.isActive());
      });
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
    tag.setSourceTimestamp(tagTime);

    AlarmCacheObject alarm = CacheObjectCreation.createTestAlarm1();
    Timestamp origTime = new Timestamp(System.currentTimeMillis() - 1000);
    alarm.setTriggerTimestamp(origTime);

    //check set as expected
    assertFalse(alarm.isActive());
    assertTrue(alarm.getCondition().evaluateState(tag.getValue()));
    assertFalse(tag.isValid());

    alarmCache.put(alarm.getId(), alarm);
    //(1)test update works
    alarmService.update(alarm, tag, true);

    assertFalse(alarm.isActive());
    assertFalse(alarm.isActive()); //also update alarm parameter object (usually in cache)
  }

  @Test
  public void updateChangesTimestamp() {
    insertAlarmAndDatatagThen(
      (alarm, dataTag) -> {
        Timestamp tagTime = new Timestamp(System.currentTimeMillis() - 1000);
        dataTag.setSourceTimestamp(tagTime);
        dataTag.setDataTagQuality(new DataTagCacheObjectFactory().createValidQuality());
        Timestamp alarmTime = new Timestamp(System.currentTimeMillis() - 50000);
        alarm.setTriggerTimestamp(alarmTime);
        alarm.setActive(true);

        assertTrue(alarm.isActive());
        assertNotEquals(alarm.getSourceTimestamp(), dataTag.getTimestamp());
      },
      (alarmAfterInsert, dataTag) -> {
        alarmService.update(alarmAfterInsert, dataTag, true);

        assertEquals(alarmAfterInsert.getSourceTimestamp(), dataTag.getTimestamp());
      }
    );
  }

  @Test
  public void updateTerminatesAlarm() {
    AlarmCacheObject alarm = new AlarmCacheObjectFactory().alarmActiveWithFalseCondition();
    Timestamp alarmTime = new Timestamp(System.currentTimeMillis() - 50000);
    alarm.setTriggerTimestamp(alarmTime);
    alarm.setActive(true);
    DataTagCacheObject dataTag = new DataTagCacheObjectFactory().sampleBase();
    Timestamp tagTime = new Timestamp(System.currentTimeMillis() - 1000);
    dataTag.setSourceTimestamp(tagTime);
    alarm.setDataTagId(dataTag.getId());

    // Alarm is active, but the condition is false
    assertTrue(alarm.isActive());
    assertTrue(dataTag.isValid());
    assertFalse(alarm.getCondition().evaluateState(dataTag.getValue()));

    // After evaluating, the alarm should be terminated
    alarmService.update(alarm, dataTag, true);
    assertFalse(alarm.isActive());
    assertTrue(dataTag.isValid());
    assertFalse(alarm.getCondition().evaluateState(dataTag.getValue()));
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
    alarm.setTriggerTimestamp(origTime);
    tag.setSourceTimestamp(tagTime);
    //check set as expected
    assertFalse(alarm.isActive());
    assertTrue(alarm.getCondition().evaluateState(tag.getValue()));

    dataTagCache.put(tag.getId(), tag);
    alarmCache.put(alarm.getId(), alarm);

    //(1)test update works
    alarmService.update(alarm, tag, true);

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

    alarmService.update(alarm2, tag, true);

    assertFalse(alarm2.isActive()); //original TERMINATE!
    assertEquals(alarm2.getTriggerTimestamp(), origTime);
  }

  @Test
  public void olderAlarmGetsRejected() {
    Timestamp now = new Timestamp(System.currentTimeMillis());
    Timestamp before = new Timestamp(now.getTime() - 1000);

    AlarmCacheObject older = CacheObjectCreation.createTestAlarm1();
    older.setSourceTimestamp(before);

    AlarmCacheObject newer = CacheObjectCreation.createTestAlarm1();
    newer.setSourceTimestamp(now);

    alarmCache.put(newer.getId(), newer);

    // newr.getId is not a typo, just making sure they go to the same place
    alarmCache.put(newer.getId(), older);

    assertEquals(newer, alarmCache.get(newer.getId()));
  }

  /**
   * Use this helper to edit the alarm and datatag before they're put in the cache
   */
  private void createAlarmAndDatatagThen(BiConsumer<AlarmCacheObject, DataTagCacheObject> postCreationOps) {
    AlarmCacheObject alarm = CacheObjectCreation.createTestAlarm1();
    DataTagCacheObject dataTag = CacheObjectCreation.createTestDataTag3();
    dataTag.setDataTagQuality(new DataTagQualityImpl(TagQualityStatus.UNKNOWN_REASON));

    postCreationOps.accept(alarm, dataTag);
  }

  /**
   * @param preInsertMutate use this lambda to edit the alarm, datatag before they're put in the cache
   * @param test            use this lambda to run assertions after the alarm, datatag are put in the cache
   */
  private void insertAlarmAndDatatagThen(BiConsumer<AlarmCacheObject, DataTagCacheObject> preInsertMutate,
                                         BiConsumer<AlarmCacheObject, DataTagCacheObject> test) {
    createAlarmAndDatatagThen((alarm, dataTag) -> {
      preInsertMutate.accept(alarm, dataTag);

      dataTagCache.put(dataTag.getId(), dataTag);
      alarmCache.put(alarm.getId(), alarm);

      test.accept(alarm, dataTag);
    });
  }

  /**
   * Overload with empty mutator
   *
   * @param test use this lambda to run assertions after the alarm, datatag are put in the cache
   */
  private void insertAlarmAndDatatagThen(BiConsumer<AlarmCacheObject, DataTagCacheObject> test) {
    insertAlarmAndDatatagThen((__, ___) -> {
    }, test);
  }
}

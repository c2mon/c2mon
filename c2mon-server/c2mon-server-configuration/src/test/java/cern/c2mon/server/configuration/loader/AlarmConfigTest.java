package cern.c2mon.server.configuration.loader;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.AlarmMapper;
import cern.c2mon.server.cache.dbaccess.RuleTagMapper;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.shared.common.NoSimpleValueParseException;
import org.junit.Test;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class AlarmConfigTest extends ConfigurationCacheLoaderTest<Alarm> {

  @Inject
  private C2monCache<Alarm> alarmCache;

  @Inject
  private AlarmMapper alarmMapper;

  @Inject
  private C2monCache<RuleTag> ruleTagCache;

  @Inject
  private RuleTagMapper ruleTagMapper;

  /**
   * Test the creation, update and removal of alarm.
   */

//  @Test
//  public void testCreateAlarmWithExistingDatatag() {
//    replay(mockManager);
//
//    // we  expect to send the alarm as the datatag is initialized.
//    C2monCacheListener<Alarm> checker = EasyMock.createMock(C2monCacheListener.class);
//    checker.notifyElementUpdated(EasyMock.isA(Alarm.class));
//    EasyMock.expectLastCall().once();
//    EasyMock.replay(checker);
//
//    alarmCache.registerSynchronousListener(checker);
//
//    DataTagCacheObject toInit = (DataTagCacheObject)dataTagCache.get(200003L);
//    toInit.setValue(Boolean.TRUE);
//    toInit.getDataTagQuality().validate();
//    dataTagCache.putQuiet(toInit);
//
//    ConfigurationReport report = configurationLoader.applyConfiguration(22);
//    verify(checker);
//    ((AbstractCache) alarmCache).getCacheListeners().remove(checker);
//  }


  /**
   * Test the creation, update and removal of alarm.
   */

//  @Test
//  public void testCreateUpdateAlarm() {
//    replay(mockManager);
//
//
//    // we do not expect to send the alarm as the datatag is unitialized.
//    C2monCacheListener<Alarm> checker = EasyMock.createMock(C2monCacheListener.class);
//    EasyMock.replay(checker);
//
//    alarmCache.registerSynchronousListener(checker);
//
//    ConfigurationReport report = configurationLoader.applyConfiguration(22);
//
//    assertFalse(report.toXML().contains(Status.FAILURE.toString()));
//
//    AlarmCacheObject cacheObject = (AlarmCacheObject) alarmCache.get(300000L);
//    AlarmCacheObject expectedObject = new AlarmCacheObject(300000L);
//    expectedObject.setDataTagId(200003L);
//    expectedObject.setFaultFamily("fault family");
//    expectedObject.setFaultMember("fault member");
//    expectedObject.setFaultCode(223);
//    expectedObject
//        .setCondition(AlarmCondition
//            .fromConfigXML("<AlarmCondition class=\"cern.c2mon.server.common.alarm.ValueAlarmCondition\"><alarm-value type=\"Boolean\">true</alarm-value></AlarmCondition>"));
//
//    ObjectEqualityComparison.assertAlarmEquals(expectedObject, cacheObject);
//
//    // also check that the Tag was updated
//    Tag tag = tagLocationService.get(expectedObject.getDataTagId());
//    assertTrue(tag.getAlarmIds().contains(expectedObject.getId()));
//
//    // update should succeed
//    report = configurationLoader.applyConfiguration(23);
//
//    assertFalse(report.toXML().contains(Status.FAILURE.toString()));
//    cacheObject = (AlarmCacheObject) alarmCache.get(300000L);
//    expectedObject.setFaultFamily("updated fault family");
//    ObjectEqualityComparison.assertAlarmEquals(expectedObject, cacheObject);
//
//    verify(mockManager);
//    verify(checker);
//    ((AbstractCache) alarmCache).getCacheListeners().remove(checker);
//  }
//
//  @Test
//  public void testRemoveAlarm() {
//    Alarm alarm = alarmCache.get(350000L);
//    assertNotNull(alarm);
//    assertTrue(alarmCache.containsKey(350000L));
//    assertNotNull(alarmMapper.getItem(350000L));
//
//    replay(mockManager);
//
//    // we  expect to notify the cache listeners about a TERM alarm.
//    C2monCacheListener<Alarm> checker = EasyMock.createMock(C2monCacheListener.class);
//    checker.notifyElementUpdated(EasyMock.isA(Alarm.class));
//    EasyMock.expectLastCall().once();
//    EasyMock.replay(checker);
//    alarmCache.registerSynchronousListener(checker);
//
//    ConfigurationReport report = configurationLoader.applyConfiguration(24);
//
//    assertFalse(report.toXML().contains(Status.FAILURE.toString()));
//    assertFalse(alarmCache.containsKey(350000L));
//    assertNull(alarmMapper.getItem(350000L));
//    Tag tag = tagLocationService.get(alarm.getDataTagId());
//    assertFalse(tag.getAlarmIds().contains(alarm.getId()));
//    verify(mockManager);
//    verify(checker);
//    ((AbstractCache) alarmCache).getCacheListeners().remove(checker);
//  }

  /**
   * Tests that a tag removal does indeed remove an associated alarm.
   *
   * @throws NoSimpleValueParseException
   * @throws NoSuchFieldException
   * @throws TransformerException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws ParserConfigurationException
   */
  @Test
  public void testAlarmRemovedOnTagRemoval() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException,
    NoSuchFieldException, NoSimpleValueParseException {
    replay(mockManager);

    // test removal of (rule)tag 60000 removes the alarm also
    configurationLoader.applyConfiguration(27);
    assertFalse(alarmCache.containsKey(350000L));
    assertNull(alarmMapper.getItem(350000L));
    assertFalse(ruleTagCache.containsKey(60000L));
    assertNull(ruleTagMapper.getItem(60000L));
    verify(mockManager);
  }
}

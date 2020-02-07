package cern.c2mon.server.configuration.loader;

import cern.c2mon.cache.actions.process.ProcessService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.AlarmMapper;
import cern.c2mon.server.cache.dbaccess.DataTagMapper;
import cern.c2mon.server.cache.dbaccess.RuleTagMapper;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.configuration.helper.ObjectEqualityComparison;
import cern.c2mon.server.configuration.parser.util.ConfigurationAlarmUtil;
import cern.c2mon.server.configuration.util.TestConfigurationProvider;
import cern.c2mon.shared.client.alarm.condition.AlarmCondition;
import cern.c2mon.shared.client.alarm.condition.ValueAlarmCondition;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.common.NoSimpleValueParseException;
import cern.c2mon.shared.daq.config.ConfigurationChangeEventReport;
import org.junit.Test;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.sql.Timestamp;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class AlarmConfigTest extends ConfigurationCacheLoaderTest<Alarm> {

  @Inject
  private C2monCache<Alarm> alarmCache;

  @Inject
  private AlarmMapper alarmMapper;

  @Inject
  private C2monCache<RuleTag> ruleTagCache;

  @Inject
  private RuleTagMapper ruleTagMapper;

  @Inject
  private C2monCache<DataTag> dataTagCache;

  @Inject
  private DataTagMapper dataTagMapper;

  @Inject
  private ProcessService processService;

  /**
   * Test the creation, update and removal of alarm.
   */

  @Test
  public void testCreateAlarmWithExistingDatatag() {
    replay(mockManager);

    // we  expect to send the alarm as the datatag is initialized.
//    C2monCacheListener<Alarm> checker = EasyMock.createMock(C2monCacheListener.class);
//    checker.notifyElementUpdated(EasyMock.isA(Alarm.class));
//    EasyMock.expectLastCall().once();
//    EasyMock.replay(checker);
//    alarmCache.registerSynchronousListener(checker);

    dataTagCache.computeQuiet(200003L, dataTag -> {
      ((DataTagCacheObject) dataTag).setValue(Boolean.TRUE);
      dataTag.getDataTagQuality().validate();
    });

    ConfigurationReport report = configurationLoader.applyConfiguration(22);
  }


  /**
   * Test the creation, update and removal of alarm.
   */
  @Test
  public void testCreateUpdateAlarm() {
    replay(mockManager);

    // we do not expect to send the alarm as the datatag is unitialized.
//    C2monCacheListener<Alarm> checker = EasyMock.createMock(C2monCacheListener.class);
//    EasyMock.replay(checker);
//    alarmCache.registerSynchronousListener(checker);

    ConfigurationReport report = configurationLoader.applyConfiguration(22);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));

    Alarm expectedObject = expectedObject();

    assertEquals(expectedObject, alarmCache.get(300000L));

    // also check that the Tag was updated
    DataTag tag = dataTagCache.get(expectedObject.getDataTagId());
    assertTrue(tag.getAlarmIds().contains(expectedObject.getId()));
  }

  @Test
  public void update() {
    configurationLoader.applyConfiguration(22);

    ConfigurationReport report = configurationLoader.applyConfiguration(23);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    AlarmCacheObject expectedObject = expectedObject();
    expectedObject.setFaultFamily("updated fault family");
    assertEquals(expectedObject, alarmCache.get(300000L));
  }

  private static AlarmCacheObject expectedObject() {
    AlarmCacheObject expectedObject = new AlarmCacheObject(300000L);
    expectedObject.setDataTagId(200003L);
    expectedObject.setFaultFamily("fault family");
    expectedObject.setFaultMember("fault member");
    expectedObject.setFaultCode(223);
    expectedObject
      .setCondition(AlarmCondition
        .fromConfigXML("<AlarmCondition class=\"cern.c2mon.server.common.alarm.ValueAlarmCondition\"><alarm-value type=\"Boolean\">true</alarm-value></AlarmCondition>"));

    return expectedObject;
  }

  @Test
  public void testRemoveAlarm() {
    Alarm alarm = alarmCache.get(350000L);
    assertNotNull(alarm);
    assertTrue(alarmCache.containsKey(350000L));
    assertNotNull(alarmMapper.getItem(350000L));

    replay(mockManager);

    // we  expect to notify the cache listeners about a TERM alarm.
//    C2monCacheListener<Alarm> checker = EasyMock.createMock(C2monCacheListener.class);
//    checker.notifyElementUpdated(EasyMock.isA(Alarm.class));
//    EasyMock.expectLastCall().once();
//    EasyMock.replay(checker);
//    alarmCache.registerSynchronousListener(checker);

    ConfigurationReport report = configurationLoader.applyConfiguration(24);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertFalse(alarmCache.containsKey(350000L));
    assertNull(alarmMapper.getItem(350000L));
    DataTag tag = dataTagCache.get(alarm.getDataTagId());
    assertFalse(tag.getAlarmIds().contains(alarm.getId()));
  }

  @Test
  public void createAlarm() {
    // SETUP:
    replay(mockManager);
    setUp();

    // TEST:Build configuration to add the test Alarm
    cern.c2mon.shared.client.configuration.api.alarm.Alarm alarm = ConfigurationAlarmUtil.buildCreateAllFieldsAlarm(2000L, null);
    Configuration configuration = new Configuration();
    configuration.addEntity(alarm);

    ///apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertEquals(1, report.getElementReports().size());

    // get cacheObject from the cache and compare to the an expected cacheObject
    AlarmCacheObject cacheObjectAlarm = (AlarmCacheObject) alarmCache.get(2000L);
    AlarmCacheObject expectedCacheObjectAlarm = cacheObjectFactory.buildAlarmCacheObject(2000L, alarm);

    ObjectEqualityComparison.assertAlarmEquals(expectedCacheObjectAlarm, cacheObjectAlarm);
    // Check if all caches are updated
    assertNotNull(alarmMapper.getItem(2000L));

    verify(mockManager);
  }

  @Test
  public void updateAlarm() {
    replay(mockManager);
    setUp();
    configurationLoader.applyConfiguration(TestConfigurationProvider.createAlarm());

    // TEST:Build configuration to update the test Alarm
    cern.c2mon.shared.client.configuration.api.alarm.Alarm alarmUpdate = cern.c2mon.shared.client.configuration.api.alarm.Alarm.update(2000L).alarmCondition(new ValueAlarmCondition(5)).build();
    Configuration configuration = new Configuration();
    configuration.addEntity(alarmUpdate);

    ///apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertEquals(1, report.getElementReports().size());

    // get cacheObject from the cache and compare to the an expected cacheObject
    AlarmCacheObject cacheObjectAlarm = (AlarmCacheObject) alarmCache.get(2000L);
    AlarmCacheObject expectedCacheObjectAlarm = cacheObjectFactory.buildAlarmUpdateCacheObject(cacheObjectAlarm, alarmUpdate);

    ObjectEqualityComparison.assertAlarmEquals(expectedCacheObjectAlarm, cacheObjectAlarm);

    verify(mockManager);
  }

  @Test
  public void deleteAlarmWithDeleteDataTag() throws IllegalAccessException, TransformerException, InstantiationException, NoSimpleValueParseException, ParserConfigurationException, NoSuchFieldException, InterruptedException {
    expect(mockManager.sendConfiguration(eq(5L), isA(List.class))).andReturn(new ConfigurationChangeEventReport());
    replay(mockManager);
    setUp();
    configurationLoader.applyConfiguration(TestConfigurationProvider.createAlarm());

    // TEST:
    // check if the DataTag and rules are in the cache
    assertTrue(alarmCache.containsKey(2000L));
    assertNotNull(alarmMapper.getItem(2000L));
    assertTrue(dataTagCache.containsKey(1000L));
    assertNotNull(dataTagMapper.getItem(1000L));

    // Build configuration to remove the DataTag
    Configuration removeTag = TestConfigurationProvider.deleteDataTag();
    ConfigurationReport report = configurationLoader.applyConfiguration(removeTag);

    //check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertSame(report.getStatus(), ConfigConstants.Status.OK);
    assertEquals(1, report.getElementReports().size());

    // Check if all caches are updated
    assertFalse(alarmCache.containsKey(2000L));
    assertNull(alarmMapper.getItem(2000L));
    assertFalse(dataTagCache.containsKey(100L));
    assertNull(dataTagMapper.getItem(100L));

    verify(mockManager);
  }

  /**
   * Tests that a tag removal does indeed remove an associated alarm.
   */
  @Test
  public void testAlarmRemovedOnTagRemoval() {
    // test removal of (rule)tag 60000 removes the alarm also
    configurationLoader.applyConfiguration(27);
    assertFalse(alarmCache.containsKey(350000L));
    assertNull(alarmMapper.getItem(350000L));
    assertFalse(ruleTagCache.containsKey(60000L));
    assertNull(ruleTagMapper.getItem(60000L));
  }

  private void setUp() {
    Configuration createProcess = TestConfigurationProvider.createProcess();
    configurationLoader.applyConfiguration(createProcess);
    Configuration createEquipment = TestConfigurationProvider.createEquipment();
    configurationLoader.applyConfiguration(createEquipment);
    Configuration createSubEquipment = TestConfigurationProvider.createSubEquipment();
    configurationLoader.applyConfiguration(createSubEquipment);
    Configuration createDataTag = TestConfigurationProvider.createEquipmentDataTag(15L);
    configurationLoader.applyConfiguration(createDataTag);
    processService.start(5L, "hostname", new Timestamp(System.currentTimeMillis()));
  }
}

package cern.c2mon.server.configuration.loader;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.AlarmMapper;
import cern.c2mon.server.cache.dbaccess.DataTagMapper;
import cern.c2mon.server.cache.dbaccess.EquipmentMapper;
import cern.c2mon.server.cache.dbaccess.RuleTagMapper;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.configuration.helper.ObjectEqualityComparison;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.common.NoSimpleValueParseException;
import cern.c2mon.shared.daq.config.Change;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ConfigurationChangeEventReport;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Test;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class EquipmentConfigTest extends ConfigurationCacheLoaderTest<Equipment> {

  @Inject
  private C2monCache<Equipment> equipmentCache;

  @Inject
  private EquipmentMapper equipmentMapper;

  @Inject
  private C2monCache<AliveTag> aliveTimerCache;

  @Inject
  private C2monCache<CommFaultTag> commFaultTagCache;

  @Inject
  private C2monCache<Process> processCache;

  @Inject
  private C2monCache<RuleTag> ruleTagCache;

  @Inject
  private RuleTagMapper ruleTagMapper;

  @Inject
  private C2monCache<DataTag> dataTagCache;

  @Inject
  private DataTagMapper dataTagMapper;

  @Inject
  private C2monCache<Alarm> alarmCache;

  @Inject
  private AlarmMapper alarmMapper;

  /**
   * Test the creation, update and removal of equipment.
   *
   * @throws NoSimpleValueParseException
   * @throws NoSuchFieldException
   * @throws TransformerException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws ParserConfigurationException
   */
  @Test
  public void testCreateUpdateEquipment() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException,
    NoSuchFieldException, NoSimpleValueParseException {
    // called once when updating the equipment;
    // mock returns a list with the correct number of SUCCESS ChangeReports
    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andAnswer(new IAnswer<ConfigurationChangeEventReport>() {

      @Override
      public ConfigurationChangeEventReport answer() throws Throwable {
        List<Change> changeList = (List<Change>) EasyMock.getCurrentArguments()[1];
        ConfigurationChangeEventReport report = new ConfigurationChangeEventReport();
        for (Change change : changeList) {
          ChangeReport changeReport = new ChangeReport(change);
          changeReport.setState(ChangeReport.CHANGE_STATE.SUCCESS);
          report.appendChangeReport(changeReport);
        }
        return report;
      }
    }).times(2); // twice: once for create, another for update

    replay(mockManager);

    ConfigurationReport report = configurationLoader.applyConfiguration(13);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus()); // ok as DAQ handles Equipment
    // creation
    assertFalse(report.getProcessesToReboot().contains("P_TESTHANDLER03"));

    EquipmentCacheObject cacheObject = (EquipmentCacheObject) equipmentCache.get(110L);
    EquipmentCacheObject expectedObject = new EquipmentCacheObject(110L);
    expectedObject.setName("E_CONFIG_TEST");
    expectedObject.setAddress("serverHostName=VGTCVENTTEST");
    expectedObject.setAliveTagId(1251L);
    expectedObject.setStateTagId(1250L);
    expectedObject.setCommFaultTagId(1252L);
    expectedObject.setHandlerClassName("cern.c2mon.driver.");
    expectedObject.setProcessId(50L);
    expectedObject.setDescription("test description");

    ObjectEqualityComparison.assertEquipmentEquals(expectedObject, cacheObject);

    // also check that the process, commfault and alive cache were updated
    Process process = processCache.get(expectedObject.getProcessId());
    // check process is running
    ((ProcessCacheObject) process).setRequiresReboot(false);
    assertFalse(process.getRequiresReboot());
    assertTrue(process.getEquipmentIds().contains(expectedObject.getId()));
    // the alivetimer and commfault have overriden those already in the cache
    // (check reference to the equipment has changed)
    assertNotNull(commFaultTagCache.get(expectedObject.getCommFaultTagId()));
    assertEquals(expectedObject.getId(), (long) commFaultTagCache.get(cacheObject.getCommFaultTagId()).getSupervisedId());

    // update (creates controltag and updates equipment) - should succeed
    report = configurationLoader.applyConfiguration(25);

    // expect 2 top elements (control and equipment, with control first)
    // equipment report should have 1 sub-reports from DAQ (control tag has no
    // address)
    List<ConfigurationElementReport> topList = report.getElementReports();
    assertEquals(2, topList.size());
    assertEquals(topList.get(0).getEntity(), ConfigConstants.Entity.CONTROLTAG);
    assertEquals(topList.get(1).getEntity(), ConfigConstants.Entity.EQUIPMENT);
    assertEquals(0, topList.get(0).getSubreports().size());
    // 2 sub-reports: One for Equipment alive tag creation and another for the actual equipment
    assertEquals(2, topList.get(1).getSubreports().size());
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    // should contain both a controltag update and equipment update
    // (note the DAQ response is generated automatically by the mock)
    assertTrue(report.toXML().contains("CONTROLTAG"));
    assertTrue(report.toXML().contains("EQUIPMENT"));

    // checks restart status is correctly set (DAQ call is mocked as success for
    // equipment update)
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());

    cacheObject = (EquipmentCacheObject) equipmentCache.get(110L);
    expectedObject.setDescription("updated description");
    expectedObject.setAddress("serverHostName=VGTCVENTTEST;test");
    expectedObject.setAliveTagId(1251L);

    ObjectEqualityComparison.assertEquipmentEquals(expectedObject, cacheObject);

    // check alive timer reference is updated in DB
    assertEquals(new Long(1251L), equipmentMapper.getItem(110L).getAliveTagId());
    // also expect alivetimercache to have element 501:
    assertNotNull(aliveTimerCache.get(1251L));

    verify(mockManager);
  }

  /**
   * Test equipment and control tags are removed correctly.
   *
   * @throws NoSimpleValueParseException
   * @throws NoSuchFieldException
   * @throws TransformerException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws ParserConfigurationException
   */
  @Test
  public void testRemoveEquipement() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException,
    NoSuchFieldException, NoSimpleValueParseException {
    // check as expected
    Equipment equipment = equipmentCache.get(150L);
    assertNotNull(equipment);
    assertNotNull(equipmentMapper.getItem(150L));
    assertTrue(aliveTimerCache.containsKey(equipment.getAliveTagId()));
    assertTrue(commFaultTagCache.containsKey(equipment.getCommFaultTagId()));

    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andAnswer(new IAnswer<ConfigurationChangeEventReport>() {

      @Override
      public ConfigurationChangeEventReport answer() throws Throwable {
        List<Change> changeList = (List<Change>) EasyMock.getCurrentArguments()[1];
        ConfigurationChangeEventReport report = new ConfigurationChangeEventReport();
        for (Change change : changeList) {
          ChangeReport changeReport = new ChangeReport(change);
          changeReport.setState(ChangeReport.CHANGE_STATE.SUCCESS);
          report.appendChangeReport(changeReport);
        }
        return report;
      }
    });

    replay(mockManager);
    // remove equipment
    // remove completes successfully; both Equipment and ControlTags are removed
    ConfigurationReport report = configurationLoader.applyConfiguration(15);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus()); // DAQ deals with Equipment
    // removal
    assertFalse(report.getProcessesToReboot().contains("P_TESTHANDLER03"));
    assertFalse(equipmentCache.containsKey(150L));
    assertNull(equipmentMapper.getItem(150L));
    // commfault and alive should no longer be in cache
    assertFalse(aliveTimerCache.containsKey(equipment.getAliveTagId()));
    assertFalse(commFaultTagCache.containsKey(equipment.getCommFaultTagId()));
    verify(mockManager);
  }

  /**
   * Tests the removal of a process succeeds, with dependent rules and alarms.
   * Relies on permanent test data in test account and must be rolled back. No
   * changes should be sent to the DAQ layer.
   *
   * @throws NoSimpleValueParseException
   * @throws NoSuchFieldException
   * @throws TransformerException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws ParserConfigurationException
   */
  @Test
  public void testRemoveEquipmentDependentObjects() throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException,
    NoSuchFieldException, NoSimpleValueParseException {

    // expect equipment remove message to DAQ
    expect(mockManager.sendConfiguration(eq(50L), isA(List.class))).andAnswer(new IAnswer<ConfigurationChangeEventReport>() {

      @Override
      public ConfigurationChangeEventReport answer() throws Throwable {
        List<Change> changeList = (List<Change>) EasyMock.getCurrentArguments()[1];
        ConfigurationChangeEventReport report = new ConfigurationChangeEventReport();
        for (Change change : changeList) {
          ChangeReport changeReport = new ChangeReport(change);
          changeReport.setState(ChangeReport.CHANGE_STATE.SUCCESS);
          report.appendChangeReport(changeReport);
        }
        return report;
      }
    });

    replay(mockManager);

    ConfigurationReport report = configurationLoader.applyConfiguration(29);
    verify(mockManager);
    // check equipment, tag, rules and alarms are gone
    assertFalse(equipmentCache.containsKey(150L));
    assertNull(equipmentMapper.getItem(150L));
    // check couple of rules
    assertFalse(ruleTagCache.containsKey(60005L));
    assertNull(ruleTagMapper.getItem(60005L));
    assertFalse(ruleTagCache.containsKey(60004L));
    assertNull(ruleTagMapper.getItem(60004L));
    // tags
    assertFalse(dataTagCache.containsKey(200001L));
    assertNull(dataTagMapper.getItem(200001L));
    assertFalse(dataTagCache.containsKey(200004L));
    assertNull(dataTagMapper.getItem(200004L));
    // control tags
//    assertNull(controlTagMapper.getItem(1222L));
//    assertNull(controlTagMapper.getItem(1223L));
//    assertNull(controlTagMapper.getItem(1224L));
    // alivetimer & commfault
    assertFalse(aliveTimerCache.containsKey(1224L));
    assertFalse(commFaultTagCache.containsKey(1223L));
    // alarms
    assertFalse(alarmCache.containsKey(350000L));
    assertNull(alarmMapper.getItem(350000L));
    assertFalse(alarmCache.containsKey(350001L));
    assertNull(alarmMapper.getItem(350001L));

    verify(mockManager);
  }
}

/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.test;

import java.sql.Timestamp;

import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.command.CommandTagCacheObject;
import cern.c2mon.server.common.control.ControlTagCacheObject;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import cern.c2mon.server.common.metadata.Metadata;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.common.process.ProcessCacheObject.LocalConfig;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
import cern.c2mon.shared.client.alarm.condition.AlarmCondition;
import cern.c2mon.shared.client.command.RbacAuthorizationDetails;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.command.CommandExecutionDetails;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.address.impl.OPCHardwareAddressImpl;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;

/**
 * Contains static methods for creating test cache objects.
 *
 * @author Mark Brightwell
 *
 */
public final class CacheObjectCreation {

  /**
   * Hide constructor.
   */
  private CacheObjectCreation() { };

  /**
   * Does not set reference to tag id.
   * @return the alamr
   */
  public static AlarmCacheObject createTestAlarm1() {
    AlarmCacheObject alarm1 = new AlarmCacheObject();
    alarm1.setId(Long.valueOf(1));
    alarm1.setFaultFamily("fault family");
    alarm1.setFaultMember("fault member");
    alarm1.setFaultCode(0);
    AlarmCondition condition = AlarmCondition.fromConfigXML("<AlarmCondition class=\"cern.c2mon.server.common.alarm.ValueAlarmCondition\">"
        + "<alarm-value type=\"String\">DOWN</alarm-value></AlarmCondition>");
    alarm1.setCondition(condition);
    alarm1.setInfo("alarm info");
    alarm1.setActive(false);
    alarm1.setInternalActive(false);
    alarm1.setTimestamp(new Timestamp(System.currentTimeMillis() - 2000));
    alarm1.setDataTagId(100003L);
    return alarm1;
  }

  /**
   * Does not set reference to tag id.
   * @return the alamr
   */
  public static AlarmCacheObject createTestAlarm3() {
    AlarmCacheObject alarm3 = new AlarmCacheObject();
    alarm3.setId(Long.valueOf(3));
    alarm3.setFaultFamily("fault family");
    alarm3.setFaultMember("fault member");
    alarm3.setFaultCode(0);
    AlarmCondition condition = AlarmCondition.fromConfigXML("<AlarmCondition class=\"cern.c2mon.server.common.alarm.ValueAlarmCondition\">"
        + "<alarm-value type=\"String\">DOWN</alarm-value></AlarmCondition>");
    alarm3.setCondition(condition);
    alarm3.setInfo("alarm info");
    alarm3.setActive(false);
    alarm3.setInternalActive(false);
    alarm3.setTimestamp(new Timestamp(System.currentTimeMillis() - 2000));
    alarm3.setDataTagId(100003L);
    Metadata metadata = new Metadata();
    metadata.addMetadata("testMetadata",11);
    alarm3.setMetadata(metadata);
    return alarm3;
  }

  /**
   * Does not set reference to tag id.
   * Created alarm has not been published to external alarm system (i.e. publication field is null)
   * @return the alarm
   */
  public static AlarmCacheObject createTestAlarm2() {
    AlarmCacheObject alarm1 = new AlarmCacheObject();
    alarm1.setId(Long.valueOf(2));
    alarm1.setFaultFamily("fault family 2");
    alarm1.setFaultMember("fault member 2");
    alarm1.setFaultCode(2);
    AlarmCondition condition = AlarmCondition.fromConfigXML("<AlarmCondition class=\"cern.c2mon.server.common.alarm.ValueAlarmCondition\">"
        + "<alarm-value type=\"Boolean\">false</alarm-value></AlarmCondition>");
    alarm1.setCondition(condition);
    alarm1.setInfo("alarm info");
    alarm1.setActive(true);
    alarm1.setInternalActive(true);
    alarm1.setTimestamp(new Timestamp(System.currentTimeMillis() - 2000));
    return alarm1;
  }

  /**
   * Constructs a test ControlTag.
   * @return the ControlTag
   */
  public static ControlTagCacheObject createTestControlTag() {
    ControlTagCacheObject cacheObject = new ControlTagCacheObject(Long.valueOf(1001),
                                                                  "Junit_test_tag",
                                                                  "Float",
                                                                  DataTagConstants.MODE_TEST);
    cacheObject.setId(Long.valueOf(1001));  //must be non null in DB
    cacheObject.setName("Junit_test_tag"); //non null
    cacheObject.setDescription("test description");
    cacheObject.setMode(DataTagConstants.MODE_TEST); //non null
    cacheObject.setDataType("Float"); // non null
    cacheObject.setLogged(false); //null allowed
    cacheObject.setUnit("test unit m/sec");
    cacheObject.setDipAddress("testDIPaddress");
    cacheObject.setJapcAddress("testJAPCaddress");
    cacheObject.setValue(new Float(1000));
    cacheObject.setValueDescription("test value description");
    cacheObject.setSimulated(false); //null allowed
    cacheObject.setEquipmentId(Long.valueOf(150)); //need test equipment inserted
    cacheObject.setMinValue(new Float(100));
    cacheObject.setMaxValue(new Float(2000));
    cacheObject.setAddress(new DataTagAddress());
    cacheObject.setDataTagQuality(createValidQuality());
    cacheObject.setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setDaqTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setSourceTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setRuleIdsString(""); //same as setting to null
    return cacheObject;
  }

  /**
   * Need to first insert test equipment using EquipmentMapper
   * @return the DataTag
   */
  public static DataTagCacheObject createTestDataTag() {
    //construct fake DataTagCacheObject, setting all fields
    DataTagCacheObject cacheObject = new DataTagCacheObject();
    cacheObject.setId(Long.valueOf(100000));  //must be non null in DB
    cacheObject.setName("Junit_test_datatag1"); //non null
    cacheObject.setDescription("test description");
    cacheObject.setMode(DataTagConstants.MODE_TEST); //non null
    cacheObject.setDataType("Boolean"); // non null
    //cacheObject.setTopic("tim.testdatatag.XADDRESS");
    cacheObject.setLogged(false); //null allowed
    cacheObject.setUnit("test unit m/sec");
    cacheObject.setDipAddress("testDIPaddress");
    cacheObject.setJapcAddress("testJAPCaddress");
    cacheObject.setValue(Boolean.TRUE);
    cacheObject.setValueDescription("test value description");
    cacheObject.setSimulated(false); //null allowed
    cacheObject.setEquipmentId(Long.valueOf(100)); //need test equipment inserted
    cacheObject.setMinValue(new Float(23.3));
    cacheObject.setMaxValue(new Float(12.2));
    cacheObject.setAddress(new DataTagAddress());
    cacheObject.setDataTagQuality(createValidQuality());
    cacheObject.setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setDaqTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setSourceTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setRuleIdsString("130");
    return cacheObject;
  }

  /**
   * Constructs second DataTag.
   * @return the DataTag.
   */
  public static DataTagCacheObject createTestDataTag2() {
    DataTagCacheObject cacheObject = new DataTagCacheObject();
    cacheObject.setId(Long.valueOf(100001));  //must be non null in DB
    cacheObject.setName("Junit_test_datatag2"); //non null
    cacheObject.setDescription("test description");
    cacheObject.setMode(DataTagConstants.MODE_TEST); //non null
    cacheObject.setDataType("Boolean"); // non null
    cacheObject.setLogged(false); //null allowed
    cacheObject.setUnit("test unit m/sec");
    cacheObject.setDipAddress("testDIPaddress");
    cacheObject.setJapcAddress("testJAPCaddress");
    cacheObject.setValue(Boolean.TRUE);
    cacheObject.setValueDescription("test value description");
    cacheObject.setSimulated(false); //null allowed
    cacheObject.setEquipmentId(Long.valueOf(100)); //need test equipment inserted - using JAPC currently
    cacheObject.setMinValue(new Float(23.3));
    cacheObject.setMaxValue(new Float(12.2));
    cacheObject.setAddress(new DataTagAddress());
    cacheObject.setDataTagQuality(createValidQuality());
    cacheObject.setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setDaqTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setSourceTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setRuleIdsString("130");
    return cacheObject;
  }

  /**
   * Need to first insert test equipment using EquipmentMapper
   * @return the DataTag
   */
  public static DataTagCacheObject createTestDataTag3() {
    //construct fake DataTagCacheObject, setting all fields
    DataTagCacheObject cacheObject = new DataTagCacheObject();
    cacheObject.setId(Long.valueOf(100003));  //must be non null in DB
    cacheObject.setName("Junit_test_datatag3"); //non null
    cacheObject.setDescription("test description");
    cacheObject.setMode(DataTagConstants.MODE_TEST); //non null
    cacheObject.setDataType("String"); // non null
    //cacheObject.setTopic("tim.testdatatag.XADDRESS");
    cacheObject.setLogged(false); //null allowed
    cacheObject.setUnit("test unit");
    cacheObject.setDipAddress("testDIPaddress");
    cacheObject.setJapcAddress("testJAPCaddress");
    cacheObject.setValue("DOWN");
    cacheObject.setValueDescription("test value description");
    cacheObject.setSimulated(false); //null allowed
    cacheObject.setEquipmentId(Long.valueOf(100)); //need test equipment inserted
    cacheObject.setMinValue(new Float(23.3));
    cacheObject.setMaxValue(new Float(12.2));
    cacheObject.setAddress(new DataTagAddress());
    cacheObject.setDataTagQuality(createValidQuality());
    cacheObject.setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setDaqTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setSourceTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.getAlarmIds().add(1L);
    cacheObject.getAlarmIds().add(3L);
    return cacheObject;
  }


  /**
   * Creates test equipment (uses JECTEST03 foreign keys).
   * @return the Equipment
   */
  public static EquipmentCacheObject createTestEquipment() {
    EquipmentCacheObject equipmentCacheObject = new EquipmentCacheObject(
                                                                          Long.valueOf(100),
                                                                          "Test Equipment",
                                                                          "Test desc",
                                                                          "Test class name",
                                                                          "Test address",
                                                                          Long.valueOf(1222),
                                                                          Long.valueOf(5000200),
                                                                          10, //alive interval
                                                                          Long.valueOf(1223),
                                                                          Long.valueOf(90)
                                                                          );
    equipmentCacheObject.setStatusDescription("Status description");
    equipmentCacheObject.setStatusTime(new Timestamp(System.currentTimeMillis()));
    equipmentCacheObject.setSupervisionStatus(SupervisionStatus.DOWN);
    return equipmentCacheObject;
  }

  /**
   * Creates a test SubEquipment of the created Equipment.
   * Again uses tags from JECTEST03.
   * @return the SubEquipment
   */
  public static SubEquipmentCacheObject createTestSubEquipment() {
    SubEquipmentCacheObject subEquipmentCacheObject = new SubEquipmentCacheObject(
                                                                        Long.valueOf(101),
                                                                        "Test SubEquipment",
                                                                        "Test desc",
                                                                        "Test class name",
                                                                        Long.valueOf(1222), //keep same as parent eq (not correct configuration, only for testing)
                                                                        Long.valueOf(5000300), //keep same as parent eq
                                                                        10,
                                                                        Long.valueOf(1223),
                                                                        Long.valueOf(100)
                                                                        );
    subEquipmentCacheObject.setStatusDescription("Status description");
    subEquipmentCacheObject.setStatusTime(new Timestamp(System.currentTimeMillis()));
    subEquipmentCacheObject.setSupervisionStatus(SupervisionStatus.DOWN);
    return subEquipmentCacheObject;
  }

  /**
   * Creates a second test SubEquipment of the created Equipment.
   * Again uses tags from JECTEST03.
   * @return the SubEquipment
   */
  public static SubEquipmentCacheObject createTestSubEquipment2() {
    SubEquipmentCacheObject subEquipmentCacheObject = new SubEquipmentCacheObject(
                                                                        Long.valueOf(102),
                                                                        "Test SubEquipment 2",
                                                                        "Test desc 2",
                                                                        "Test class name 2",
                                                                        Long.valueOf(1222), //keep same as parent eq (not correct configuration, only for testing)
                                                                        Long.valueOf(5000300), //keep same as parent eq
                                                                        10,
                                                                        Long.valueOf(1223),
                                                                        Long.valueOf(100)
                                                                        );
    subEquipmentCacheObject.setStatusDescription("Status description");
    subEquipmentCacheObject.setStatusTime(new Timestamp(System.currentTimeMillis()));
    subEquipmentCacheObject.setSupervisionStatus(SupervisionStatus.DOWN);
    return subEquipmentCacheObject;
  }

  /**
   * Uses JECTEST01 alive id and state id (for FK constraints).
   * @return the Process
   */
  public static ProcessCacheObject createTestProcess1() {
    ProcessCacheObject processCacheObject = new ProcessCacheObject(Long.valueOf(90), "Test Process", Long.valueOf(1200), 100, 100);
    processCacheObject.setDescription("Test process description");
    processCacheObject.setAliveInterval(60);
    processCacheObject.setAliveTagId(Long.valueOf(510)); //FK ref
    processCacheObject.setStateTagId(510L);
    processCacheObject.setSupervisionStatus(SupervisionStatus.DOWN);
    processCacheObject.setStatusTime(new Timestamp(System.currentTimeMillis()));
    processCacheObject.setStartupTime(new Timestamp(0));
    processCacheObject.setStatusDescription("Status description");
    processCacheObject.setCurrentHost("test host");
    processCacheObject.setRequiresReboot(false);
    processCacheObject.setProcessPIK(12345L);
    processCacheObject.setLocalConfig(LocalConfig.Y);

    return processCacheObject;
  }

  /**
   * Returns a test rule tag object
   * @return the RuleTag
   */
  public static RuleTagCacheObject createTestRuleTag() {
    RuleTagCacheObject cacheObject =
        new RuleTagCacheObject(Long.valueOf(130L),
          "Junit_test_tag",
          "Integer",
          DataTagConstants.MODE_TEST,
          "(#100000 = true)&(#100001 = true)[2],true[3]"); //rule text set here - only extra field on top of abstract class
    cacheObject.setId(Long.valueOf(130L));  //must be non null in DB
    cacheObject.setName("Junit_test_rule_tag"); //non null
    cacheObject.setDescription("test rule description");
    cacheObject.setMode(DataTagConstants.MODE_TEST); //non null
    cacheObject.setDataType("Integer"); // non null
    //cacheObject.setTopic(System.getProperty("c2mon.jms.tag.publication.topic")); //topic set to default in constructor
    cacheObject.setLogged(false); //null allowed
    cacheObject.setUnit("test unit m/sec");
    cacheObject.setDipAddress("testDIPaddress");
    cacheObject.setJapcAddress("testJAPCaddress");
    cacheObject.setValue(Integer.valueOf(1000));
    cacheObject.setValueDescription("test value description");
    cacheObject.setSimulated(false); //null allowed
    cacheObject.setDataTagQuality(createValidQuality());
    cacheObject.setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setRuleIdsString("");
    return cacheObject;
  }

  /**
   * Creates an AliveTimer for a Process
   * @return the ControlTag
   */
  public static ControlTagCacheObject createTestProcessAlive() {
    ControlTagCacheObject cacheObject = new ControlTagCacheObject(Long.valueOf(510),
                                                                  "Test process alive tag",
                                                                  "Long",
                                                                  DataTagConstants.MODE_TEST);
    //cacheObject.setId(Long.valueOf(5000100));  //must be non null in DB
    //cacheObject.setName("Test process alive tag"); //non null
    cacheObject.setDescription("test alive description");
    //cacheObject.setMode(DataTagConstants.MODE_TEST); //non null
    //cacheObject.setDataType("Long"); // non null
    //cacheObject.setTopic("tim.testdatatag.XADDRESS");
    cacheObject.setLogged(false); //null allowed
    cacheObject.setUnit("seconds since 1970");
    cacheObject.setDipAddress("testDIPaddress");
    cacheObject.setJapcAddress("testJAPCaddress");
    cacheObject.setValue(Long.valueOf(System.currentTimeMillis()));
    cacheObject.setValueDescription("test value description");
    cacheObject.setSimulated(false); //null allowed
    //cacheObject.setEquipmentId(Long.valueOf(300000)); //null for alive tags!
    cacheObject.setMinValue(Long.MIN_VALUE);
    cacheObject.setMaxValue(Long.MAX_VALUE);
    cacheObject.setAddress(new DataTagAddress());
    cacheObject.setDataTagQuality(createValidQuality());
    cacheObject.setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setDaqTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setSourceTimestamp(new Timestamp(System.currentTimeMillis()));
    return cacheObject;
  }

  /**
   * Creates a an AliveTimer for the Equipment.
   * @return the ControlTag
   */
  public static ControlTagCacheObject createTestEquipmentAlive() {
    ControlTagCacheObject cacheObject = new ControlTagCacheObject(Long.valueOf(5000200),
                                                                  "Test equipment alive tag",
                                                                  "Long",
                                                                  DataTagConstants.MODE_TEST);
    //cacheObject.setId(Long.valueOf(5000200));  //must be non null in DB
    //cacheObject.setName("Test equipment alive tag"); //non null
    cacheObject.setDescription("test alive description");
    //cacheObject.setMode(DataTagConstants.MODE_TEST); //non null
    //cacheObject.setDataType("Long"); // non null
    //cacheObject.setTopic("tim.testdatatag.XADDRESS");
    cacheObject.setLogged(false); //null allowed
    cacheObject.setUnit("seconds since 1970");
    cacheObject.setDipAddress("testDIPaddress");
    cacheObject.setJapcAddress("testJAPCaddress");
    cacheObject.setValue(Long.valueOf(System.currentTimeMillis()));
    cacheObject.setValueDescription("test value description");
    cacheObject.setSimulated(false); //null allowed
    //cacheObject.setEquipmentId(Long.valueOf(300000)); //null for alive tags!
    cacheObject.setMinValue(Long.MIN_VALUE);
    cacheObject.setMaxValue(Long.MAX_VALUE);
    cacheObject.setAddress(new DataTagAddress());
    cacheObject.setDataTagQuality(createValidQuality());
    cacheObject.setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setDaqTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setSourceTimestamp(new Timestamp(System.currentTimeMillis()));
    return cacheObject;
  }

  /**
   * Creates a test AliveTimer for a SubEquipment.
   * @return the ControlTag
   */
  public static ControlTagCacheObject createTestSubEquipmentAlive() {
    ControlTagCacheObject cacheObject = new ControlTagCacheObject(Long.valueOf(5000300),
                                                                  "Test subequipment alive tag",
                                                                  "Long",
                                                                  DataTagConstants.MODE_TEST);
    //cacheObject.setId(Long.valueOf(5000300));  //must be non null in DB
    //cacheObject.setName("Test subequipment alive tag"); //non null
    cacheObject.setDescription("test alive description");
    //cacheObject.setMode(DataTagConstants.MODE_TEST); //non null
    //cacheObject.setDataType("Long"); // non null
    //cacheObject.setTopic("tim.testdatatag.XADDRESS");
    cacheObject.setLogged(false); //null allowed
    cacheObject.setUnit("seconds since 1970");
    cacheObject.setDipAddress("testDIPaddress");
    cacheObject.setJapcAddress("testJAPCaddress");
    cacheObject.setValue(Long.valueOf(System.currentTimeMillis()));
    cacheObject.setValueDescription("test value description");
    cacheObject.setSimulated(false); //null allowed
    //cacheObject.setEquipmentId(Long.valueOf(300000)); //null for alive tags!
    cacheObject.setMinValue(Long.MIN_VALUE);
    cacheObject.setMaxValue(Long.MAX_VALUE);
    cacheObject.setAddress(new DataTagAddress());
    cacheObject.setDataTagQuality(createValidQuality());
    cacheObject.setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setDaqTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setSourceTimestamp(new Timestamp(System.currentTimeMillis()));
    return cacheObject;
  }

  /**
   * Creates a test CommandTag
   * @return the test CommandTag
   */
  public static CommandTagCacheObject createTestCommandTag() {
    CommandTagCacheObject commandTag =
      new CommandTagCacheObject(Long.valueOf(2000), "Test command tag",
                                "Test command tag desc", "Float", DataTagConstants.MODE_OPERATIONAL);
      commandTag.setEquipmentId(Long.valueOf(100));
      commandTag.setMaximum(Float.valueOf(5));
      commandTag.setMinimum(Float.valueOf(1));
      RbacAuthorizationDetails details = new RbacAuthorizationDetails();
      details.setRbacClass("class");
      details.setRbacDevice("device");
      details.setRbacProperty("property");
      commandTag.setAuthorizationDetails(details);
      commandTag.setClientTimeout(10000);
      try {
        commandTag.setHardwareAddress(new OPCHardwareAddressImpl("test"));
      } catch (ConfigurationException e) {
        e.printStackTrace();
      }
      commandTag.setSourceTimeout(10000);
      commandTag.setSourceRetries(4);
      commandTag.setExecTimeout(10000);

      //set process field - usually loaded using join from DB -  here must set to parent of Eq 300000
      commandTag.setProcessId(Long.valueOf(90));

      CommandExecutionDetails<Long> commandExecutionDetails = new CommandExecutionDetails<>();
      commandExecutionDetails.setUsername("john");
      commandExecutionDetails.setHost("host");
      commandExecutionDetails.setExecutionStartTime(new Timestamp(System.currentTimeMillis() - 1000));
      commandExecutionDetails.setExecutionEndTime(new Timestamp(System.currentTimeMillis()));
      commandExecutionDetails.setValue(10L);
      commandTag.setCommandExecutionDetails(commandExecutionDetails);
      return commandTag;
  }

  private static DataTagQuality createValidQuality() {
    DataTagQuality dataTagQuality = new DataTagQualityImpl();
    dataTagQuality.validate();
    return dataTagQuality;
  }

}

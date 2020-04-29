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
package cern.c2mon.daq.common.impl;

import java.io.File;
import java.io.IOException;

import cern.c2mon.daq.common.timer.FreshnessMonitor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.daq.common.messaging.IProcessMessageSender;
import cern.c2mon.daq.filter.IFilterMessageSender;
import cern.c2mon.daq.filter.dynamic.IDynamicTimeDeadbandFilterActivator;
import cern.c2mon.shared.common.datatag.*;
import cern.c2mon.shared.common.filter.FilteredDataTagValue;
import cern.c2mon.shared.common.process.EquipmentConfiguration;
import cern.c2mon.shared.common.process.ProcessConfiguration;
import cern.c2mon.shared.common.process.SubEquipmentConfiguration;
import cern.c2mon.shared.daq.config.ChangeReport;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class EquipmentMessageSenderTest {

  private static final long EQ_COMFAULT_ID = 1L;
  private static final long EQ_ALIVE_ID = 0L;
  private static final float VALUE_DEADBAND = 25.0f;
  private static final long EQUIPMENT_ID = 1L;
  private static final Long SUB_KEY1 = 1337L;
  private static final Long SUB_KEY2 = 31415926L;

  // Mocks
  private IFilterMessageSender filterMessageSenderMock;
  private IProcessMessageSender processMessageSenderMock;
  private IDynamicTimeDeadbandFilterActivator medDynamicTimeDeadbandFilterActivatorMock;
  private IDynamicTimeDeadbandFilterActivator lowDynamicTimeDeadbandFilterActivatorMock;
  private FreshnessMonitor freshnessMonitorMock;

  private SourceDataTag sdt1;
  private SourceDataTag sdt2;
  private SourceDataTag sdt3;
  private SourceDataTag sdt4;
  private SourceDataTag sdt5;
  private SourceDataTag sdt6;
  private SourceDataTag alive;

  private EquipmentMessageSender equipmentMessageSender;

  @Before
  public void setUp() {
    filterMessageSenderMock = createStrictMock(IFilterMessageSender.class);
    processMessageSenderMock = createStrictMock(IProcessMessageSender.class);
    medDynamicTimeDeadbandFilterActivatorMock = createStrictMock(IDynamicTimeDeadbandFilterActivator.class);
    lowDynamicTimeDeadbandFilterActivatorMock = createStrictMock(IDynamicTimeDeadbandFilterActivator.class);
    freshnessMonitorMock = createMock(FreshnessMonitor.class);
    equipmentMessageSender = new EquipmentMessageSender(filterMessageSenderMock, processMessageSenderMock, medDynamicTimeDeadbandFilterActivatorMock,
        lowDynamicTimeDeadbandFilterActivatorMock, freshnessMonitorMock);

    freshnessMonitorMock.setIEquipmentMessageSender(equipmentMessageSender);
    EasyMock.expectLastCall();

    EquipmentConfiguration equipmentConfiguration = new EquipmentConfiguration();
    equipmentConfiguration.setId(EQUIPMENT_ID);
    equipmentConfiguration.setCommFaultTagId(EQ_COMFAULT_ID);
    equipmentConfiguration.setCommFaultTagValue(false);
    equipmentConfiguration.setName("EQ");

    sdt1 = createSourceDataTag(1L, "sdt1", "Boolean", DataTagDeadband.DEADBAND_NONE, DataTagConstants.PRIORITY_LOW, false);
    sdt2 = createSourceDataTag(2L, "sdt2", "Float", DataTagDeadband.DEADBAND_PROCESS_ABSOLUTE, DataTagConstants.PRIORITY_MEDIUM, false);
    sdt3 = createSourceDataTag(3L, "sdt3", "Integer", DataTagDeadband.DEADBAND_PROCESS_RELATIVE_VALUE_DESCR_CHANGE, DataTagConstants.PRIORITY_LOW, false);
    sdt4 = createSourceDataTag(4L, "sdt4", Integer[].class.getName(), DataTagDeadband.DEADBAND_NONE, DataTagConstants.PRIORITY_LOW, false);
    sdt5 = createSourceDataTag(5L, "sdt5", ArbitraryObject.class.getName(), DataTagDeadband.DEADBAND_NONE, DataTagConstants.PRIORITY_LOW, false);
    sdt6 = createSourceDataTag(6L, "sdt6", "my.random.own.MyObject", DataTagDeadband.DEADBAND_NONE, DataTagConstants.PRIORITY_LOW, false);

    alive = createSourceDataTag(EQ_ALIVE_ID, "eqalive", "String", DataTagDeadband.DEADBAND_NONE, DataTagConstants.PRIORITY_HIGH, false);

    equipmentConfiguration.getDataTags().put(1L, sdt1);
    equipmentConfiguration.getDataTags().put(2L, sdt2);
    equipmentConfiguration.getDataTags().put(3L, sdt3);
    equipmentConfiguration.getDataTags().put(4L, sdt4);
    equipmentConfiguration.getDataTags().put(5L, sdt5);
    equipmentConfiguration.getDataTags().put(6L, sdt6);
    equipmentConfiguration.getDataTags().put(0L, alive);

    equipmentConfiguration.getSubEquipmentConfigurations().put(10L, new SubEquipmentConfiguration(10L, "SUBEQ.1", SUB_KEY1, false));
    equipmentConfiguration.getSubEquipmentConfigurations().put(11L, new SubEquipmentConfiguration(11L, "SUBEQ.2", SUB_KEY2, true));

    // equipmentMessageSender.setEquipmentConfiguration(equipmentConfiguration);
    ProcessConfiguration processConf = new ProcessConfiguration();
    processConf.setProcessID(1L);
    processConf.setProcessName("ad");
    equipmentConfiguration.setHandlerClassName("asd");
    // equipmentMessageSender.setEquipmentLoggerFactory(EquipmentLoggerFactory.createFactory(equipmentConfiguration,
    // processConf, new RunOptions()));

    this.equipmentMessageSender.init(equipmentConfiguration);

    // Setup calls should not affect later tests
    reset(lowDynamicTimeDeadbandFilterActivatorMock, medDynamicTimeDeadbandFilterActivatorMock, filterMessageSenderMock, processMessageSenderMock);
  }

  @Test
  public void testEquipmentStateIncorrect() {
    processMessageSenderMock.sendCommfaultTag(EQ_COMFAULT_ID, "EQ:COMM_FAULT", false, null);
    processMessageSenderMock.sendCommfaultTag(SUB_KEY1, "SUBEQ.1:COMM_FAULT", false, null);
    processMessageSenderMock.sendCommfaultTag(SUB_KEY2, "SUBEQ.2:COMM_FAULT", true, null);

    replay(processMessageSenderMock);

    equipmentMessageSender.confirmEquipmentStateIncorrect();

    verify(processMessageSenderMock);
  }

  @Test
  public void testEquipmentStateIncorrectDescription() {
    String description = "description";
    processMessageSenderMock.sendCommfaultTag(EQ_COMFAULT_ID, "EQ:COMM_FAULT", false, description);
    processMessageSenderMock.sendCommfaultTag(SUB_KEY1, "SUBEQ.1:COMM_FAULT", false, description);
    processMessageSenderMock.sendCommfaultTag(SUB_KEY2, "SUBEQ.2:COMM_FAULT", true, description);

    replay(processMessageSenderMock);

    equipmentMessageSender.confirmEquipmentStateIncorrect(description);

    verify(processMessageSenderMock);
  }

  @Test
  public void testEquipmentStateOK() {
    processMessageSenderMock.sendCommfaultTag(EQ_COMFAULT_ID, "EQ:COMM_FAULT", true, null);
    processMessageSenderMock.sendCommfaultTag(SUB_KEY1, "SUBEQ.1:COMM_FAULT", true, null);
    processMessageSenderMock.sendCommfaultTag(SUB_KEY2, "SUBEQ.2:COMM_FAULT", false, null);

    replay(processMessageSenderMock);

    equipmentMessageSender.confirmEquipmentStateOK();

    verify(processMessageSenderMock);
  }

  @Test
  public void testEquipmentStateOKDescription() {
    String description = "description";
    processMessageSenderMock.sendCommfaultTag(EQ_COMFAULT_ID, "EQ:COMM_FAULT", true, description);
    processMessageSenderMock.sendCommfaultTag(SUB_KEY1, "SUBEQ.1:COMM_FAULT", true, description);
    processMessageSenderMock.sendCommfaultTag(SUB_KEY2, "SUBEQ.2:COMM_FAULT", false, description);

    replay(processMessageSenderMock);

    equipmentMessageSender.confirmEquipmentStateOK(description);

    verify(processMessageSenderMock);
  }

  @Test
  public void testOnAddDataTag() {
    SourceDataTag sdtLow = createSourceDataTag(5324L, "", "Float", DataTagDeadband.DEADBAND_NONE, DataTagConstants.PRIORITY_LOW, false);
    SourceDataTag sdtMed = createSourceDataTag(5325L, "", "Float", DataTagDeadband.DEADBAND_NONE, DataTagConstants.PRIORITY_MEDIUM, false);
    SourceDataTag sdtHigh = createSourceDataTag(5326L, "", "Float", DataTagDeadband.DEADBAND_NONE, DataTagConstants.PRIORITY_HIGH, false);

    lowDynamicTimeDeadbandFilterActivatorMock.addDataTag(sdtLow);
    medDynamicTimeDeadbandFilterActivatorMock.addDataTag(sdtMed);

    replay(lowDynamicTimeDeadbandFilterActivatorMock, medDynamicTimeDeadbandFilterActivatorMock);

    equipmentMessageSender.onAddDataTag(sdtHigh, new ChangeReport(1L));
    equipmentMessageSender.onAddDataTag(sdtLow, new ChangeReport(2L));
    equipmentMessageSender.onAddDataTag(sdtMed, new ChangeReport(3L));

    verify(lowDynamicTimeDeadbandFilterActivatorMock, medDynamicTimeDeadbandFilterActivatorMock);
  }

  @Test
  public void testOnRemoveDataTag() {
    SourceDataTag sdt1Clone = sdt1.clone();
    SourceDataTag sdt2Clone = sdt2.clone();
    SourceDataTag sdt3Clone = sdt3.clone();
    sdt1.setDataType("Float");
    sdt2.getAddress().setPriority(DataTagConstants.PRIORITY_LOW);
    sdt3.getAddress().setPriority(DataTagConstants.PRIORITY_HIGH);

    /*
     * Update should try to remove sdt2 from any other activators and add it to
     * the low one.
     */
    lowDynamicTimeDeadbandFilterActivatorMock.removeDataTag(sdt2);
    medDynamicTimeDeadbandFilterActivatorMock.removeDataTag(sdt2);
    lowDynamicTimeDeadbandFilterActivatorMock.addDataTag(sdt2);

    /*
     * Update should try to remove sdt3 from any other activators and add it to
     * the low one.
     */
    lowDynamicTimeDeadbandFilterActivatorMock.removeDataTag(sdt3);
    medDynamicTimeDeadbandFilterActivatorMock.removeDataTag(sdt3);

    replay(lowDynamicTimeDeadbandFilterActivatorMock, medDynamicTimeDeadbandFilterActivatorMock);

    equipmentMessageSender.onUpdateDataTag(sdt1, sdt1Clone, new ChangeReport(1L));
    equipmentMessageSender.onUpdateDataTag(sdt2, sdt2Clone, new ChangeReport(1L));
    equipmentMessageSender.onUpdateDataTag(sdt3, sdt3Clone, new ChangeReport(1L));

    verify(lowDynamicTimeDeadbandFilterActivatorMock, medDynamicTimeDeadbandFilterActivatorMock);
  }

  @Test
  public void testOnUpdateDataTag() {
    lowDynamicTimeDeadbandFilterActivatorMock.removeDataTag(sdt1);
    medDynamicTimeDeadbandFilterActivatorMock.removeDataTag(sdt1);

    replay(lowDynamicTimeDeadbandFilterActivatorMock, medDynamicTimeDeadbandFilterActivatorMock);

    equipmentMessageSender.onRemoveDataTag(sdt1, new ChangeReport(1L));

    verify(lowDynamicTimeDeadbandFilterActivatorMock, medDynamicTimeDeadbandFilterActivatorMock);
  }

  @Test
  public void testSendInvalidTag() throws InterruptedException {
    lowDynamicTimeDeadbandFilterActivatorMock.newTagValueSent(sdt1.getId());
    processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    filterMessageSenderMock.addValue(isA(FilteredDataTagValue.class));

    replay(lowDynamicTimeDeadbandFilterActivatorMock, processMessageSenderMock, filterMessageSenderMock);

    SourceDataTagQuality quality = new SourceDataTagQuality(SourceDataTagQualityCode.DATA_UNAVAILABLE);

    // The first one has null currentSDValue and should not be filter
    this.equipmentMessageSender.update(sdt1.getId(), quality, System.currentTimeMillis() + 1L);
    // The second should go to filter module because the it has same
    // currentSDvalue and newValue (null)
    // and same Quality
    this.equipmentMessageSender.update(sdt1.getId(), quality, System.currentTimeMillis() + 2L);

    verify(lowDynamicTimeDeadbandFilterActivatorMock, processMessageSenderMock);
  }

  @Test
  public void testSendValidTagTimeDeadbandEnabled() throws Exception {
    // 3 of the values will be recorded and the other one send to the filter
    lowDynamicTimeDeadbandFilterActivatorMock.newTagValueSent(sdt1.getId());
    expectLastCall().times(3);

    processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    expectLastCall().times(3);
    filterMessageSenderMock.addValue(isA(FilteredDataTagValue.class));
    expectLastCall().times(1);

    // Lets figure out the Time Deadband is enabled (in this case would be the
    // Dynamic)
    this.sdt1.getAddress().setTimeDeadband(1);
    this.sdt1.update(new ValueUpdate(true));

    replay(this.lowDynamicTimeDeadbandFilterActivatorMock, this.processMessageSenderMock, this.filterMessageSenderMock);

    // The first one: the run method sends it to the server with NO_FILTERING
    // (first time running the schedule)
    this.equipmentMessageSender.update(sdt1.getId(), new ValueUpdate(false, "test", System.currentTimeMillis() + 1L));
    assertEquals(false, this.sdt1.getCurrentValue().getValue());
    Thread.sleep(300);

    // The second one is also sent to the server since the value is different
    this.equipmentMessageSender.update(sdt1.getId(), new ValueUpdate(true, "test", System.currentTimeMillis() + 2L));
    assertEquals(true, this.sdt1.getCurrentValue().getValue());
    Thread.sleep(300);

    // The third one is filtered with REPEATED_VALUE because the value and value
    // description are the same
    this.equipmentMessageSender.update(sdt1.getId(), new ValueUpdate(true, "test", System.currentTimeMillis() + 3L));
    assertEquals(true, this.sdt1.getCurrentValue().getValue());
    Thread.sleep(300);

    // The fourth one is also sent to the server since the value is different
    this.equipmentMessageSender.update(sdt1.getId(), new ValueUpdate(false, System.currentTimeMillis() + 4L));
    assertEquals(false, this.sdt1.getCurrentValue().getValue());
    Thread.sleep(300);

    verify(this.lowDynamicTimeDeadbandFilterActivatorMock, this.processMessageSenderMock, this.filterMessageSenderMock);
  }

  @Test
  public void testSendValidInvalidTagTimeDeadbandEnabled() throws Exception {
    lowDynamicTimeDeadbandFilterActivatorMock.newTagValueSent(sdt1.getId());
    expectLastCall().times(3);
    processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    expectLastCall().times(3);

    this.sdt1.getAddress().setTimeDeadband(1);
    this.sdt1.update(new ValueUpdate(true));

    replay(this.lowDynamicTimeDeadbandFilterActivatorMock, this.processMessageSenderMock);

    // The first one: the run method sends it to the server with NO_FILTERING
    // (first time running the schedule)
    this.equipmentMessageSender.update(sdt1.getId(), new ValueUpdate(false, System.currentTimeMillis() + 1L));
    Thread.sleep(300);
    assertEquals(false, this.sdt1.getCurrentValue().getValue());

    // The second one is also sent to the server since the value is different
    this.equipmentMessageSender.update(sdt1.getId(), new ValueUpdate(true, System.currentTimeMillis() + 2L));
    Thread.sleep(300);
    assertEquals(true, this.sdt1.getCurrentValue().getValue());

    SourceDataTagQuality quality = new SourceDataTagQuality(SourceDataTagQualityCode.DATA_UNAVAILABLE);

    // The third one is invalid so it flush and cancel. It run the run() for the
    // first time so the second value is sent to the server
    this.equipmentMessageSender.update(sdt1.getId(), quality, System.currentTimeMillis() + 3L);
    Thread.sleep(300);
    assertEquals(SourceDataTagQualityCode.DATA_UNAVAILABLE, this.sdt1.getCurrentValue().getQuality().getQualityCode());

    verify(this.lowDynamicTimeDeadbandFilterActivatorMock, this.processMessageSenderMock);
  }

  @Test
  public void testSendValidInvalidValidTagTimeDeadbandEnabled() throws Exception {
    lowDynamicTimeDeadbandFilterActivatorMock.newTagValueSent(sdt1.getId());
    expectLastCall().times(4);
    processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    expectLastCall().times(4);

    this.sdt1.getAddress().setTimeDeadband(1);
    this.sdt1.update(new ValueUpdate(true));

    replay(this.lowDynamicTimeDeadbandFilterActivatorMock, this.processMessageSenderMock);

    // The first one: the run method sends it to the server with NO_FILTERING
    // (first time running the schedule)
    this.equipmentMessageSender.update(sdt1.getId(), new ValueUpdate(false, System.currentTimeMillis() + 1L));
    Thread.sleep(200);
    assertEquals(false, this.sdt1.getCurrentValue().getValue());

    // The second one is also sent to the server since the value is different
    this.equipmentMessageSender.update(sdt1.getId(), new ValueUpdate(true, System.currentTimeMillis() + 2L));
    Thread.sleep(200);
    assertEquals(true, this.sdt1.getCurrentValue().getValue());

    SourceDataTagQuality quality = new SourceDataTagQuality(SourceDataTagQualityCode.DATA_UNAVAILABLE);

    // The third one is invalid so it flush and cancel. It run the run() for the
    // first time so the second value is sent to the server
    this.equipmentMessageSender.update(sdt1.getId(), quality, System.currentTimeMillis() + 3L);
    Thread.sleep(200);
    assertEquals(SourceDataTagQualityCode.DATA_UNAVAILABLE, this.sdt1.getCurrentValue().getQuality().getQualityCode());

    // The forth one is also sent to the server since the value is different
    // (flush and cancel again since the last one was invalid)
    this.equipmentMessageSender.update(sdt1.getId(), new ValueUpdate(true, System.currentTimeMillis() + 4L));
    Thread.sleep(200);
    assertEquals(true, this.sdt1.getCurrentValue().getValue());
    assertEquals(SourceDataTagQualityCode.OK, this.sdt1.getCurrentValue().getQuality().getQualityCode());

    verify(this.lowDynamicTimeDeadbandFilterActivatorMock, this.processMessageSenderMock);
  }

  @Test
  public void testSendInvalidTimeDeadbandEnabledDisable() throws Exception {
    lowDynamicTimeDeadbandFilterActivatorMock.newTagValueSent(sdt1.getId());
    expectLastCall().times(2);
    processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    expectLastCall().times(2);

    this.sdt1.getAddress().setTimeDeadband(1);
    this.sdt1.update(new ValueUpdate(true));

    replay(this.lowDynamicTimeDeadbandFilterActivatorMock, this.processMessageSenderMock);

    SourceDataTagQuality quality = new SourceDataTagQuality(SourceDataTagQualityCode.DATA_UNAVAILABLE);

    // The first one: the run method sends it to the server with NO_FILTERING
    // (first time running the schedule)
    this.equipmentMessageSender.update(sdt1.getId(), quality, System.currentTimeMillis() + 1L);
    Thread.sleep(500);
    assertEquals(SourceDataTagQualityCode.DATA_UNAVAILABLE, this.sdt1.getCurrentValue().getQuality().getQualityCode());

    // Time dead band disable
    this.sdt1.getAddress().setTimeDeadband(0);

    // The second one is also sent to the server since all the checks are done
    // and there is no Time Deadband
    this.equipmentMessageSender.update(sdt1.getId(), new SourceDataTagQuality(SourceDataTagQualityCode.UNKNOWN), System.currentTimeMillis() + 2L);
    Thread.sleep(300);
    assertEquals(SourceDataTagQualityCode.UNKNOWN, this.sdt1.getCurrentValue().getQuality().getQualityCode());

    verify(this.lowDynamicTimeDeadbandFilterActivatorMock, this.processMessageSenderMock);
  }

  @Test
  public void testSendValidTimeDeadbandEnabledDisable() throws Exception {
    lowDynamicTimeDeadbandFilterActivatorMock.newTagValueSent(sdt1.getId());
    expectLastCall().times(2);
    processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    expectLastCall().times(2);

    this.sdt1.getAddress().setTimeDeadband(1);
    this.sdt1.update(new ValueUpdate(true));

    replay(this.lowDynamicTimeDeadbandFilterActivatorMock, this.processMessageSenderMock);

    // The first one: the run method sends it to the server with NO_FILTERING
    // (first time running the schedule)
    this.equipmentMessageSender.update(sdt1.getId(), new ValueUpdate(false, System.currentTimeMillis() + 1L));
    Thread.sleep(200);
    assertEquals(false, this.sdt1.getCurrentValue().getValue());

    // Time dead band disable
    this.sdt1.getAddress().setTimeDeadband(0);

    // The second one is also sent to the server since all the checks are done
    // and there is no Time Deadband
    this.equipmentMessageSender.update(sdt1.getId(), new ValueUpdate(true, System.currentTimeMillis() + 2L));
    Thread.sleep(200);
    assertEquals(true, this.sdt1.getCurrentValue().getValue());

    verify(this.lowDynamicTimeDeadbandFilterActivatorMock, this.processMessageSenderMock);
  }

  @Test
  public void testSendInvalidTagFutureSourceTS() throws InterruptedException {
    // Add value to the SourceDatTag
    this.sdt1.update(new ValueUpdate(false));

    // One value is added
    this.processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    expectLastCall().times(1);
    // One value is filtered out
    this.filterMessageSenderMock.addValue(isA(FilteredDataTagValue.class));
    expectLastCall().times(1);

    replay(this.processMessageSenderMock, this.filterMessageSenderMock);

    SourceDataTagValue sourceDTValue = this.sdt1.getCurrentValue();
    // Get the source data quality from the quality code and description
    SourceDataTagQuality newSDQuality = new SourceDataTagQuality(SourceDataTagQualityCode.FUTURE_SOURCE_TIMESTAMP, sourceDTValue.getQuality().getDescription());

    // It has:
    // - same currentSDValue and new value (false)
    // - same Value Description
    // - different Quality Code (OK vs FUTURE_SOURCE_TIMESTAMP)
    // - same Quality Description
    //
    // Should not be filtered
    this.equipmentMessageSender.update(this.sdt1.getId(), new ValueUpdate(sourceDTValue.getValue(), sourceDTValue.getValueDescription(), System.currentTimeMillis() + 1L), newSDQuality);

    assertEquals(SourceDataTagQualityCode.FUTURE_SOURCE_TIMESTAMP, this.sdt1.getCurrentValue().getQuality().getQualityCode());

    // It has:
    // - same currentSDValue and new value (false)
    // - same Value Description
    // - same Quality Code (FUTURE_SOURCE_TIMESTAMP)
    // - same Quality Description
    //
    // Should be filtered
    this.equipmentMessageSender.update(this.sdt1.getId(), new ValueUpdate(sourceDTValue.getValue(), sourceDTValue.getValueDescription(), System.currentTimeMillis() + 2L), newSDQuality);

    verify(this.processMessageSenderMock, this.filterMessageSenderMock);
  }

  @Test
  public void testSendInvalidTagDifQuality() throws InterruptedException {
    // Add value to the SourceDatTag
    this.sdt1.update(new ValueUpdate(false));

    // One value is added
    this.processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    expectLastCall().times(1);

    replay(this.processMessageSenderMock, this.filterMessageSenderMock);

    SourceDataTagValue sourceDTValue = this.sdt1.getCurrentValue();
    // Get the source data quality from the quality code and description
    SourceDataTagQuality newSDQuality = new SourceDataTagQuality(SourceDataTagQualityCode.DATA_UNAVAILABLE, sourceDTValue.getQuality().getDescription());

    // It has:
    // - same currentSDValue and new value (false)
    // - same Value Description
    // - different Quality Code (OK vs DATA_UNAVAILABLE)
    // - same Quality Description
    //
    // Should not be filtered
    this.equipmentMessageSender.update(this.sdt1.getId(), new ValueUpdate(sourceDTValue.getValue(), sourceDTValue.getValueDescription(), System.currentTimeMillis() + 1L), newSDQuality);

    // The Quality Code has changed
    assertEquals(SourceDataTagQualityCode.DATA_UNAVAILABLE, this.sdt1.getCurrentValue().getQuality().getQualityCode());

    verify(this.processMessageSenderMock, this.filterMessageSenderMock);
  }

  
  /**
   * Note: Value-based deadband filtering is enabled for the process (we use
   * sdt1)
   */
  @Test
  public void testSendInvalidTagDifValue() throws InterruptedException {
    // Add value to the SourceDatTag
    this.sdt1.update(new ValueUpdate(true));

    // One value is added
    this.processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    expectLastCall().times(1);

    replay(this.processMessageSenderMock, this.filterMessageSenderMock);

    SourceDataTagValue sourceDTValue = this.sdt1.getCurrentValue();
    // Get the source data quality from the quality code and description
    SourceDataTagQuality newSDQuality = new SourceDataTagQuality(SourceDataTagQualityCode.DATA_UNAVAILABLE, sourceDTValue.getQuality().getDescription());

    // It has:
    // - different currentSDValue and new value (true vs false)
    // - same Value Description
    // - different Quality Code (OK vs DATA_UNAVAILABLE)
    // - same Quality Description
    //
    // Should not be filtered
    this.equipmentMessageSender.update(this.sdt1.getId(), new ValueUpdate(false, sourceDTValue.getValueDescription(), System.currentTimeMillis() + 1L), newSDQuality);

    // The Value has changed
    assertEquals(false, this.sdt1.getCurrentValue().getValue());
    // The Quality Code has changed
    assertEquals(SourceDataTagQualityCode.DATA_UNAVAILABLE, this.sdt1.getCurrentValue().getQuality().getQualityCode());

    verify(this.processMessageSenderMock, this.filterMessageSenderMock);
  }

  @Test
  public void testSendInvalidTagWithValueConversionFloat2Boolean() throws InterruptedException {
    // Add value to the SourceDatTag
    this.sdt1.update(new ValueUpdate(true));

    // One value is added
    this.processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    expectLastCall().times(1);

    replay(this.processMessageSenderMock, this.filterMessageSenderMock);

    SourceDataTagValue sourceDTValue = this.sdt1.getCurrentValue();
    // Get the source data quality from the quality code and description
    SourceDataTagQuality newSDQuality = new SourceDataTagQuality(SourceDataTagQualityCode.DATA_UNAVAILABLE, sourceDTValue.getQuality().getDescription());

    // It has:
    // - different currentSDValue and new value (true vs false)
    // - same Value Description
    // - different Quality Code (OK vs DATA_UNAVAILABLE)
    // - same Quality Description
    //
    // Should not be filtered
    this.equipmentMessageSender.update(this.sdt1.getId(), new ValueUpdate(Float.valueOf(0f),  sourceDTValue.getValueDescription(), System.currentTimeMillis() + 1L), newSDQuality);

    // The Value has changed
    assertEquals(false, this.sdt1.getCurrentValue().getValue());
    assertTrue(this.sdt1.getCurrentValue().getValue().getClass() == Boolean.class);
    // The Quality Code has changed
    assertEquals(SourceDataTagQualityCode.DATA_UNAVAILABLE, this.sdt1.getCurrentValue().getQuality().getQualityCode());

    verify(this.processMessageSenderMock, this.filterMessageSenderMock);
  }

  /**
   * Note: Value-based deadband filtering is enabled for the process (we use
   * sdt2)
   * @throws InterruptedException 
   */
  @Test
  public void testSendInvalidTagWithValueConversionBoolean2Float() throws InterruptedException {
    // Add value to the SourceDatTag
    this.sdt2.update(new ValueUpdate(Float.valueOf(0f)));

    // One value is added
    this.processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    expectLastCall().times(1);

    replay(this.processMessageSenderMock, this.filterMessageSenderMock);

    SourceDataTagValue sourceDTValue = this.sdt2.getCurrentValue();
    // Get the source data quality from the quality code and description
    SourceDataTagQuality newSDQuality = new SourceDataTagQuality(SourceDataTagQualityCode.DATA_UNAVAILABLE, sourceDTValue.getQuality().getDescription());

    // It has:
    // - different currentSDValue and new value (true vs false)
    // - same Value Description
    // - different Quality Code (OK vs DATA_UNAVAILABLE)
    // - same Quality Description
    //
    // Should not be filtered
    this.equipmentMessageSender.update(this.sdt2.getId(), new ValueUpdate(true, sourceDTValue.getValueDescription(), System.currentTimeMillis() + 1L), newSDQuality);

    // The Value has changed
    assertEquals(Float.valueOf(1f), this.sdt2.getCurrentValue().getValue());
    assertTrue(this.sdt2.getCurrentValue().getValue().getClass() == Float.class);
    // The Quality Code has changed
    assertEquals(SourceDataTagQualityCode.DATA_UNAVAILABLE, this.sdt2.getCurrentValue().getQuality().getQualityCode());

    verify(this.processMessageSenderMock, this.filterMessageSenderMock);
  }

  @Test
  public void testSendInvalidTagWithValueConversionShort2Integer() throws InterruptedException {
    // Add value to the SourceDatTag
    this.sdt3.update(new ValueUpdate(Integer.valueOf(5)));

    // One value is added
    this.processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    expectLastCall().times(1);

    replay(this.processMessageSenderMock, this.filterMessageSenderMock);

    SourceDataTagValue sourceDTValue = this.sdt3.getCurrentValue();
    // Get the source data quality from the quality code and description
    SourceDataTagQuality newSDQuality = new SourceDataTagQuality(SourceDataTagQualityCode.DATA_UNAVAILABLE, sourceDTValue.getQuality().getDescription());

    // It has:
    // - different currentSDValue and new value (true vs false)
    // - same Value Description
    // - different Quality Code (OK vs DATA_UNAVAILABLE)
    // - same Quality Description
    //
    // Should not be filtered
    this.equipmentMessageSender.update(this.sdt3.getId(), new ValueUpdate(Short.valueOf((short) 33), sourceDTValue.getValueDescription(), System.currentTimeMillis() + 1L), newSDQuality);

    // The Value has changed
    assertEquals(Integer.valueOf(33), this.sdt3.getCurrentValue().getValue());
    assertTrue(this.sdt3.getCurrentValue().getValue().getClass() == Integer.class);
    // The Quality Code has changed
    assertEquals(SourceDataTagQualityCode.DATA_UNAVAILABLE, this.sdt3.getCurrentValue().getQuality().getQualityCode());

    verify(this.processMessageSenderMock, this.filterMessageSenderMock);
  }

  @Test
  public void testSendInvalidTagIsValueDeadbandFiltered() throws Exception {
    // Give current values (are null be default)
    this.sdt2.update(new ValueUpdate(1));
    this.sdt3.update(new ValueUpdate(1));

    // 5 values should not be filtered out
    this.processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    expectLastCall().times(5);
    // 5 values should be filtered out
    this.filterMessageSenderMock.addValue(isA(FilteredDataTagValue.class));
    expectLastCall().times(5);

    replay(this.processMessageSenderMock, this.filterMessageSenderMock);

    SourceDataTagValue sourceDTValue = this.sdt3.getCurrentValue();
    // Get the source data quality from the quality code and description
    // - quality code is OK by default and it will never go for invalidation
    // because is a special case. New quality code added
    SourceDataTagQuality newSDQuality = new SourceDataTagQuality(SourceDataTagQualityCode.DATA_UNAVAILABLE, sourceDTValue.getQuality().getDescription());

    // Relative. Should not be filtered

    this.equipmentMessageSender.update(this.sdt3.getId(), new ValueUpdate(109, "value description 1", System.currentTimeMillis() + 2L), newSDQuality);

    // Should not be filtered because value descr. is different from the
    // previous one
    this.equipmentMessageSender.update(this.sdt3.getId(), new ValueUpdate(108, System.currentTimeMillis() + 3L), newSDQuality);

    // Should not be filtered because value descr. is different from the
    // previous one
    this.equipmentMessageSender.update(this.sdt3.getId(), new ValueUpdate(109, "value description 2", System.currentTimeMillis() + 4L), newSDQuality);

    // Should be filtered because value descr. has not change
    this.equipmentMessageSender.update(this.sdt3.getId(), new ValueUpdate(110, "value description 2", System.currentTimeMillis() + 5L), newSDQuality);

    // Should be filtered (is Relative Value Deadband)
    this.equipmentMessageSender.update(this.sdt3.getId(), new ValueUpdate(108, "value description 2", System.currentTimeMillis() + 6L), newSDQuality);

    // Should be filtered (is Relative Value Deadband)
    this.equipmentMessageSender.update(this.sdt3.getId(), new ValueUpdate(108, "value description 2", System.currentTimeMillis() + 7L), newSDQuality);

    // Should not be filtered because quality is OK even if value is very close
    // to previous one
    this.equipmentMessageSender.update(this.sdt3.getId(), new ValueUpdate(107, "value description 2", System.currentTimeMillis() + 8L));
    assertEquals(SourceDataTagQualityCode.OK, this.sdt3.getCurrentValue().getQuality().getQualityCode());

    // absolute
    sourceDTValue = this.sdt2.getCurrentValue();
    // - quality code is OK by default and it will never go for invalidation
    // because is a special case. New quality code added
    newSDQuality = new SourceDataTagQuality(SourceDataTagQualityCode.DATA_UNAVAILABLE, sourceDTValue.getQuality().getDescription());

    // Should not be filtered (different Value and Quality Code)
    this.equipmentMessageSender.update(this.sdt2.getId(), new ValueUpdate(9.0f, System.currentTimeMillis() + 9L), newSDQuality);
    // Should be filtered (is Absolute ValueDeadband)
    this.equipmentMessageSender.update(this.sdt2.getId(), new ValueUpdate(9.1f, System.currentTimeMillis() + 10L), newSDQuality);
    // Should be filtered (is Absolute ValueDeadband)
    this.equipmentMessageSender.update(this.sdt2.getId(), new ValueUpdate(9.2f, System.currentTimeMillis() + 11L), newSDQuality);

    verify(this.processMessageSenderMock, this.filterMessageSenderMock);
  }

  @Test
  public void testSendInvalidTagWithStaticTimeDeadbandEnabled() throws Exception {

    // overwrite the default configuration
    EquipmentConfiguration equipmentConfiguration = new EquipmentConfiguration();
    equipmentConfiguration.setId(EQUIPMENT_ID);
    equipmentConfiguration.setCommFaultTagId(EQ_COMFAULT_ID);
    equipmentConfiguration.setCommFaultTagValue(false);
    // disable dynamic time deadband filtering
    equipmentConfiguration.setDynamicTimeDeadbandEnabled(false);

    // initialize the tag
    sdt1.update(new ValueUpdate(false));

    equipmentConfiguration.getDataTags().put(1L, sdt1);

    ProcessConfiguration processConf = new ProcessConfiguration();
    processConf.setProcessID(1L);
    processConf.setProcessName("ad");
    equipmentConfiguration.setHandlerClassName("asd");

    equipmentMessageSender.init(equipmentConfiguration);

    // setup calls should not affect later tests
    reset(filterMessageSenderMock, processMessageSenderMock);

    // the processMessageSender should be called exactly 3 times.
    processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    expectLastCall().times(3);

    // the filterMessageSender should be called exactly once
    filterMessageSenderMock.addValue(isA(FilteredDataTagValue.class));

    replay(processMessageSenderMock, filterMessageSenderMock);

    // send value "true" to the server
    equipmentMessageSender.update(sdt1.getId(), new ValueUpdate(true, System.currentTimeMillis() + 1L));

    // invalidate the tag
    equipmentMessageSender.update(sdt1.getId(), new SourceDataTagQuality(SourceDataTagQualityCode.DATA_UNAVAILABLE), System.currentTimeMillis() + 2L);

    // send again value "true" to the server ( it should not be filtered out,
    // because
    // it was invalidated previously)
    equipmentMessageSender.update(sdt1.getId(), new ValueUpdate(true, System.currentTimeMillis() + 3L));

    // sleep a bit
    Thread.sleep(120);

    // update again, with unchanged value this update should go to filter module
    equipmentMessageSender.update(sdt1.getId(), new ValueUpdate(true, System.currentTimeMillis() + 4L));

    verify(processMessageSenderMock, filterMessageSenderMock);
  }

  @Test
  public void testSendSupervisionAlive() throws InterruptedException {
    processMessageSenderMock.addValue(isA(SourceDataTagValue.class));

    replay(processMessageSenderMock);

    equipmentMessageSender.sendSupervisionAlive();

    verify(processMessageSenderMock);
  }

  @Test
  public void testSendTagFiltered() throws InterruptedException {
    lowDynamicTimeDeadbandFilterActivatorMock.newTagValueSent(sdt1.getId());
    processMessageSenderMock.addValue(isA(SourceDataTagValue.class));

    replay(lowDynamicTimeDeadbandFilterActivatorMock, processMessageSenderMock);
    sdt1.getAddress().setTimeDeadband(0);
    equipmentMessageSender.update(sdt1.getId(), new ValueUpdate(false, System.currentTimeMillis() + 1L));

    assertEquals(SourceDataTagQualityCode.OK, sdt1.getCurrentValue().getQuality().getQualityCode());

    verify(lowDynamicTimeDeadbandFilterActivatorMock, processMessageSenderMock);
  }

  @Test
  public void testSendTagFilteredWithIntegerArray() throws InterruptedException {
    processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    lowDynamicTimeDeadbandFilterActivatorMock.newTagValueSent(sdt4.getId());

    replay(lowDynamicTimeDeadbandFilterActivatorMock, processMessageSenderMock);
    sdt4.getAddress().setTimeDeadband(0);
    equipmentMessageSender.update(sdt4.getId(), new ValueUpdate(new Integer[] { 1, 2, 3, 4, 5 }, System.currentTimeMillis() + 1L));

    assertEquals(SourceDataTagQualityCode.OK, sdt4.getCurrentValue().getQuality().getQualityCode());

    verify(lowDynamicTimeDeadbandFilterActivatorMock, processMessageSenderMock);
  }

  @Test
  public void testSendTagFilteredWithIntArray() throws InterruptedException {
    processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    lowDynamicTimeDeadbandFilterActivatorMock.newTagValueSent(sdt4.getId());

    replay(lowDynamicTimeDeadbandFilterActivatorMock, processMessageSenderMock);
    sdt4.getAddress().setTimeDeadband(0);
    equipmentMessageSender.update(sdt4.getId(), new ValueUpdate(new int[] { 1, 2, 3, 4, 5 }, System.currentTimeMillis() + 1L));

    assertEquals(SourceDataTagQualityCode.OK, sdt4.getCurrentValue().getQuality().getQualityCode());

    verify(lowDynamicTimeDeadbandFilterActivatorMock, processMessageSenderMock);
  }

  @Test
  public void testSendTagFilteredWithArbitraryObject() throws InterruptedException {

    processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    lowDynamicTimeDeadbandFilterActivatorMock.newTagValueSent(sdt5.getId());

    replay(lowDynamicTimeDeadbandFilterActivatorMock, processMessageSenderMock);

    sdt5.getAddress().setTimeDeadband(0);

    Object arbitraryObject = ArbitraryObject.builder().fields(new Integer[] { 1, 2, 3, 4, 5 }).field1("Test").field2(1.3f).build();
    equipmentMessageSender.update(sdt5.getId(), new ValueUpdate(arbitraryObject, System.currentTimeMillis() + 1L));

    assertEquals(SourceDataTagQualityCode.OK, sdt5.getCurrentValue().getQuality().getQualityCode());

    verify(lowDynamicTimeDeadbandFilterActivatorMock, processMessageSenderMock);
  }

  @Test
  public void testSendTagFilteredWithArbitraryObjectWithWrongValue() throws InterruptedException {

    processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    lowDynamicTimeDeadbandFilterActivatorMock.newTagValueSent(sdt5.getId());

    replay(lowDynamicTimeDeadbandFilterActivatorMock, processMessageSenderMock);

    sdt5.getAddress().setTimeDeadband(0);

    Object wrongArbitraryObject = "{ \"test\" : value } ";
    equipmentMessageSender.update(sdt5.getId(), new ValueUpdate(wrongArbitraryObject, System.currentTimeMillis() + 1L));

    assertEquals(SourceDataTagQualityCode.CONVERSION_ERROR, sdt5.getCurrentValue().getQuality().getQualityCode());

    verify(lowDynamicTimeDeadbandFilterActivatorMock, processMessageSenderMock);
  }

  @Test
  public void testSendTagFilteredWithArbitraryObjectWithCurrentValue() throws InterruptedException {

    processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    lowDynamicTimeDeadbandFilterActivatorMock.newTagValueSent(sdt5.getId());

    replay(lowDynamicTimeDeadbandFilterActivatorMock, processMessageSenderMock);

    // setUp current SourceDataTagValue:
    Object currentArbitraryObject = ArbitraryObject.builder().field1("TestCurrent").field2(9.87f).build();
    sdt5.getAddress().setTimeDeadband(0);
    sdt5.update(new ValueUpdate(currentArbitraryObject));

    Object arbitraryObject = ArbitraryObject.builder().field1("Test").field2(1.3f).build();
    equipmentMessageSender.update(sdt5.getId(), new ValueUpdate(arbitraryObject, System.currentTimeMillis() + 1L));

    assertEquals(SourceDataTagQualityCode.OK, sdt5.getCurrentValue().getQuality().getQualityCode());

    verify(lowDynamicTimeDeadbandFilterActivatorMock, processMessageSenderMock);
  }

  @Test
  public void testSendTagFilteredWithUnknownArbitraryObject() throws InterruptedException {

    processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    lowDynamicTimeDeadbandFilterActivatorMock.newTagValueSent(sdt6.getId());

    replay(lowDynamicTimeDeadbandFilterActivatorMock, processMessageSenderMock);

    sdt6.getAddress().setTimeDeadband(0);

    Object unknownObject = "{ \"test\" : value } ";
    equipmentMessageSender.update(sdt6.getId(),new ValueUpdate(unknownObject, System.currentTimeMillis() + 1L));

    assertEquals(SourceDataTagQualityCode.OK, sdt6.getCurrentValue().getQuality().getQualityCode());

    verify(lowDynamicTimeDeadbandFilterActivatorMock, processMessageSenderMock);
  }

  @Test
  public void testSendTagFilteredInvalidTimestamp() throws Exception {
    this.lowDynamicTimeDeadbandFilterActivatorMock.newTagValueSent(sdt1.getId());
    expectLastCall().times(2);

    this.processMessageSenderMock.addValue(isA(SourceDataTagValue.class));

    // even though we try to send twice the tag, it should only be invalidated
    // once, since the quality code does not
    // change and it is FUTURE_SOURCE_TIMESTAMP
    expectLastCall().times(2);

    // One value is filtered out
    this.filterMessageSenderMock.addValue(isA(FilteredDataTagValue.class));
    expectLastCall().times(1);

    replay(this.lowDynamicTimeDeadbandFilterActivatorMock, this.processMessageSenderMock, this.filterMessageSenderMock);

    long futureTimestamp = System.currentTimeMillis() + 600000L;

    long futureTimestamp2 = futureTimestamp + 2000;

    long futureTimestamp3 = futureTimestamp + 4000;

    // Should not be filtered
    this.equipmentMessageSender.update(this.sdt1.getId(), new ValueUpdate(false, futureTimestamp));

    // Quality has change to FUTURE_SOURCE_TIMESTAMP
    assertEquals(SourceDataTagQualityCode.FUTURE_SOURCE_TIMESTAMP, this.sdt1.getCurrentValue().getQuality().getQualityCode());

    // String qualityDesc = String
    // .format("value: false received with source timestamp: [%s] in the future!
    // No further updates will be processed and the tag's value will stay
    // unchanged until this problem is fixed",
    // new Timestamp(futureTimestamp));
    //
    // // Problema con test "Value received with source timestamp in the future!
    // Time on server was: " + new Timestamp(System.currentTimeMillis()));
    //
    // assertEquals(qualityDesc,
    // sdt1.getCurrentValue().getQuality().getDescription());
    //
    // Thread.sleep(200);

    // Should be filtered (FUTURE_SOURCE_TIMESTAMP)
    this.equipmentMessageSender.update(this.sdt1.getId(), new ValueUpdate(false, futureTimestamp2));

    // Quality did not change
    assertEquals(SourceDataTagQualityCode.FUTURE_SOURCE_TIMESTAMP, sdt1.getCurrentValue().getQuality().getQualityCode());

    // Should not be filtered
    this.equipmentMessageSender.update(this.sdt1.getId(), new ValueUpdate(true, futureTimestamp3));

    // Another value update should be sent with invalid quality
    // FUTURE_SOURCE_TIMESTAMP
    assertEquals(SourceDataTagQualityCode.FUTURE_SOURCE_TIMESTAMP, sdt1.getCurrentValue().getQuality().getQualityCode());
    assertEquals(true, sdt1.getCurrentValue().getValue());
    assertEquals(futureTimestamp3, sdt1.getCurrentValue().getTimestamp().getTime());

    verify(this.lowDynamicTimeDeadbandFilterActivatorMock, this.processMessageSenderMock, this.filterMessageSenderMock);
  }

  @Test
  public void testSendTagFilteredNotConvertable() throws InterruptedException {
    medDynamicTimeDeadbandFilterActivatorMock.newTagValueSent(sdt2.getId());
    processMessageSenderMock.addValue(isA(SourceDataTagValue.class));

    replay(lowDynamicTimeDeadbandFilterActivatorMock, processMessageSenderMock);

    equipmentMessageSender.update(sdt2.getId(), new ValueUpdate("asdasdasd", System.currentTimeMillis() + 1L));

    assertEquals(SourceDataTagQualityCode.CONVERSION_ERROR, sdt2.getCurrentValue().getQuality().getQualityCode());

    verify(lowDynamicTimeDeadbandFilterActivatorMock, processMessageSenderMock);
  }

  @Test
  public void testSendTagFilteredNotConvertableTimeDeadbandDisable() throws InterruptedException {
    this.medDynamicTimeDeadbandFilterActivatorMock.newTagValueSent(sdt2.getId());
    this.processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    expectLastCall().times(1);
    this.filterMessageSenderMock.addValue(isA(FilteredDataTagValue.class));
    expectLastCall().times(1);

    this.sdt2.getAddress().setTimeDeadband(0);
    this.sdt2.update(new ValueUpdate(9.0f));

    replay(this.medDynamicTimeDeadbandFilterActivatorMock, this.processMessageSenderMock, this.filterMessageSenderMock);

    // Invalid (not convertible) and Not filtered
    this.equipmentMessageSender.update(sdt2.getId(), new ValueUpdate("test", System.currentTimeMillis() + 1L));
    assertEquals(SourceDataTagQualityCode.CONVERSION_ERROR, sdt2.getCurrentValue().getQuality().getQualityCode());

    // Invalid (not convertible) and filtered
    this.equipmentMessageSender.update(sdt2.getId(), new ValueUpdate("test", System.currentTimeMillis() + 2L));
    assertEquals(SourceDataTagQualityCode.CONVERSION_ERROR, sdt2.getCurrentValue().getQuality().getQualityCode());

    verify(this.medDynamicTimeDeadbandFilterActivatorMock, this.processMessageSenderMock, this.filterMessageSenderMock);
  }

  @Test
  public void testSendTagFilteredNotInRange() throws InterruptedException {
    this.lowDynamicTimeDeadbandFilterActivatorMock.newTagValueSent(sdt3.getId());
    this.medDynamicTimeDeadbandFilterActivatorMock.newTagValueSent(sdt2.getId());
    this.processMessageSenderMock.addValue(isA(SourceDataTagValue.class));

    // even though we try to send sdt3 twice, it should only be invalidated
    // once, since the quality code does not
    // change and is is OUT_OF_BOUNDS
    expectLastCall().times(2);

    // One value is filtered out
    this.filterMessageSenderMock.addValue(isA(FilteredDataTagValue.class));
    expectLastCall().times(1);

    replay(this.lowDynamicTimeDeadbandFilterActivatorMock, this.processMessageSenderMock, this.filterMessageSenderMock);

    this.sdt3.setMaxValue(100);
    this.sdt3.setMinValue(90);
    this.sdt2.setMaxValue(15.0f);
    this.sdt2.setMinValue(10.0f);

    // Out of bounds. Invalidate and not filtered
    this.equipmentMessageSender.update(this.sdt3.getId(), new ValueUpdate(109, System.currentTimeMillis() + 1L));
    // Out of bounds. Invalidate and not filtered
    this.equipmentMessageSender.update(this.sdt2.getId(), new ValueUpdate(9.0f, System.currentTimeMillis() + 2L));

    assertEquals(SourceDataTagQualityCode.OUT_OF_BOUNDS, this.sdt2.getCurrentValue().getQuality().getQualityCode());
    assertEquals(SourceDataTagQualityCode.OUT_OF_BOUNDS, this.sdt3.getCurrentValue().getQuality().getQualityCode());

    String oldQualityDesc = sdt3.getCurrentValue().getQuality().getDescription();

    // Out of bounds. Invalidate and filtered
    this.equipmentMessageSender.update(this.sdt3.getId(), new ValueUpdate(109, System.currentTimeMillis() + 3L));

    // Assure that the filtered value is not set to the reference
    assertEquals(109, sdt3.getCurrentValue().getValue());
    // The quality description should always be the same
    assertEquals(oldQualityDesc, sdt3.getCurrentValue().getQuality().getDescription());

    verify(this.lowDynamicTimeDeadbandFilterActivatorMock, this.processMessageSenderMock, this.filterMessageSenderMock);
  }

  /**
   * Filter when: - New TS <= Current TS + Current Good Quality - New TS <=
   * Current TS + Current Bad Quality + New Bad Quality
   *
   * No filter when: - New TS <= Current TS + New Good Quality + Current Bad
   * Quality - New TS > Current TS
   * @throws InterruptedException 
   */
  @Test
  public void testSendTagFilteredOldUpdateSent() throws InterruptedException {
    // update the value
    this.sdt1.update(new ValueUpdate(false));
    // Timestamps to use
    long sourceTS = System.currentTimeMillis() + 1000;
    long sourceTS_2 = sourceTS + 2000;
    long sourceTS_3 = sourceTS + 3000;

    SourceDataTagQuality newSDBadQuality = new SourceDataTagQuality(SourceDataTagQualityCode.DATA_UNAVAILABLE, "");

    this.processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    expectLastCall().times(4);
    this.filterMessageSenderMock.addValue(isA(FilteredDataTagValue.class));
    expectLastCall().times(1);

    replay(this.processMessageSenderMock, this.filterMessageSenderMock);

    // The first one: the run method sends it to the server with NO_FILTERING
    this.equipmentMessageSender.update(sdt1.getId(), new ValueUpdate(true, sourceTS));
    assertEquals(true, this.sdt1.getCurrentValue().getValue());

    // This one should NOT be filtered out. New TS > Current TS
    this.equipmentMessageSender.update(sdt1.getId(), new ValueUpdate(false, sourceTS_2));
    assertEquals(false, this.sdt1.getCurrentValue().getValue());

    // This one should be filtered out. New TS <= Current TS + Current Good
    // Quality
    this.equipmentMessageSender.update(sdt1.getId(), new ValueUpdate(true, sourceTS));
    assertEquals(false, this.sdt1.getCurrentValue().getValue());

    // Changing quality to BAD. Not filtering
    this.equipmentMessageSender.update(this.sdt1.getId(), new ValueUpdate(false, sourceTS_3), newSDBadQuality);
    assertEquals(false, this.sdt1.getCurrentValue().getValue());
    assertEquals(SourceDataTagQualityCode.DATA_UNAVAILABLE, this.sdt1.getCurrentValue().getQuality().getQualityCode());

    // This one should NOT be filtered out. New TS <= Current TS + New Good
    // Quality + Current Bad Quality
    this.equipmentMessageSender.update(sdt1.getId(), new ValueUpdate(true, sourceTS));
    assertEquals(true, this.sdt1.getCurrentValue().getValue());

    verify(this.processMessageSenderMock, this.filterMessageSenderMock);
  }

  /**
   * Filter when: - New TS <= Current TS + Current Good Quality - New TS <=
   * Current TS + Current Bad Quality + New Bad Quality
   *
   * No filter when: - New TS <= Current TS + New Good Quality + Current Bad
   * Quality - New TS > Current TS
   * @throws InterruptedException 
   */
  @Test
  public void testSendInvalidTagFilteredOldUpdateSent() throws InterruptedException {
    // update the value
    this.sdt1.update(new ValueUpdate(false));
    // Timestamps to use
    long sourceTS = System.currentTimeMillis() + 1000;
    long sourceTS_2 = sourceTS + 2000;

    // Creating Bad and Good Quality for testing
    SourceDataTagQuality newSDBadQuality = new SourceDataTagQuality(SourceDataTagQualityCode.DATA_UNAVAILABLE, "");
    SourceDataTagQuality newSDGoodQuality = new SourceDataTagQuality(SourceDataTagQualityCode.OK, "");

    this.processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    expectLastCall().times(3);
    this.filterMessageSenderMock.addValue(isA(FilteredDataTagValue.class));
    expectLastCall().times(1);

    replay(this.processMessageSenderMock, this.filterMessageSenderMock);

    // The first one: the run method sends it to the server with NO_FILTERING
    this.equipmentMessageSender.update(this.sdt1.getId(), new ValueUpdate(true, sourceTS), newSDBadQuality);
    assertEquals(true, this.sdt1.getCurrentValue().getValue());

    // This one should NOT be filtered out. New TS > Current TS
    this.equipmentMessageSender.update(this.sdt1.getId(), new ValueUpdate(false, sourceTS_2), newSDBadQuality);
    assertEquals(false, this.sdt1.getCurrentValue().getValue());
    assertEquals(SourceDataTagQualityCode.DATA_UNAVAILABLE, this.sdt1.getCurrentValue().getQuality().getQualityCode());

    // This one should be filtered out. New TS <= Current TS + Current Bad
    // Quality + New Bad Quality
    this.equipmentMessageSender.update(this.sdt1.getId(), new ValueUpdate(true, sourceTS), newSDBadQuality);
    assertEquals(false, this.sdt1.getCurrentValue().getValue());

    // This one should NOT be filtered out. New TS <= Current TS + New Good
    // Quality + Current Bad Quality
    // This should normally not happen! Redirecting call to update() method.
    this.equipmentMessageSender.update(this.sdt1.getId(), new ValueUpdate(true, sourceTS), newSDGoodQuality);
    assertEquals(true, this.sdt1.getCurrentValue().getValue());

    verify(this.processMessageSenderMock, this.filterMessageSenderMock);
  }

  @Test
  public void testSendTagFilteredIsValueDeadbandFiltered() throws Exception {
    filterMessageSenderMock.addValue(isA(FilteredDataTagValue.class));
    expectLastCall().times(4);

    // Value deadband type 6: as long as value description stays unchanged, it
    // works in exactly the same fashion as
    // DEADBAND_PROCESS_RELATIVE_VALUE. If, however value description change is
    // detected, deadband filtering is skipped.

    replay(filterMessageSenderMock);

    // relative

    // Not filtered
    equipmentMessageSender.update(sdt3.getId(), new ValueUpdate(109, "value description 1", System.currentTimeMillis() + 1L)); // <-
    // should
    // be sent

    // Not filtered, because values are different from the previous one.
    // Deadband filtering skipped
    equipmentMessageSender.update(sdt3.getId(), new ValueUpdate(108, System.currentTimeMillis() + 2L));
    // Not filtered, because values are different from the previous one.
    // Deadband filtering skipped
    equipmentMessageSender.update(sdt3.getId(), new ValueUpdate(109, "value description 2", System.currentTimeMillis() + 3L));
    // should be filtered, because value description has not changed. Deadband
    // filtering applied
    equipmentMessageSender.update(sdt3.getId(), new ValueUpdate(110, "value description 2", System.currentTimeMillis() + 4L));
    // should be sent, because value descr. is different from the previous one.
    // Deadband filtering skipped
    equipmentMessageSender.update(sdt3.getId(), new ValueUpdate(108, System.currentTimeMillis() + 5L));
    // should be filtered. REPEATED_VALUE
    equipmentMessageSender.update(sdt3.getId(), new ValueUpdate(108, System.currentTimeMillis() + 6L));
    // Should not be filtered out, because of bad quality (Same Value, Value
    // Descrp but dif Quality Code)
    equipmentMessageSender.update(sdt3.getId(), new ValueUpdate(108, "value description 2", System.currentTimeMillis() + 7L), new SourceDataTagQuality(SourceDataTagQualityCode.UNKNOWN));
    assertEquals(SourceDataTagQualityCode.UNKNOWN, sdt3.getCurrentValue().getQuality().getQualityCode());

    // absolute (DEADBAND_PROCESS_ABSOLUTE)
    // First Value => sent
    equipmentMessageSender.update(sdt2.getId(), new ValueUpdate(9.0f, System.currentTimeMillis() + 8L));
    // should be filtered, because value description has not changed. Deadband
    // filtering applied
    equipmentMessageSender.update(sdt2.getId(), new ValueUpdate(9.1f, System.currentTimeMillis() + 9L));
    // should be filtered, because value description has not changed. Deadband
    // filtering applied
    equipmentMessageSender.update(sdt2.getId(), new ValueUpdate(9.2f, System.currentTimeMillis() + 10L));
    // should be sent, because value descr. is different from the previous one.
    // Deadband filtering skipped
    this.sdt2.getAddress().setValueDeadbandType(DataTagDeadband.DEADBAND_PROCESS_ABSOLUTE_VALUE_DESCR_CHANGE);
    equipmentMessageSender.update(sdt2.getId(), new ValueUpdate(9.3f, "value description 3", System.currentTimeMillis() + 11L));

    verify(filterMessageSenderMock);
  }

  @Test
  public void testSendTagFilteredTwiceSame() throws InterruptedException {
    filterMessageSenderMock.addValue(isA(FilteredDataTagValue.class));
    processMessageSenderMock.addValue(isA(SourceDataTagValue.class));

    replay(filterMessageSenderMock, processMessageSenderMock);

    // Sent
    equipmentMessageSender.update(sdt1.getId(), new ValueUpdate(true, System.currentTimeMillis() + 1L));
    // Filter with REPEATED_VALUE
    equipmentMessageSender.update(sdt1.getId(), new ValueUpdate(true, System.currentTimeMillis() + 2L));

    verify(filterMessageSenderMock, processMessageSenderMock);
  }

  @Test
  public void testSendIntegerArrayTagFilteredTwiceSame() throws InterruptedException {
    filterMessageSenderMock.addValue(isA(FilteredDataTagValue.class));
    processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    Boolean sendSuccess;

    replay(filterMessageSenderMock, processMessageSenderMock);

    sdt4.getAddress().setTimeDeadband(0);
    Object arbitraryObject1 = new Integer[] { 1, 2, 3, 4, 5 };
    Object arbitraryObject2 = new Integer[] { 1, 2, 3, 4, 5 };

    // Sent
    sendSuccess = equipmentMessageSender.update(sdt4.getId(), new ValueUpdate(arbitraryObject1, System.currentTimeMillis() + 1L));
    assertTrue(sendSuccess);
    // Filter with REPEATED_VALUE
    sendSuccess = equipmentMessageSender.update(sdt4.getId(), new ValueUpdate(arbitraryObject2, System.currentTimeMillis() + 2L));
    assertFalse(sendSuccess);

    verify(filterMessageSenderMock, processMessageSenderMock);
  }

  @Test
  public void testSendIntegerArrayTagFilteredTwiceNotSame() throws InterruptedException {
    processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    Boolean sendSuccess;

    replay(processMessageSenderMock);

    sdt4.getAddress().setTimeDeadband(0);
    Object arbitraryObject1 = new Integer[] { 1, 2, 3, 4, 5 };
    Object arbitraryObject2 = new Integer[] { 1, 2, 3, 4 };

    // Sent
    sendSuccess = equipmentMessageSender.update(sdt4.getId(), new ValueUpdate(arbitraryObject1, System.currentTimeMillis() + 1L));
    assertTrue(sendSuccess);
    // Filter with REPEATED_VALUE
    sendSuccess = equipmentMessageSender.update(sdt4.getId(), new ValueUpdate(arbitraryObject2, System.currentTimeMillis() + 2L));
    assertTrue(sendSuccess);

    verify(processMessageSenderMock);
  }

  @Test
  public void testSendArbitraryObjectTagFilteredTwiceSame() throws InterruptedException {
    filterMessageSenderMock.addValue(isA(FilteredDataTagValue.class));
    processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    Boolean sendSuccess;

    replay(filterMessageSenderMock, processMessageSenderMock);

    sdt5.getAddress().setTimeDeadband(0);
    Object arbitraryObject1 = ArbitraryObject.builder().field1("TestTwice").field2(1.01f).build();
    Object arbitraryObject2 = ArbitraryObject.builder().field1("TestTwice").field2(1.01f).build();

    // Sent
    sendSuccess = equipmentMessageSender.update(sdt5.getId(), new ValueUpdate(arbitraryObject1, System.currentTimeMillis() + 1L));
    assertTrue(sendSuccess);
    // Filter with REPEATED_VALUE
    sendSuccess = equipmentMessageSender.update(sdt5.getId(), new ValueUpdate(arbitraryObject2, System.currentTimeMillis() + 2L));
    assertFalse(sendSuccess);

    verify(filterMessageSenderMock, processMessageSenderMock);
  }

  @Test
  public void testSendArbitraryObjectTagFilteredNotSame() throws InterruptedException {
    processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    Boolean sendSuccess;

    replay(processMessageSenderMock);

    sdt5.getAddress().setTimeDeadband(0);
    Object arbitraryObject1 = ArbitraryObject.builder().field1("TestTwice").field2(1.01f).build();
    Object arbitraryObject2 = ArbitraryObject.builder().field1("TestTwice2").field2(1.01f).build();

    // Sent
    sendSuccess = equipmentMessageSender.update(sdt5.getId(), new ValueUpdate(arbitraryObject1, System.currentTimeMillis() + 1L));
    assertTrue(sendSuccess);
    // Filter with REPEATED_VALUE
    sendSuccess = equipmentMessageSender.update(sdt5.getId(), new ValueUpdate(arbitraryObject2, System.currentTimeMillis() + 2L));
    assertTrue(sendSuccess);

    verify(processMessageSenderMock);
  }

  @Test
  public void testSendTagFilteredTwiceSameValuesButDiffValueDesc() throws Exception {
    processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    expectLastCall().times(2);

    replay(processMessageSenderMock);

    // Send to the server
    equipmentMessageSender.update(sdt1.getId(), new ValueUpdate(true, "test description A", System.currentTimeMillis()));
    // Send to the server. Equal Value but dif Value Description
    equipmentMessageSender.update(sdt1.getId(), new ValueUpdate(true, "test description B", System.currentTimeMillis() + 2L));

    verify(processMessageSenderMock);
  }

  @Test
  public void testSendInvalidTagFilteredTwiceSameValuesButDiffValueDesc() throws InterruptedException {
    this.processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    expectLastCall().times(2);
    replay(this.processMessageSenderMock);
    this.sdt1.getAddress().setTimeDeadband(0);
    this.sdt1.update(new ValueUpdate(true));

    SourceDataTagQuality newSDBadQuality = new SourceDataTagQuality(SourceDataTagQualityCode.DATA_UNAVAILABLE, "");

    // Not filtered. Quality codes are different
    // this.equipmentMessageSender.update(sdt1.getId(),
    // SourceDataTagQualityCode.DATA_UNAVAILABLE, "test description A");
    this.equipmentMessageSender.update(this.sdt1.getId(), new ValueUpdate(false, "test description A", System.currentTimeMillis() + 1L), newSDBadQuality);
    assertEquals(false, this.sdt1.getCurrentValue().getValue());
    // Not filtered. Quality descriptions are different
    // this.equipmentMessageSender.update(sdt1.getId(),
    // SourceDataTagQualityCode.DATA_UNAVAILABLE, "test description B");
    this.equipmentMessageSender.update(this.sdt1.getId(), new ValueUpdate(false, "test description B", System.currentTimeMillis() + 2L), newSDBadQuality);

    verify(this.processMessageSenderMock);
  }

  @Test
  /*
   * This test checks isCandidate for filtering when the value is the same but
   * the value descriptions changes. Cases Curent Vale Desc vs New Value Desc =>
   * isCandidate4Filtering? ---------------- --------------
   * --------------------- empty null Y empty not null N not empty null N not
   * empty not null N/Y depends on the desc
   */
  public void testSendTagFilteredIsCandidateValueDescriptions() throws Exception {
    filterMessageSenderMock.addValue(isA(FilteredDataTagValue.class));
    expectLastCall().times(2);

    processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    expectLastCall().times(5);

    // Value deadband type 6: as long as value description stays unchanged, it
    // works in exactly the same fashion as
    // DEADBAND_PROCESS_RELATIVE_VALUE. If, however value description change is
    // detected, deadband filtering is skipped.

    replay(filterMessageSenderMock, processMessageSenderMock);

    // relative

    // NO_FILTERING
    equipmentMessageSender.update(sdt3.getId(), new ValueUpdate(109, System.currentTimeMillis() + 1L));
    // REPEATED_VALUE Current Desc empty vs New Desc empty
    equipmentMessageSender.update(sdt3.getId(), new ValueUpdate(109, "", System.currentTimeMillis() + 2L));
    // REPEATED_VALUE Current Desc empty vs New Desc null
    equipmentMessageSender.update(sdt3.getId(), new ValueUpdate(109, System.currentTimeMillis() + 3L));
    // NO_FILTERING Current Desc null vs New Desc not empty
    equipmentMessageSender.update(sdt3.getId(), new ValueUpdate(109, "test description 1", System.currentTimeMillis() + 4L));
    // NO_FILTERING Current Desc not empty vs New Desc null
    equipmentMessageSender.update(sdt3.getId(), new ValueUpdate(109, System.currentTimeMillis() + 5L));
    // NO_FILTERING Current Desc null vs New Desc not empty
    equipmentMessageSender.update(sdt3.getId(), new ValueUpdate(109, "test description 2", System.currentTimeMillis() + 6L));
    // NO_FILTERING Current Desc not empty vs New Desc not empty
    equipmentMessageSender.update(sdt3.getId(), new ValueUpdate(109, "test description 3", System.currentTimeMillis() + 7L));

    verify(filterMessageSenderMock, processMessageSenderMock);
  }

  @Test
  public void testSendTagFilteredSameSourceTS() {
    // Timestamps to use
    long sourceTS = 1395210136000L;
    // update the value
    this.sdt1.update(new ValueUpdate(false, sourceTS));

    this.filterMessageSenderMock.addValue(isA(FilteredDataTagValue.class));
    expectLastCall().times(1);

    replay(this.processMessageSenderMock, this.filterMessageSenderMock);

    // Should be filtered out since the TS is the same as the one just updated
    this.equipmentMessageSender.update(sdt1.getId(), new ValueUpdate(false, sourceTS));
    assertEquals(false, this.sdt1.getCurrentValue().getValue());

    verify(this.processMessageSenderMock, this.filterMessageSenderMock);
  }

  @Test
  public void testSendTagFilteredSameSourceValue() {
    // update the value
    this.sdt1.update(new ValueUpdate(false));

    this.filterMessageSenderMock.addValue(isA(FilteredDataTagValue.class));
    expectLastCall().times(1);

    replay(this.processMessageSenderMock, this.filterMessageSenderMock);

    // Should be filtered out since the value is the same as the one just
    // updated
    this.equipmentMessageSender.update(sdt1.getId(), new ValueUpdate(false, System.currentTimeMillis() + 1L));
    assertEquals(false, this.sdt1.getCurrentValue().getValue());

    verify(this.processMessageSenderMock, this.filterMessageSenderMock);
  }

  @Test
  public void testSendTagFilteredWithValueConversionShort2Boolean() throws InterruptedException {
    // update the value
    this.sdt1.update(new ValueUpdate(false));

    this.processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    expectLastCall().times(1);

    replay(this.processMessageSenderMock, this.filterMessageSenderMock);

    // Should be filtered out since the value is the same as the one just
    // updated
    this.equipmentMessageSender.update(sdt1.getId(), new ValueUpdate(1.0f, System.currentTimeMillis() + 1L));
    assertEquals(true, this.sdt1.getCurrentValue().getValue());
    assertTrue(this.sdt1.getCurrentValue().getValue().getClass() == Boolean.class);

    verify(this.processMessageSenderMock, this.filterMessageSenderMock);
  }

  @Test
  public void testSendTagFilteredWithValueConversionShort2Float() throws InterruptedException {
    // update the value
    this.sdt2.update(new ValueUpdate(Float.valueOf(10f)));

    this.processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    expectLastCall().times(1);

    replay(this.processMessageSenderMock, this.filterMessageSenderMock);

    // Should be filtered out since the value is the same as the one just
    // updated
    this.equipmentMessageSender.update(sdt2.getId(), new ValueUpdate((short) 100, System.currentTimeMillis() + 1L));
    assertEquals(Float.valueOf(100), this.sdt2.getCurrentValue().getValue());
    assertTrue(this.sdt2.getCurrentValue().getValue().getClass() == Float.class);

    verify(this.processMessageSenderMock, this.filterMessageSenderMock);
  }

  @Test
  public void testSendTagFilteredWithValueConversionFloat2Integer() throws InterruptedException {
    // update the value
    this.sdt3.update(new ValueUpdate(Integer.valueOf(10)));

    this.processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    expectLastCall().times(1);

    replay(this.processMessageSenderMock, this.filterMessageSenderMock);

    // Should be filtered out since the value is the same as the one just
    // updated
    this.equipmentMessageSender.update(sdt3.getId(), new ValueUpdate(Float.valueOf(100.3f), System.currentTimeMillis() + 1L));
    assertEquals(Integer.valueOf(100), this.sdt3.getCurrentValue().getValue());
    assertTrue(this.sdt3.getCurrentValue().getValue().getClass() == Integer.class);

    verify(this.processMessageSenderMock, this.filterMessageSenderMock);
  }

  private SourceDataTag createSourceDataTag(long id, String name, String dataType, short deadBandType, int priority, boolean guaranteed) {
    DataTagAddress address = new DataTagAddress(null, 100, deadBandType, VALUE_DEADBAND, 0, priority, guaranteed);
    return new SourceDataTag(id, name, false, DataTagConstants.MODE_OPERATIONAL, dataType, address);
  }

  @Data
  @Builder
  static class ArbitraryObject {
    private Integer[] fields;

    private String field1;

    private Float field2;
  }

  @Test
  public void privateTempTestToJSON() {
    try {

      ObjectMapper mapper = new ObjectMapper();
      mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
      mapper.enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY);

      File tempOut = new File("temp.json");

      // Test Integer:
      SourceDataTagValue valueToWrite = sdt3.update(new ValueUpdate(1, "test", System.currentTimeMillis()));

      mapper.writeValue(tempOut, valueToWrite);

      SourceDataTagValue readValue = mapper.readValue(tempOut, SourceDataTagValue.class);
      assertTrue(readValue.equals(valueToWrite));

      // Test IntegerArray
      Integer[] integerArray = { 7, 8, 9 };
      valueToWrite = sdt3.update(new ValueUpdate(integerArray, "test", System.currentTimeMillis()));

      mapper.writeValue(tempOut, valueToWrite);

      readValue = mapper.readValue(tempOut, SourceDataTagValue.class);

      // Object Test
      SourceDataTagValue objectValue = sdt1.update(new ValueUpdate("Tach", "test2", System.currentTimeMillis()));
      valueToWrite = sdt3.update(new ValueUpdate(objectValue, "test", System.currentTimeMillis()));

      mapper.writeValue(tempOut, valueToWrite);

      readValue = mapper.readValue(tempOut, SourceDataTagValue.class);
      String blub = mapper.writeValueAsString(123456L);
    }
    catch (IOException e) {
      e.printStackTrace();
    }

  }

}

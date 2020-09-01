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

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.daq.common.IDynamicTimeDeadbandFilterer;
import cern.c2mon.daq.common.messaging.IProcessMessageSender;
import cern.c2mon.daq.filter.impl.DummyFilterSender;
import cern.c2mon.shared.common.datatag.*;
import cern.c2mon.shared.common.datatag.util.JmsMessagePriority;
import cern.c2mon.shared.common.datatag.util.SourceDataTagQualityCode;
import cern.c2mon.shared.common.datatag.util.ValueDeadbandType;
import cern.c2mon.shared.common.filter.FilteredDataTagValue.FilterType;
import cern.c2mon.shared.common.process.EquipmentConfiguration;
import cern.c2mon.shared.common.process.ProcessConfiguration;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author vilches
 *
 */
public class EquipmentTimeDeadbandTest {

  private static final long EQ_COMFAULT_ID = 1L;
  private static final float VALUE_DEADBAND = 25.0f;
  private static final long EQUIPMENT_ID = 1L;

  private EquipmentTimeDeadbandTester equipmentTimeDeadbandTester;

  private SourceDataTag sdt1;

  /**
   * Mocks
   */
  private IDynamicTimeDeadbandFilterer dynamicTimeDeadbandFiltererMock;
  private EquipmentSenderFilterModule equipmentSenderFilterModul;
  private IProcessMessageSender processMessageSenderMock;


  @Before
  public void setUp() {
    this.dynamicTimeDeadbandFiltererMock = createMock(IDynamicTimeDeadbandFilterer.class);
    this.processMessageSenderMock = createMock(IProcessMessageSender.class);
    this.equipmentSenderFilterModul = new EquipmentSenderFilterModule(new DummyFilterSender());

    EquipmentConfiguration equipmentConfiguration = new EquipmentConfiguration();
    equipmentConfiguration.setId(EQUIPMENT_ID);
    equipmentConfiguration.setCommFaultTagId(EQ_COMFAULT_ID);
    equipmentConfiguration.setCommFaultTagValue(false);

    // No Time Deadband (we will call directly the addToTimeDeadband. Not needed)
    this.sdt1 = createSourceDataTag(1L, "sdt1", "Boolean", ValueDeadbandType.PROCESS_RELATIVE, 0, JmsMessagePriority.PRIORITY_LOW,
            false);

    equipmentConfiguration.getDataTags().put(1L, this.sdt1);

    ProcessConfiguration processConf = new ProcessConfiguration();
    processConf.setProcessID(1L);
    processConf.setProcessName("Test");
    equipmentConfiguration.setHandlerClassName("testClass");

    this.equipmentTimeDeadbandTester = new EquipmentTimeDeadbandTester(this.dynamicTimeDeadbandFiltererMock, this.processMessageSenderMock,
        this.equipmentSenderFilterModul);
  }

  /**
   * Sending 3 valid values. The 2 first will be sent to the filter
   * Dynamic Time Deadband filter disable
   * Static Time Deadband filter enabled
   */
  @Test
  public void testAddValidStaticTimeDeadband() {

    long ms = System.currentTimeMillis();

    this.dynamicTimeDeadbandFiltererMock.recordTag(isA(SourceDataTag.class));
    expectLastCall().times(3);

    // Dymanic Time Deadband disabled
    EasyMock.expect(this.dynamicTimeDeadbandFiltererMock.isDynamicTimeDeadband(this.sdt1)).andReturn(false).times(2);

    // Lets figure out the Time Deadband is enabled (in this case would be the Static)
    this.sdt1.getAddress().setTimeDeadband(1);
    this.sdt1.update(new ValueUpdate(true));

    replay(this.dynamicTimeDeadbandFiltererMock);

    // The first one does nothing
    this.equipmentTimeDeadbandTester.addToTimeDeadband(this.sdt1, new ValueUpdate(false, ms));
    assertEquals(false, this.sdt1.getCurrentValue().getValue());

    // The second one is valid so it sends the first one to the filter
    this.equipmentTimeDeadbandTester.addToTimeDeadband(this.sdt1, new ValueUpdate(true, ms + 1L));
    assertEquals(true, this.sdt1.getCurrentValue().getValue());

    // The third one is valid so it sends the second one to the filter
    this.equipmentTimeDeadbandTester.addToTimeDeadband(this.sdt1, new ValueUpdate(false, ms + 2L));
    assertEquals(false, this.sdt1.getCurrentValue().getValue());

    verify(this.dynamicTimeDeadbandFiltererMock);
  }

  /**
   * Sending 3 valid values. The 2 first will be sent to the filter
   * Dynamic Time Deadband filter disable
   * Static Time Deadband filter enabled
   *
   */
  @Test
  public void testAddValidDynamicTimeDeadband() {

    long ms = System.currentTimeMillis();

    this.dynamicTimeDeadbandFiltererMock.recordTag(isA(SourceDataTag.class));
    expectLastCall().times(3);

    // Dymanic Time Deadband enabled
    EasyMock.expect(this.dynamicTimeDeadbandFiltererMock.isDynamicTimeDeadband(this.sdt1)).andReturn(true).times(2);

    // Lets figure out the Time Deadband is enabled (in this case would be the Dynamic only)
    this.sdt1.getAddress().setTimeDeadband(1);
    this.sdt1.update(new ValueUpdate(true));

    replay(this.dynamicTimeDeadbandFiltererMock);

    // The first one does nothing
    this.equipmentTimeDeadbandTester.addToTimeDeadband(this.sdt1, new ValueUpdate(false, ms));
    assertEquals(false, this.sdt1.getCurrentValue().getValue());

    // The second one is valid so it sends the first one to the filter
    this.equipmentTimeDeadbandTester.addToTimeDeadband(this.sdt1, new ValueUpdate(true, ms + 1L));
    assertEquals(true, this.sdt1.getCurrentValue().getValue());

    // The third one is valid so it sends the second one to the filter
    this.equipmentTimeDeadbandTester.addToTimeDeadband(this.sdt1, new ValueUpdate(false, ms + 2L));
    assertEquals(false, this.sdt1.getCurrentValue().getValue());

    verify(this.dynamicTimeDeadbandFiltererMock);
  }

  /**
   * Sending 2 valid and 1 invalid values. The first will be sent to the filter.
   * The second will be sent to the server after flush and cancel
   */
  @Test
  public void testAddValidInvalidTimeDeadband() {

    long ms = System.currentTimeMillis();

    // Dynamic Time Deadband disabled
    EasyMock.expect(this.dynamicTimeDeadbandFiltererMock.isDynamicTimeDeadband(this.sdt1)).andReturn(false).times(2);

    this.dynamicTimeDeadbandFiltererMock.recordTag(isA(SourceDataTag.class));
    expectLastCall().times(3);

    // Lets figure out the Time Deadband is enabled (in this case would be the Static)
    this.sdt1.getAddress().setTimeDeadband(1);
    this.sdt1.update(new ValueUpdate(true));

    replay(this.dynamicTimeDeadbandFiltererMock);

    // The first one does nothing
    this.equipmentTimeDeadbandTester.addToTimeDeadband(this.sdt1, new ValueUpdate(false, ms));
    assertEquals(false, this.sdt1.getCurrentValue().getValue());

    // The second one is valid so it sends the first one to the filter
    this.equipmentTimeDeadbandTester.addToTimeDeadband(this.sdt1, new ValueUpdate(true, ms + 1L));
    assertEquals(true, this.sdt1.getCurrentValue().getValue());

    SourceDataTagValue sourceDTValue = this.sdt1.getCurrentValue();
    // Get the source data quality from the quality code and description
    SourceDataTagQuality newSDQuality = new SourceDataTagQuality(SourceDataTagQualityCode.DATA_UNAVAILABLE,
        sourceDTValue.getQuality().getDescription());

    // The third one is invalid
    this.equipmentTimeDeadbandTester.addToTimeDeadband(this.sdt1, new ValueUpdate(true, ms + 2L), newSDQuality);
    assertEquals(true, this.sdt1.getCurrentValue().getValue());
    assertEquals(newSDQuality.getQualityCode(), this.sdt1.getCurrentValue().getQuality().getQualityCode());

    verify(this.dynamicTimeDeadbandFiltererMock);
  }

  /**
   * Sending 2 valid and 1 invalid values. The first will be sent to the filter.
   * The second will be sent to the server after flush and cancel
   */
  @Test
  public void testAddValidInvalidValidTimeDeadband() {

    long ms = System.currentTimeMillis();
    
    // Get the source data quality from the quality code and description
    SourceDataTagQuality newSDQuality = new SourceDataTagQuality(SourceDataTagQualityCode.DATA_UNAVAILABLE, "");

    this.dynamicTimeDeadbandFiltererMock.recordTag(isA(SourceDataTag.class));
    expectLastCall().times(3);
    
    // Dymanic Time Deadband true
    EasyMock.expect(this.dynamicTimeDeadbandFiltererMock.isDynamicTimeDeadband(this.sdt1)).andReturn(true);
    expectLastCall().times(2);

    // Lets figure out the Time Deadband is enabled (in this case would be the Dynamic)
    this.sdt1.getAddress().setTimeDeadband(1);
    this.sdt1.update(new ValueUpdate(true));

    replay(this.dynamicTimeDeadbandFiltererMock);

    // The first one does nothing
    this.equipmentTimeDeadbandTester.addToTimeDeadband(this.sdt1, new ValueUpdate(false, ms +1));
    assertEquals(false, this.sdt1.getCurrentValue().getValue());

    // The second one is invalid so it flush and cancel. It run the run() for the first time so the value is sent to the server
    this.equipmentTimeDeadbandTester.addToTimeDeadband(this.sdt1, new ValueUpdate(true, ms + 1L), newSDQuality);
    assertEquals(true, this.sdt1.getCurrentValue().getValue());
    assertEquals(SourceDataTagQualityCode.DATA_UNAVAILABLE, this.sdt1.getCurrentValue().getQuality().getQualityCode());

    // The third one is valid so it flush and cancel again
    this.equipmentTimeDeadbandTester.addToTimeDeadband(this.sdt1, new ValueUpdate(false, ms + 2L));
    assertEquals(false, this.sdt1.getCurrentValue().getValue());
    assertEquals(SourceDataTagQualityCode.OK, this.sdt1.getCurrentValue().getQuality().getQualityCode());

    verify(this.dynamicTimeDeadbandFiltererMock);
  }

  @Test
  public void testRemoveTagTimeDeadband() {

    long ms = System.currentTimeMillis();

    this.dynamicTimeDeadbandFiltererMock.recordTag(isA(SourceDataTag.class));
    expectLastCall().times(1);

    // Lets figure out the Time Deadband is enabled (in this case would be the Static)
    this.sdt1.getAddress().setTimeDeadband(1);
    this.sdt1.update(new ValueUpdate(true));

    replay(this.dynamicTimeDeadbandFiltererMock);

    // Add the tag to the TimeDBScheduler
    this.equipmentTimeDeadbandTester.addToTimeDeadband(this.sdt1, new ValueUpdate(false, ms +1));
    assertEquals(false, this.sdt1.getCurrentValue().getValue());
    assertEquals(false, this.equipmentTimeDeadbandTester.getSdtTimeDeadbandSchedulers().isEmpty());

    // Remove the tag from the TimeDBScheduler
    this.equipmentTimeDeadbandTester.removeFromTimeDeadband(this.sdt1);
    assertEquals(true, this.equipmentTimeDeadbandTester.getSdtTimeDeadbandSchedulers().isEmpty());

    verify(this.dynamicTimeDeadbandFiltererMock);
  }

  /**
   *
   * @param id
   * @param name
   * @param dataType
   * @param deadBandType
   * @param timeDeadband
   * @param priority
   * @param guaranteed
   * @return
   */
  private SourceDataTag createSourceDataTag(long id, String name, String dataType, ValueDeadbandType deadBandType,  int timeDeadband,
      JmsMessagePriority priority, boolean guaranteed) {
  DataTagAddress address = new DataTagAddress(null, 100, deadBandType, VALUE_DEADBAND, timeDeadband, priority, guaranteed);
  return new SourceDataTag(id, name, false, DataTagConstants.MODE_OPERATIONAL, dataType, address);
}
}

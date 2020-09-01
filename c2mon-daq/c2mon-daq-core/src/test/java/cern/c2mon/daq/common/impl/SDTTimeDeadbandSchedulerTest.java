/******************************************************************************
 * Copyright (C) 2010-2020 CERN. All rights not expressly granted are reserved.
 * <p>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.daq.common.impl;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Timer;

import cern.c2mon.daq.common.timer.FreshnessMonitor;
import cern.c2mon.daq.config.DaqProperties;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.daq.common.IDynamicTimeDeadbandFilterer;
import cern.c2mon.daq.common.conf.core.ConfigurationController;
import cern.c2mon.daq.common.messaging.IProcessMessageSender;
import cern.c2mon.daq.filter.IFilterMessageSender;
import cern.c2mon.daq.filter.dynamic.IDynamicTimeDeadbandFilterActivator;
import cern.c2mon.daq.tools.DataTagValueFilter;
import cern.c2mon.shared.common.datatag.*;
import cern.c2mon.shared.common.datatag.util.JmsMessagePriority;
import cern.c2mon.shared.common.datatag.util.ValueDeadbandType;
import cern.c2mon.shared.common.filter.FilteredDataTagValue;
import cern.c2mon.shared.common.process.EquipmentConfiguration;
import cern.c2mon.shared.common.process.ProcessConfiguration;

//import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

/**
 * @author vilches
 */
public class SDTTimeDeadbandSchedulerTest {

  private static final float VALUE_DEADBAND = 25.0f;

  private SDTTimeDeadbandScheduler scheduler;
  private SourceDataTag tag;
  private EquipmentMessageSender equipmentMessageSender;
  private Throwable exception = null;

  private DataTagValueFilter dataTagValueFilter;
  private EquipmentConfiguration conf = new EquipmentConfiguration();

  /**
   * The class with the message sender to send filtered tag values
   */
  private EquipmentSenderFilterModule equipmentSenderFilterModule;

  /**
   * Mock
   */
  private IProcessMessageSender processMessageSenderMock;
  private IFilterMessageSender filterMessageSenderMock;
  private ConfigurationController configurationControllerMock;
  private ProcessConfiguration processConfigurationMock;
  private IDynamicTimeDeadbandFilterer dynamicTimeDeadbandFiltererMock;
  private FreshnessMonitor freshnessMonitorMock;

  @Before
  public void setUp() {
    this.processMessageSenderMock = createMock(IProcessMessageSender.class);
    this.filterMessageSenderMock = createMock(IFilterMessageSender.class);
    this.dynamicTimeDeadbandFiltererMock = createMock(IDynamicTimeDeadbandFilterer.class);
    this.freshnessMonitorMock = createMock(FreshnessMonitor.class);

    this.configurationControllerMock = EasyMock.createMockBuilder(ConfigurationController.class).
        addMockedMethod("getProcessConfiguration").
        createMock();

    this.processConfigurationMock = EasyMock.createMockBuilder(ProcessConfiguration.class).
        addMockedMethod("getProcessName").
        createMock();

    IDynamicTimeDeadbandFilterActivator activatorMock = createMock(IDynamicTimeDeadbandFilterActivator.class);
    freshnessMonitorMock = createMock(FreshnessMonitor.class);
    this.equipmentMessageSender = new EquipmentMessageSender(this.filterMessageSenderMock,
        this.processMessageSenderMock, activatorMock, freshnessMonitorMock, new DaqProperties());

    freshnessMonitorMock.setIEquipmentMessageSender(equipmentMessageSender);
    EasyMock.expectLastCall();
    this.tag = createSourceDataTag(1L, "sdt1", "Boolean", ValueDeadbandType.NONE, JmsMessagePriority.PRIORITY_LOW, false);
    this.tag.getAddress().setTimeDeadband(30);

    this.conf.getDataTags().put(1L, tag);
    //        equipmentMessageSender.setEquipmentConfiguration(conf);

    tag.update(new ValueUpdate(""));
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(Thread t, Throwable e) {
        exception = e;
      }

    });

    this.conf.setHandlerClassName(getClass().getName());
  }

  /**
   * Send value
   *
   * @throws Exception
   */
  @Test
  public void testSchedule() throws Exception {
    // Tag update
    this.tag.update(new ValueUpdate(true, "test", System.currentTimeMillis()));

    EasyMock.expect(this.configurationControllerMock.getProcessConfiguration()).andReturn(this.processConfigurationMock);
    EasyMock.expect(this.processConfigurationMock.getProcessName()).andReturn("TEST_PROCESS_NAME");

    // No Dymanic Time Deadband
    EasyMock.expect(this.dynamicTimeDeadbandFiltererMock.isDynamicTimeDeadband(tag)).andReturn(false);

    // This message will be sent since it is the first one
    this.processMessageSenderMock.addValue(isA(SourceDataTagValue.class));

    replay(this.processMessageSenderMock, this.configurationControllerMock, this.processConfigurationMock, this.dynamicTimeDeadbandFiltererMock);

    this.equipmentMessageSender.init(this.conf);

    this.dataTagValueFilter = new DataTagValueFilter();

    // Filter module
    this.equipmentSenderFilterModule = new EquipmentSenderFilterModule(this.filterMessageSenderMock);

    //        EquipmentLogger equipmentLogger = new EquipmentLogger("asd", "asd", "asd");
    this.scheduler = new SDTTimeDeadbandScheduler(tag, this.processMessageSenderMock, this.equipmentSenderFilterModule, new Timer(true), this
        .dataTagValueFilter, this.dynamicTimeDeadbandFiltererMock);

    this.scheduler.scheduleValueForSending();
    this.scheduler.run();

    verify(this.processMessageSenderMock);

    if (this.exception != null) throw new Exception(this.exception);
  }

  /**
   * Send value
   *
   * @throws Exception
   */
  @Test
  public void testScheduleNoFiltering() throws Exception {
    // Tag update
    this.tag.update(new ValueUpdate(true, "test", System.currentTimeMillis()));

    EasyMock.expect(this.configurationControllerMock.getProcessConfiguration()).andReturn(this.processConfigurationMock);
    EasyMock.expect(this.processConfigurationMock.getProcessName()).andReturn("TEST_PROCESS_NAME");

    // No Dymanic Time Deadband
    EasyMock.expect(this.dynamicTimeDeadbandFiltererMock.isDynamicTimeDeadband(tag)).andReturn(false);

    // This message will be sent since it is the first one
    this.processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    expectLastCall().times(2);

    replay(this.processMessageSenderMock, this.configurationControllerMock, this.processConfigurationMock, this
        .dynamicTimeDeadbandFiltererMock);

    this.equipmentMessageSender.init(this.conf);

    this.dataTagValueFilter = new DataTagValueFilter();

    // Filter module
    this.equipmentSenderFilterModule = new EquipmentSenderFilterModule(this.filterMessageSenderMock);

    //        EquipmentLogger equipmentLogger = new EquipmentLogger("asd", "asd", "asd");
    this.scheduler = new SDTTimeDeadbandScheduler(tag, this.processMessageSenderMock, this.equipmentSenderFilterModule, new Timer(true), this
        .dataTagValueFilter, this.dynamicTimeDeadbandFiltererMock);

    this.scheduler.scheduleValueForSending();
    this.scheduler.run();

    // New value
    this.tag.update(new ValueUpdate(false, "test", System.currentTimeMillis()));
    scheduler.scheduleValueForSending();
    this.scheduler.run();

    verify(this.processMessageSenderMock);

    if (this.exception != null) throw new Exception(this.exception);
  }

  /**
   * filter value: REPEATED_VALUE
   *
   * @throws Exception
   */
  @Test
  public void testScheduleFilterRepeatedValue() throws Exception {

    // Tag update
    this.tag.update(new ValueUpdate(true, "test", System.currentTimeMillis()));

    EasyMock.expect(this.configurationControllerMock.getProcessConfiguration()).andReturn(this.processConfigurationMock);
    EasyMock.expect(this.processConfigurationMock.getProcessName()).andReturn("TEST_PROCESS_NAME");

    // No Dymanic Time Deadband
    EasyMock.expect(this.dynamicTimeDeadbandFiltererMock.isDynamicTimeDeadband(tag)).andReturn(false);

    // This message will be sent since it is the first one
    this.processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    // This message will be filtered with filter type REPEATED_VALUE
    this.filterMessageSenderMock.addValue(isA(FilteredDataTagValue.class));

    replay(this.processMessageSenderMock, this.filterMessageSenderMock, this.configurationControllerMock, this.processConfigurationMock,
        this.dynamicTimeDeadbandFiltererMock);

    this.equipmentMessageSender.init(this.conf);

    this.dataTagValueFilter = new DataTagValueFilter();

    // Filter module
    this.equipmentSenderFilterModule = new EquipmentSenderFilterModule(this.filterMessageSenderMock);

//     EquipmentLogger equipmentLogger = new EquipmentLogger("asd", "asd", "asd");
    this.scheduler = new SDTTimeDeadbandScheduler(tag, this.processMessageSenderMock, this.equipmentSenderFilterModule, new Timer(true), this
        .dataTagValueFilter, this.dynamicTimeDeadbandFiltererMock);

    this.scheduler.scheduleValueForSending();
    this.scheduler.run();

    // Repeated value
    this.scheduler.scheduleValueForSending();
    this.scheduler.run();

    verify(this.processMessageSenderMock, this.filterMessageSenderMock);

    if (this.exception != null) throw new Exception(this.exception);
  }

  /**
   * filter value: REPEATED_VALUE
   *
   * @throws Exception
   */
  @Test
  public void testScheduleFlashAndReset() throws Exception {

    // Tag update
    this.tag.update(new ValueUpdate(true, "test", System.currentTimeMillis()));

    EasyMock.expect(this.configurationControllerMock.getProcessConfiguration()).andReturn(this.processConfigurationMock);
    EasyMock.expect(this.processConfigurationMock.getProcessName()).andReturn("TEST_PROCESS_NAME");

    // No Dymanic Time Deadband
    EasyMock.expect(this.dynamicTimeDeadbandFiltererMock.isDynamicTimeDeadband(tag)).andReturn(false);

    // This message will be sent 2 times to the server since the timer is cancelled and started again
    this.processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
    EasyMock.expectLastCall().times(2);

    replay(this.processMessageSenderMock, this.filterMessageSenderMock, this.configurationControllerMock, this.processConfigurationMock,
        this.dynamicTimeDeadbandFiltererMock);

    this.equipmentMessageSender.init(this.conf);

    this.dataTagValueFilter = new DataTagValueFilter();

    // Filter module
    this.equipmentSenderFilterModule = new EquipmentSenderFilterModule(this.filterMessageSenderMock);

//     EquipmentLogger equipmentLogger = new EquipmentLogger("asd", "asd", "asd");
    this.scheduler = new SDTTimeDeadbandScheduler(tag, this.processMessageSenderMock, this.equipmentSenderFilterModule, new Timer(true), this
        .dataTagValueFilter, this.dynamicTimeDeadbandFiltererMock);

    this.scheduler.scheduleValueForSending();
    this.scheduler.run();

    // FlushAndReset
    this.scheduler.flushAndCancel();
    // Start the new task
    this.scheduler = new SDTTimeDeadbandScheduler(tag, this.processMessageSenderMock, this.equipmentSenderFilterModule, new Timer(true), this
        .dataTagValueFilter, this.dynamicTimeDeadbandFiltererMock);

    // Repeated value (should pass cause it was flush and reset)
    this.scheduler.scheduleValueForSending();
    this.scheduler.run();

    verify(this.processMessageSenderMock, this.filterMessageSenderMock);

    if (this.exception != null) throw new Exception(this.exception);
  }

  /**
   * @param id
   * @param name
   * @param dataType
   * @param deadBandType
   * @param priority
   * @param guaranteed
   *
   * @return
   */
  private SourceDataTag createSourceDataTag(long id, String name, String dataType, ValueDeadbandType deadBandType, JmsMessagePriority priority, boolean guaranteed) {
    DataTagAddress address = new DataTagAddress(null, 100, deadBandType, VALUE_DEADBAND, 0, priority, guaranteed);
    return new SourceDataTag(id, name, false, DataTagConstants.MODE_OPERATIONAL, dataType, address);
  }
}

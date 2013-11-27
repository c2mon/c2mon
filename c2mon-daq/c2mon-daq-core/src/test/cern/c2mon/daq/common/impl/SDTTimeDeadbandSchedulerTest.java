package cern.c2mon.daq.common.impl;

//import static org.junit.Assert.*;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Timer;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.daq.common.conf.core.CommonConfiguration;
import cern.c2mon.daq.common.conf.core.ConfigurationController;
import cern.c2mon.daq.common.conf.core.EquipmentConfiguration;
import cern.c2mon.daq.common.conf.core.ProcessConfiguration;
import cern.c2mon.daq.common.conf.core.RunOptions;
import cern.c2mon.daq.common.impl.EquipmentMessageSender;
import cern.c2mon.daq.common.impl.SDTTimeDeadbandScheduler;
import cern.c2mon.daq.common.logger.EquipmentLoggerFactory;
import cern.c2mon.daq.common.messaging.IProcessMessageSender;
import cern.c2mon.daq.filter.IFilterMessageSender;
import cern.c2mon.daq.filter.dynamic.IDynamicTimeDeadbandFilterActivator;
import cern.c2mon.daq.tools.DataTagValueFilter;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.daq.datatag.SourceDataTag;
import cern.c2mon.shared.daq.datatag.SourceDataTagValue;
import cern.c2mon.shared.daq.filter.FilteredDataTagValue;

public class SDTTimeDeadbandSchedulerTest {

	private IProcessMessageSender processMessageSender;
	private SDTTimeDeadbandScheduler scheduler;
	private SourceDataTag tag;
	private IFilterMessageSender filterMessageSender;
	private EquipmentMessageSender equipmentMessageSender;
	private Throwable exception = null;

	private DataTagValueFilter dataTagValueFilter;
	private EquipmentLoggerFactory equipmentLoggerFactory;
	private EquipmentConfiguration conf = new EquipmentConfiguration();

	/**
	 * Mock
	 */
	 private ConfigurationController configurationControllerMock;
	 private ProcessConfiguration processConfigurationMock;
	 private RunOptions runOptionsMock;

	 @Before
	 public void setUp() {
		 this.processMessageSender = createMock(IProcessMessageSender.class);
		 this.filterMessageSender = createMock(IFilterMessageSender.class);

		 // Mock for configurationController to use getCommandParamsHandler()
		 // The run options of the DAQ process
		 this.configurationControllerMock = EasyMock.createMockBuilder(ConfigurationController.class).
				 withConstructor(RunOptions.class, CommonConfiguration.class).
				 withArgs(null, null).
				 addMockedMethod("getRunOptions").
				 addMockedMethod("getProcessConfiguration").
				 createMock();  

		 this.runOptionsMock = EasyMock.createMockBuilder(RunOptions.class).
				 addMockedMethod("getEqLoggers").
				 addMockedMethod("getEqAppendersOnly").
				 createMock();

		 this.processConfigurationMock = EasyMock.createMockBuilder(ProcessConfiguration.class).
				 addMockedMethod("getProcessName").
				 createMock();  

		 IDynamicTimeDeadbandFilterActivator activator = createMock(IDynamicTimeDeadbandFilterActivator.class);
		 this.equipmentMessageSender = new EquipmentMessageSender(
				 this.filterMessageSender, this.processMessageSender, activator , activator);
		 DataTagAddress address = new DataTagAddress();
		 address.setTimeDeadband(30);
		 tag = new SourceDataTag(1L, "asd", false);
		 tag.setAddress(address);

		 this.conf.getDataTags().put(1L, tag);
		 //        equipmentMessageSender.setEquipmentConfiguration(conf);

		 tag.update("");
		 Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			 @Override
			 public void uncaughtException(Thread t, Throwable e) {
				 exception = e;
			 }

		 });

		 this.conf.setHandlerClassName(getClass().getName());
	 }

	 @Test
	 public void testSchedule() throws Exception {
		 EasyMock.expect(this.configurationControllerMock.getRunOptions()).andReturn(this.runOptionsMock);
		 EasyMock.expect(this.runOptionsMock.getEqLoggers()).andReturn(false);
		 EasyMock.expect(this.runOptionsMock.getEqAppendersOnly()).andReturn(false);

		 EasyMock.expect(this.configurationControllerMock.getProcessConfiguration()).andReturn(this.processConfigurationMock);
		 EasyMock.expect(this.processConfigurationMock.getProcessName()).andReturn("TEST_PROCESS_NAME");

		 this.processMessageSender.addValue(isA(SourceDataTagValue.class));
		 replay(this.processMessageSender, this.configurationControllerMock, this.processConfigurationMock, 
				 this.runOptionsMock);

		 this.equipmentLoggerFactory = EquipmentLoggerFactory.createFactory(this.conf,
				 this.configurationControllerMock.getProcessConfiguration(), this.configurationControllerMock.getRunOptions());

		 this.equipmentMessageSender.init(this.conf, this.equipmentLoggerFactory);

		 this.dataTagValueFilter = new DataTagValueFilter(this.equipmentLoggerFactory);

		 //        EquipmentLogger equipmentLogger = new EquipmentLogger("asd", "asd", "asd");
		 this.scheduler = new SDTTimeDeadbandScheduler(tag, this.equipmentMessageSender.getEquipmentSenderValid(), 
				 new Timer(true), this.dataTagValueFilter);
		 this.scheduler.scheduleValueForSending();
		 Thread.sleep(50);
		 verify(this.processMessageSender);
		 if (this.exception != null)
			 throw new Exception(this.exception);
	 }

	 @Test
	 public void testScheduleRepeated() throws Exception {
		 EasyMock.expect(this.configurationControllerMock.getRunOptions()).andReturn(this.runOptionsMock);
		 EasyMock.expect(this.runOptionsMock.getEqLoggers()).andReturn(false);
		 EasyMock.expect(this.runOptionsMock.getEqAppendersOnly()).andReturn(false);

		 EasyMock.expect(this.configurationControllerMock.getProcessConfiguration()).andReturn(this.processConfigurationMock);
		 EasyMock.expect(this.processConfigurationMock.getProcessName()).andReturn("TEST_PROCESS_NAME");

		 this.processMessageSender.addValue(isA(SourceDataTagValue.class));
		 this.filterMessageSender.addValue(isA(FilteredDataTagValue.class));
		 replay(this.processMessageSender, this.filterMessageSender, this.configurationControllerMock, this.processConfigurationMock, 
				 this.runOptionsMock);

		 this.equipmentLoggerFactory = EquipmentLoggerFactory.createFactory(this.conf,
				 this.configurationControllerMock.getProcessConfiguration(), this.configurationControllerMock.getRunOptions());

		 this.equipmentMessageSender.init(this.conf, this.equipmentLoggerFactory);

		 this.dataTagValueFilter = new DataTagValueFilter(this.equipmentLoggerFactory);

//		 EquipmentLogger equipmentLogger = new EquipmentLogger("asd", "asd", "asd");
		 this.scheduler = new SDTTimeDeadbandScheduler(tag, this.equipmentMessageSender.getEquipmentSenderValid(), 
				 new Timer(true), this.dataTagValueFilter);
		 this.scheduler.scheduleValueForSending();
		 Thread.sleep(50);
		 // repeated value
		 scheduler.scheduleValueForSending();
		 Thread.sleep(100);
		 verify(this.processMessageSender, this.filterMessageSender);
		 if (this.exception != null)
			 throw new Exception(this.exception);
	 }
}

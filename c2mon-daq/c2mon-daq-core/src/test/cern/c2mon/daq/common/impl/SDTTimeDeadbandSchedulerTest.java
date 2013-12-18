package cern.c2mon.daq.common.impl;

//import static org.junit.Assert.*;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.lang.Thread.UncaughtExceptionHandler;
import java.sql.Timestamp;
import java.util.Timer;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.daq.common.conf.core.CommonConfiguration;
import cern.c2mon.daq.common.conf.core.ConfigurationController;
import cern.c2mon.daq.common.conf.core.EquipmentConfiguration;
import cern.c2mon.daq.common.conf.core.ProcessConfiguration;
import cern.c2mon.daq.common.conf.core.RunOptions;
import cern.c2mon.daq.common.logger.EquipmentLoggerFactory;
import cern.c2mon.daq.common.messaging.IProcessMessageSender;
import cern.c2mon.daq.filter.IFilterMessageSender;
import cern.c2mon.daq.filter.dynamic.IDynamicTimeDeadbandFilterActivator;
import cern.c2mon.daq.tools.DataTagValueFilter;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.DataTagDeadband;
import cern.c2mon.shared.daq.datatag.SourceDataTag;
import cern.c2mon.shared.daq.datatag.SourceDataTagValue;
import cern.c2mon.shared.daq.filter.FilteredDataTagValue;

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
  private RunOptions runOptionsMock;

	 @Before
	 public void setUp() {
		 this.processMessageSenderMock = createMock(IProcessMessageSender.class);
		 this.filterMessageSenderMock = createMock(IFilterMessageSender.class);

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

		 IDynamicTimeDeadbandFilterActivator activatorMock = createMock(IDynamicTimeDeadbandFilterActivator.class);
		 this.equipmentMessageSender = new EquipmentMessageSender(
				 this.filterMessageSenderMock, this.processMessageSenderMock, activatorMock , activatorMock);

		 this.tag = createSourceDataTag(1L, "sdt1", "Boolean", DataTagDeadband.DEADBAND_NONE, DataTagConstants.PRIORITY_LOW,
         false);
		 this.tag.getAddress().setTimeDeadband(30);

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

	 /**
	  * Send value
	  * 
	  * @throws Exception
	  */
	 @Test
	 public void testSchedule() throws Exception {
	   // Tag update
     this.tag.update(true, "test", new Timestamp(System.currentTimeMillis()));
     
		 EasyMock.expect(this.configurationControllerMock.getRunOptions()).andReturn(this.runOptionsMock);
		 EasyMock.expect(this.runOptionsMock.getEqLoggers()).andReturn(false);
		 EasyMock.expect(this.runOptionsMock.getEqAppendersOnly()).andReturn(false);

		 EasyMock.expect(this.configurationControllerMock.getProcessConfiguration()).andReturn(this.processConfigurationMock);
		 EasyMock.expect(this.processConfigurationMock.getProcessName()).andReturn("TEST_PROCESS_NAME");

		 // This message will be sent since it is the first one
		 this.processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
		 
		 replay(this.processMessageSenderMock, this.configurationControllerMock, this.processConfigurationMock, 
				 this.runOptionsMock);

		 EquipmentLoggerFactory equipmentLoggerFactory = EquipmentLoggerFactory.createFactory(this.conf,
				 this.configurationControllerMock.getProcessConfiguration(), this.configurationControllerMock.getRunOptions());

		 this.equipmentMessageSender.init(this.conf, equipmentLoggerFactory);

		 this.dataTagValueFilter = new DataTagValueFilter(equipmentLoggerFactory);
		 
		 // Filter module
     this.equipmentSenderFilterModule = new EquipmentSenderFilterModule(this.filterMessageSenderMock, equipmentLoggerFactory);

		 //        EquipmentLogger equipmentLogger = new EquipmentLogger("asd", "asd", "asd");
		 this.scheduler = new SDTTimeDeadbandScheduler(tag, this.processMessageSenderMock, this.equipmentSenderFilterModule, 
				 new Timer(true), this.dataTagValueFilter);
		 
		 this.scheduler.scheduleValueForSending();
		 this.scheduler.run();
		 
		 verify(this.processMessageSenderMock);
		 
		 if (this.exception != null)
			 throw new Exception(this.exception);
	 }
	 
	 /**
    * Send value
    * 
    * @throws Exception
    */
   @Test
   public void testScheduleNoFiltering() throws Exception {
     // Tag update
     this.tag.update(true, "test", new Timestamp(System.currentTimeMillis()));
     
     EasyMock.expect(this.configurationControllerMock.getRunOptions()).andReturn(this.runOptionsMock);
     EasyMock.expect(this.runOptionsMock.getEqLoggers()).andReturn(false);
     EasyMock.expect(this.runOptionsMock.getEqAppendersOnly()).andReturn(false);

     EasyMock.expect(this.configurationControllerMock.getProcessConfiguration()).andReturn(this.processConfigurationMock);
     EasyMock.expect(this.processConfigurationMock.getProcessName()).andReturn("TEST_PROCESS_NAME");

     // This message will be sent since it is the first one
     this.processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
     expectLastCall().times(2);
     
     replay(this.processMessageSenderMock, this.configurationControllerMock, this.processConfigurationMock, 
         this.runOptionsMock);

     EquipmentLoggerFactory equipmentLoggerFactory = EquipmentLoggerFactory.createFactory(this.conf,
         this.configurationControllerMock.getProcessConfiguration(), this.configurationControllerMock.getRunOptions());

     this.equipmentMessageSender.init(this.conf, equipmentLoggerFactory);

     this.dataTagValueFilter = new DataTagValueFilter(equipmentLoggerFactory);
     
     // Filter module
     this.equipmentSenderFilterModule = new EquipmentSenderFilterModule(this.filterMessageSenderMock, equipmentLoggerFactory);

     //        EquipmentLogger equipmentLogger = new EquipmentLogger("asd", "asd", "asd");
     this.scheduler = new SDTTimeDeadbandScheduler(tag, this.processMessageSenderMock, this.equipmentSenderFilterModule, 
         new Timer(true), this.dataTagValueFilter);
     
     this.scheduler.scheduleValueForSending();
     this.scheduler.run();
     
     // New value
     this.tag.update(false, "nacho New", new Timestamp(System.currentTimeMillis()));
     scheduler.scheduleValueForSending();
     this.scheduler.run();
     
     verify(this.processMessageSenderMock);
     
     if (this.exception != null)
       throw new Exception(this.exception);
   }

	 /**
	  * filter value: REPEATED_VALUE
	  * 
	  * @throws Exception
	  */
	 @Test
	 public void testScheduleRepeatedValue() throws Exception {
	   
	   // Tag update
	   this.tag.update(true, "test", new Timestamp(System.currentTimeMillis()));
	   
		 EasyMock.expect(this.configurationControllerMock.getRunOptions()).andReturn(this.runOptionsMock);
		 EasyMock.expect(this.runOptionsMock.getEqLoggers()).andReturn(false);
		 EasyMock.expect(this.runOptionsMock.getEqAppendersOnly()).andReturn(false);

		 EasyMock.expect(this.configurationControllerMock.getProcessConfiguration()).andReturn(this.processConfigurationMock);
		 EasyMock.expect(this.processConfigurationMock.getProcessName()).andReturn("TEST_PROCESS_NAME");

		 // This message will be sent since it is the first one
		 this.processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
		 // This message will be filtered with filter type REPEATED_VALUE
		 this.filterMessageSenderMock.addValue(isA(FilteredDataTagValue.class));
		 
		 replay(this.processMessageSenderMock, this.filterMessageSenderMock, this.configurationControllerMock, this.processConfigurationMock, 
				 this.runOptionsMock);

		 EquipmentLoggerFactory equipmentLoggerFactory = EquipmentLoggerFactory.createFactory(this.conf,
				 this.configurationControllerMock.getProcessConfiguration(), this.configurationControllerMock.getRunOptions());

		 this.equipmentMessageSender.init(this.conf, equipmentLoggerFactory);

		 this.dataTagValueFilter = new DataTagValueFilter(equipmentLoggerFactory);
		 
		 // Filter module
     this.equipmentSenderFilterModule = new EquipmentSenderFilterModule(this.filterMessageSenderMock, equipmentLoggerFactory);

//		 EquipmentLogger equipmentLogger = new EquipmentLogger("asd", "asd", "asd");
		 this.scheduler = new SDTTimeDeadbandScheduler(tag, this.processMessageSenderMock, this.equipmentSenderFilterModule, 
         new Timer(true), this.dataTagValueFilter);
		 
		 this.scheduler.scheduleValueForSending();
		 this.scheduler.run();
		 
		 // Repeated value 
		 this.scheduler.scheduleValueForSending();
		 this.scheduler.run();
		 
		 verify(this.processMessageSenderMock, this.filterMessageSenderMock);
		 
		 if (this.exception != null)
			 throw new Exception(this.exception);
	 }
	 
	 /**
    * filter value: REPEATED_VALUE
    * 
    * @throws Exception
    */
   @Test
   public void testScheduleFlashAndReset() throws Exception {
     
     // Tag update
     this.tag.update(true, "test", new Timestamp(System.currentTimeMillis()));
     
     EasyMock.expect(this.configurationControllerMock.getRunOptions()).andReturn(this.runOptionsMock);
     EasyMock.expect(this.runOptionsMock.getEqLoggers()).andReturn(false);
     EasyMock.expect(this.runOptionsMock.getEqAppendersOnly()).andReturn(false);

     EasyMock.expect(this.configurationControllerMock.getProcessConfiguration()).andReturn(this.processConfigurationMock);
     EasyMock.expect(this.processConfigurationMock.getProcessName()).andReturn("TEST_PROCESS_NAME");

     // This message will be sent 2 times to the server since the timer is cancelled and started again
     this.processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
     EasyMock.expectLastCall().times(2);
     
     replay(this.processMessageSenderMock, this.filterMessageSenderMock, this.configurationControllerMock, this.processConfigurationMock, 
         this.runOptionsMock);

     EquipmentLoggerFactory equipmentLoggerFactory = EquipmentLoggerFactory.createFactory(this.conf,
         this.configurationControllerMock.getProcessConfiguration(), this.configurationControllerMock.getRunOptions());

     this.equipmentMessageSender.init(this.conf, equipmentLoggerFactory);

     this.dataTagValueFilter = new DataTagValueFilter(equipmentLoggerFactory);
     
     // Filter module
     this.equipmentSenderFilterModule = new EquipmentSenderFilterModule(this.filterMessageSenderMock, equipmentLoggerFactory);

//     EquipmentLogger equipmentLogger = new EquipmentLogger("asd", "asd", "asd");
     this.scheduler = new SDTTimeDeadbandScheduler(tag, this.processMessageSenderMock, this.equipmentSenderFilterModule, 
         new Timer(true), this.dataTagValueFilter);
     
     this.scheduler.scheduleValueForSending();
     this.scheduler.run();
     
     // FlushAndReset
     this.scheduler.flushAndCancel();
     // Start the new task
     this.scheduler = new SDTTimeDeadbandScheduler(tag, this.processMessageSenderMock, this.equipmentSenderFilterModule, 
         new Timer(true), this.dataTagValueFilter);
     
     // Repeated value (should pass cause it was flush and reset)
     this.scheduler.scheduleValueForSending();
     this.scheduler.run();
     
     verify(this.processMessageSenderMock, this.filterMessageSenderMock);
     
     if (this.exception != null)
       throw new Exception(this.exception);
   }
	 
	 /**
	  * 
	  * @param id
	  * @param name
	  * @param dataType
	  * @param deadBandType
	  * @param priority
	  * @param guaranteed
	  * @return
	  */
	 private SourceDataTag createSourceDataTag(long id, String name, String dataType, short deadBandType, int priority,
       boolean guaranteed) {
   DataTagAddress address = new DataTagAddress(null, 100, deadBandType, VALUE_DEADBAND, 0, priority, guaranteed);
   return new SourceDataTag(id, name, false, DataTagConstants.MODE_OPERATIONAL, dataType, address);
}
}

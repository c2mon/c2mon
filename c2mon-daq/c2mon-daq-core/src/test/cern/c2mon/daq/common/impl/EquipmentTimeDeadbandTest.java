package cern.c2mon.daq.common.impl;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.daq.common.IDynamicTimeDeadbandFilterer;
import cern.c2mon.daq.common.conf.core.EquipmentConfiguration;
import cern.c2mon.daq.common.conf.core.ProcessConfiguration;
import cern.c2mon.daq.common.conf.core.RunOptions;
import cern.c2mon.daq.common.logger.EquipmentLoggerFactory;
import cern.c2mon.daq.common.messaging.IProcessMessageSender;
import cern.c2mon.daq.tools.EquipmentSenderHelper;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.DataTagDeadband;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;
import cern.c2mon.shared.daq.datatag.SourceDataTag;
import cern.c2mon.shared.daq.datatag.SourceDataTagValue;
import cern.c2mon.shared.daq.filter.FilteredDataTagValue.FilterType;

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
  private EquipmentSenderHelper equipmentSenderHelper = new EquipmentSenderHelper();
  
  /**
   * Mocks
   */
  private IDynamicTimeDeadbandFilterer dynamicTimeDeadbandFiltererMock;
  private EquipmentSenderFilterModule equipmentSenderFilterModuleMock;    
  private IProcessMessageSender processMessageSenderMock;
  
  
  @Before
  public void setUp() {
    this.dynamicTimeDeadbandFiltererMock = createMock(IDynamicTimeDeadbandFilterer.class);
    this.processMessageSenderMock = createMock(IProcessMessageSender.class);
    this.equipmentSenderFilterModuleMock = EasyMock.createMockBuilder(EquipmentSenderFilterModule.class).
        addMockedMethod("sendToFilterModule", SourceDataTag.class, Object.class, long.class, String.class, boolean.class, short.class).
        createMock();  
    
    EquipmentConfiguration equipmentConfiguration = new EquipmentConfiguration();
    equipmentConfiguration.setId(EQUIPMENT_ID);
    equipmentConfiguration.setCommFaultTagId(EQ_COMFAULT_ID);
    equipmentConfiguration.setCommFaultTagValue(false);

    sdt1 = createSourceDataTag(1L, "sdt1", "Boolean", DataTagDeadband.DEADBAND_PROCESS_RELATIVE, DataTagConstants.PRIORITY_LOW,
            false);
   
    equipmentConfiguration.getDataTags().put(1L, sdt1);

    ProcessConfiguration processConf = new ProcessConfiguration();
    processConf.setProcessID(1L);
    processConf.setProcessName("Test");
    equipmentConfiguration.setHandlerClassName("testClass");
    
  
    EquipmentLoggerFactory equipmentLoggerFactory = EquipmentLoggerFactory.createFactory(equipmentConfiguration,
        processConf, new RunOptions());
    
    this.equipmentTimeDeadbandTester = new EquipmentTimeDeadbandTester(this.dynamicTimeDeadbandFiltererMock, this.processMessageSenderMock, 
        this.equipmentSenderFilterModuleMock, equipmentLoggerFactory);
  }
  
  /**
   * Sending 3 valid values. The 2 first will be sent to the filter
   */
  @Test
  public void testAddValidTimeDeadband() {
    
    long ms = System.currentTimeMillis();

    this.dynamicTimeDeadbandFiltererMock.recordTag(isA(SourceDataTag.class));
    expectLastCall().times(3);

    // The first two values will be sent to the filter
    this.equipmentSenderFilterModuleMock.sendToFilterModule(this.sdt1, false, ms, 
        "", true, FilterType.TIME_DEADBAND.getNumber());
    expectLastCall().times(1);
    this.equipmentSenderFilterModuleMock.sendToFilterModule(this.sdt1, true, ms+1L, 
        "", true, FilterType.TIME_DEADBAND.getNumber());
    expectLastCall().times(1);

    this.sdt1.getAddress().setTimeDeadband(1);
    this.sdt1.update(true);

    replay(this.dynamicTimeDeadbandFiltererMock, this.equipmentSenderFilterModuleMock);

    // The first one does nothing
    this.equipmentTimeDeadbandTester.addToTimeDeadband(this.sdt1, false, ms, "");
    assertEquals(false, this.sdt1.getCurrentValue().getValue());

    // The second one is valid so it sends the first one to the filter
    this.equipmentTimeDeadbandTester.addToTimeDeadband(this.sdt1, true, ms+1L, "");
    assertEquals(true, this.sdt1.getCurrentValue().getValue());

    // The third one is valid so it sends the second one to the filter
    this.equipmentTimeDeadbandTester.addToTimeDeadband(this.sdt1, false, ms+2L, "");
    assertEquals(false, this.sdt1.getCurrentValue().getValue());

    verify(this.dynamicTimeDeadbandFiltererMock, this.equipmentSenderFilterModuleMock);
  }
  
  /**
   * Sending 2 valid and 1 invalid values. The first will be sent to the filter.
   * The second will be sent to the server after flush and cancel
   */
  @Test
  public void testAddValidInvalidTimeDeadband() {
    
    long ms = System.currentTimeMillis();

    this.dynamicTimeDeadbandFiltererMock.recordTag(isA(SourceDataTag.class));
    expectLastCall().times(3);

    // Only the first value will be sent to the filter
    this.equipmentSenderFilterModuleMock.sendToFilterModule(this.sdt1, false, ms, 
        "", true, FilterType.TIME_DEADBAND.getNumber());
    expectLastCall().times(1);

    this.sdt1.getAddress().setTimeDeadband(1);
    this.sdt1.update(true);

    replay(this.dynamicTimeDeadbandFiltererMock, this.equipmentSenderFilterModuleMock);

    // The first one does nothing
    this.equipmentTimeDeadbandTester.addToTimeDeadband(this.sdt1, false, ms, "");
    assertEquals(false, this.sdt1.getCurrentValue().getValue());

    // The second one is valid so it sends the first one to the filter
    this.equipmentTimeDeadbandTester.addToTimeDeadband(this.sdt1, true, ms+1L, "");
    assertEquals(true, this.sdt1.getCurrentValue().getValue());

    SourceDataTagValue sourceDTValue = this.sdt1.getCurrentValue();
    // Get the source data quality from the quality code and description
    SourceDataQuality newSDQuality = this.equipmentSenderHelper.createTagQualityObject(SourceDataQuality.DATA_UNAVAILABLE,
        sourceDTValue.getQuality().getDescription());
    
    // The third one is invalid so it flush and cancel. It run the run() for the first time so the second value is sent to the server
    this.equipmentTimeDeadbandTester.addToTimeDeadband(this.sdt1, true, ms+2L, "", newSDQuality);
    assertEquals(true, this.sdt1.getCurrentValue().getValue());
    assertEquals(SourceDataQuality.DATA_UNAVAILABLE, this.sdt1.getCurrentValue().getQuality().getQualityCode());

    verify(this.dynamicTimeDeadbandFiltererMock, this.equipmentSenderFilterModuleMock);
  }
  
  /**
   * Sending 2 valid and 1 invalid values. The first will be sent to the filter.
   * The second will be sent to the server after flush and cancel
   */
  @Test
  public void testAddValidInvalidValidTimeDeadband() {
    
    long ms = System.currentTimeMillis();

    this.dynamicTimeDeadbandFiltererMock.recordTag(isA(SourceDataTag.class));
    expectLastCall().times(3);

    this.sdt1.getAddress().setTimeDeadband(1);
    this.sdt1.update(true);

    replay(this.dynamicTimeDeadbandFiltererMock, this.equipmentSenderFilterModuleMock);

    // The first one does nothing
    this.equipmentTimeDeadbandTester.addToTimeDeadband(this.sdt1, false, ms, "");
    assertEquals(false, this.sdt1.getCurrentValue().getValue());

    SourceDataTagValue sourceDTValue = this.sdt1.getCurrentValue();
    // Get the source data quality from the quality code and description
    SourceDataQuality newSDQuality = this.equipmentSenderHelper.createTagQualityObject(SourceDataQuality.DATA_UNAVAILABLE,
        sourceDTValue.getQuality().getDescription());
    
    // The second one is invalid so it flush and cancel. It run the run() for the first time so the value is sent to the server
    this.equipmentTimeDeadbandTester.addToTimeDeadband(this.sdt1, true, ms+1L, "", newSDQuality);
    assertEquals(true, this.sdt1.getCurrentValue().getValue());
    assertEquals(SourceDataQuality.DATA_UNAVAILABLE, this.sdt1.getCurrentValue().getQuality().getQualityCode());
    
    // The third one is valid so it flush and cancel again
    this.equipmentTimeDeadbandTester.addToTimeDeadband(this.sdt1, false, ms+2L, "");
    assertEquals(false, this.sdt1.getCurrentValue().getValue());
    assertEquals(SourceDataQuality.OK, this.sdt1.getCurrentValue().getQuality().getQualityCode());

    verify(this.dynamicTimeDeadbandFiltererMock, this.equipmentSenderFilterModuleMock);
  }
  
  @Test
  public void testRemoveTagTimeDeadband() {
    
    long ms = System.currentTimeMillis();

    this.dynamicTimeDeadbandFiltererMock.recordTag(isA(SourceDataTag.class));
    expectLastCall().times(1);

    this.sdt1.getAddress().setTimeDeadband(1);
    this.sdt1.update(true);

    replay(this.dynamicTimeDeadbandFiltererMock);

    // Add the tag to the TimeDBScheduler
    this.equipmentTimeDeadbandTester.addToTimeDeadband(this.sdt1, false, ms, "");
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

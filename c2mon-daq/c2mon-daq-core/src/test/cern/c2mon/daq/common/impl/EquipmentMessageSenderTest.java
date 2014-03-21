package cern.c2mon.daq.common.impl;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.sql.Timestamp;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.daq.common.conf.core.EquipmentConfiguration;
import cern.c2mon.daq.common.conf.core.ProcessConfiguration;
import cern.c2mon.daq.common.conf.core.RunOptions;
import cern.c2mon.daq.common.impl.EquipmentMessageSender;
import cern.c2mon.daq.common.logger.EquipmentLoggerFactory;
import cern.c2mon.daq.common.messaging.IProcessMessageSender;
import cern.c2mon.daq.filter.IFilterMessageSender;
import cern.c2mon.daq.filter.dynamic.IDynamicTimeDeadbandFilterActivator;
import cern.c2mon.daq.tools.EquipmentSenderHelper;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.DataTagDeadband;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;
import cern.c2mon.shared.daq.datatag.SourceDataTag;
import cern.c2mon.shared.daq.datatag.SourceDataTagValue;
import cern.c2mon.shared.daq.filter.FilteredDataTagValue;

public class EquipmentMessageSenderTest {

    private static final long EQ_COMFAULT_ID = 1L;
    private static final float VALUE_DEADBAND = 25.0f;
    private static final long EQUIPMENT_ID = 1L;
    private static final Long SUB_KEY1 = 1337L;
    private static final Long SUB_KEY2 = 31415926L;

    // Mocks
    private IFilterMessageSender filterMessageSenderMock;
    private IProcessMessageSender processMessageSenderMock;
    private IDynamicTimeDeadbandFilterActivator medDynamicTimeDeadbandFilterActivatorMock;
    private IDynamicTimeDeadbandFilterActivator lowDynamicTimeDeadbandFilterActivatorMock;
    
    private SourceDataTag sdt1;
    private SourceDataTag sdt2;
    private SourceDataTag sdt3;

    private EquipmentMessageSender equipmentMessageSender;
    private EquipmentSenderHelper equipmentSenderHelper = new EquipmentSenderHelper();

    @Before
    public void setUp() {
        filterMessageSenderMock = createStrictMock(IFilterMessageSender.class);
        processMessageSenderMock = createStrictMock(IProcessMessageSender.class);
        medDynamicTimeDeadbandFilterActivatorMock = createStrictMock(IDynamicTimeDeadbandFilterActivator.class);
        lowDynamicTimeDeadbandFilterActivatorMock = createStrictMock(IDynamicTimeDeadbandFilterActivator.class);
        equipmentMessageSender = new EquipmentMessageSender(filterMessageSenderMock, processMessageSenderMock,
                medDynamicTimeDeadbandFilterActivatorMock, lowDynamicTimeDeadbandFilterActivatorMock);
        EquipmentConfiguration equipmentConfiguration = new EquipmentConfiguration();
        equipmentConfiguration.setId(EQUIPMENT_ID);
        equipmentConfiguration.setCommFaultTagId(EQ_COMFAULT_ID);
        equipmentConfiguration.setCommFaultTagValue(false);

        sdt1 = createSourceDataTag(1L, "sdt1", "Boolean", DataTagDeadband.DEADBAND_NONE, DataTagConstants.PRIORITY_LOW,
                false);
        sdt2 = createSourceDataTag(2L, "sdt2", "Float", DataTagDeadband.DEADBAND_PROCESS_ABSOLUTE,
                DataTagConstants.PRIORITY_MEDIUM, false);
        sdt3 = createSourceDataTag(3L, "sdt3", "Integer", DataTagDeadband.DEADBAND_PROCESS_RELATIVE_VALUE_DESCR_CHANGE,
                DataTagConstants.PRIORITY_LOW, false);

        equipmentConfiguration.getDataTags().put(1L, sdt1);
        equipmentConfiguration.getDataTags().put(2L, sdt2);
        equipmentConfiguration.getDataTags().put(3L, sdt3);

        equipmentConfiguration.getSubEqCommFaultValues().put(SUB_KEY1, false);
        equipmentConfiguration.getSubEqCommFaultValues().put(SUB_KEY2, true);

//        equipmentMessageSender.setEquipmentConfiguration(equipmentConfiguration);
        ProcessConfiguration processConf = new ProcessConfiguration();
        processConf.setProcessID(1L);
        processConf.setProcessName("ad");
        equipmentConfiguration.setHandlerClassName("asd");
//        equipmentMessageSender.setEquipmentLoggerFactory(EquipmentLoggerFactory.createFactory(equipmentConfiguration,
//                processConf, new RunOptions()));
        EquipmentLoggerFactory equipmentLoggerFactory = EquipmentLoggerFactory.createFactory(equipmentConfiguration,
                processConf, new RunOptions());
        
        this.equipmentMessageSender.init(equipmentConfiguration, equipmentLoggerFactory);
        
        // Setup calls should not affect later tests
        reset(lowDynamicTimeDeadbandFilterActivatorMock, medDynamicTimeDeadbandFilterActivatorMock, filterMessageSenderMock,
                processMessageSenderMock);     
    }

    @Test
    public void testEquipmentStateIncorrect() {
        processMessageSenderMock.sendCommfaultTag(EQ_COMFAULT_ID, false);
        processMessageSenderMock.sendCommfaultTag(SUB_KEY1, false);
        processMessageSenderMock.sendCommfaultTag(SUB_KEY2, true);

        replay(processMessageSenderMock);

        equipmentMessageSender.confirmEquipmentStateIncorrect();

        verify(processMessageSenderMock);
    }

    @Test
    public void testEquipmentStateIncorrectDescription() {
        String description = "description";
        processMessageSenderMock.sendCommfaultTag(EQ_COMFAULT_ID, false, description);
        processMessageSenderMock.sendCommfaultTag(SUB_KEY1, false, description);
        processMessageSenderMock.sendCommfaultTag(SUB_KEY2, true, description);

        replay(processMessageSenderMock);

        equipmentMessageSender.confirmEquipmentStateIncorrect(description);

        verify(processMessageSenderMock);
    }

    @Test
    public void testEquipmentStateOK() {
        processMessageSenderMock.sendCommfaultTag(EQ_COMFAULT_ID, true);
        processMessageSenderMock.sendCommfaultTag(SUB_KEY1, true);
        processMessageSenderMock.sendCommfaultTag(SUB_KEY2, false);

        replay(processMessageSenderMock);

        equipmentMessageSender.confirmEquipmentStateOK();

        verify(processMessageSenderMock);
    }

    @Test
    public void testEquipmentStateOKDescription() {
        String description = "description";
        processMessageSenderMock.sendCommfaultTag(EQ_COMFAULT_ID, true, description);
        processMessageSenderMock.sendCommfaultTag(SUB_KEY1, true, description);
        processMessageSenderMock.sendCommfaultTag(SUB_KEY2, false, description);

        replay(processMessageSenderMock);

        equipmentMessageSender.confirmEquipmentStateOK(description);

        verify(processMessageSenderMock);
    }

    @Test
    public void testOnAddDataTag() {
        SourceDataTag sdtLow = createSourceDataTag(5324L, "", "Float", DataTagDeadband.DEADBAND_NONE,
                DataTagConstants.PRIORITY_LOW, false);
        SourceDataTag sdtMed = createSourceDataTag(5325L, "", "Float", DataTagDeadband.DEADBAND_NONE,
                DataTagConstants.PRIORITY_MEDIUM, false);
        SourceDataTag sdtHigh = createSourceDataTag(5326L, "", "Float", DataTagDeadband.DEADBAND_NONE,
                DataTagConstants.PRIORITY_HIGH, false);

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
         * Update should try to remove sdt2 from any other activators and add it to the low one.
         */
        lowDynamicTimeDeadbandFilterActivatorMock.removeDataTag(sdt2);
        medDynamicTimeDeadbandFilterActivatorMock.removeDataTag(sdt2);
        lowDynamicTimeDeadbandFilterActivatorMock.addDataTag(sdt2);

        /*
         * Update should try to remove sdt3 from any other activators and add it to the low one.
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
    public void testSendInvalidTag() {
        lowDynamicTimeDeadbandFilterActivatorMock.newTagValueSent(sdt1.getId());
        processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
        filterMessageSenderMock.addValue(isA(FilteredDataTagValue.class));

        replay(lowDynamicTimeDeadbandFilterActivatorMock, processMessageSenderMock, filterMessageSenderMock);

        // The first one has null currentSDValue and should not be filter
        equipmentMessageSender.sendInvalidTag(sdt1, SourceDataQuality.DATA_UNAVAILABLE, "");
        // The second should go to filter module because the it has same currentSDvalue and newValue (null)
        // and same Quality
        equipmentMessageSender.sendInvalidTag(sdt1, SourceDataQuality.DATA_UNAVAILABLE, "");

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

      // Lets figure out the Time Deadband is enabled (in this case would be the Dynamic)
      this.sdt1.getAddress().setTimeDeadband(1);
      this.sdt1.update(true);

      replay(this.lowDynamicTimeDeadbandFilterActivatorMock, this.processMessageSenderMock, this.filterMessageSenderMock);

      // The first one: the run method sends it to the server with NO_FILTERING (first time running the schedule)
      this.equipmentMessageSender.sendTagFiltered(sdt1, false, System.currentTimeMillis() + 1L, "Nacho");
      assertEquals(false, this.sdt1.getCurrentValue().getValue());
      Thread.sleep(300);

      // The second one is also sent to the server since the value is different
      this.equipmentMessageSender.sendTagFiltered(sdt1, true, System.currentTimeMillis() + 2L, "Nacho");
      assertEquals(true, this.sdt1.getCurrentValue().getValue());
      Thread.sleep(300);

      // The third one is filtered with REPEATED_VALUE because the value and value description are the same
      this.equipmentMessageSender.sendTagFiltered(sdt1, true, System.currentTimeMillis() + 3L, "Nacho");
      assertEquals(true, this.sdt1.getCurrentValue().getValue());
      Thread.sleep(300);

      // The fourth one is also sent to the server since the value is different
      this.equipmentMessageSender.sendTagFiltered(sdt1, false, System.currentTimeMillis() + 4L);
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
        this.sdt1.update(true);

        replay(this.lowDynamicTimeDeadbandFilterActivatorMock, this.processMessageSenderMock);

        // The first one: the run method sends it to the server with NO_FILTERING (first time running the schedule)
        this.equipmentMessageSender.sendTagFiltered(sdt1, false, System.currentTimeMillis() + 1L);
        Thread.sleep(300);
        assertEquals(false, this.sdt1.getCurrentValue().getValue());
        
        // The second one is also sent to the server since the value is different
        this.equipmentMessageSender.sendTagFiltered(sdt1, true, System.currentTimeMillis() + 2L);
        Thread.sleep(300);
        assertEquals(true, this.sdt1.getCurrentValue().getValue());
        
        // The third one is invalid so it flush and cancel. It run the run() for the first time so the second value is sent to the server
        this.equipmentMessageSender.sendInvalidTag(sdt1, SourceDataQuality.DATA_UNAVAILABLE, "");
        Thread.sleep(300);
        assertEquals(SourceDataQuality.DATA_UNAVAILABLE, this.sdt1.getCurrentValue().getQuality().getQualityCode());

        verify(this.lowDynamicTimeDeadbandFilterActivatorMock, this.processMessageSenderMock);
    }
    
    @Test
    public void testSendValidInvalidValidTagTimeDeadbandEnabled() throws Exception {
        lowDynamicTimeDeadbandFilterActivatorMock.newTagValueSent(sdt1.getId());
        expectLastCall().times(4);
        processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
        expectLastCall().times(4);
        
        this.sdt1.getAddress().setTimeDeadband(1);
        this.sdt1.update(true);

        replay(this.lowDynamicTimeDeadbandFilterActivatorMock, this.processMessageSenderMock);

        // The first one: the run method sends it to the server with NO_FILTERING (first time running the schedule)
        this.equipmentMessageSender.sendTagFiltered(sdt1, false, System.currentTimeMillis() + 1L);
        Thread.sleep(200);
        assertEquals(false, this.sdt1.getCurrentValue().getValue());
        
        // The second one is also sent to the server since the value is different
        this.equipmentMessageSender.sendTagFiltered(sdt1, true, System.currentTimeMillis() + 2L);
        Thread.sleep(200);
        assertEquals(true, this.sdt1.getCurrentValue().getValue());
        
        // The third one is invalid so it flush and cancel. It run the run() for the first time so the second value is sent to the server
        this.equipmentMessageSender.sendInvalidTag(sdt1, SourceDataQuality.DATA_UNAVAILABLE, "");
        Thread.sleep(200);
        assertEquals(SourceDataQuality.DATA_UNAVAILABLE, this.sdt1.getCurrentValue().getQuality().getQualityCode());
        
        // The forth one is also sent to the server since the value is different (flush and cancel again since the last one was invalid)
        this.equipmentMessageSender.sendTagFiltered(sdt1, true, System.currentTimeMillis() + 3L);
        Thread.sleep(200);
        assertEquals(true, this.sdt1.getCurrentValue().getValue());
        assertEquals(SourceDataQuality.OK, this.sdt1.getCurrentValue().getQuality().getQualityCode());

        verify(this.lowDynamicTimeDeadbandFilterActivatorMock, this.processMessageSenderMock);
    }
    
    @Test
    public void testSendInvalidTimeDeadbandEnabledDisable() throws Exception {
        lowDynamicTimeDeadbandFilterActivatorMock.newTagValueSent(sdt1.getId());
        expectLastCall().times(2);
        processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
        expectLastCall().times(2);
        
        this.sdt1.getAddress().setTimeDeadband(1);
        this.sdt1.update(true);

        replay(this.lowDynamicTimeDeadbandFilterActivatorMock, this.processMessageSenderMock);
        
        // The first one: the run method sends it to the server with NO_FILTERING (first time running the schedule)
        this.equipmentMessageSender.sendInvalidTag(sdt1, SourceDataQuality.DATA_UNAVAILABLE, "",  new Timestamp(System.currentTimeMillis() + 1L));
        Thread.sleep(500);
        assertEquals(SourceDataQuality.DATA_UNAVAILABLE, this.sdt1.getCurrentValue().getQuality().getQualityCode());

        
        // Time dead band disable
        this.sdt1.getAddress().setTimeDeadband(0);
        
        // The second one is also sent to the server since all the checks are done and there is no Time Deadband
        this.equipmentMessageSender.sendInvalidTag(sdt1, SourceDataQuality.UNKNOWN, "", new Timestamp(System.currentTimeMillis() + 2L));
        Thread.sleep(300);
        assertEquals(SourceDataQuality.UNKNOWN, this.sdt1.getCurrentValue().getQuality().getQualityCode());

        verify(this.lowDynamicTimeDeadbandFilterActivatorMock, this.processMessageSenderMock);
    }
    
    @Test
    public void testSendValidTimeDeadbandEnabledDisable() throws Exception {
        lowDynamicTimeDeadbandFilterActivatorMock.newTagValueSent(sdt1.getId());
        expectLastCall().times(2);
        processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
        expectLastCall().times(2);
        
        this.sdt1.getAddress().setTimeDeadband(1);
        this.sdt1.update(true);

        replay(this.lowDynamicTimeDeadbandFilterActivatorMock, this.processMessageSenderMock);

        // The first one: the run method sends it to the server with NO_FILTERING (first time running the schedule)
        this.equipmentMessageSender.sendTagFiltered(sdt1, false, System.currentTimeMillis() + 1L);
        Thread.sleep(200);
        assertEquals(false, this.sdt1.getCurrentValue().getValue());
        
        // Time dead band disable
        this.sdt1.getAddress().setTimeDeadband(0);
        
        // The second one is also sent to the server since all the checks are done and there is no Time Deadband
        this.equipmentMessageSender.sendTagFiltered(sdt1, true, System.currentTimeMillis() + 2L);
        Thread.sleep(200);
        assertEquals(true, this.sdt1.getCurrentValue().getValue());

        verify(this.lowDynamicTimeDeadbandFilterActivatorMock, this.processMessageSenderMock);
    }
    
    @Test
    public void testSendInvalidTagFutureSourceTS() {
      // Add value to the SourceDatTag
      this.sdt2.update(false);

      // One value is added
      this.processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
      expectLastCall().times(1);
      // One value is filtered out
      this.filterMessageSenderMock.addValue(isA(FilteredDataTagValue.class));
      expectLastCall().times(1);

      replay(this.processMessageSenderMock, this.filterMessageSenderMock);

      SourceDataTagValue sourceDTValue = this.sdt2.getCurrentValue();
      // Get the source data quality from the quality code and description
      SourceDataQuality newSDQuality = this.equipmentSenderHelper.createTagQualityObject(SourceDataQuality.FUTURE_SOURCE_TIMESTAMP,
          sourceDTValue.getQuality().getDescription());
      
      // It has:
      // - same currentSDValue and new value (false)
      // - same Value Description
      // - different Quality Code (OK vs FUTURE_SOURCE_TIMESTAMP)
      // - same Quality Description
      // 
      // Should not be filtered 
      this.equipmentMessageSender.sendInvalidTag(this.sdt2, sourceDTValue.getValue(), sourceDTValue.getValueDescription(), 
          newSDQuality, new Timestamp(System.currentTimeMillis() + 1L));
      
      assertEquals(SourceDataQuality.FUTURE_SOURCE_TIMESTAMP, this.sdt2.getCurrentValue().getQuality().getQualityCode());
      
      // It has:
      // - same currentSDValue and new value (false)
      // - same Value Description
      // - same Quality Code (FUTURE_SOURCE_TIMESTAMP)
      // - same Quality Description
      // 
      //Should be filtered 
      this.equipmentMessageSender.sendInvalidTag(this.sdt2, sourceDTValue.getValue(), sourceDTValue.getValueDescription(), 
          newSDQuality, new Timestamp(System.currentTimeMillis() + 2L));

      verify(this.processMessageSenderMock, this.filterMessageSenderMock);
    }
    
    @Test
    public void testSendInvalidTagDifQuality() {
      // Add value to the SourceDatTag
      this.sdt1.update(false);
     
      // One value is added
      this.processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
      expectLastCall().times(1);

      replay(this.processMessageSenderMock, this.filterMessageSenderMock);

      SourceDataTagValue sourceDTValue = this.sdt1.getCurrentValue();
      // Get the source data quality from the quality code and description
      SourceDataQuality newSDQuality = this.equipmentSenderHelper.createTagQualityObject(SourceDataQuality.DATA_UNAVAILABLE, 
          sourceDTValue.getQuality().getDescription());
      
      // It has:
      // - same currentSDValue and new value (false)
      // - same Value Description
      // - different Quality Code (OK vs DATA_UNAVAILABLE)
      // - same Quality Description
      //
      // Should not be filtered 
      this.equipmentMessageSender.sendInvalidTag(this.sdt1, sourceDTValue.getValue(), sourceDTValue.getValueDescription(), 
          newSDQuality, new Timestamp(System.currentTimeMillis() + 1L));
      
      // The Quality Code has changed
      assertEquals(SourceDataQuality.DATA_UNAVAILABLE, this.sdt1.getCurrentValue().getQuality().getQualityCode());

      verify(this.processMessageSenderMock, this.filterMessageSenderMock);
    }
    
    @Test
    /**
     * Note: Value-based deadband filtering is enabled for the process (we use sdt2)
     */
    public void testSendInvalidTagDifValue() {
      // Add value to the SourceDatTag
      this.sdt2.update(true);
      
      // One value is added
      this.processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
      expectLastCall().times(1);

      replay(this.processMessageSenderMock, this.filterMessageSenderMock);

      SourceDataTagValue sourceDTValue = this.sdt2.getCurrentValue();
      // Get the source data quality from the quality code and description
      SourceDataQuality newSDQuality = this.equipmentSenderHelper.createTagQualityObject(SourceDataQuality.DATA_UNAVAILABLE, 
          sourceDTValue.getQuality().getDescription());
      
      // It has:
      // - different currentSDValue and new value (true vs false)
      // - same Value Description 
      // - different Quality Code (OK vs DATA_UNAVAILABLE)
      // - same Quality Description
      //
      // Should not be filtered      
      this.equipmentMessageSender.sendInvalidTag(this.sdt2, false, sourceDTValue.getValueDescription(), newSDQuality, 
          new Timestamp(System.currentTimeMillis() + 1L));
      
      // The Value has changed
      assertEquals(false, this.sdt2.getCurrentValue().getValue());
      // The Quality Code has changed
      assertEquals(SourceDataQuality.DATA_UNAVAILABLE, this.sdt2.getCurrentValue().getQuality().getQualityCode());

      verify(this.processMessageSenderMock, this.filterMessageSenderMock);
    }
    
    @Test
    public void testSendInvalidTagIsValueDeadbandFiltered() throws Exception {
      // Give current values (are null be default)
      this.sdt2.update(1);
      this.sdt3.update(1);

      // 5 values should not be filtered out
      this.processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
      expectLastCall().times(5);
      // 5 values should be filtered out
      this.filterMessageSenderMock.addValue(isA(FilteredDataTagValue.class));
      expectLastCall().times(5);

      replay(this.processMessageSenderMock, this.filterMessageSenderMock);

      SourceDataTagValue sourceDTValue = this.sdt3.getCurrentValue();
      // Get the source data quality from the quality code and description 
      //  - quality code is OK by default and it will never go for invalidation because is a special case. New quality code added
      SourceDataQuality newSDQuality = this.equipmentSenderHelper.createTagQualityObject(SourceDataQuality.DATA_UNAVAILABLE, 
          sourceDTValue.getQuality().getDescription());

      // Relative. Should not be filtered      
      this.equipmentMessageSender.sendInvalidTag(this.sdt3, 109, "value description 1", newSDQuality, 
          new Timestamp(System.currentTimeMillis() + 2L));

      // Should not be filtered because value descr. is different from the previous one
      this.equipmentMessageSender.sendInvalidTag(this.sdt3, 108, "", newSDQuality, 
          new Timestamp(System.currentTimeMillis() + 3L));

      // Should not be filtered because value descr. is different from the previous one
      this.equipmentMessageSender.sendInvalidTag(this.sdt3, 109, "value description 2", newSDQuality, 
          new Timestamp(System.currentTimeMillis() + 4L));

      // Should be filtered because value descr. has not change
      this.equipmentMessageSender.sendInvalidTag(this.sdt3, 110, "value description 2", newSDQuality, 
          new Timestamp(System.currentTimeMillis() + 5L));

      // Should be filtered (is Relative Value Deadband) 
      this.equipmentMessageSender.sendInvalidTag(this.sdt3, 108, "value description 2", newSDQuality, 
          new Timestamp(System.currentTimeMillis() + 6L));

      // Should be filtered (is Relative Value Deadband)
      this.equipmentMessageSender.sendInvalidTag(this.sdt3, 108, "value description 2", newSDQuality, 
          new Timestamp(System.currentTimeMillis() + 7L));
      
      // Should not be filtered because quality is OK even if value is very close to previous one
      this.equipmentMessageSender.sendTagFiltered(this.sdt3, 107, System.currentTimeMillis() + 8L, "value description 2");
      assertEquals(SourceDataQuality.OK, this.sdt3.getCurrentValue().getQuality().getQualityCode());


      // absolute
      sourceDTValue = this.sdt2.getCurrentValue();
      //    - quality code is OK by default and it will never go for invalidation because is a special case. New quality code added
      newSDQuality = this.equipmentSenderHelper.createTagQualityObject(SourceDataQuality.DATA_UNAVAILABLE, 
          sourceDTValue.getQuality().getDescription());

      // Should not be filtered (different Value and Quality Code)
      this.equipmentMessageSender.sendInvalidTag(this.sdt2, 9.0f, "", newSDQuality, new Timestamp(System.currentTimeMillis() + 9L));
      // Should be filtered (is Absolute ValueDeadband)
      this.equipmentMessageSender.sendInvalidTag(this.sdt2, 9.1f, "", newSDQuality, new Timestamp(System.currentTimeMillis() + 10L));
      // Should be filtered (is Absolute ValueDeadband)
      this.equipmentMessageSender.sendInvalidTag(this.sdt2, 9.2f, "", newSDQuality, new Timestamp(System.currentTimeMillis() + 11L));

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
        sdt1.update(false);

        equipmentConfiguration.getDataTags().put(1L, sdt1);

        ProcessConfiguration processConf = new ProcessConfiguration();
        processConf.setProcessID(1L);
        processConf.setProcessName("ad");
        equipmentConfiguration.setHandlerClassName("asd");

        equipmentMessageSender.init(equipmentConfiguration, EquipmentLoggerFactory.createFactory(equipmentConfiguration,
                processConf, new RunOptions()));

        // setup calls should not affect later tests
        reset(filterMessageSenderMock, processMessageSenderMock);

        // the processMessageSender should be called exactly 3 times.
        processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
        expectLastCall().times(3);

        // the filterMessageSender should be called exactly once
        filterMessageSenderMock.addValue(isA(FilteredDataTagValue.class));

        replay(processMessageSenderMock, filterMessageSenderMock);

        // send value "true" to the server
        equipmentMessageSender.sendTagFiltered(sdt1, true, System.currentTimeMillis() + 1L);

        // invalidate the tag
        equipmentMessageSender.sendInvalidTag(sdt1, SourceDataQuality.DATA_UNAVAILABLE, "",  new Timestamp(System.currentTimeMillis() + 2L));

        // send again value "true" to the server ( it should not be filtered out, because
        // it was invalidated previously)
        equipmentMessageSender.sendTagFiltered(sdt1, true, System.currentTimeMillis() + 3L);

        // sleep a bit
        Thread.sleep(120);

        // update again, with unchanged value this update should go to filter module
        equipmentMessageSender.sendTagFiltered(sdt1, true, System.currentTimeMillis() + 4L);

        verify(processMessageSenderMock, filterMessageSenderMock);
    }

    @Test
    public void testSendSupervisionAlive() {
        processMessageSenderMock.addValue(isA(SourceDataTagValue.class));

        replay(processMessageSenderMock);

        equipmentMessageSender.sendSupervisionAlive();

        verify(processMessageSenderMock);
    }

    @Test
    public void testSendTagFiltered() {
        lowDynamicTimeDeadbandFilterActivatorMock.newTagValueSent(sdt1.getId());
        processMessageSenderMock.addValue(isA(SourceDataTagValue.class));

        replay(lowDynamicTimeDeadbandFilterActivatorMock, processMessageSenderMock);
        sdt1.getAddress().setTimeDeadband(0);
        equipmentMessageSender.sendTagFiltered(sdt1, false, System.currentTimeMillis() + 1L);

        assertEquals(SourceDataQuality.OK, sdt1.getCurrentValue().getQuality().getQualityCode());

        verify(lowDynamicTimeDeadbandFilterActivatorMock, processMessageSenderMock);
    }

    @Test
    public void testSendTagFilteredInvalidTimestamp() throws Exception {
        this.lowDynamicTimeDeadbandFilterActivatorMock.newTagValueSent(sdt1.getId());
        expectLastCall().times(2);
        
        this.processMessageSenderMock.addValue(isA(SourceDataTagValue.class));

        // even though we try to send twice the tag, it should only be invalidated once, since the quality code does not
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
        this.equipmentMessageSender.sendTagFiltered(this.sdt1, false, futureTimestamp);

        // Quality has change to FUTURE_SOURCE_TIMESTAMP
        assertEquals(SourceDataQuality.FUTURE_SOURCE_TIMESTAMP, this.sdt1.getCurrentValue().getQuality().getQualityCode());

//        String qualityDesc = String
//                .format("value: false received with source timestamp: [%s] in the future! No further updates will be processed and the tag's value will stay unchanged until this problem is fixed",
//                        new Timestamp(futureTimestamp));
//        
//        // Problema con test "Value received with source timestamp in the future! Time on server was: " + new Timestamp(System.currentTimeMillis()));
//
//        System.out.println(qualityDesc);
//
//        assertEquals(qualityDesc, sdt1.getCurrentValue().getQuality().getDescription());
//
//        Thread.sleep(200);

        // Should be filtered (FUTURE_SOURCE_TIMESTAMP)
        this.equipmentMessageSender.sendTagFiltered(this.sdt1, false, futureTimestamp2);
        
        // Quality did not change
        assertEquals(SourceDataQuality.FUTURE_SOURCE_TIMESTAMP, sdt1.getCurrentValue().getQuality().getQualityCode());
        
        // Should not be filtered 
        this.equipmentMessageSender.sendTagFiltered(this.sdt1, true, futureTimestamp3);
        
        // Another value update should be sent with invalid quality FUTURE_SOURCE_TIMESTAMP
        assertEquals(SourceDataQuality.FUTURE_SOURCE_TIMESTAMP, sdt1.getCurrentValue().getQuality().getQualityCode());
        assertEquals(true, sdt1.getCurrentValue().getValue());
        assertEquals(futureTimestamp3, sdt1.getCurrentValue().getTimestamp().getTime());

        verify(this.lowDynamicTimeDeadbandFilterActivatorMock, this.processMessageSenderMock, this.filterMessageSenderMock);
    }

    @Test
    public void testSendTagFilteredNotConvertable() {
        medDynamicTimeDeadbandFilterActivatorMock.newTagValueSent(sdt2.getId());
        processMessageSenderMock.addValue(isA(SourceDataTagValue.class));

        replay(lowDynamicTimeDeadbandFilterActivatorMock, processMessageSenderMock);

        equipmentMessageSender.sendTagFiltered(sdt2, "asdasdasd", System.currentTimeMillis() + 1L);

        assertEquals(SourceDataQuality.CONVERSION_ERROR, sdt2.getCurrentValue().getQuality().getQualityCode());

        verify(lowDynamicTimeDeadbandFilterActivatorMock, processMessageSenderMock);
    }

    @Test
    public void testSendTagFilteredNotConvertableTimeDeadbandEnabled() {
        medDynamicTimeDeadbandFilterActivatorMock.newTagValueSent(sdt2.getId());
        processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
        
        this.sdt2.getAddress().setTimeDeadband(1);

        replay(lowDynamicTimeDeadbandFilterActivatorMock, processMessageSenderMock);

        equipmentMessageSender.sendTagFiltered(sdt2, "Nacho", System.currentTimeMillis() + 1L);
        equipmentMessageSender.sendTagFiltered(sdt2, "Nacho", System.currentTimeMillis() + 2L);

        assertEquals(SourceDataQuality.CONVERSION_ERROR, sdt2.getCurrentValue().getQuality().getQualityCode());

        verify(lowDynamicTimeDeadbandFilterActivatorMock, processMessageSenderMock);
    }
    
    @Test
    public void testSendTagFilteredNotInRange() {
        this.lowDynamicTimeDeadbandFilterActivatorMock.newTagValueSent(sdt3.getId());
        this.medDynamicTimeDeadbandFilterActivatorMock.newTagValueSent(sdt2.getId());
        this.processMessageSenderMock.addValue(isA(SourceDataTagValue.class));

        // even though we try to send sdt3 twice, it should only be invalidated once, since the quality code does not
        // change and is is OUT_OF_BOUNDS
        expectLastCall().times(2);
        
        // One value is filtered out
        this.filterMessageSenderMock.addValue(isA(FilteredDataTagValue.class));
        expectLastCall().times(1);

        replay(this.lowDynamicTimeDeadbandFilterActivatorMock, this.processMessageSenderMock, this.filterMessageSenderMock);
        
        this.sdt3.setMaxValue(100);
        this.sdt2.setMinValue(10.0f);
        
        this.equipmentMessageSender.sendTagFiltered(this.sdt3, 109, System.currentTimeMillis() + 1L);
        this.equipmentMessageSender.sendTagFiltered(this.sdt2, 9.0f, System.currentTimeMillis() + 2L);

        assertEquals(SourceDataQuality.OUT_OF_BOUNDS, this.sdt2.getCurrentValue().getQuality().getQualityCode());
        assertEquals(SourceDataQuality.OUT_OF_BOUNDS, this.sdt3.getCurrentValue().getQuality().getQualityCode());

        String oldQualityDesc = sdt3.getCurrentValue().getQuality().getDescription();
        
        // try to send once more value out of configured max value
        this.equipmentMessageSender.sendTagFiltered(this.sdt3, 130, System.currentTimeMillis() + 3L);
        
        // Assure that the filtered value is not set to the reference
        assertEquals(109, sdt3.getCurrentValue().getValue());
        // The quality description should always be the same
        assertEquals(oldQualityDesc, sdt3.getCurrentValue().getQuality().getDescription());
//        System.out.println(sdt3.getCurrentValue().getQuality().getDescription());

        verify(this.lowDynamicTimeDeadbandFilterActivatorMock, this.processMessageSenderMock, this.filterMessageSenderMock);
    }

    /**
     * Filter when:
     * - New TS <= Current TS + Current Good Quality 
     * - New TS <= Current TS + Current Bad Quality + New Bad Quality
     * 
     * No filter when:
     * - New TS <= Current TS + New Good Quality + Current Bad Quality
     * - New TS > Current TS
     */
    @Test
    public void testSendTagFilteredOldUpdateSent() {
      // update the value
      this.sdt1.update(false);
      // Timestamps to use
      long sourceTS = System.currentTimeMillis() + 1000;
      long sourceTS_2 = sourceTS + 2000;
      long sourceTS_3 = sourceTS + 3000;
      
      SourceDataQuality newSDBadQuality = this.equipmentSenderHelper.createTagQualityObject(SourceDataQuality.DATA_UNAVAILABLE, "");
      
      this.processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
      expectLastCall().times(4);
      this.filterMessageSenderMock.addValue(isA(FilteredDataTagValue.class));
      expectLastCall().times(1);

      replay(this.processMessageSenderMock, this.filterMessageSenderMock);

      // The first one: the run method sends it to the server with NO_FILTERING
      this.equipmentMessageSender.sendTagFiltered(sdt1, true, sourceTS);
      assertEquals(true, this.sdt1.getCurrentValue().getValue());
      
      // This one should NOT be filtered out. New TS > Current TS
      this.equipmentMessageSender.sendTagFiltered(sdt1, false, sourceTS_2);
      assertEquals(false, this.sdt1.getCurrentValue().getValue());
      
      // This one should be filtered out. New TS <= Current TS + Current Good Quality
      this.equipmentMessageSender.sendTagFiltered(sdt1, true, sourceTS);
      assertEquals(false, this.sdt1.getCurrentValue().getValue());
      
      // Changing quality to BAD. Not filtering
      this.equipmentMessageSender.sendInvalidTag(this.sdt1, false, "", newSDBadQuality, new Timestamp(sourceTS_3));
      assertEquals(false, this.sdt1.getCurrentValue().getValue());
      assertEquals(SourceDataQuality.DATA_UNAVAILABLE, this.sdt1.getCurrentValue().getQuality().getQualityCode());

      // This one should NOT be filtered out. New TS <= Current TS + New Good Quality + Current Bad Quality
      this.equipmentMessageSender.sendTagFiltered(sdt1, true, sourceTS);
      assertEquals(true, this.sdt1.getCurrentValue().getValue());
           
      verify(this.processMessageSenderMock, this.filterMessageSenderMock);
    }
    
    /**
     * Filter when:
     * - New TS <= Current TS + Current Good Quality 
     * - New TS <= Current TS + Current Bad Quality + New Bad Quality
     * 
     * No filter when:
     * - New TS <= Current TS + New Good Quality + Current Bad Quality
     * - New TS > Current TS
     */
    @Test
    public void testSendInvalidTagFilteredOldUpdateSent() {
      // update the value
      this.sdt1.update(false);
      // Timestamps to use
      long sourceTS = System.currentTimeMillis() + 1000;
      long sourceTS_2 = sourceTS + 2000;
      
      // Creating Bad and Good Quality for testing 
      SourceDataQuality newSDBadQuality = this.equipmentSenderHelper.createTagQualityObject(SourceDataQuality.DATA_UNAVAILABLE, "");
      SourceDataQuality newSDGoodQuality = this.equipmentSenderHelper.createTagQualityObject(SourceDataQuality.OK, "");
      
      this.processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
      expectLastCall().times(3);
      this.filterMessageSenderMock.addValue(isA(FilteredDataTagValue.class));
      expectLastCall().times(1);

      replay(this.processMessageSenderMock, this.filterMessageSenderMock);
      
      // The first one: the run method sends it to the server with NO_FILTERING
      this.equipmentMessageSender.sendInvalidTag(this.sdt1, true, "", newSDBadQuality, new Timestamp(sourceTS));
      assertEquals(true, this.sdt1.getCurrentValue().getValue());
      
      // This one should NOT be filtered out. New TS > Current TS
      this.equipmentMessageSender.sendInvalidTag(this.sdt1, false, "", newSDBadQuality, new Timestamp(sourceTS_2));
      assertEquals(false, this.sdt1.getCurrentValue().getValue());
      assertEquals(SourceDataQuality.DATA_UNAVAILABLE, this.sdt1.getCurrentValue().getQuality().getQualityCode());
      
      // This one should be filtered out. New TS <= Current TS + Current Bad Quality + New Bad Quality 
      this.equipmentMessageSender.sendInvalidTag(this.sdt1, true, "", newSDBadQuality, new Timestamp(sourceTS));
      assertEquals(false, this.sdt1.getCurrentValue().getValue());
      
      // This one should NOT be filtered out. New TS <= Current TS + New Good Quality + Current Bad Quality
      // This should normally not happen! Redirecting call to sendTagFiltered() method.
      this.equipmentMessageSender.sendInvalidTag(this.sdt1, true, "", newSDGoodQuality, new Timestamp(sourceTS));
      assertEquals(true, this.sdt1.getCurrentValue().getValue());
           
      verify(this.processMessageSenderMock, this.filterMessageSenderMock);
    }
    
    @Test
    public void testSendTagFilteredIsValueDeadbandFiltered() throws Exception {
        filterMessageSenderMock.addValue(isA(FilteredDataTagValue.class));         
        expectLastCall().times(4);
        
        // Value deadband type 6: as long as value description stays unchanged, it works in exactly the same fashion as 
        // DEADBAND_PROCESS_RELATIVE_VALUE. If, however value description change is detected, deadband filtering is skipped.

        replay(filterMessageSenderMock);

        // relative
        
        // Sent
        equipmentMessageSender.sendTagFiltered(sdt3, 109, System.currentTimeMillis() + 1L, "value description 1"); // <-
                                                                                                              // should
                                                                                                              // be sent
        
        //System.out.println("TIME DEADBAND: "+sdt3.getAddress().getTimeDeadband());
        
        // should be sent, because value descr. is different from the previous one. Deadband filtering skipped
        equipmentMessageSender.sendTagFiltered(sdt3, 108, System.currentTimeMillis() + 2L);
        // should be sent, because value descr. is different from the previous one. Deadband filtering skipped
        equipmentMessageSender.sendTagFiltered(sdt3, 109, System.currentTimeMillis() + 3L, "value description 2");
        // should be filtered, because value description has not changed. Deadband filtering applied
        equipmentMessageSender.sendTagFiltered(sdt3, 110, System.currentTimeMillis() + 4L, "value description 2");
        // should be sent, because value descr. is different from the previous one. Deadband filtering skipped
        equipmentMessageSender.sendTagFiltered(sdt3, 108, System.currentTimeMillis() + 5L);
        // should be filtered. REPEATED_VALUE
        equipmentMessageSender.sendTagFiltered(sdt3, 108, System.currentTimeMillis() + 6L);
        // Should not be filtered out, because of bad quality (Same Value, Value Descrp but dif Quality Code)
        equipmentMessageSender.sendInvalidTag(sdt3, 108, "value description 2", new SourceDataQuality(SourceDataQuality.UNKNOWN), 
            new Timestamp(System.currentTimeMillis() + 7L));
        assertEquals(SourceDataQuality.UNKNOWN, sdt3.getCurrentValue().getQuality().getQualityCode());

        // absolute (DEADBAND_PROCESS_ABSOLUTE)
        // First Value => sent
        equipmentMessageSender.sendTagFiltered(sdt2, 9.0f, System.currentTimeMillis() + 8L);
        // should be filtered, because value description has not changed. Deadband filtering applied
        equipmentMessageSender.sendTagFiltered(sdt2, 9.1f, System.currentTimeMillis() + 9L);
        // should be filtered, because value description has not changed. Deadband filtering applied
        equipmentMessageSender.sendTagFiltered(sdt2, 9.2f, System.currentTimeMillis() + 10L);
        // should be sent, because value descr. is different from the previous one. Deadband filtering skipped
        this.sdt2.getAddress().setValueDeadbandType(DataTagDeadband.DEADBAND_PROCESS_ABSOLUTE_VALUE_DESCR_CHANGE);
        equipmentMessageSender.sendTagFiltered(sdt2, 9.3f, System.currentTimeMillis() + 11L, "value description 3");
           
        verify(filterMessageSenderMock);
    }

    @Test
    public void testSendTagFilteredTwiceSame() {
      filterMessageSenderMock.addValue(isA(FilteredDataTagValue.class));
      processMessageSenderMock.addValue(isA(SourceDataTagValue.class));

      replay(filterMessageSenderMock, processMessageSenderMock);

      // Sent
      equipmentMessageSender.sendTagFiltered(sdt1, true, System.currentTimeMillis() + 1L);
      // Filter with REPEATED_VALUE
      equipmentMessageSender.sendTagFiltered(sdt1, true, System.currentTimeMillis() + 2L);

      verify(filterMessageSenderMock, processMessageSenderMock);
    }

    @Test
    public void testSendTagFilteredTwiceSameValuesButDiffValueDesc() throws Exception {
      processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
      expectLastCall().times(2);

      filterMessageSenderMock.addValue(isA(FilteredDataTagValue.class));
      expectLastCall().times(1);

      replay(processMessageSenderMock, filterMessageSenderMock);

      // Send to the server
      equipmentMessageSender.sendTagFiltered(sdt1, true, System.currentTimeMillis() + 1L, "test description A");
      Thread.sleep(300);
      // Send to the server. Equal Value but dif Value Description
      equipmentMessageSender.sendTagFiltered(sdt1, true, System.currentTimeMillis() + 2L, "test description B");
      Thread.sleep(300);
      // Filter with REPEATED_VALUE
      equipmentMessageSender.sendTagFiltered(sdt1, true, System.currentTimeMillis() + 3L, "test description B");


      verify(processMessageSenderMock, filterMessageSenderMock);
    }
    
    @Test
    public void testSendInvalidTagFilteredTwiceSameValuesButDiffValueDesc() throws Exception {
      processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
      expectLastCall().times(2);

      filterMessageSenderMock.addValue(isA(FilteredDataTagValue.class));
      expectLastCall().times(1);
      
      this.sdt1.update(true);

      replay(processMessageSenderMock, filterMessageSenderMock);

      // Send to the server
      this.equipmentMessageSender.sendInvalidTag(sdt1, SourceDataQuality.DATA_UNAVAILABLE, "test description A");
      Thread.sleep(300);
      // Send to the server
      this.equipmentMessageSender.sendInvalidTag(sdt1, SourceDataQuality.DATA_UNAVAILABLE, "test description B");
      Thread.sleep(300);
      // Filter with REPEATED_INVALID
      this.equipmentMessageSender.sendInvalidTag(sdt1, SourceDataQuality.DATA_UNAVAILABLE, "test description B");


      verify(processMessageSenderMock, filterMessageSenderMock);
    }
    
    @Test
    /*
     * This test checks isCandidate for filtering when the value is the same but the
     * value descriptions changes. Cases
     * Curent Vale Desc vs New Value Desc => isCandidate4Filtering?
     * ----------------    --------------    ---------------------
     *      empty             null                    Y
     *      empty             not null                N
     *      not empty         null                    N
     *      not empty         not null                N/Y depends on the desc
     */
    public void testSendTagFilteredIsCandidateValueDescriptions() throws Exception {
        filterMessageSenderMock.addValue(isA(FilteredDataTagValue.class));         
        expectLastCall().times(2);
        
        processMessageSenderMock.addValue(isA(SourceDataTagValue.class));
        expectLastCall().times(5);
        
        // Value deadband type 6: as long as value description stays unchanged, it works in exactly the same fashion as 
        // DEADBAND_PROCESS_RELATIVE_VALUE. If, however value description change is detected, deadband filtering is skipped.

        replay(filterMessageSenderMock, processMessageSenderMock);

        // relative
        
        // NO_FILTERING
        equipmentMessageSender.sendTagFiltered(sdt3, 109, System.currentTimeMillis() + 1L);
        // REPEATED_VALUE Current Desc empty vs New Desc empty
        equipmentMessageSender.sendTagFiltered(sdt3, 109, System.currentTimeMillis() + 2L, "");
        // REPEATED_VALUE Current Desc empty vs New Desc null
        equipmentMessageSender.sendTagFiltered(sdt3, 109, System.currentTimeMillis() + 3L);
        // NO_FILTERING Current Desc null vs New Desc not empty
        equipmentMessageSender.sendTagFiltered(sdt3, 109, System.currentTimeMillis() + 4L, "test description 1");
        // NO_FILTERING Current Desc not empty vs New Desc null
        equipmentMessageSender.sendTagFiltered(sdt3, 109, System.currentTimeMillis() + 5L);
        // NO_FILTERING Current Desc null vs New Desc not empty
        equipmentMessageSender.sendTagFiltered(sdt3, 109, System.currentTimeMillis() + 6L, "test description 2");
        // NO_FILTERING Current Desc not empty vs New Desc not empty
        equipmentMessageSender.sendTagFiltered(sdt3, 109, System.currentTimeMillis() + 7L, "test description 3");
           
        verify(filterMessageSenderMock, processMessageSenderMock);
    }

    private SourceDataTag createSourceDataTag(long id, String name, String dataType, short deadBandType, int priority,
            boolean guaranteed) {
        DataTagAddress address = new DataTagAddress(null, 100, deadBandType, VALUE_DEADBAND, 0, priority, guaranteed);
        return new SourceDataTag(id, name, false, DataTagConstants.MODE_OPERATIONAL, dataType, address);
    }

}

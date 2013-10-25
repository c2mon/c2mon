package cern.c2mon.driver.common;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.sql.Timestamp;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.driver.common.conf.core.EquipmentConfiguration;
import cern.c2mon.driver.common.conf.core.ProcessConfiguration;
import cern.c2mon.driver.common.conf.core.RunOptions;
import cern.c2mon.driver.common.messaging.IProcessMessageSender;
import cern.c2mon.driver.filter.IFilterMessageSender;
import cern.c2mon.driver.filter.dynamic.IDynamicTimeDeadbandFilterActivator;
import cern.tim.shared.common.datatag.DataTagAddress;
import cern.tim.shared.common.datatag.DataTagConstants;
import cern.tim.shared.common.datatag.DataTagDeadband;
import cern.tim.shared.daq.config.ChangeReport;
import cern.tim.shared.daq.datatag.SourceDataQuality;
import cern.tim.shared.daq.datatag.SourceDataTag;
import cern.tim.shared.daq.datatag.SourceDataTagValue;
import cern.tim.shared.daq.filter.FilteredDataTagValue;

public class EquipmentMessageSenderTest {

    private static final long EQ_COMFAULT_ID = 1L;
    private static final float VALUE_DEADBAND = 25.0f;
    private static final long EQUIPMENT_ID = 1L;
    private static final Long SUB_KEY1 = 1337L;
    private static final Long SUB_KEY2 = 31415926L;

    private IFilterMessageSender filterMessageSender;
    private IProcessMessageSender processMessageSender;
    private IDynamicTimeDeadbandFilterActivator medDynamicTimeDeadbandFilterActivator;
    private IDynamicTimeDeadbandFilterActivator lowDynamicTimeDeadbandFilterActivator;
    private SourceDataTag sdt1;
    private SourceDataTag sdt2;
    private SourceDataTag sdt3;

    private EquipmentMessageSender equipmentMessageSender;

    @Before
    public void setUp() {
        filterMessageSender = createStrictMock(IFilterMessageSender.class);
        processMessageSender = createStrictMock(IProcessMessageSender.class);
        medDynamicTimeDeadbandFilterActivator = createStrictMock(IDynamicTimeDeadbandFilterActivator.class);
        lowDynamicTimeDeadbandFilterActivator = createStrictMock(IDynamicTimeDeadbandFilterActivator.class);
        equipmentMessageSender = new EquipmentMessageSender(filterMessageSender, processMessageSender,
                medDynamicTimeDeadbandFilterActivator, lowDynamicTimeDeadbandFilterActivator);
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

        equipmentMessageSender.setEquipmentConfiguration(equipmentConfiguration);
        ProcessConfiguration processConf = new ProcessConfiguration();
        processConf.setProcessID(1L);
        processConf.setProcessName("ad");
        equipmentConfiguration.setHandlerClassName("asd");
        equipmentMessageSender.setEquipmentLoggerFactory(EquipmentLoggerFactory.createFactory(equipmentConfiguration,
                processConf, new RunOptions()));
        // Setup calls should not affect later tests
        reset(lowDynamicTimeDeadbandFilterActivator, medDynamicTimeDeadbandFilterActivator, filterMessageSender,
                processMessageSender);     
    }

    @Test
    public void testEquipmentStateIncorrect() {
        processMessageSender.sendCommfaultTag(EQ_COMFAULT_ID, false);
        processMessageSender.sendCommfaultTag(SUB_KEY1, false);
        processMessageSender.sendCommfaultTag(SUB_KEY2, true);

        replay(processMessageSender);

        equipmentMessageSender.confirmEquipmentStateIncorrect();

        verify(processMessageSender);
    }

    @Test
    public void testEquipmentStateIncorrectDescription() {
        String description = "description";
        processMessageSender.sendCommfaultTag(EQ_COMFAULT_ID, false, description);
        processMessageSender.sendCommfaultTag(SUB_KEY1, false, description);
        processMessageSender.sendCommfaultTag(SUB_KEY2, true, description);

        replay(processMessageSender);

        equipmentMessageSender.confirmEquipmentStateIncorrect(description);

        verify(processMessageSender);
    }

    @Test
    public void testEquipmentStateOK() {
        processMessageSender.sendCommfaultTag(EQ_COMFAULT_ID, true);
        processMessageSender.sendCommfaultTag(SUB_KEY1, true);
        processMessageSender.sendCommfaultTag(SUB_KEY2, false);

        replay(processMessageSender);

        equipmentMessageSender.confirmEquipmentStateOK();

        verify(processMessageSender);
    }

    @Test
    public void testEquipmentStateOKDescription() {
        String description = "description";
        processMessageSender.sendCommfaultTag(EQ_COMFAULT_ID, true, description);
        processMessageSender.sendCommfaultTag(SUB_KEY1, true, description);
        processMessageSender.sendCommfaultTag(SUB_KEY2, false, description);

        replay(processMessageSender);

        equipmentMessageSender.confirmEquipmentStateOK(description);

        verify(processMessageSender);
    }

    @Test
    public void testOnAddDataTag() {
        SourceDataTag sdtLow = createSourceDataTag(5324L, "", "Float", DataTagDeadband.DEADBAND_NONE,
                DataTagConstants.PRIORITY_LOW, false);
        SourceDataTag sdtMed = createSourceDataTag(5325L, "", "Float", DataTagDeadband.DEADBAND_NONE,
                DataTagConstants.PRIORITY_MEDIUM, false);
        SourceDataTag sdtHigh = createSourceDataTag(5326L, "", "Float", DataTagDeadband.DEADBAND_NONE,
                DataTagConstants.PRIORITY_HIGH, false);

        lowDynamicTimeDeadbandFilterActivator.addDataTag(sdtLow);
        medDynamicTimeDeadbandFilterActivator.addDataTag(sdtMed);

        replay(lowDynamicTimeDeadbandFilterActivator, medDynamicTimeDeadbandFilterActivator);

        equipmentMessageSender.onAddDataTag(sdtHigh, new ChangeReport(1L));
        equipmentMessageSender.onAddDataTag(sdtLow, new ChangeReport(2L));
        equipmentMessageSender.onAddDataTag(sdtMed, new ChangeReport(3L));

        verify(lowDynamicTimeDeadbandFilterActivator, medDynamicTimeDeadbandFilterActivator);
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
        lowDynamicTimeDeadbandFilterActivator.removeDataTag(sdt2);
        medDynamicTimeDeadbandFilterActivator.removeDataTag(sdt2);
        lowDynamicTimeDeadbandFilterActivator.addDataTag(sdt2);

        /*
         * Update should try to remove sdt3 from any other activators and add it to the low one.
         */
        lowDynamicTimeDeadbandFilterActivator.removeDataTag(sdt3);
        medDynamicTimeDeadbandFilterActivator.removeDataTag(sdt3);

        replay(lowDynamicTimeDeadbandFilterActivator, medDynamicTimeDeadbandFilterActivator);

        equipmentMessageSender.onUpdateDataTag(sdt1, sdt1Clone, new ChangeReport(1L));
        equipmentMessageSender.onUpdateDataTag(sdt2, sdt2Clone, new ChangeReport(1L));
        equipmentMessageSender.onUpdateDataTag(sdt3, sdt3Clone, new ChangeReport(1L));

        verify(lowDynamicTimeDeadbandFilterActivator, medDynamicTimeDeadbandFilterActivator);
    }

    @Test
    public void testOnUpdateDataTag() {
        lowDynamicTimeDeadbandFilterActivator.removeDataTag(sdt1);
        medDynamicTimeDeadbandFilterActivator.removeDataTag(sdt1);

        replay(lowDynamicTimeDeadbandFilterActivator, medDynamicTimeDeadbandFilterActivator);

        equipmentMessageSender.onRemoveDataTag(sdt1, new ChangeReport(1L));

        verify(lowDynamicTimeDeadbandFilterActivator, medDynamicTimeDeadbandFilterActivator);
    }

    @Test
    public void testSendInvalidTag() {
        lowDynamicTimeDeadbandFilterActivator.newTagValueSent(sdt1.getId());
        processMessageSender.addValue(isA(SourceDataTagValue.class));
        filterMessageSender.addValue(isA(FilteredDataTagValue.class));

        replay(lowDynamicTimeDeadbandFilterActivator, processMessageSender, filterMessageSender);

        // The first one has null currentSDValue and should not be filter
        equipmentMessageSender.sendInvalidTag(sdt1, SourceDataQuality.DATA_UNAVAILABLE, "");
        // The second should go to filter module because the it has same currentSDvalue and newValue (null)
        // and same Quality
        equipmentMessageSender.sendInvalidTag(sdt1, SourceDataQuality.DATA_UNAVAILABLE, "");

        verify(lowDynamicTimeDeadbandFilterActivator, processMessageSender);
    }
    
    @Test
    public void testSendInvalidTagFutureSourceTS() {
      // Add value to the SourceDatTag
      this.sdt2.update(false);

      // One value is added
      this.processMessageSender.addValue(isA(SourceDataTagValue.class));
      expectLastCall().times(1);
      // One value is filtered out
      this.filterMessageSender.addValue(isA(FilteredDataTagValue.class));
      expectLastCall().times(1);

      replay(this.processMessageSender, this.filterMessageSender);

      SourceDataTagValue sourceDTValue = this.sdt2.getCurrentValue();
      // Get the source data quality from the quality code and description
      SourceDataQuality newSDQuality = this.equipmentMessageSender.createTagQualityObject(SourceDataQuality.FUTURE_SOURCE_TIMESTAMP,
          sourceDTValue.getQuality().getDescription());
      
      // It has:
      // - same currentSDValue and new value (false)
      // - same Value Description
      // - different Quality Code (OK vs FUTURE_SOURCE_TIMESTAMP)
      // - same Quality Description
      // 
      // Should not be filtered 
      this.equipmentMessageSender.sendInvalidTag(this.sdt2, sourceDTValue.getValue(), sourceDTValue.getValueDescription(), 
          newSDQuality, sourceDTValue.getDaqTimestamp());
      
      assertEquals(SourceDataQuality.FUTURE_SOURCE_TIMESTAMP, this.sdt2.getCurrentValue().getQuality().getQualityCode());
      
      // It has:
      // - same currentSDValue and new value (false)
      // - same Value Description
      // - same Quality Code (FUTURE_SOURCE_TIMESTAMP)
      // - same Quality Description
      // 
      // Normally should not be filtered but FUTURE_SOURCE_TIMESTAMP is is a especial case. Should be filtered 
      this.equipmentMessageSender.sendInvalidTag(this.sdt2, sourceDTValue.getValue(), sourceDTValue.getValueDescription(), 
          newSDQuality, sourceDTValue.getDaqTimestamp());

      verify(this.processMessageSender, this.filterMessageSender);
    }
    
    @Test
    public void testSendInvalidTagDifQuality() {
      // Add value to the SourceDatTag
      this.sdt1.update(false);
     
      // One value is added
      this.processMessageSender.addValue(isA(SourceDataTagValue.class));
      expectLastCall().times(1);

      replay(this.processMessageSender, this.filterMessageSender);

      SourceDataTagValue sourceDTValue = this.sdt1.getCurrentValue();
      // Get the source data quality from the quality code and description
      SourceDataQuality newSDQuality = this.equipmentMessageSender.createTagQualityObject(SourceDataQuality.DATA_UNAVAILABLE, 
          sourceDTValue.getQuality().getDescription());
      
      // It has:
      // - same currentSDValue and new value (false)
      // - same Value Description
      // - different Quality Code (OK vs DATA_UNAVAILABLE)
      // - same Quality Description
      //
      // Should not be filtered 
      this.equipmentMessageSender.sendInvalidTag(this.sdt1, sourceDTValue.getValue(), sourceDTValue.getValueDescription(), 
          newSDQuality, sourceDTValue.getDaqTimestamp());
      
      // The Quality Code has changed
      assertEquals(SourceDataQuality.DATA_UNAVAILABLE, this.sdt1.getCurrentValue().getQuality().getQualityCode());

      verify(this.processMessageSender, this.filterMessageSender);
    }
    
    @Test
    /**
     * Note: Value-based deadband filtering is enabled for the process (we use sdt2)
     */
    public void testSendInvalidTagDifValue() {
      // Add value to the SourceDatTag
      this.sdt2.update(true);
      
      // One value is added
      this.processMessageSender.addValue(isA(SourceDataTagValue.class));
      expectLastCall().times(1);

      replay(this.processMessageSender, this.filterMessageSender);

      SourceDataTagValue sourceDTValue = this.sdt2.getCurrentValue();
      // Get the source data quality from the quality code and description
      SourceDataQuality newSDQuality = this.equipmentMessageSender.createTagQualityObject(SourceDataQuality.DATA_UNAVAILABLE, 
          sourceDTValue.getQuality().getDescription());
      
      // It has:
      // - different currentSDValue and new value (true vs false)
      // - same Value Description 
      // - different Quality Code (OK vs DATA_UNAVAILABLE)
      // - same Quality Description
      //
      // Should not be filtered      
      this.equipmentMessageSender.sendInvalidTag(this.sdt2, false, sourceDTValue.getValueDescription(), newSDQuality, 
          sourceDTValue.getDaqTimestamp());
      
      // The Value has changed
      assertEquals(false, this.sdt2.getCurrentValue().getValue());
      // The Quality Code has changed
      assertEquals(SourceDataQuality.DATA_UNAVAILABLE, this.sdt2.getCurrentValue().getQuality().getQualityCode());

      verify(this.processMessageSender, this.filterMessageSender);
    }
    
    @Test
    public void testSendInvaliTagIsValueDeadbandFiltered() throws Exception {
      // Give current values (are null be default)
      this.sdt2.update(1);
      this.sdt3.update(1);

      // 5 values should not be filtered out
      this.processMessageSender.addValue(isA(SourceDataTagValue.class));
      expectLastCall().times(5);
      // 5 values should be filtered out
      this.filterMessageSender.addValue(isA(FilteredDataTagValue.class));
      expectLastCall().times(5);

      replay(this.processMessageSender, this.filterMessageSender);

      SourceDataTagValue sourceDTValue = this.sdt3.getCurrentValue();
      // Get the source data quality from the quality code and description 
      //  - quality code is OK by default and it will never go for invalidation because is a special case. New quality code added
      SourceDataQuality newSDQuality = this.equipmentMessageSender.createTagQualityObject(SourceDataQuality.DATA_UNAVAILABLE, 
          sourceDTValue.getQuality().getDescription());

      // Relative. Should not be filtered      
      this.equipmentMessageSender.sendInvalidTag(this.sdt3, 109, "value description 1", newSDQuality, 
          new Timestamp(System.currentTimeMillis()));

      // Should not be filtered because value descr. is different from the previous one
      this.equipmentMessageSender.sendInvalidTag(this.sdt3, 108, "", newSDQuality, 
          new Timestamp(System.currentTimeMillis()));

      // Should not be filtered because value descr. is different from the previous one
      this.equipmentMessageSender.sendInvalidTag(this.sdt3, 109, "value description 2", newSDQuality, 
          new Timestamp(System.currentTimeMillis()));

      // Should be filtered because value descr. has not change
      this.equipmentMessageSender.sendInvalidTag(this.sdt3, 110, "value description 2", newSDQuality, 
          new Timestamp(System.currentTimeMillis()));

      // Should be filtered (is Relative Value Deadband) 
      this.equipmentMessageSender.sendInvalidTag(this.sdt3, 108, "value description 2", newSDQuality, 
          new Timestamp(System.currentTimeMillis()));

      // Should be filtered (is Relative Value Deadband)
      this.equipmentMessageSender.sendInvalidTag(this.sdt3, 108, "value description 2", newSDQuality, 
          new Timestamp(System.currentTimeMillis()));
      
      // Should not be filtered because quality is OK even if value is very close to previous one
      this.equipmentMessageSender.sendTagFiltered(this.sdt3, 107, System.currentTimeMillis(), "value description 2");
      assertEquals(SourceDataQuality.OK, this.sdt3.getCurrentValue().getQuality().getQualityCode());


      // absolute
      sourceDTValue = this.sdt2.getCurrentValue();
      //    - quality code is OK by default and it will never go for invalidation because is a special case. New quality code added
      newSDQuality = this.equipmentMessageSender.createTagQualityObject(SourceDataQuality.DATA_UNAVAILABLE, 
          sourceDTValue.getQuality().getDescription());

      // Should not be filtered (different Value and Quality Code)
      this.equipmentMessageSender.sendInvalidTag(this.sdt2, 9.0f, "", newSDQuality, new Timestamp(System.currentTimeMillis()));
      // Should be filtered (is Absolute ValueDeadband)
      this.equipmentMessageSender.sendInvalidTag(this.sdt2, 9.1f, "", newSDQuality, new Timestamp(System.currentTimeMillis()));
      // Should be filtered (is Absolute ValueDeadband)
      this.equipmentMessageSender.sendInvalidTag(this.sdt2, 9.2f, "", newSDQuality, new Timestamp(System.currentTimeMillis()));

      verify(this.processMessageSender, this.filterMessageSender);
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

        equipmentMessageSender.setEquipmentConfiguration(equipmentConfiguration);

        ProcessConfiguration processConf = new ProcessConfiguration();
        processConf.setProcessID(1L);
        processConf.setProcessName("ad");
        equipmentConfiguration.setHandlerClassName("asd");

        equipmentMessageSender.setEquipmentLoggerFactory(EquipmentLoggerFactory.createFactory(equipmentConfiguration,
                processConf, new RunOptions()));

        // setup calls should not affect later tests
        reset(filterMessageSender, processMessageSender);

        // the processMessageSender should be called exactly 3 times.
        processMessageSender.addValue(isA(SourceDataTagValue.class));
        expectLastCall().times(3);

        // the filterMessageSender should be called exactly once
        filterMessageSender.addValue(isA(FilteredDataTagValue.class));

        replay(processMessageSender, filterMessageSender);

        // send value "true" to the server
        equipmentMessageSender.sendTagFiltered(sdt1, true, System.currentTimeMillis());

        // invalidate the tag
        equipmentMessageSender.sendInvalidTag(sdt1, SourceDataQuality.DATA_UNAVAILABLE, "");

        // send again value "true" to the server ( it should not be filtered out, because
        // it was invalidated previously)
        equipmentMessageSender.sendTagFiltered(sdt1, true, System.currentTimeMillis());

        // sleep a bit
        Thread.sleep(120);

        // update again, with unchanged value this update should go to filter module
        equipmentMessageSender.sendTagFiltered(sdt1, true, System.currentTimeMillis());

        verify(processMessageSender, filterMessageSender);
    }

    @Test
    public void testSendSupervisionAlive() {
        processMessageSender.addValue(isA(SourceDataTagValue.class));

        replay(processMessageSender);

        equipmentMessageSender.sendSupervisionAlive();

        verify(processMessageSender);
    }

    @Test
    public void testSendTagFiltered() {
        lowDynamicTimeDeadbandFilterActivator.newTagValueSent(sdt1.getId());
        processMessageSender.addValue(isA(SourceDataTagValue.class));

        replay(lowDynamicTimeDeadbandFilterActivator, processMessageSender);
        sdt1.getAddress().setTimeDeadband(0);
        equipmentMessageSender.sendTagFiltered(sdt1, false, System.currentTimeMillis());

        assertEquals(SourceDataQuality.OK, sdt1.getCurrentValue().getQuality().getQualityCode());

        verify(lowDynamicTimeDeadbandFilterActivator, processMessageSender);
    }

    @Test
    public void testSendTagFilteredInvalidTimestamp() throws Exception {
        this.lowDynamicTimeDeadbandFilterActivator.newTagValueSent(sdt1.getId());
        expectLastCall().times(2);
        
        this.processMessageSender.addValue(isA(SourceDataTagValue.class));

        // even though we try to send twice the tag, it should only be invalidated once, since the quality code does not
        // change and it is FUTURE_SOURCE_TIMESTAMP
        expectLastCall().times(2);
        
        // One value is filtered out
        this.filterMessageSender.addValue(isA(FilteredDataTagValue.class));
        expectLastCall().times(1);

        replay(this.lowDynamicTimeDeadbandFilterActivator, this.processMessageSender, this.filterMessageSender);

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

        verify(this.lowDynamicTimeDeadbandFilterActivator, this.processMessageSender, this.filterMessageSender);
    }

    @Test
    public void testSendTagFilteredNotConvertable() {
        medDynamicTimeDeadbandFilterActivator.newTagValueSent(sdt2.getId());
        processMessageSender.addValue(isA(SourceDataTagValue.class));

        replay(lowDynamicTimeDeadbandFilterActivator, processMessageSender);

        equipmentMessageSender.sendTagFiltered(sdt2, "asdasdasd", System.currentTimeMillis());

        assertEquals(SourceDataQuality.CONVERSION_ERROR, sdt2.getCurrentValue().getQuality().getQualityCode());

        verify(lowDynamicTimeDeadbandFilterActivator, processMessageSender);
    }

    @Test
    public void testSendTagFilteredNotInRange() {
        this.lowDynamicTimeDeadbandFilterActivator.newTagValueSent(sdt3.getId());
        this.medDynamicTimeDeadbandFilterActivator.newTagValueSent(sdt2.getId());
        this.processMessageSender.addValue(isA(SourceDataTagValue.class));

        // even though we try to send sdt3 twice, it should only be invalidated once, since the quality code does not
        // change and is is OUT_OF_BOUNDS
        expectLastCall().times(2);
        
        // One value is filtered out
        this.filterMessageSender.addValue(isA(FilteredDataTagValue.class));
        expectLastCall().times(1);

        replay(this.lowDynamicTimeDeadbandFilterActivator, this.processMessageSender, this.filterMessageSender);
        
        this.sdt3.setMaxValue(100);
        this.sdt2.setMinValue(10.0f);
        
        this.equipmentMessageSender.sendTagFiltered(this.sdt3, 109, System.currentTimeMillis());
        this.equipmentMessageSender.sendTagFiltered(this.sdt2, 9.0f, System.currentTimeMillis());

        assertEquals(SourceDataQuality.OUT_OF_BOUNDS, this.sdt2.getCurrentValue().getQuality().getQualityCode());
        assertEquals(SourceDataQuality.OUT_OF_BOUNDS, this.sdt3.getCurrentValue().getQuality().getQualityCode());

        String oldQualityDesc = sdt3.getCurrentValue().getQuality().getDescription();
        
        // try to send once more value out of configured max value
        this.equipmentMessageSender.sendTagFiltered(this.sdt3, 130, System.currentTimeMillis());
        
        // Assure that the filtered value is not set to the reference
        assertEquals(109, sdt3.getCurrentValue().getValue());
        // The quality description should always be the same
        assertEquals(oldQualityDesc, sdt3.getCurrentValue().getQuality().getDescription());
//        System.out.println(sdt3.getCurrentValue().getQuality().getDescription());

        verify(this.lowDynamicTimeDeadbandFilterActivator, this.processMessageSender, this.filterMessageSender);
    }

    @Test
    public void testSendTagFilteredIsValueDeadbandFiltered() throws Exception {
        filterMessageSender.addValue(isA(FilteredDataTagValue.class));
                
        expectLastCall().times(4);

        replay(filterMessageSender);

        // relative
        equipmentMessageSender.sendTagFiltered(sdt3, 109, System.currentTimeMillis(), "value description 1"); // <-
                                                                                                              // should
                                                                                                              // be sent
        
        //System.out.println("TIME DEADBAND: "+sdt3.getAddress().getTimeDeadband());
        
        // should be sent, because value descr. is different from the previous one
        equipmentMessageSender.sendTagFiltered(sdt3, 108, System.currentTimeMillis());
        // should be sent, because value descr. is different from the previous one
        equipmentMessageSender.sendTagFiltered(sdt3, 109, System.currentTimeMillis(), "value description 2");
        // should be filtered, because value description has not changed
        equipmentMessageSender.sendTagFiltered(sdt3, 110, System.currentTimeMillis(), "value description 2");
  

        // should be filtered out
        equipmentMessageSender.sendTagFiltered(sdt3, 108, System.currentTimeMillis());
        // should be filtered out
        equipmentMessageSender.sendTagFiltered(sdt3, 108, System.currentTimeMillis());
        // Should not be filtered out, because of bad quality
        equipmentMessageSender.sendInvalidTag(sdt3, 108, "value description 2", new SourceDataQuality(SourceDataQuality.UNKNOWN), new Timestamp(System.currentTimeMillis()));
        assertEquals(SourceDataQuality.UNKNOWN, sdt3.getCurrentValue().getQuality().getQualityCode());

        // absolute
        equipmentMessageSender.sendTagFiltered(sdt2, 9.0f, System.currentTimeMillis());
        equipmentMessageSender.sendTagFiltered(sdt2, 9.1f, System.currentTimeMillis());
        equipmentMessageSender.sendTagFiltered(sdt2, 9.2f, System.currentTimeMillis());
           
        verify(filterMessageSender);
    }

    // @Test
    public void testSendTagFilteredTwiceSame() {
        filterMessageSender.addValue(isA(FilteredDataTagValue.class));

        replay(filterMessageSender);

        equipmentMessageSender.sendTagFiltered(sdt1, true, System.currentTimeMillis());
        equipmentMessageSender.sendTagFiltered(sdt1, true, System.currentTimeMillis());

        verify(filterMessageSender);
    }

    @Test
    public void testSendTagFilteredTwiceSameValuesButDiffValueDesc() throws Exception {
        processMessageSender.addValue(isA(SourceDataTagValue.class));
        expectLastCall().times(2);

        filterMessageSender.addValue(isA(FilteredDataTagValue.class));

        replay(processMessageSender, filterMessageSender);

        equipmentMessageSender.sendTagFiltered(sdt1, true, System.currentTimeMillis(), "test description A");
        Thread.sleep(110);
        equipmentMessageSender.sendTagFiltered(sdt1, true, System.currentTimeMillis(), "test description B");
        equipmentMessageSender.sendTagFiltered(sdt1, true, System.currentTimeMillis(), "test description B");
        Thread.sleep(100);
        verify(processMessageSender, filterMessageSender);
    }

    private SourceDataTag createSourceDataTag(long id, String name, String dataType, short deadBandType, int priority,
            boolean guaranteed) {
        DataTagAddress address = new DataTagAddress(null, 100, deadBandType, VALUE_DEADBAND, 0, priority, guaranteed);
        return new SourceDataTag(id, name, false, DataTagConstants.MODE_OPERATIONAL, dataType, address);
    }

}

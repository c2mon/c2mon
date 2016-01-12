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
package cern.c2mon.daq.jec.frames;

import org.junit.Before;
import org.junit.Test;

import static org.easymock.classextension.EasyMock.*;
import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.daq.jec.JECMessageHandler;
import cern.c2mon.daq.jec.PLCObjectFactory;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.SourceDataQuality;
import cern.c2mon.shared.common.datatag.SourceDataTag;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.datatag.address.PLCHardwareAddress;
import cern.c2mon.daq.jec.address.BooleanJECAdressSpace;
import cern.c2mon.daq.jec.config.PLCConfiguration;
import cern.c2mon.daq.jec.frames.BooleanDataProcessor;
import cern.c2mon.daq.jec.plc.JECIndexOutOfRangeException;
import cern.c2mon.daq.jec.plc.JECPFrames;
import cern.c2mon.shared.common.datatag.address.impl.PLCHardwareAddressImpl;

public class BooleanDataProcessorTest {

    private BooleanDataProcessor<BooleanJECAdressSpace> booleanDataProcessor;
    private PLCObjectFactory plcFactory;
    private IEquipmentMessageSender equipmentMessageSender;
    private SourceDataTag sourceDataTag;
    
    @Before
    public void setUp() throws InstantiationException, IllegalAccessException, ClassNotFoundException, ConfigurationException {
        EquipmentLogger equipmentLogger = new EquipmentLogger("asd", "asd", "asd");
        PLCConfiguration plcConfiguration = new PLCConfiguration();
        plcConfiguration.setProtocol("TestPLCDriver");
        plcFactory = new PLCObjectFactory(plcConfiguration);
        equipmentMessageSender = createMock(IEquipmentMessageSender.class);
        booleanDataProcessor = new BooleanDataProcessor<BooleanJECAdressSpace>(1, new BooleanJECAdressSpace(), plcFactory, equipmentMessageSender, equipmentLogger);
        sourceDataTag = new SourceDataTag(1L, "asd", false);
        HardwareAddress hardwareAddress = new PLCHardwareAddressImpl(PLCHardwareAddress.STRUCT_BOOLEAN, 0, 5, 0, 100, 1000, "TEST001");
        DataTagAddress dataTagAddress = new DataTagAddress(hardwareAddress);
        sourceDataTag.setAddress(dataTagAddress);
        sourceDataTag.update(true);
        booleanDataProcessor.addSourceDataTag(sourceDataTag);
        booleanDataProcessor.initArrays();
        booleanDataProcessor.setInitialValuesSent(0, true); //o.w. all values are sent when processing the first frame
    }
    
    @Test
    public void testDetectChanges() throws JECIndexOutOfRangeException {
        JECPFrames jecpFrames = plcFactory.getSendFrame((byte) 1);
        jecpFrames.AddJECData((short) 0x0020, 0); // change in bit 5
        jecpFrames.SetDataStartNumber((short) 0);
        jecpFrames.SetDataOffset((short) 1);
        
        equipmentMessageSender.sendTagFiltered(sourceDataTag, true, jecpFrames.GetJECCurrTimeMilliseconds());
        expectLastCall().andReturn(true);
        
        replay(equipmentMessageSender);
        booleanDataProcessor.processJECPFrame(jecpFrames);
        verify(equipmentMessageSender);
    }
    
    @Test
    public void testSendTag() {
        equipmentMessageSender.sendTagFiltered(eq(sourceDataTag), eq(false), geq(System.currentTimeMillis()));
        expectLastCall().andReturn(true);
        
        replay(equipmentMessageSender);
        booleanDataProcessor.sendTag(0, 5);
        sourceDataTag.invalidate(new SourceDataQuality(SourceDataQuality.DATA_UNAVAILABLE, JECMessageHandler.HIERARCHICAL_INVALIDATION_MESSAGE));
        booleanDataProcessor.sendTag(0, 5);
        verify(equipmentMessageSender);
    }
    
    @Test
    public void testRevalidateTag() {
        equipmentMessageSender.sendTagFiltered(eq(sourceDataTag), eq(false), geq(System.currentTimeMillis()));
        expectLastCall().andReturn(true);
        equipmentMessageSender.sendTagFiltered(eq(sourceDataTag), eq(false), geq(System.currentTimeMillis()));
        expectLastCall().andReturn(true);
        
        replay(equipmentMessageSender);
        booleanDataProcessor.revalidateTag(0, 5, System.currentTimeMillis());
        sourceDataTag.getCurrentValue().setQuality(new SourceDataQuality(SourceDataQuality.DATA_UNAVAILABLE, JECMessageHandler.HIERARCHICAL_INVALIDATION_MESSAGE));
        booleanDataProcessor.revalidateTag(0, 5, System.currentTimeMillis());
        verify(equipmentMessageSender);
    }
}

package cern.c2mon.driver.jec.frames;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.driver.jec.JECMessageHandler;
import cern.c2mon.driver.jec.PLCObjectFactory;
import cern.c2mon.driver.jec.address.AbstractJECAddressSpace;
import cern.c2mon.driver.jec.address.BooleanJECAdressSpace;
import cern.c2mon.driver.jec.config.PLCConfiguration;
import cern.c2mon.driver.jec.plc.JECIndexOutOfRangeException;
import cern.c2mon.driver.jec.plc.JECPFrames;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.address.PLCHardwareAddress;
import cern.c2mon.shared.common.datatag.address.impl.PLCHardwareAddressImpl;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;
import cern.c2mon.shared.daq.datatag.SourceDataTag;

public class AbstractDataProcessorTest {
    
    private AbstractDataProcessor abstractDataProcessorMock;
    private IEquipmentMessageSender equipmentMessageSender;
    private PLCObjectFactory plcObjectFactory;

    @Before
    public void setUp() throws SecurityException, NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        equipmentMessageSender = createMock(IEquipmentMessageSender.class);
        PLCConfiguration plcConfiguration = new PLCConfiguration();
        plcConfiguration.setProtocol("TestPLCDriver");
        plcObjectFactory = new PLCObjectFactory(plcConfiguration);
//        abstractDataProcessorMock = createMock(AbstractDataProcessor.class,
//                new ConstructorArgs(AbstractDataProcessor.class.getConstructor(
//                        Integer.TYPE, PLCObjectFactory.class, IEquipmentMessageSender.class, 
//                        EquipmentLogger.class), 
//                        1, plcObjectFactory, equipmentMessageSender, 
//                        new EquipmentLogger("ad", "ad", "ad")));
        
        this.abstractDataProcessorMock = EasyMock.createMockBuilder(AbstractDataProcessor.class).
                withConstructor(Integer.TYPE, PLCObjectFactory.class, IEquipmentMessageSender.class, EquipmentLogger.class).
                withArgs(1, plcObjectFactory, equipmentMessageSender, new EquipmentLogger("ad", "ad", "ad")).
                createMock();
    }
    
    @Test
    public void testAddSourceDataTag() throws ConfigurationException {
        SourceDataTag sourceDataTag = new SourceDataTag(1L, "asd", false);
        PLCHardwareAddressImpl hardwareAddress = new PLCHardwareAddressImpl(PLCHardwareAddress.STRUCT_BOOLEAN, 10, 5, -1, 1, 10, "asd");
        sourceDataTag.setAddress(new DataTagAddress(hardwareAddress));
        
        expect(abstractDataProcessorMock.getJecAddressSpace()).andReturn(new BooleanJECAdressSpace());
        
        replay(abstractDataProcessorMock);
        abstractDataProcessorMock.addSourceDataTag(sourceDataTag);
        assertEquals(1L, abstractDataProcessorMock.getTag(10, 5).getId().longValue());
        assertTrue(abstractDataProcessorMock.containsSourceDataTag(sourceDataTag));
        verify(abstractDataProcessorMock);
    }
    
    @Test
    public void testProcessFrame() throws ConfigurationException, JECIndexOutOfRangeException {
        JECPFrames jecpFrames = plcObjectFactory.getSendFrame((byte) 0);
        jecpFrames.AddJECData((short) 0x0001, 0);
        jecpFrames.SetDataStartNumber((short) 0);
        jecpFrames.SetDataOffset((short) 1);
        JECPFrames jecpFrames2 = plcObjectFactory.getSendFrame((byte) 0);
        jecpFrames2.AddJECData((short) 0x1001, 0);
        jecpFrames2.SetDataStartNumber((short) 0);
        jecpFrames2.SetDataOffset((short) 1);
        
        AbstractJECAddressSpace booleanJECAddressSpace = new BooleanJECAdressSpace();
        booleanJECAddressSpace.setMaxWordId(0);
        expect(abstractDataProcessorMock.getJecAddressSpace()).andReturn(booleanJECAddressSpace);
        abstractDataProcessorMock.sendAllInBlock(0); //all are sent when processing first frame
        abstractDataProcessorMock.detectAndSendArrayChanges(0, jecpFrames.GetJECCurrTimeMilliseconds());
        abstractDataProcessorMock.detectAndSendArrayChanges(0, jecpFrames2.GetJECCurrTimeMilliseconds());
        
        replay(abstractDataProcessorMock);
        abstractDataProcessorMock.initArrays();
        byte[] array = new byte[224];
        assertArrayEquals(array, abstractDataProcessorMock.getCurrentValues());
        abstractDataProcessorMock.processJECPFrame(jecpFrames);
        array[1] = 0x01;
        assertArrayEquals(array, abstractDataProcessorMock.getCurrentValues());
        abstractDataProcessorMock.processJECPFrame(jecpFrames);
        array[1] = 0x01;
        assertArrayEquals(array, abstractDataProcessorMock.getCurrentValues());
        abstractDataProcessorMock.processJECPFrame(jecpFrames2);
        array[0] = 0x10;
        assertArrayEquals(array, abstractDataProcessorMock.getCurrentValues());
        verify(abstractDataProcessorMock);
    }
    
    @Test
    public void testInvalidation() throws ConfigurationException {
        SourceDataTag sourceDataTag = new SourceDataTag(1L, "asd", false);
        PLCHardwareAddressImpl hardwareAddress = new PLCHardwareAddressImpl(PLCHardwareAddress.STRUCT_BOOLEAN, 10, 5, -1, 1, 10, "TEST001");
        sourceDataTag.setAddress(new DataTagAddress(hardwareAddress));
        
        expect(abstractDataProcessorMock.getJecAddressSpace()).andReturn(new BooleanJECAdressSpace());
        equipmentMessageSender.sendInvalidTag(sourceDataTag, SourceDataQuality.DATA_UNAVAILABLE, JECMessageHandler.HIERARCHICAL_INVALIDATION_MESSAGE);
        
        replay(abstractDataProcessorMock, equipmentMessageSender);
        abstractDataProcessorMock.addSourceDataTag(sourceDataTag);
        abstractDataProcessorMock.invalidateForUnavailableSlave("TEST001");
        verify(abstractDataProcessorMock, equipmentMessageSender);
    }
    
    @Test
    public void testValidation() throws ConfigurationException {
        SourceDataTag sourceDataTag = new SourceDataTag(1L, "asd", false);
        PLCHardwareAddressImpl hardwareAddress = new PLCHardwareAddressImpl(PLCHardwareAddress.STRUCT_BOOLEAN, 10, 5, -1, 1, 10, "TEST001");
        sourceDataTag.setAddress(new DataTagAddress(hardwareAddress));
        sourceDataTag.update(true);
        sourceDataTag.invalidate(new SourceDataQuality(SourceDataQuality.DATA_UNAVAILABLE));
        expect(abstractDataProcessorMock.getJecAddressSpace()).andReturn(new BooleanJECAdressSpace());
        abstractDataProcessorMock.revalidateTag(10, 5);
        
        replay(abstractDataProcessorMock, equipmentMessageSender);
        abstractDataProcessorMock.addSourceDataTag(sourceDataTag);
        abstractDataProcessorMock.revalidateForUnavailableSlave("TEST001");
        verify(abstractDataProcessorMock, equipmentMessageSender);
    }
}

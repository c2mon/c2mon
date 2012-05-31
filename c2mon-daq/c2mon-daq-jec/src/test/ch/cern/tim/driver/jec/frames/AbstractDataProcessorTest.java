package ch.cern.tim.driver.jec.frames;

import org.easymock.classextension.ConstructorArgs;
import org.junit.Before;
import org.junit.Test;

import cern.tim.driver.common.EquipmentLogger;
import cern.tim.driver.common.IEquipmentMessageSender;
import cern.tim.shared.common.ConfigurationException;
import cern.tim.shared.common.datatag.DataTagAddress;
import cern.tim.shared.common.datatag.DataTagQuality;
import cern.tim.shared.common.datatag.address.PLCHardwareAddress;
import cern.tim.shared.daq.datatag.SourceDataQuality;
import cern.tim.shared.daq.datatag.SourceDataTag;
import ch.cern.tim.driver.jec.JECMessageHandler;
import ch.cern.tim.driver.jec.PLCObjectFactory;
import ch.cern.tim.driver.jec.address.AbstractJECAddressSpace;
import ch.cern.tim.driver.jec.address.BooleanJECAdressSpace;
import ch.cern.tim.driver.jec.config.PLCConfiguration;
import ch.cern.tim.jec.JECIndexOutOfRangeException;
import ch.cern.tim.jec.JECPFrames;
import ch.cern.tim.shared.datatag.address.impl.PLCHardwareAddressImpl;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

public class AbstractDataProcessorTest {
    
    private AbstractDataProcessor abstractDataProcessor;
    private IEquipmentMessageSender equipmentMessageSender;
    private PLCObjectFactory plcObjectFactory;

    @Before
    public void setUp() throws SecurityException, NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        equipmentMessageSender = createMock(IEquipmentMessageSender.class);
        PLCConfiguration plcConfiguration = new PLCConfiguration();
        plcConfiguration.setProtocol("TestPLCDriver");
        plcObjectFactory = new PLCObjectFactory(plcConfiguration);
        abstractDataProcessor = createMock(AbstractDataProcessor.class,
                new ConstructorArgs(AbstractDataProcessor.class.getConstructor(
                        Integer.TYPE, PLCObjectFactory.class, IEquipmentMessageSender.class, 
                        EquipmentLogger.class), 
                        1, plcObjectFactory, equipmentMessageSender, 
                        new EquipmentLogger("ad", "ad", "ad")));
    }
    
    @Test
    public void testAddSourceDataTag() throws ConfigurationException {
        SourceDataTag sourceDataTag = new SourceDataTag(1L, "asd", false);
        PLCHardwareAddressImpl hardwareAddress = new PLCHardwareAddressImpl(PLCHardwareAddress.STRUCT_BOOLEAN, 10, 5, -1, 1, 10, "asd");
        sourceDataTag.setAddress(new DataTagAddress(hardwareAddress));
        
        expect(abstractDataProcessor.getJecAddressSpace()).andReturn(new BooleanJECAdressSpace());
        
        replay(abstractDataProcessor);
        abstractDataProcessor.addSourceDataTag(sourceDataTag);
        assertEquals(1L, abstractDataProcessor.getTag(10, 5).getId().longValue());
        assertTrue(abstractDataProcessor.containsSourceDataTag(sourceDataTag));
        verify(abstractDataProcessor);
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
        expect(abstractDataProcessor.getJecAddressSpace()).andReturn(booleanJECAddressSpace);
        abstractDataProcessor.sendAllInBlock(0); //all are sent when processing first frame
        abstractDataProcessor.detectAndSendArrayChanges(0, jecpFrames.GetJECCurrTimeMilliseconds());
        abstractDataProcessor.detectAndSendArrayChanges(0, jecpFrames2.GetJECCurrTimeMilliseconds());
        
        replay(abstractDataProcessor);
        abstractDataProcessor.initArrays();
        byte[] array = new byte[224];
        assertArrayEquals(array, abstractDataProcessor.getCurrentValues());
        abstractDataProcessor.processJECPFrame(jecpFrames);
        array[1] = 0x01;
        assertArrayEquals(array, abstractDataProcessor.getCurrentValues());
        abstractDataProcessor.processJECPFrame(jecpFrames);
        array[1] = 0x01;
        assertArrayEquals(array, abstractDataProcessor.getCurrentValues());
        abstractDataProcessor.processJECPFrame(jecpFrames2);
        array[0] = 0x10;
        assertArrayEquals(array, abstractDataProcessor.getCurrentValues());
        verify(abstractDataProcessor);
    }
    
    @Test
    public void testInvalidation() throws ConfigurationException {
        SourceDataTag sourceDataTag = new SourceDataTag(1L, "asd", false);
        PLCHardwareAddressImpl hardwareAddress = new PLCHardwareAddressImpl(PLCHardwareAddress.STRUCT_BOOLEAN, 10, 5, -1, 1, 10, "TEST001");
        sourceDataTag.setAddress(new DataTagAddress(hardwareAddress));
        
        expect(abstractDataProcessor.getJecAddressSpace()).andReturn(new BooleanJECAdressSpace());
        equipmentMessageSender.sendInvalidTag(sourceDataTag, SourceDataQuality.DATA_UNAVAILABLE, JECMessageHandler.HIERARCHICAL_INVALIDATION_MESSAGE);
        
        replay(abstractDataProcessor, equipmentMessageSender);
        abstractDataProcessor.addSourceDataTag(sourceDataTag);
        abstractDataProcessor.invalidateForUnavailableSlave("TEST001");
        verify(abstractDataProcessor, equipmentMessageSender);
    }
    
    @Test
    public void testValidation() throws ConfigurationException {
        SourceDataTag sourceDataTag = new SourceDataTag(1L, "asd", false);
        PLCHardwareAddressImpl hardwareAddress = new PLCHardwareAddressImpl(PLCHardwareAddress.STRUCT_BOOLEAN, 10, 5, -1, 1, 10, "TEST001");
        sourceDataTag.setAddress(new DataTagAddress(hardwareAddress));
        sourceDataTag.update(true);
        sourceDataTag.invalidate(new SourceDataQuality(SourceDataQuality.DATA_UNAVAILABLE));
        expect(abstractDataProcessor.getJecAddressSpace()).andReturn(new BooleanJECAdressSpace());
        abstractDataProcessor.revalidateTag(10, 5);
        
        replay(abstractDataProcessor, equipmentMessageSender);
        abstractDataProcessor.addSourceDataTag(sourceDataTag);
        abstractDataProcessor.revalidateForUnavailableSlave("TEST001");
        verify(abstractDataProcessor, equipmentMessageSender);
    }
}

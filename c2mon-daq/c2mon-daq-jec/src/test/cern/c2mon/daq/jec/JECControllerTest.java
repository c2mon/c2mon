package cern.c2mon.daq.jec;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.easymock.classextension.EasyMock.*;

import cern.c2mon.daq.common.logger.EquipmentLoggerFactory;
import cern.c2mon.daq.common.impl.EquipmentMessageSender;
import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.daq.common.conf.core.EquipmentConfiguration;
import cern.c2mon.daq.common.conf.core.ProcessConfiguration;
import cern.c2mon.daq.common.conf.core.RunOptions;
import cern.c2mon.daq.jec.JECController;
import cern.c2mon.daq.jec.PLCConnectionSampler;
import cern.c2mon.daq.jec.PLCObjectFactory;
import cern.c2mon.daq.jec.address.TestPLCHardwareAddress;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.address.PLCHardwareAddress;
import cern.c2mon.shared.daq.command.ISourceCommandTag;
import cern.c2mon.shared.daq.command.SourceCommandTag;
import cern.c2mon.shared.daq.datatag.SourceDataTag;
import cern.c2mon.daq.jec.config.PLCConfiguration;
import cern.c2mon.daq.jec.frames.JECCommandRunner;
import cern.c2mon.daq.jec.plc.JECPFrames;
import cern.c2mon.daq.jec.plc.StdConstants;
import cern.c2mon.daq.jec.plc.TestPLCDriver;
import cern.c2mon.shared.common.datatag.address.impl.PLCHardwareAddressImpl;

public class JECControllerTest {
    
    private JECController jecController;
    private PLCObjectFactory plcFactory;
    
    @Before
    public void setUp() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SecurityException, NoSuchMethodException {
        PLCConfiguration plcConfiguration = new PLCConfiguration();
        plcConfiguration.setProtocol("TestPLCDriver");
        plcFactory = new PLCObjectFactory(plcConfiguration);
        PLCConnectionSampler connectionSampler = createMock(PLCConnectionSampler.class);
        JECCommandRunner jecCommandRunner = createMock(JECCommandRunner.class);
        IEquipmentMessageSender equipmentMessageSender = createMock(EquipmentMessageSender.class);
        EquipmentConfiguration equipmentConfiguration = new EquipmentConfiguration();
        equipmentConfiguration.setName("asd");
        equipmentConfiguration.setHandlerClassName("asd");
        ProcessConfiguration processConfiguration = new ProcessConfiguration();
        processConfiguration.setProcessName("asd");
        EquipmentLoggerFactory equipmentLoggerFactory = EquipmentLoggerFactory.createFactory(equipmentConfiguration, processConfiguration, new RunOptions());
        jecController = new JECController(plcFactory, connectionSampler, jecCommandRunner, equipmentMessageSender, equipmentLoggerFactory);
    }
    
    @Test
    public void testConfigureAnalogCommandTag() throws ConfigurationException {
        PLCHardwareAddressImpl hwAddress = new PLCHardwareAddressImpl(PLCHardwareAddress.STRUCT_ANALOG_COMMAND, 1, 5, 0, 10, 100, "PWA-asd", 1000);
        ISourceCommandTag commandTag = new SourceCommandTag(1L, "sd", 500, 5, hwAddress);
        jecController.configureCommandTag(commandTag);
        hwAddress.setNativeAddress("asd"); //should now be ignored.
        hwAddress.setWordId(100); 
        jecController.configureCommandTag(commandTag);
        hwAddress.setNativeAddress("PWAasd"); //should now be ignored.
        hwAddress.setWordId(50); 
        jecController.configureCommandTag(commandTag);
        jecController.initArrays();
        JECPFrames jecFrame = jecController.getSetConfigurationMessage();
        byte[] frame = jecFrame.frame;
        int offset = jecFrame.offset;
        // written on pos 26 and 27 + offset in config frame
        int numberOfMMDAnalog = (frame[26 + offset] << 8) | frame[27 + offset];//JECBinaryHelper.getBooleanWord(26 + offset, frame);
        // number of MMD modules:  maxPWAwordId/2 + 1 = 50/2 + 1 = 26
        assertEquals(26, numberOfMMDAnalog);
        assertTrue(jecController.isInAddressRange(hwAddress));
        hwAddress.setWordId(75);
        assertFalse(jecController.isInAddressRange(hwAddress));
    }
    
    @Test
    public void testConfigureBooleanCommandTag() throws ConfigurationException {
        PLCHardwareAddressImpl hwAddress = new PLCHardwareAddressImpl(PLCHardwareAddress.STRUCT_BOOLEAN_COMMAND, 1, 5, 0, 10, 100, "PWA-asd", 1000);
        ISourceCommandTag commandTag = new SourceCommandTag(1L, "sd", 500, 5, hwAddress);
        jecController.configureCommandTag(commandTag);
        hwAddress.setNativeAddress("asd"); //should now be ignored.
        hwAddress.setWordId(100); 
        hwAddress.setBitId(10); 
        jecController.configureCommandTag(commandTag);
        hwAddress.setNativeAddress("PWAasd");
        hwAddress.setWordId(50);
        hwAddress.setBitId(7); 
        jecController.configureCommandTag(commandTag);
        jecController.initArrays();
        JECPFrames jecFrame = jecController.getSetConfigurationMessage();
        byte[] frame = jecFrame.frame;
        int offset = jecFrame.offset;
        // written on pos 26 and 27 + offset in config frame
        int numberOfMMDAnalog = (frame[24 + offset] << 8) | frame[25 + offset] & 0xff;//JECBinaryHelper.getBooleanWord(26 + offset, frame);
        // number of MMD modules:  maxPWAwordId * 4 + 1 + maxPWABitid/4= 50*4 2 + 1 + 7/4= 202
        assertEquals(202, numberOfMMDAnalog);
        assertTrue(jecController.isInAddressRange(hwAddress));
        hwAddress.setWordId(75);
        assertFalse(jecController.isInAddressRange(hwAddress));
        hwAddress.setWordId(50);
        hwAddress.setBitId(8); 
        assertFalse(jecController.isInAddressRange(hwAddress));
    }
    
    @Test
    public void testAcknowledgeReceivedMessage() throws IOException {
        JECPFrames recvMsg = plcFactory.getRawRecvFrame();
        recvMsg.SetMessageIdentifier((byte) 12);
        JECPFrames sendFrame = plcFactory.getRawSendFrame();
        jecController.acknowledgeReceivedMessage(sendFrame, recvMsg);
        JECPFrames lastSend = ((TestPLCDriver) plcFactory.getPLCDriver()).getLastSend();
        assertEquals(StdConstants.ACK_MSG, lastSend.getMsgID());
        assertEquals(12, lastSend.GetDataType());
        assertEquals(lastSend, sendFrame);
    }
    
    @Test
    public void testClearTagConfiguration() {
        jecController.clearTagConfiguration();
    }
       
    @Test
    public void testGetNumberAnalogFrames() {
      assertEquals(0, jecController.getNumberOfAnalogDataJECFrames());
      //need a single frame
      jecController.configureDataTag(new SourceDataTag(10L, "test tag", false, (short)0, "Float", new DataTagAddress(new TestPLCHardwareAddress("PWA", 1, 0, 0, PLCHardwareAddress.STRUCT_ANALOG))));
      assertEquals(1, jecController.getNumberOfAnalogDataJECFrames());
      //still 1 frame only
      jecController.configureDataTag(new SourceDataTag(11L, "test tag", false, (short)0, "Float", new DataTagAddress(new TestPLCHardwareAddress("PWA", 1, 110, 13, PLCHardwareAddress.STRUCT_ANALOG))));
      assertEquals(1, jecController.getNumberOfAnalogDataJECFrames());
      //need 2 frames
      jecController.configureDataTag(new SourceDataTag(12L, "test tag", false, (short)0, "Float", new DataTagAddress(new TestPLCHardwareAddress("PWA", 1, 222, 0, PLCHardwareAddress.STRUCT_ANALOG))));
      assertEquals(2, jecController.getNumberOfAnalogDataJECFrames());
    }
    
    @Test
    public void testGetNumberBooleanFrames() {
      assertEquals(0, jecController.getNumberOfBooleanDataJECFrames());
      //need a single frame
      jecController.configureDataTag(new SourceDataTag(10L, "test tag", false, (short)0, "Boolean", new DataTagAddress(new TestPLCHardwareAddress("PWA", 1, 0, 0, PLCHardwareAddress.STRUCT_BOOLEAN))));
      assertEquals(1, jecController.getNumberOfBooleanDataJECFrames());
      //still 1 frame only
      jecController.configureDataTag(new SourceDataTag(11L, "test tag", false, (short)0, "Boolean", new DataTagAddress(new TestPLCHardwareAddress("PWA", 1, 111, 12, PLCHardwareAddress.STRUCT_BOOLEAN))));
      assertEquals(1, jecController.getNumberOfBooleanDataJECFrames());
      //need 2 frames
      jecController.configureDataTag(new SourceDataTag(12L, "test tag", false, (short)0, "Boolean", new DataTagAddress(new TestPLCHardwareAddress("PWA", 1, 112, 0, PLCHardwareAddress.STRUCT_BOOLEAN))));
      assertEquals(2, jecController.getNumberOfBooleanDataJECFrames());
    }
}

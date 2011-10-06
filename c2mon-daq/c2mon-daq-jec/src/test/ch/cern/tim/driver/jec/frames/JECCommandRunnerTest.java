package ch.cern.tim.driver.jec.frames;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import cern.tim.driver.common.EquipmentLogger;
import cern.tim.driver.common.EquipmentLoggerFactory;
import cern.tim.driver.common.conf.core.EquipmentConfiguration;
import cern.tim.driver.tools.equipmentexceptions.EqCommandTagException;
import cern.tim.shared.common.ConfigurationException;
import cern.tim.shared.common.datatag.address.PLCHardwareAddress;
import cern.tim.shared.daq.command.SourceCommandTag;
import cern.tim.shared.daq.command.SourceCommandTagValue;
import ch.cern.tim.driver.jec.PLCObjectFactory;
import ch.cern.tim.driver.jec.config.PLCConfiguration;
import ch.cern.tim.jec.JECPFrames;
import ch.cern.tim.jec.StdConstants;
import ch.cern.tim.shared.datatag.address.impl.PLCHardwareAddressImpl;

public class JECCommandRunnerTest {

    private JECCommandRunner jecCommandRunner;
    private PLCObjectFactory plcFactory;
    
    @Before
    public void setUp() throws ConfigurationException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        EquipmentLogger equipmentLogger = new EquipmentLogger("asd", "asd", "asd");
        EquipmentConfiguration equipmentConfiguration = new EquipmentConfiguration();
        PLCConfiguration plcConfiguration = new PLCConfiguration();
        plcConfiguration.setProtocol("TestPLCDriver");
        plcFactory = new PLCObjectFactory(plcConfiguration);
        SourceCommandTag sourceCommandTag1 = getCommandTag(PLCHardwareAddress.STRUCT_BOOLEAN_COMMAND, 1L, 0, 1, 0, 1, 2, "asd");
        SourceCommandTag sourceCommandTag2 = getCommandTag(PLCHardwareAddress.STRUCT_ANALOG_COMMAND, 2L, 1, -1, 0, 1, 2, "asd");
        equipmentConfiguration.getCommandTags().put(1L, sourceCommandTag1);
        equipmentConfiguration.getCommandTags().put(2L, sourceCommandTag2);
        jecCommandRunner = new JECCommandRunner(equipmentLogger, plcFactory, equipmentConfiguration);
        
    }
    
    @Test
    public void testSendBooleanCommandACK() throws EqCommandTagException {
        SourceCommandTagValue value = new SourceCommandTagValue(1L, "asd", 1L, (short) 0, true, "Boolean");
        prepareAndSendDelayedCommandAnswer(StdConstants.ACK_MSG);
        jecCommandRunner.runCommand(value);
    }
    
    @Test
    public void testSendBooleanCommandNACK() {
        SourceCommandTagValue value = new SourceCommandTagValue(1L, "asd", 1L, (short) 0, true, "Boolean");
        prepareAndSendDelayedCommandAnswer(StdConstants.NACK_MSG);
        try {
            jecCommandRunner.runCommand(value);
            fail("Expected EqCommandTagException not thrown.");
        }
        catch (EqCommandTagException e) {
            // expected exception thrown
        }
    }
    
    @Test
    public void testSendBooleanCommandUnknownReturnCode() {
        SourceCommandTagValue value = new SourceCommandTagValue(1L, "asd", 1L, (short) 0, true, "Boolean");
        prepareAndSendDelayedCommandAnswer((short) 1337);
        try {
            jecCommandRunner.runCommand(value);
            fail("Expected EqCommandTagException not thrown.");
        }
        catch (EqCommandTagException e) {
            // expected exception thrown
        }
    }
    
    @Test
    public void testSendAnalogCommandACK() throws EqCommandTagException {
        SourceCommandTagValue value = new SourceCommandTagValue(2L, "asd", 1L, (short) 0, 0.23, "Float");
        prepareAndSendDelayedCommandAnswer(StdConstants.ACK_MSG);
        jecCommandRunner.runCommand(value);
    }
    
    @Test
    public void testSendAnalogCommandNACK() {
        SourceCommandTagValue value = new SourceCommandTagValue(2L, "asd", 1L, (short) 0, 0.23, "Float");
        prepareAndSendDelayedCommandAnswer(StdConstants.NACK_MSG);
        try {
            jecCommandRunner.runCommand(value);
            fail("Expected EqCommandTagException not thrown.");
        }
        catch (EqCommandTagException e) {
            // expected exception thrown
        }
    }
    
    @Test
    public void testSendAnalogCommandUnknownReturnCode() {
        SourceCommandTagValue value = new SourceCommandTagValue(2L, "asd", 1L, (short) 0, 0.23, "Float");
        prepareAndSendDelayedCommandAnswer((short) 1337);
        try {
            jecCommandRunner.runCommand(value);
            fail("Expected EqCommandTagException not thrown.");
        }
        catch (EqCommandTagException e) {
            // expected exception thrown
        }
    }
    
    @Test
    public void testUnknownCommandTag() {
        SourceCommandTagValue value = new SourceCommandTagValue(31415926L, "asd", 1L, (short) 0, 0.23, "Float");
        prepareAndSendDelayedCommandAnswer((short) 1337);
        try {
            jecCommandRunner.runCommand(value);
            fail("Expected EqCommandTagException not thrown.");
        }
        catch (EqCommandTagException e) {
            // expected exception thrown
        }
    }
    
    @Test
    public void testIsCorrectMessageId() {
        JECPFrames jecpFrame = plcFactory.getRawRecvFrame();
        jecpFrame.SetMessageIdentifier(StdConstants.CONFIRM_ANA_CMD_MSG);
        assertTrue(jecCommandRunner.isCorrectMessageId(jecpFrame));
        jecpFrame.SetMessageIdentifier(StdConstants.CONFIRM_BOOL_CMD_CTRL_MSG);
        assertTrue(jecCommandRunner.isCorrectMessageId(jecpFrame));
        jecpFrame.SetMessageIdentifier(StdConstants.CONFIRM_BOOL_CMD_MSG);
        assertTrue(jecCommandRunner.isCorrectMessageId(jecpFrame));
    }

    /**
     * @param commandReturnCode
     */
    private void prepareAndSendDelayedCommandAnswer(final short commandReturnCode) {
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                JECPFrames frame = plcFactory.getSendFrame(StdConstants.BOOL_CMD_MSG);
                frame.SetJECWord(1, commandReturnCode);
                jecCommandRunner.processJECPFrame(frame);
                
            }
        }).start();
    }

    private SourceCommandTag getCommandTag(int pBlockType, Long id, int pWordId, int pBitId, int pResolutionFactor, float pMinVal, float pMaxVal, String pNativeAddress) throws ConfigurationException {
        SourceCommandTag commandTag = new SourceCommandTag(id, "asd");
        commandTag.setSourceRetries(5);
        commandTag.setSourceTimeout(150);
        PLCHardwareAddressImpl plcHardwareAddress = new PLCHardwareAddressImpl(pBlockType, pWordId, pBitId, pResolutionFactor, pMinVal, pMaxVal, pNativeAddress, 1000);
        commandTag.setHardwareAddress(plcHardwareAddress);
        return commandTag;
    }
}

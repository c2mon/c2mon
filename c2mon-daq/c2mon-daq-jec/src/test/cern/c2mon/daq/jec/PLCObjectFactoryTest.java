package cern.c2mon.daq.jec;

import org.junit.Before;
import org.junit.Test;
import static junit.framework.Assert.*;

import cern.c2mon.daq.jec.PLCObjectFactory;
import cern.c2mon.daq.jec.config.PLCConfiguration;
import cern.c2mon.daq.jec.tools.JECBinaryHelper;
import cern.c2mon.daq.jec.plc.ConnectionData;
import cern.c2mon.daq.jec.plc.JECPFrames;
import cern.c2mon.daq.jec.plc.PLCDriver;
import cern.c2mon.daq.jec.plc.StdConstants;

public class PLCObjectFactoryTest {
    
    private PLCObjectFactory plcObjectFactory;
    
    private String addressString = "plc_name=plcstaa05,plcstaa06;" +
        "Protocol=SiemensISO;Time_sync=Jec;Port=102;S_tsap=TCP-1;" +
        "D_tsap=TCP-1;Alive_handler_period=5000;" +
        "Dp_slave_address=4,5,6,7,8,9;";

    private PLCConfiguration plcConfiguration;
    
    @Before
    public void setUp() throws Exception {
        plcConfiguration = new PLCConfiguration();
        plcConfiguration.parsePLCAddress(addressString);
        plcObjectFactory = new PLCObjectFactory(plcConfiguration);
    }
    
    @Test
    public void testGetPLCDriver() {
        PLCDriver driver = plcObjectFactory.getPLCDriver();
        assertEquals(plcConfiguration.getProtocol(), driver.getClass().getSimpleName());
    }
    
    @Test
    public void testWrongPLCDriver() throws InstantiationException, IllegalAccessException {
        plcConfiguration.setProtocol("Nonsense");
        try {
            plcObjectFactory = new PLCObjectFactory(plcConfiguration);
        }
        catch (ClassNotFoundException e) {
            // should happen
            return;
        }
        fail("There should be a class not found excpetion.");
    }
    
    @Test
    public void testGetRawSendFrame() {
        JECPFrames sendFrame = plcObjectFactory.getRawSendFrame();
        assertEquals(0, sendFrame.getMsgID());
        assertEquals(0, sendFrame.offset);
        plcConfiguration.setProtocol("Schneider");
        JECPFrames sendFrame2 = plcObjectFactory.getRawSendFrame();
        assertEquals(0, sendFrame2.getMsgID());
        assertEquals(2, sendFrame2.offset);
    }
    
    @Test
    public void testGetSendFrame() {
        byte msgType = (byte) 12;
        JECPFrames sendFrame = plcObjectFactory.getSendFrame(msgType);
        assertEquals(msgType, sendFrame.getMsgID());
        assertEquals(0, sendFrame.offset);
        plcConfiguration.setProtocol("Schneider");
        byte msgType2 = (byte) 15;
        JECPFrames sendFrame2 = plcObjectFactory.getSendFrame(msgType2);
        assertEquals(msgType2, sendFrame2.getMsgID());
        assertEquals(2, sendFrame2.offset);
    }
    
    @Test
    public void testEmptyJECPFrame() {
        JECPFrames jecpFrame = plcObjectFactory.getRawRecvFrame();
        assertEquals(0, jecpFrame.offset);
        plcConfiguration.setProtocol("Schneider");
        JECPFrames jecpFrame2 = plcObjectFactory.getRawRecvFrame();
        assertEquals(0, jecpFrame2.offset);
    }
    
    @Test
    public void testCreateConnectionData() {
        ConnectionData connectionDataISO = plcObjectFactory.createConnectionData("aName");
        assertEquals("TCP-1", connectionDataISO.dest_tsap);
        assertEquals(102, connectionDataISO.port);
        assertEquals("TCP-1", connectionDataISO.src_tsap);
        assertEquals("aName", connectionDataISO.ip);
        plcConfiguration.setProtocol("Schneider");
        ConnectionData connectionDataSchneider = plcObjectFactory.createConnectionData("anotherName");
        assertNull(connectionDataSchneider.dest_tsap);
        assertEquals(102, connectionDataSchneider.port);
        assertNull(connectionDataSchneider.src_tsap);
        assertEquals("anotherName", connectionDataSchneider.ip);
    }
    
    @Test
    public void testGetBasicSetConfigurationMessage() {
        int booleanDataLength = 1;
        int analogDataLength = 2;
        int mMDBoolModules = 3;
        int mMDAnalogModules = 4;
        int mMDBoolCommandModules = 5;
        int mMDAnalogCommandModules = 6;
        JECPFrames jecpFrame = 
            plcObjectFactory.getBasicSetConfigurationMessage(
                    booleanDataLength, analogDataLength, mMDBoolModules, 
                    mMDAnalogModules, mMDBoolCommandModules, 
                    mMDAnalogCommandModules);
        assertEquals(StdConstants.SET_CFG_MSG, jecpFrame.getMsgID());
        assertEquals(StdConstants.PLC_CONF_DATA, jecpFrame.GetDataType());
        assertEquals(booleanDataLength, JECBinaryHelper.getBooleanWord(8 + jecpFrame.offset/2, jecpFrame.frame));
        assertEquals(analogDataLength, JECBinaryHelper.getBooleanWord(9 + jecpFrame.offset/2, jecpFrame.frame));
        assertEquals(mMDBoolModules, JECBinaryHelper.getBooleanWord(10 + jecpFrame.offset/2, jecpFrame.frame));
        assertEquals(mMDAnalogModules, JECBinaryHelper.getBooleanWord(11 + jecpFrame.offset/2, jecpFrame.frame));
        assertEquals(mMDBoolCommandModules, JECBinaryHelper.getBooleanWord(12 + jecpFrame.offset/2, jecpFrame.frame));
        assertEquals(mMDAnalogCommandModules, JECBinaryHelper.getBooleanWord(13 + jecpFrame.offset/2, jecpFrame.frame));
        assertEquals(mMDAnalogCommandModules, JECBinaryHelper.getBooleanWord(13 + jecpFrame.offset/2, jecpFrame.frame));
        assertEquals(4, jecpFrame.frame[30 + jecpFrame.offset]);
        assertEquals(5, jecpFrame.frame[31 + jecpFrame.offset]);
        assertEquals(6, jecpFrame.frame[32 + jecpFrame.offset]);
        assertEquals(7, jecpFrame.frame[33 + jecpFrame.offset]);
        assertEquals(8, jecpFrame.frame[34 + jecpFrame.offset]);
        assertEquals(9, jecpFrame.frame[35 + jecpFrame.offset]);
    }
}

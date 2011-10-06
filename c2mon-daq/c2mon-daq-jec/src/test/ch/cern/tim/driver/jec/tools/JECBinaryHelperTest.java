package ch.cern.tim.driver.jec.tools;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import ch.cern.tim.driver.jec.tools.JECBinaryHelper;
import ch.cern.tim.jec.StdConstants;

public class JECBinaryHelperTest {
    
    private byte[] testByteArray = new byte[StdConstants.JEC_DATA_SIZE];
    private int testWord = 3;
    
    @Before
    public void setUp() {
        testByteArray[testWord * 2] = 0x10;
        testByteArray[(testWord * 2) + 1] = -1; // 0xFF
        testByteArray[(testWord * 2) + 2] = 0x12;
        testByteArray[(testWord * 2) + 3] = 0x13;
    }
    
    @Test
    public void testGetBooleanWord() {
        int result = JECBinaryHelper.getBooleanWord(testWord, testByteArray);
        assertEquals(0x10FF, result);
    }
    
    @Test
    public void testGetAnalogWord() {
        int result = JECBinaryHelper.getAnalogWord(testWord, testByteArray);
        assertEquals(0x10FF, result);
    }
    
    @Test
    public void testGetAnalogIEEEWord() {
        int result = JECBinaryHelper.getAnalogIEEEWord(testWord, testByteArray);
        assertEquals(0x10FF1213, result);
    }
    
    @Test
    public void testPutIEEEWord() {
        JECBinaryHelper.putIEEEAnalogValueIntoArray(testByteArray, 0x10FF1213, 0);
        assertEquals(0x10, testByteArray[0]);
        assertEquals(-1, testByteArray[1]);
        assertEquals(0x12, testByteArray[2]);
        assertEquals(0x13, testByteArray[3]);
    }
    
    @Test
    public void testPutAnalogWord() {
        JECBinaryHelper.putAnalogValueIntoArray(testByteArray, (short) 0x10FF, 0);
        assertEquals(0x10, testByteArray[0]);
        assertEquals(-1, testByteArray[1]);
    }
    
    @Test
    public void testAnalogCycle() {
        JECBinaryHelper.putAnalogValueIntoArray(testByteArray, (short) -50, 0);
        int value = JECBinaryHelper.getAnalogWord(0, testByteArray);
        assertEquals(-50, value);
    }
}

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
package cern.c2mon.daq.jec.tools;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import cern.c2mon.daq.jec.tools.JECBinaryHelper;
import cern.c2mon.daq.jec.plc.StdConstants;

/**
 * 
 * @author Andreas Lang, Mark Brightwell
 *
 */
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
    
    @Test
    public void testMaskIEEEAbsoluteFilteringType() {
      int testDeadband = Float.floatToIntBits(4.5f); //assume positive 
      int maskedValue = JECBinaryHelper.maskIEEEAbsoluteFilteringType(testDeadband);      
      assertEquals("01" + Integer.toBinaryString(testDeadband).substring(0, 30), "0" + Integer.toBinaryString(maskedValue)); 
    }
    
    @Test
    public void testMaskIEEERelativeFilteringType() {
      int testDeadband = Float.floatToIntBits(40.5f); //assume positive 
      int maskedValue = JECBinaryHelper.maskIEEERelativeFilteringType(testDeadband);      
      assertEquals("11" + Integer.toBinaryString(testDeadband).substring(0, 30), Integer.toBinaryString(maskedValue)); 
    }
    
    @Test
    public void testMaskAbsoluteFilteringType() {
      short testDeadband = 4000;
      short maskedValue = JECBinaryHelper.maskAbsoluteFilteringType(testDeadband);      
      assertEquals(Integer.toBinaryString(testDeadband), Integer.toBinaryString(maskedValue)); 
    }
    
    @Test
    public void testMaskRelativeFilteringType() {
      short testDeadband = 23; 
      short maskedValue = JECBinaryHelper.maskRelativeFilteringType(testDeadband);      
      assertEquals(Integer.toBinaryString(testDeadband), Integer.toBinaryString(maskedValue & 0x7FFF)); //remove leading 1 
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testMaskAbsoluteFilteringTypeException() {
      short deadband = 16400; //bigger than 2^14 (2 most significant bits are 0)
      short maskedValue = JECBinaryHelper.maskAbsoluteFilteringType(deadband);
    }
}

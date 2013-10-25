/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005 - 2011 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.driver.tools;

import static org.junit.Assert.*;

import org.junit.Test;

import cern.c2mon.driver.tools.TIMDriverSimpleTypeConverter;
import cern.tim.shared.common.type.TagDataType;
import cern.tim.shared.daq.datatag.SourceDataTag;

public class TIMDriverSimpleTypeConverterTest {
    
    private TIMDriverSimpleTypeConverter timDriverSimpleTypeConverter = new TIMDriverSimpleTypeConverter();

    private SourceDataTag sourceDataTag = new SourceDataTag(1L, "Hello World", false);
    
    @Test
    public void testConvertBooleanToBoolean() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_BOOLEAN);
        Object objTrue = timDriverSimpleTypeConverter.convert(sourceDataTag, true);
        Object objFalse = timDriverSimpleTypeConverter.convert(sourceDataTag, false);
        
        assertTrue(objTrue instanceof Boolean);
        assertTrue((Boolean)objTrue);
        assertTrue(objFalse instanceof Boolean);
        assertFalse((Boolean)objFalse);
    }
    
    @Test
    public void testConvertBooleanToInteger() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_INTEGER);
        Object objTrue = timDriverSimpleTypeConverter.convert(sourceDataTag, true);
        Object objFalse = timDriverSimpleTypeConverter.convert(sourceDataTag, false);
        
        assertTrue(objTrue instanceof Integer);
        assertEquals(1, objTrue);
        assertTrue(objFalse instanceof Integer);
        assertEquals(0, objFalse);
    }
    
    @Test
    public void testConvertBooleanToLong() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_LONG);
        Object objTrue = timDriverSimpleTypeConverter.convert(sourceDataTag, true);
        Object objFalse = timDriverSimpleTypeConverter.convert(sourceDataTag, false);
        
        assertTrue(objTrue instanceof Long);
        assertEquals(1L, objTrue);
        assertTrue(objFalse instanceof Long);
        assertEquals(0L, objFalse);
    }
    
    @Test
    public void testConvertBooleanToFloat() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_FLOAT);
        Object objTrue = timDriverSimpleTypeConverter.convert(sourceDataTag, true);
        Object objFalse = timDriverSimpleTypeConverter.convert(sourceDataTag, false);
        
        assertTrue(objTrue instanceof Float);
        assertEquals(1.0f, objTrue);
        assertTrue(objFalse instanceof Float);
        assertEquals(0.0f, objFalse);
    }
    
    @Test
    public void testConvertBooleanToDouble() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_DOUBLE);
        Object objTrue = timDriverSimpleTypeConverter.convert(sourceDataTag, true);
        Object objFalse = timDriverSimpleTypeConverter.convert(sourceDataTag, false);
        
        assertTrue(objTrue instanceof Double);
        assertEquals(1.0, objTrue);
        assertTrue(objFalse instanceof Double);
        assertEquals(0.0, objFalse);
    }
    
    @Test
    public void testConvertBooleanToString() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_STRING);
        Object objTrue = timDriverSimpleTypeConverter.convert(sourceDataTag, true);
        Object objFalse = timDriverSimpleTypeConverter.convert(sourceDataTag, false);
        
        assertTrue(objTrue instanceof String);
        assertEquals("true", objTrue);
        assertTrue(objFalse instanceof String);
        assertEquals("false", objFalse);
    }
    
    @Test
    public void testConvertByteToBoolean() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_BOOLEAN);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, (byte)1);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, (byte)0);
        Object objTwo = timDriverSimpleTypeConverter.convert(sourceDataTag, (byte)2);
        
        assertTrue(objOne instanceof Boolean);
        assertTrue((Boolean)objOne);
        assertTrue(objZero instanceof Boolean);
        assertFalse((Boolean)objZero);
        assertNull(objTwo);
    }
    
    @Test
    public void testConvertByteToInteger() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_INTEGER);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, (byte)1);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, (byte)0);
        
        assertTrue(objOne instanceof Integer);
        assertEquals(1, objOne);
        assertTrue(objZero instanceof Integer);
        assertEquals(0, objZero);
    }
    
    @Test
    public void testConvertByteToLong() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_LONG);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, (byte)1);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, (byte)0);
        
        assertTrue(objOne instanceof Long);
        assertEquals(1L, objOne);
        assertTrue(objZero instanceof Long);
        assertEquals(0L, objZero);
    }
    
    @Test
    public void testConvertByteToFloat() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_FLOAT);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, (byte)1);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, (byte)0);
        
        assertTrue(objOne instanceof Float);
        assertEquals(1.0f, objOne);
        assertTrue(objZero instanceof Float);
        assertEquals(0.0f, objZero);
    }
    
    @Test
    public void testConvertByteToDouble() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_DOUBLE);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, (byte)1);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, (byte)0);
        
        assertTrue(objOne instanceof Double);
        assertEquals(1.0, objOne);
        assertTrue(objZero instanceof Double);
        assertEquals(0.0, objZero);
    }
    
    @Test
    public void testConvertByteToString() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_STRING);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, (byte)1);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, (byte)0);
        
        assertTrue(objOne instanceof String);
        assertEquals("1", objOne);
        assertTrue(objZero instanceof String);
        assertEquals("0", objZero);
    }
    
    
    @Test
    public void testConvertShortToBoolean() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_BOOLEAN);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, (short)1);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, (short)0);
        Object objTwo = timDriverSimpleTypeConverter.convert(sourceDataTag, (short)2);
        
        assertTrue(objOne instanceof Boolean);
        assertTrue((Boolean)objOne);
        assertTrue(objZero instanceof Boolean);
        assertFalse((Boolean)objZero);
        assertNull(objTwo);
    }
    
    @Test
    public void testConvertShortToInteger() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_INTEGER);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, (short)1);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, (short)0);
        
        assertTrue(objOne instanceof Integer);
        assertEquals(1, objOne);
        assertTrue(objZero instanceof Integer);
        assertEquals(0, objZero);
    }
    
    @Test
    public void testConvertShortToLong() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_LONG);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, (short)1);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, (short)0);
        
        assertTrue(objOne instanceof Long);
        assertEquals(1L, objOne);
        assertTrue(objZero instanceof Long);
        assertEquals(0L, objZero);
    }
    
    @Test
    public void testConvertShortToFloat() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_FLOAT);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, (short)1);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, (short)0);
        
        assertTrue(objOne instanceof Float);
        assertEquals(1.0f, objOne);
        assertTrue(objZero instanceof Float);
        assertEquals(0.0f, objZero);
    }
    
    @Test
    public void testConvertShortToDouble() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_DOUBLE);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, (short)1);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, (short)0);
        
        assertTrue(objOne instanceof Double);
        assertEquals(1.0, objOne);
        assertTrue(objZero instanceof Double);
        assertEquals(0.0, objZero);
    }
    
    @Test
    public void testConvertShortToString() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_STRING);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, (short)1);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, (short)0);
        
        assertTrue(objOne instanceof String);
        assertEquals("1", objOne);
        assertTrue(objZero instanceof String);
        assertEquals("0", objZero);
    }
    
    @Test
    public void testConvertIntegerToBoolean() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_BOOLEAN);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, (int)1);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, (int)0);
        Object objTwo = timDriverSimpleTypeConverter.convert(sourceDataTag, (int)2);
        
        assertTrue(objOne instanceof Boolean);
        assertTrue((Boolean)objOne);
        assertTrue(objZero instanceof Boolean);
        assertFalse((Boolean)objZero);
        assertNull(objTwo);
    }
    
    @Test
    public void testConvertIntegerToInteger() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_INTEGER);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, (int)1);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, (int)0);
        
        assertTrue(objOne instanceof Integer);
        assertEquals(1, objOne);
        assertTrue(objZero instanceof Integer);
        assertEquals(0, objZero);
    }
    
    @Test
    public void testConvertIntegerToLong() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_LONG);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, (int)1);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, (int)0);
        
        assertTrue(objOne instanceof Long);
        assertEquals(1L, objOne);
        assertTrue(objZero instanceof Long);
        assertEquals(0L, objZero);
    }
    
    @Test
    public void testConvertIntegerToFloat() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_FLOAT);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, (int)1);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, (int)0);
        
        assertTrue(objOne instanceof Float);
        assertEquals(1.0f, objOne);
        assertTrue(objZero instanceof Float);
        assertEquals(0.0f, objZero);
    }
    
    @Test
    public void testConvertIntegerToDouble() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_DOUBLE);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, (int)1);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, (int)0);
        
        assertTrue(objOne instanceof Double);
        assertEquals(1.0, objOne);
        assertTrue(objZero instanceof Double);
        assertEquals(0.0, objZero);
    }
    
    @Test
    public void testConvertIntegerToString() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_STRING);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, (int)1);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, (int)0);
        
        assertTrue(objOne instanceof String);
        assertEquals("1", objOne);
        assertTrue(objZero instanceof String);
        assertEquals("0", objZero);
    }
    
    @Test
    public void testConvertLongToBoolean() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_BOOLEAN);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, 1L);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, 0L);
        Object objTwo = timDriverSimpleTypeConverter.convert(sourceDataTag, 2L);
        
        assertTrue(objOne instanceof Boolean);
        assertTrue((Boolean)objOne);
        assertTrue(objZero instanceof Boolean);
        assertFalse((Boolean)objZero);
        assertNull(objTwo);
    }
    
    @Test
    public void testConvertLongToInteger() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_INTEGER);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, 1L);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, 0L);
        Object objOutOfInt = timDriverSimpleTypeConverter.convert(sourceDataTag, Integer.MAX_VALUE * 3L);
        
        assertTrue(objOne instanceof Integer);
        assertEquals(1, objOne);
        assertTrue(objZero instanceof Integer);
        assertEquals(0, objZero);
        assertNull(objOutOfInt);
    }
    
    @Test
    public void testConvertLongToLong() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_LONG);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, 1L);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, 0L);
        
        assertTrue(objOne instanceof Long);
        assertEquals(1L, objOne);
        assertTrue(objZero instanceof Long);
        assertEquals(0L, objZero);
    }
    
    @Test
    public void testConvertLongToFloat() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_FLOAT);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, 1L);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, 0L);

        assertTrue(objOne instanceof Float);
        assertEquals(1.0f, objOne);
        assertTrue(objZero instanceof Float);
        assertEquals(0.0f, objZero);
    }
    
    @Test
    public void testConvertLongToDouble() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_DOUBLE);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, 1L);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, 0L);
        
        assertTrue(objOne instanceof Double);
        assertEquals(1.0, objOne);
        assertTrue(objZero instanceof Double);
        assertEquals(0.0, objZero);
    }
    
    @Test
    public void testConvertLongToString() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_STRING);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, 1L);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, 0L);
        
        assertTrue(objOne instanceof String);
        assertEquals("1", objOne);
        assertTrue(objZero instanceof String);
        assertEquals("0", objZero);
    }
    
    @Test
    public void testConvertFloatToBoolean() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_BOOLEAN);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, 1.0f);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, 0.0f);
        Object objTwo = timDriverSimpleTypeConverter.convert(sourceDataTag, 2.0f);
        
        assertTrue(objOne instanceof Boolean);
        assertTrue((Boolean)objOne);
        assertTrue(objZero instanceof Boolean);
        assertFalse((Boolean)objZero);
        assertNull(objTwo);
    }
    
    @Test
    public void testConvertFloatToInteger() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_INTEGER);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, 1.0f);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, 0.0f);
        Object objOutOfInt = timDriverSimpleTypeConverter.convert(sourceDataTag, Integer.MAX_VALUE * 3.1f);
        
        assertTrue(objOne instanceof Integer);
        assertEquals(1, objOne);
        assertTrue(objZero instanceof Integer);
        assertEquals(0, objZero);
        assertNull(objOutOfInt);
    }
    
    @Test
    public void testConvertFloatToLong() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_LONG);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, 1.0f);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, 0.0f);
        Object objOutOfLong = timDriverSimpleTypeConverter.convert(sourceDataTag, Long.MAX_VALUE * 3.1f);
        
        assertTrue(objOne instanceof Long);
        assertEquals(1L, objOne);
        assertTrue(objZero instanceof Long);
        assertEquals(0L, objZero);
        assertNull(objOutOfLong);
    }
    
    @Test
    public void testConvertFloatToFloat() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_FLOAT);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, 1.0f);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, 0.0f);

        assertTrue(objOne instanceof Float);
        assertEquals(1.0f, objOne);
        assertTrue(objZero instanceof Float);
        assertEquals(0.0f, objZero);
    }
    
    @Test
    public void testConvertFloatToDouble() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_DOUBLE);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, 1.0f);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, 0.0f);
        
        assertTrue(objOne instanceof Double);
        assertEquals(1.0, objOne);
        assertTrue(objZero instanceof Double);
        assertEquals(0.0, objZero);
    }
    
    @Test
    public void testConvertFloatToString() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_STRING);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, 1.0f);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, 0.0f);
        
        assertTrue(objOne instanceof String);
        assertEquals("1.0", objOne);
        assertTrue(objZero instanceof String);
        assertEquals("0.0", objZero);
    }

    @Test
    public void testConvertDoubleToBoolean() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_BOOLEAN);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, 1.0);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, 0.0);
        Object objTwo = timDriverSimpleTypeConverter.convert(sourceDataTag, 2.0);
        
        assertTrue(objOne instanceof Boolean);
        assertTrue((Boolean)objOne);
        assertTrue(objZero instanceof Boolean);
        assertFalse((Boolean)objZero);
        assertNull(objTwo);
    }
    
    @Test
    public void testConvertDoubleToInteger() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_INTEGER);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, 1.0);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, 0.0);
        Object objOutOfInt = timDriverSimpleTypeConverter.convert(sourceDataTag, Integer.MAX_VALUE * 3.1);
        
        assertTrue(objOne instanceof Integer);
        assertEquals(1, objOne);
        assertTrue(objZero instanceof Integer);
        assertEquals(0, objZero);
        assertNull(objOutOfInt);
    }
    
    @Test
    public void testConvertDoubleToLong() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_LONG);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, 1.0);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, 0.0);
        Object objOutOfLong = timDriverSimpleTypeConverter.convert(sourceDataTag, Long.MAX_VALUE * 3.1);
        
        assertTrue(objOne instanceof Long);
        assertEquals(1L, objOne);
        assertTrue(objZero instanceof Long);
        assertEquals(0L, objZero);
        assertNull(objOutOfLong);
    }
    
    @Test
    public void testConvertDoubleToFloat() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_FLOAT);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, 1.0);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, 0.0);
        Object objOutOfFloat = timDriverSimpleTypeConverter.convert(sourceDataTag, Float.MAX_VALUE * 3.1);

        assertTrue(objOne instanceof Float);
        assertEquals(1.0f, objOne);
        assertTrue("Object: " + objZero, objZero instanceof Float);
        assertEquals(0.0f, objZero);
        assertNull(objOutOfFloat);
    }
    
    @Test
    public void testConvertDoubleToDouble() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_DOUBLE);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, 1.0);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, 0.0);
        
        assertTrue(objOne instanceof Double);
        assertEquals(1.0, objOne);
        assertTrue(objZero instanceof Double);
        assertEquals(0.0, objZero);
    }
    
    @Test
    public void testConvertDoubleToString() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_STRING);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, 1.0);
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, 0.0);
        
        assertTrue(objOne instanceof String);
        assertEquals("1.0", objOne);
        assertTrue(objZero instanceof String);
        assertEquals("0.0", objZero);
    }

    @Test
    public void testConvertStringToBoolean() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_BOOLEAN);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, "1");
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, "0.0");
        Object objOne2 = timDriverSimpleTypeConverter.convert(sourceDataTag, "true");
        Object objZero2 = timDriverSimpleTypeConverter.convert(sourceDataTag, "false");
        Object objTwo = timDriverSimpleTypeConverter.convert(sourceDataTag, "asd");
        
        assertTrue(objOne instanceof Boolean);
        assertTrue((Boolean)objOne);
        assertTrue(objZero instanceof Boolean);
        assertFalse((Boolean)objZero);
        assertTrue(objOne instanceof Boolean);
        assertTrue((Boolean)objOne2);
        assertTrue(objZero instanceof Boolean);
        assertFalse((Boolean)objZero2);
        assertNull(objTwo);
    }
    
    @Test
    public void testConvertStringToInteger() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_INTEGER);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, "1");
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, "0");
        Object objOutOfInt = timDriverSimpleTypeConverter.convert(sourceDataTag, "asjdasd");
        
        assertTrue(objOne instanceof Integer);
        assertEquals(1, objOne);
        assertTrue(objZero instanceof Integer);
        assertEquals(0, objZero);
        assertNull(objOutOfInt);
    }
    
    @Test
    public void testConvertStringToLong() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_LONG);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, "1");
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, "0");
        Object objOutOfLong = timDriverSimpleTypeConverter.convert(sourceDataTag, "xyz");
        
        assertTrue(objOne instanceof Long);
        assertEquals(1L, objOne);
        assertTrue(objZero instanceof Long);
        assertEquals(0L, objZero);
        assertNull(objOutOfLong);
    }
    
    @Test
    public void testConvertStringToFloat() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_FLOAT);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, "1.0");
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, "0");
        Object objOutOfFloat = timDriverSimpleTypeConverter.convert(sourceDataTag, "asdas");

        assertTrue(objOne instanceof Float);
        assertEquals(1.0f, objOne);
        assertTrue("Object: " + objZero, objZero instanceof Float);
        assertEquals(0.0f, objZero);
        assertNull(objOutOfFloat);
    }
    
    @Test
    public void testConvertStringToDouble() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_DOUBLE);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, "1");
        Object objZero = timDriverSimpleTypeConverter.convert(sourceDataTag, "0.0");
        
        assertTrue(objOne instanceof Double);
        assertEquals(1.0, objOne);
        assertTrue(objZero instanceof Double);
        assertEquals(0.0, objZero);
    }
    
    @Test
    public void testConvertStringToString() {
        sourceDataTag.setDataTypeNumeric(TagDataType.TYPE_STRING);
        Object objOne = timDriverSimpleTypeConverter.convert(sourceDataTag, "1.0");
        Object objAsd = timDriverSimpleTypeConverter.convert(sourceDataTag, "asd");
        
        assertTrue(objOne instanceof String);
        assertEquals("1.0", objOne);
        assertTrue(objAsd instanceof String);
        assertEquals("asd", objAsd);
    }
}

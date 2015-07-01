/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2012 CERN. This program is free software; you can
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
 ******************************************************************************/
package cern.c2mon.shared.common.type;

import static org.junit.Assert.*;
import org.junit.Test;

import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;

public class TypeConverterTest {

  @Test
  public void testObjectCast() {
    // String
    castTest(Boolean.TRUE, String.class, "true");
    castTest(Boolean.FALSE, String.class, "false");  
    castTest(new Double(25), String.class, "25.0");
    castTest(new Float(25), String.class, "25.0");
    castTest(new Short((short)25), String.class, "25");
    castTest(new Long(25), String.class, "25");
    castTest(new Integer(25), String.class, "25");
    castTest("25", String.class, "25");

    // Boolean
    castTest(Boolean.TRUE, Boolean.class, Boolean.TRUE);
    castTest(Boolean.FALSE, Boolean.class, Boolean.FALSE);  
    castTest(new Double(0), Boolean.class, Boolean.FALSE);
    castTest(new Double(1), Boolean.class, Boolean.TRUE);
    castTest(new Float(0), Boolean.class, Boolean.FALSE);
    castTest(new Float(1), Boolean.class, Boolean.TRUE);
    castTest(new Short((short)0), Boolean.class, Boolean.FALSE);
    castTest(new Short((short)1), Boolean.class, Boolean.TRUE);
    castTest(new Long(0), Boolean.class, Boolean.FALSE);
    castTest(new Long(1), Boolean.class, Boolean.TRUE);
    castTest(new Integer(0), Boolean.class, Boolean.FALSE);
    castTest(new Integer(1), Boolean.class, Boolean.TRUE);
    castTest("true", Boolean.class, Boolean.TRUE);
    castTest("false", Boolean.class, Boolean.FALSE);

    // Integer
    castTest(Boolean.TRUE, Integer.class, new Integer(1));
    castTest(Boolean.FALSE, Integer.class, new Integer(0));  
    castTest(new Double(0), Integer.class, new Integer(0));
    castTest(new Double(1), Integer.class, new Integer(1));
    castTest(new Double(25.5d), Integer.class, new Integer(26));
    castTest(new Float(0), Integer.class, new Integer(0));
    castTest(new Float(1), Integer.class, new Integer(1));
    castTest(new Float(25.5f), Integer.class, new Integer(26));
    castTest(new Short((short)0), Integer.class, new Integer(0));
    castTest(new Short((short)1), Integer.class, new Integer(1));
    castTest(new Short((short)25), Integer.class, new Integer(25));
    castTest(new Long(0), Integer.class, new Integer(0));
    castTest(new Long(1), Integer.class, new Integer(1));
    
    castTest(new Integer(0), Integer.class, new Integer(0));
    castTest(new Integer(1), Integer.class, new Integer(1));
    castTest(new Integer(25), Integer.class, new Integer(25));
    castTest("0", Integer.class, new Integer(0));
    castTest("25.5", Integer.class, Integer.valueOf(26));

    // Short
    castTest(Boolean.TRUE, Short.class, new Short((short)1));
    castTest(Boolean.FALSE, Short.class, new Short((short)0));  
    castTest(new Double(0), Short.class, new Short((short)0));
    castTest(new Double(1), Short.class, new Short((short)1));
    castTest(new Double(25.5), Short.class, new Short((short)26));
    castTest(new Float(0), Short.class, new Short((short)0));
    castTest(new Float(1), Short.class, new Short((short)1));
    castTest(new Float(25.5), Short.class, new Short((short)26));
    castTest(new Short((short)0), Short.class, new Short((short)0));
    castTest(new Short((short)1), Short.class, new Short((short)1));
    castTest(new Short((short)25), Short.class, new Short((short)25));
    castTest(new Long(0), Short.class, new Short((short)0));
    castTest(new Long(1), Short.class, new Short((short)1));
    castTest(new Integer(0), Short.class, new Short((short)0));
    castTest(new Integer(1), Short.class, new Short((short)1));
    castTest(new Integer(25), Short.class, new Short((short)25));
    castTest("0", Short.class, new Short((short)0));
    castTest("25.5", Short.class, Short.valueOf((short) 26));

    // Long
    castTest(Boolean.TRUE, Long.class, new Long(1));
    castTest(Boolean.FALSE, Long.class, new Long(0));  
    castTest(new Double(0), Long.class, new Long(0));
    castTest(new Double(1), Long.class, new Long(1));
    castTest(new Double(25.5), Long.class, new Long(26));
    castTest(new Float(0), Long.class, new Long(0));
    castTest(new Float(1), Long.class, new Long(1));
    castTest(new Float(25.5), Long.class, new Long(26));
    castTest(new Short((short)0), Long.class, new Long(0));
    castTest(new Short((short)1), Long.class, new Long(1));
    castTest(new Short((short)25), Long.class, new Long(25));
    castTest(new Long(0), Long.class, new Long(0));
    castTest(new Long(1), Long.class, new Long(1));
    castTest(new Long(2123190123125l), Long.class, new Long(2123190123125l));
    castTest(new Integer(0), Long.class, new Long(0));
    castTest(new Integer(1), Long.class, new Long(1));
    castTest(new Integer(25), Long.class, new Long(25));
    castTest("0", Long.class, new Long(0));
    castTest("25.5", Long.class, Long.valueOf(26L));

    // Float
    castTest(Boolean.TRUE, Float.class, new Float(1));
    castTest(Boolean.FALSE, Float.class, new Float(0));  
    castTest(new Double(0), Float.class, new Float(0));
    castTest(new Double(1), Float.class, new Float(1));
    castTest(new Double(25.5), Float.class, new Float(25.5));
    castTest(new Float(0), Float.class, new Float(0));
    castTest(new Float(1), Float.class, new Float(1));
    castTest(new Float(25.5), Float.class, new Float(25.5));
    castTest(new Short((short)0), Float.class, new Float(0));
    castTest(new Short((short)1), Float.class, new Float(1));
    castTest(new Short((short)25), Float.class, new Float(25));
    castTest(new Long(0), Float.class, new Float(0));
    castTest(new Long(1), Float.class, new Float(1));
    castTest(new Long(2123190123125l), Float.class, new Float(2123190123125l));
    castTest(new Integer(0), Float.class, new Float(0));
    castTest(new Integer(1), Float.class, new Float(1));
    castTest(new Integer(25), Float.class, new Float(25));
    castTest("0", Float.class, new Float(0));
    castTest("25.5", Float.class, new Float(25.5));

    // Double
    castTest(Boolean.TRUE, Double.class, new Double(1));
    castTest(Boolean.FALSE, Double.class, new Double(0));  
    castTest(new Double(0), Double.class, new Double(0));
    castTest(new Double(1), Double.class, new Double(1));
    castTest(new Double(25.5), Double.class, new Double(25.5));
    castTest(new Float(0), Double.class, new Double(0));
    castTest(new Float(1), Double.class, new Double(1));
    castTest(new Float(25.5), Double.class, new Double(25.5));
    castTest(new Short((short)0), Double.class, new Double(0));
    castTest(new Short((short)1), Double.class, new Double(1));
    castTest(new Short((short)25), Double.class, new Double(25));
    castTest(new Long(0), Double.class, new Double(0));
    castTest(new Long(1), Double.class, new Double(1));
    castTest(new Long(2123190123125l), Double.class, new Double(2123190123125l));
    castTest(new Integer(0), Double.class, new Double(0));
    castTest(new Integer(1), Double.class, new Double(1));
    castTest(new Integer(25), Double.class, new Double(25));
    castTest("0", Double.class, new Double(0));
    castTest("25.5", Double.class, new Double(25.5));
    
    castTest("RUNNING", SupervisionStatus.class, SupervisionStatus.RUNNING);
    castTest("DOWN", SupervisionStatus.class, SupervisionStatus.DOWN);    
  }
  
  @Test(expected=ClassCastException.class)
  public void testDoubleToBooleanCastException() {
    castTest(Double.valueOf(25d), Boolean.class);
  }
  
  @Test(expected=ClassCastException.class)
  public void testFloatToBooleanCastException() {
    castTest(Float.valueOf(25f), Boolean.class);
  }
  
  @Test(expected=ClassCastException.class)
  public void testLongToBooleanCastException() {
    castTest(Long.valueOf(25L), Boolean.class);
  }
  
  @Test(expected=ClassCastException.class)
  public void testIntegerToBooleanCastException() {
    castTest(Integer.valueOf(25), Boolean.class);
  }
  
  @Test(expected=ClassCastException.class)
  public void testShortToBooleanCastException() {
    castTest(Short.valueOf((short)25), Boolean.class);
  }
  
  @Test(expected=ClassCastException.class)
  public void testStringToBooleanCastException() {
    castTest("25", Boolean.class);
  }
  
  @Test(expected=ClassCastException.class)
  public void testExceptionToBooleanCastException() {
    castTest(new Exception("test"), Boolean.class);
  }
  
  @Test(expected=ClassCastException.class)
  public void testTooBigLongToIntegerCastException() {
    castTest(new Long(2123190123125l), Integer.class);
  }
  
  @Test(expected=ClassCastException.class)
  public void testTooBigLongToShortCastException() {
    castTest(new Long(2123190123125l), Short.class);
  }
  
  @Test
  public void testBooleanStringToLongCast() {
    assertEquals(Long.valueOf(0l), TypeConverter.castToType("false", Long.class));
    assertEquals(Long.valueOf(1l), TypeConverter.castToType("true", Long.class));
    assertEquals(true, TypeConverter.isConvertible("false", Long.class));
    assertEquals(true, TypeConverter.isConvertible("true", Long.class));
  }
  
  @Test
  public void testBooleanStringToIntegerCast() {
    assertEquals(Integer.valueOf(0), TypeConverter.castToType("false", Integer.class));
    assertEquals(Integer.valueOf(1), TypeConverter.castToType("true", Integer.class));
    assertEquals(true, TypeConverter.isConvertible("false", Integer.class));
    assertEquals(true, TypeConverter.isConvertible("true", Integer.class));
  }
  
  @Test
  public void testBooleanStringToFloatCast() {
    assertEquals(Float.valueOf(0f), TypeConverter.castToType("False", Float.class));
    assertEquals(Float.valueOf(1f), TypeConverter.castToType("True", Float.class));
    assertEquals(true, TypeConverter.isConvertible("False", Float.class));
    assertEquals(true, TypeConverter.isConvertible("True", Float.class));
  }
  
  @Test
  public void testBooleanStringToDoubleCast() {
    assertEquals(Double.valueOf(0d), TypeConverter.castToType("false", Double.class));
    assertEquals(Double.valueOf(1d), TypeConverter.castToType("true", Double.class));
    assertEquals(true, TypeConverter.isConvertible("false", Double.class));
    assertEquals(true, TypeConverter.isConvertible("true", Double.class));
  }
  
  @Test
  public void testBooleanStringToShortCast() {
    assertEquals(Short.valueOf((short) 0), TypeConverter.castToType("false", Short.class));
    assertEquals(Short.valueOf((short) 1), TypeConverter.castToType("true", Short.class));
    assertEquals(true, TypeConverter.isConvertible("false", Short.class));
    assertEquals(true, TypeConverter.isConvertible("true", Short.class));
  }
  
  @Test(expected=ClassCastException.class)
  public void testStringToEnumCastException() {
    castTest("RNNING", SupervisionStatus.class);
  }
  
  @Test(expected=ClassCastException.class)
  public void testExceptionToLongCastException() {
    castTest(new Exception("test"), Long.class);
  }
  
  @Test(expected=ClassCastException.class)
  public void testExceptionToIntegerCastException() {
    castTest(new Exception("test"), Integer.class);
  }
  
  @Test(expected=ClassCastException.class)
  public void testExceptionToFloatCastException() {
    castTest(new Exception("test"), Float.class);
  }
  
  @Test(expected=ClassCastException.class)
  public void testExceptionToDoubleCastException() {
    castTest(new Exception("test"), Double.class);
  }
  
  @Test(expected=ClassCastException.class)
  public void testExceptionToShortCastException() {
    castTest(new Exception("test"), Short.class);
  }
  
  private static void castTest(final Object pValue, final Class<?> pTargetClass) {
    castTest(pValue, pTargetClass, null);
  }
  
  private static void castTest(final Object pValue, final Class<?> pTargetClass, Object pExpectedResult) {
    Object resultValue = null;
    resultValue = TypeConverter.castToType(pValue, pTargetClass);
    if (pExpectedResult != null) {
      assertEquals("!!! ERROR: Expected result was " + pExpectedResult + " of type " + pExpectedResult.getClass().getName(), pExpectedResult, resultValue);
    }
    else {
      assertNull("!!! ERROR: Conversion should not have succeeded", resultValue);
    }
  }
}

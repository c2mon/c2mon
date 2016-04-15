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
package cern.c2mon.shared.common.type;

import static org.junit.Assert.*;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.junit.Test;

import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;

import java.util.Random;

public class TypeConverterTest {

  @Test
  public void testStringCast() {
    // String
    castTest(Boolean.TRUE, String.class, "true");
    castTest(Boolean.FALSE, String.class, "false");
    castTest(new Double(25), String.class, "25.0");
    castTest(new Float(25), String.class, "25.0");
    castTest(new Short((short) 25), String.class, "25");
    castTest(new Long(25), String.class, "25");
    castTest(new Integer(25), String.class, "25");
    castTest("25", String.class, "25");

    castTest(Boolean.TRUE, String.class.getName(), "true");
    castTest(Boolean.FALSE, String.class.getName(), "false");
    castTest(new Double(25), String.class.getName(), "25.0");
    castTest(new Float(25), String.class.getName(), "25.0");
    castTest(new Short((short) 25), String.class.getName(), "25");
    castTest(new Long(25), String.class.getName(), "25");
    castTest(new Integer(25), String.class.getName(), "25");
    castTest("25", String.class, "25");

    castTest(Boolean.TRUE, "String", "true");
    castTest(Boolean.FALSE, "String", "false");
    castTest(new Double(25), "String", "25.0");
    castTest(new Float(25), "String", "25.0");
    castTest(new Short((short) 25), "String", "25");
    castTest(new Long(25), "String", "25");
    castTest(new Integer(25), "String", "25");
    castTest("25", "String", "25");
  }

  @Test
  public void testBooleanCast() {
    // Boolean
    castTest(Boolean.TRUE, Boolean.class, Boolean.TRUE);
    castTest(Boolean.FALSE, Boolean.class, Boolean.FALSE);
    castTest(new Double(0), Boolean.class, Boolean.FALSE);
    castTest(new Double(1), Boolean.class, Boolean.TRUE);
    castTest(new Float(0), Boolean.class, Boolean.FALSE);
    castTest(new Float(1), Boolean.class, Boolean.TRUE);
    castTest(new Short((short) 0), Boolean.class, Boolean.FALSE);
    castTest(new Short((short) 1), Boolean.class, Boolean.TRUE);
    castTest(new Long(0), Boolean.class, Boolean.FALSE);
    castTest(new Long(1), Boolean.class, Boolean.TRUE);
    castTest(new Integer(0), Boolean.class, Boolean.FALSE);
    castTest(new Integer(1), Boolean.class, Boolean.TRUE);
    castTest("true", Boolean.class, Boolean.TRUE);
    castTest("false", Boolean.class, Boolean.FALSE);
    castTest("false", "Boolean", Boolean.FALSE);

    castTest(Boolean.TRUE, Boolean.class.getName(), Boolean.TRUE);
    castTest(Boolean.FALSE, Boolean.class.getName(), Boolean.FALSE);
    castTest(new Double(0), Boolean.class.getName(), Boolean.FALSE);
    castTest(new Double(1), Boolean.class.getName(), Boolean.TRUE);
    castTest(new Float(0), Boolean.class.getName(), Boolean.FALSE);
    castTest(new Float(1), Boolean.class, Boolean.TRUE);
    castTest(new Short((short) 0), Boolean.class.getName(), Boolean.FALSE);
    castTest(new Short((short) 1), Boolean.class.getName(), Boolean.TRUE);
    castTest(new Long(0), Boolean.class.getName(), Boolean.FALSE);
    castTest(new Long(1), Boolean.class.getName(), Boolean.TRUE);
    castTest(new Integer(0), Boolean.class.getName(), Boolean.FALSE);
    castTest(new Integer(1), Boolean.class.getName(), Boolean.TRUE);
    castTest("true", Boolean.class.getName(), Boolean.TRUE);
    castTest("false", Boolean.class.getName(), Boolean.FALSE);

    castTest(Boolean.TRUE, "Boolean", Boolean.TRUE);
    castTest(Boolean.FALSE, "Boolean", Boolean.FALSE);
    castTest(new Double(0), "Boolean", Boolean.FALSE);
    castTest(new Double(1), "Boolean", Boolean.TRUE);
    castTest(new Float(0), "Boolean", Boolean.FALSE);
    castTest(new Float(1), "Boolean", Boolean.TRUE);
    castTest(new Short((short) 0), "Boolean", Boolean.FALSE);
    castTest(new Short((short) 1), "Boolean", Boolean.TRUE);
    castTest(new Long(0), "Boolean", Boolean.FALSE);
    castTest(new Long(1), "Boolean", Boolean.TRUE);
    castTest(new Integer(0), "Boolean", Boolean.FALSE);
    castTest(new Integer(1), "Boolean", Boolean.TRUE);
    castTest("true", "Boolean", Boolean.TRUE);
    castTest("false", "Boolean", Boolean.FALSE);
  }

  @Test
  public void testIntegerCast() {
    // Integer
    castTest(Boolean.TRUE, Integer.class, new Integer(1));
    castTest(Boolean.FALSE, Integer.class, new Integer(0));
    castTest(new Double(0), Integer.class, new Integer(0));
    castTest(new Double(1), Integer.class, new Integer(1));
    castTest(new Double(25.5d), Integer.class, new Integer(26));
    castTest(new Float(0), Integer.class, new Integer(0));
    castTest(new Float(1), Integer.class, new Integer(1));
    castTest(new Float(25.5f), Integer.class, new Integer(26));
    castTest(new Short((short) 0), Integer.class, new Integer(0));
    castTest(new Short((short) 1), Integer.class, new Integer(1));
    castTest(new Short((short) 25), Integer.class, new Integer(25));
    castTest(new Long(0), Integer.class, new Integer(0));
    castTest(new Long(1), Integer.class, new Integer(1));
    castTest(new Integer(0), Integer.class, new Integer(0));
    castTest(new Integer(1), Integer.class, new Integer(1));
    castTest(new Integer(25), Integer.class, new Integer(25));
    castTest(Integer.MAX_VALUE, Integer.class, Integer.MAX_VALUE);
    castTest(Integer.MIN_VALUE, Integer.class, Integer.MIN_VALUE);
    castTest(new Integer(25), Integer.class, new Integer(25));
    castTest("0", Integer.class, new Integer(0));
    castTest("25.5", Integer.class, Integer.valueOf(26));

    castTest(Boolean.TRUE, Integer.class.getName(), new Integer(1));
    castTest(Boolean.FALSE, Integer.class.getName(), new Integer(0));
    castTest(new Double(0), Integer.class.getName(), new Integer(0));
    castTest(new Double(1), Integer.class.getName(), new Integer(1));
    castTest(new Double(25.5d), Integer.class.getName(), new Integer(26));
    castTest(new Float(0), Integer.class.getName(), new Integer(0));
    castTest(new Float(1), Integer.class.getName(), new Integer(1));
    castTest(new Float(25.5f), Integer.class.getName(), new Integer(26));
    castTest(new Short((short) 0), Integer.class.getName(), new Integer(0));
    castTest(new Short((short) 1), Integer.class.getName(), new Integer(1));
    castTest(new Short((short) 25), Integer.class.getName(), new Integer(25));
    castTest(new Long(0), Integer.class.getName(), new Integer(0));
    castTest(new Long(1), Integer.class.getName(), new Integer(1));
    castTest(new Integer(0), Integer.class.getName(), new Integer(0));
    castTest(new Integer(1), Integer.class.getName(), new Integer(1));
    castTest(new Integer(25), Integer.class.getName(), new Integer(25));
    castTest(Integer.MAX_VALUE, Integer.class.getName(), Integer.MAX_VALUE);
    castTest(Integer.MIN_VALUE, Integer.class.getName(), Integer.MIN_VALUE);
    castTest(new Integer(25), Integer.class.getName(), new Integer(25));
    castTest("0", Integer.class.getName(), new Integer(0));
    castTest("25.5", Integer.class.getName(), Integer.valueOf(26));

    castTest(Boolean.TRUE, "Integer", new Integer(1));
    castTest(Boolean.FALSE, "Integer", new Integer(0));
    castTest(new Double(0), "Integer", new Integer(0));
    castTest(new Double(1), "Integer", new Integer(1));
    castTest(new Double(25.5d), "Integer", new Integer(26));
    castTest(new Float(0), "Integer", new Integer(0));
    castTest(new Float(1), "Integer", new Integer(1));
    castTest(new Float(25.5f), "Integer", new Integer(26));
    castTest(new Short((short) 0), "Integer", new Integer(0));
    castTest(new Short((short) 1), "Integer", new Integer(1));
    castTest(new Short((short) 25), "Integer", new Integer(25));
    castTest(new Long(0), "Integer", new Integer(0));
    castTest(new Long(1), "Integer", new Integer(1));
    castTest(new Integer(0), "Integer", new Integer(0));
    castTest(new Integer(1), "Integer", new Integer(1));
    castTest(new Integer(25), "Integer", new Integer(25));
    castTest(Integer.MAX_VALUE, "Integer", Integer.MAX_VALUE);
    castTest(Integer.MIN_VALUE, "Integer", Integer.MIN_VALUE);
    castTest(new Integer(25), "Integer", new Integer(25));
    castTest("0", "Integer", new Integer(0));
    castTest("25.5", "Integer", Integer.valueOf(26));
  }

    // Short
    @Test
    public void testShortCast() {
      castTest(Boolean.TRUE, Short.class, new Short((short) 1));
      castTest(Boolean.FALSE, Short.class, new Short((short) 0));
      castTest(new Double(0), Short.class, new Short((short) 0));
      castTest(new Double(1), Short.class, new Short((short) 1));
      castTest(new Double(25.5), Short.class, new Short((short) 26));
      castTest(new Float(0), Short.class, new Short((short) 0));
      castTest(new Float(1), Short.class, new Short((short) 1));
      castTest(new Float(25.5), Short.class, new Short((short) 26));
      castTest(new Short((short) 0), Short.class, new Short((short) 0));
      castTest(new Short((short) 1), Short.class, new Short((short) 1));
      castTest(new Short((short) 25), Short.class, new Short((short) 25));
      castTest(new Long(0), Short.class, new Short((short) 0));
      castTest(new Long(1), Short.class, new Short((short) 1));
      castTest(new Integer(0), Short.class, new Short((short) 0));
      castTest(new Integer(1), Short.class, new Short((short) 1));
      castTest(new Integer(25), Short.class, new Short((short) 25));
      castTest("0", Short.class, new Short((short) 0));
      castTest("25.5", Short.class, Short.valueOf((short) 26));

      castTest(Boolean.TRUE, Short.class.getName(), new Short((short) 1));
      castTest(Boolean.FALSE, Short.class.getName(), new Short((short) 0));
      castTest(new Double(0), Short.class.getName(), new Short((short) 0));
      castTest(new Double(1), Short.class.getName(), new Short((short) 1));
      castTest(new Double(25.5), Short.class.getName(), new Short((short) 26));
      castTest(new Float(0), Short.class.getName(), new Short((short) 0));
      castTest(new Float(1), Short.class.getName(), new Short((short) 1));
      castTest(new Float(25.5), Short.class.getName(), new Short((short) 26));
      castTest(new Short((short) 0), Short.class.getName(), new Short((short) 0));
      castTest(new Short((short) 1), Short.class.getName(), new Short((short) 1));
      castTest(new Short((short) 25), Short.class.getName(), new Short((short) 25));
      castTest(new Long(0), Short.class.getName(), new Short((short) 0));
      castTest(new Long(1), Short.class.getName(), new Short((short) 1));
      castTest(new Integer(0), Short.class.getName(), new Short((short) 0));
      castTest(new Integer(1), Short.class.getName(), new Short((short) 1));
      castTest(new Integer(25), Short.class.getName(), new Short((short) 25));
      castTest("0", Short.class.getName(), new Short((short) 0));
      castTest("25.5", Short.class.getName(), Short.valueOf((short) 26));

      castTest(Boolean.TRUE, "Short", new Short((short) 1));
      castTest(Boolean.FALSE, "Short", new Short((short) 0));
      castTest(new Double(0), "Short", new Short((short) 0));
      castTest(new Double(1), "Short", new Short((short) 1));
      castTest(new Double(25.5), "Short", new Short((short) 26));
      castTest(new Float(0), "Short", new Short((short) 0));
      castTest(new Float(1), "Short", new Short((short) 1));
      castTest(new Float(25.5), "Short", new Short((short) 26));
      castTest(new Short((short) 0), "Short", new Short((short) 0));
      castTest(new Short((short) 1), "Short", new Short((short) 1));
      castTest(new Short((short) 25), "Short", new Short((short) 25));
      castTest(new Long(0), "Short", new Short((short) 0));
      castTest(new Long(1), "Short", new Short((short) 1));
      castTest(new Integer(0), "Short", new Short((short) 0));
      castTest(new Integer(1), "Short", new Short((short) 1));
      castTest(new Integer(25), "Short", new Short((short) 25));
      castTest("0", "Short", new Short((short) 0));
      castTest("25.5", "Short", Short.valueOf((short) 26));
    }

  // Byte
  @Test
  public void testByteCast() {
    castTest(Boolean.TRUE, Byte.class, new Byte((byte) 1));
    castTest(Boolean.FALSE, Byte.class, new Byte((byte) 0));
    castTest(new Double(0), Byte.class, new Byte((byte) 0));
    castTest(new Double(1), Byte.class, new Byte((byte) 1));
    castTest(new Double(25.5), Byte.class, new Byte((byte) 26));
    castTest(new Float(0), Byte.class, new Byte((byte) 0));
    castTest(new Float(1), Byte.class, new Byte((byte) 1));
    castTest(new Float(25.5), Byte.class, new Byte((byte) 26));
    castTest(new Byte((byte) 0), Byte.class, new Byte((byte) 0));
    castTest(new Byte((byte) 1), Byte.class, new Byte((byte) 1));
    castTest(new Byte((byte) 25), Byte.class, new Byte((byte) 25));
    castTest(Byte.MAX_VALUE, Byte.class, Byte.MAX_VALUE);
    castTest(Byte.MIN_VALUE, Byte.class, Byte.MIN_VALUE);
    castTest(new Long(0), Byte.class, new Byte((byte) 0));
    castTest(new Long(1), Byte.class, new Byte((byte) 1));
    castTest(new Integer(0), Byte.class, new Byte((byte) 0));
    castTest(new Integer(1), Byte.class, new Byte((byte) 1));
    castTest(new Integer(25), Byte.class, new Byte((byte) 25));
    castTest("0", Byte.class, new Byte((byte) 0));
    castTest("25.5", Byte.class, Byte.valueOf((byte) 26));

    castTest(Boolean.TRUE, Byte.class.getName(), new Byte((byte) 1));
    castTest(Boolean.FALSE, Byte.class.getName(), new Byte((byte) 0));
    castTest(new Double(0), Byte.class.getName(), new Byte((byte) 0));
    castTest(new Double(1), Byte.class.getName(), new Byte((byte) 1));
    castTest(new Double(25.5), Byte.class.getName(), new Byte((byte) 26));
    castTest(new Float(0), Byte.class.getName(), new Byte((byte) 0));
    castTest(new Float(1), Byte.class.getName(), new Byte((byte) 1));
    castTest(new Float(25.5), Byte.class.getName(), new Byte((byte) 26));
    castTest(new Byte((byte) 0), Byte.class.getName(), new Byte((byte) 0));
    castTest(new Byte((byte) 1), Byte.class.getName(), new Byte((byte) 1));
    castTest(new Byte((byte) 25), Byte.class.getName(), new Byte((byte) 25));
    castTest(Byte.MAX_VALUE, Byte.class.getName(), Byte.MAX_VALUE);
    castTest(Byte.MIN_VALUE, Byte.class.getName(), Byte.MIN_VALUE);
    castTest(new Long(0), Byte.class.getName(), new Byte((byte) 0));
    castTest(new Long(1), Byte.class.getName(), new Byte((byte) 1));
    castTest(new Integer(0), Byte.class.getName(), new Byte((byte) 0));
    castTest(new Integer(1), Byte.class.getName(), new Byte((byte) 1));
    castTest(new Integer(25), Byte.class.getName(), new Byte((byte) 25));
    castTest("0", Byte.class.getName(), new Byte((byte) 0));
    castTest("25.5", Byte.class.getName(), Byte.valueOf((byte) 26));

    castTest(Boolean.TRUE, "Byte", new Byte((byte) 1));
    castTest(Boolean.FALSE, "Byte", new Byte((byte) 0));
    castTest(new Double(0), "Byte", new Byte((byte) 0));
    castTest(new Double(1), "Byte", new Byte((byte) 1));
    castTest(new Double(25.5), "Byte", new Byte((byte) 26));
    castTest(new Float(0), "Byte", new Byte((byte) 0));
    castTest(new Float(1), "Byte", new Byte((byte) 1));
    castTest(new Float(25.5), "Byte", new Byte((byte) 26));
    castTest(new Byte((byte) 0), "Byte", new Byte((byte) 0));
    castTest(new Byte((byte) 1), "Byte", new Byte((byte) 1));
    castTest(new Byte((byte) 25), "Byte", new Byte((byte) 25));
    castTest(Byte.MAX_VALUE, "Byte", Byte.MAX_VALUE);
    castTest(Byte.MIN_VALUE, "Byte", Byte.MIN_VALUE);
    castTest(new Long(0), "Byte", new Byte((byte) 0));
    castTest(new Long(1), "Byte", new Byte((byte) 1));
    castTest(new Integer(0), "Byte", new Byte((byte) 0));
    castTest(new Integer(1), "Byte", new Byte((byte) 1));
    castTest(new Integer(25), "Byte", new Byte((byte) 25));
    castTest("0", "Byte", new Byte((byte) 0));
    castTest("25.5", "Byte", Byte.valueOf((byte) 26));
  }

    // Long
    @Test
    public void testLongCast() {
      castTest(Boolean.TRUE, Long.class, new Long(1));
      castTest(Boolean.FALSE, Long.class, new Long(0));
      castTest(new Double(0), Long.class, new Long(0));
      castTest(new Double(1), Long.class, new Long(1));
      castTest(new Double(25.5), Long.class, new Long(26));
      castTest(new Float(0), Long.class, new Long(0));
      castTest(new Float(1), Long.class, new Long(1));
      castTest(new Float(25.5), Long.class, new Long(26));
      castTest(new Short((short) 0), Long.class, new Long(0));
      castTest(new Short((short) 1), Long.class, new Long(1));
      castTest(new Short((short) 25), Long.class, new Long(25));
      castTest(new Long(0), Long.class, new Long(0));
      castTest(new Long(1), Long.class, new Long(1));
      castTest(new Long(2123190123125l), Long.class, new Long(2123190123125l));
      castTest(new Integer(0), Long.class, new Long(0));
      castTest(new Integer(1), Long.class, new Long(1));
      castTest(new Integer(25), Long.class, new Long(25));
      castTest("0", Long.class, new Long(0));
      castTest("25.5", Long.class, Long.valueOf(26L));

      castTest(Boolean.TRUE, Long.class.getName(), new Long(1));
      castTest(Boolean.FALSE, Long.class.getName(), new Long(0));
      castTest(new Double(0), Long.class.getName(), new Long(0));
      castTest(new Double(1), Long.class.getName(), new Long(1));
      castTest(new Double(25.5), Long.class.getName(), new Long(26));
      castTest(new Float(0), Long.class.getName(), new Long(0));
      castTest(new Float(1), Long.class.getName(), new Long(1));
      castTest(new Float(25.5), Long.class.getName(), new Long(26));
      castTest(new Short((short) 0), Long.class.getName(), new Long(0));
      castTest(new Short((short) 1), Long.class.getName(), new Long(1));
      castTest(new Short((short) 25), Long.class.getName(), new Long(25));
      castTest(new Long(0), Long.class.getName(), new Long(0));
      castTest(new Long(1), Long.class.getName(), new Long(1));
      castTest(new Long(2123190123125l), Long.class.getName(), new Long(2123190123125l));
      castTest(new Integer(0), Long.class.getName(), new Long(0));
      castTest(new Integer(1), Long.class.getName(), new Long(1));
      castTest(new Integer(25), Long.class.getName(), new Long(25));
      castTest("0", Long.class.getName(), new Long(0));
      castTest("25.5", Long.class.getName(), Long.valueOf(26L));

      castTest(Boolean.TRUE, "Long", new Long(1));
      castTest(Boolean.FALSE, "Long", new Long(0));
      castTest(new Double(0), "Long", new Long(0));
      castTest(new Double(1), "Long", new Long(1));
      castTest(new Double(25.5), "Long", new Long(26));
      castTest(new Float(0), "Long", new Long(0));
      castTest(new Float(1), "Long", new Long(1));
      castTest(new Float(25.5), "Long", new Long(26));
      castTest(new Short((short) 0), "Long", new Long(0));
      castTest(new Short((short) 1), "Long", new Long(1));
      castTest(new Short((short) 25), "Long", new Long(25));
      castTest(new Long(0), "Long", new Long(0));
      castTest(new Long(1), "Long", new Long(1));
      castTest(new Long(2123190123125l), "Long", new Long(2123190123125l));
      castTest(new Integer(0), "Long", new Long(0));
      castTest(new Integer(1), "Long", new Long(1));
      castTest(new Integer(25), "Long", new Long(25));
      castTest("0", "Long", new Long(0));
      castTest("25.5", "Long", Long.valueOf(26L));
    }

    // Float
    @Test
    public void testFloatCast() {
      castTest(Boolean.TRUE, Float.class, new Float(1));
      castTest(Boolean.FALSE, Float.class, new Float(0));
      castTest(new Double(0), Float.class, new Float(0));
      castTest(new Double(1), Float.class, new Float(1));
      castTest(new Double(25.5), Float.class, new Float(25.5));
      castTest(new Float(0), Float.class, new Float(0));
      castTest(new Float(1), Float.class, new Float(1));
      castTest(new Float(25.5), Float.class, new Float(25.5));
      castTest(new Short((short) 0), Float.class, new Float(0));
      castTest(new Short((short) 1), Float.class, new Float(1));
      castTest(new Short((short) 25), Float.class, new Float(25));
      castTest(new Long(0), Float.class, new Float(0));
      castTest(new Long(1), Float.class, new Float(1));
      castTest(new Long(2123190123125l), Float.class, new Float(2123190123125l));
      castTest(new Integer(0), Float.class, new Float(0));
      castTest(new Integer(1), Float.class, new Float(1));
      castTest(new Integer(25), Float.class, new Float(25));
      castTest("0", Float.class, new Float(0));
      castTest("25.5", Float.class, new Float(25.5));

      castTest(Boolean.TRUE, Float.class.getName(), new Float(1));
      castTest(Boolean.FALSE, Float.class.getName(), new Float(0));
      castTest(new Double(0), Float.class.getName(), new Float(0));
      castTest(new Double(1), Float.class.getName(), new Float(1));
      castTest(new Double(25.5), Float.class.getName(), new Float(25.5));
      castTest(new Float(0), Float.class.getName(), new Float(0));
      castTest(new Float(1), Float.class.getName(), new Float(1));
      castTest(new Float(25.5), Float.class.getName(), new Float(25.5));
      castTest(new Short((short) 0), Float.class.getName(), new Float(0));
      castTest(new Short((short) 1), Float.class.getName(), new Float(1));
      castTest(new Short((short) 25), Float.class.getName(), new Float(25));
      castTest(new Long(0), Float.class.getName(), new Float(0));
      castTest(new Long(1), Float.class.getName(), new Float(1));
      castTest(new Long(2123190123125l), Float.class.getName(), new Float(2123190123125l));
      castTest(new Integer(0), Float.class.getName(), new Float(0));
      castTest(new Integer(1), Float.class.getName(), new Float(1));
      castTest(new Integer(25), Float.class.getName(), new Float(25));
      castTest("0", Float.class.getName(), new Float(0));
      castTest("25.5", Float.class.getName(), new Float(25.5));

      castTest(Boolean.TRUE, "Float", new Float(1));
      castTest(Boolean.FALSE, "Float", new Float(0));
      castTest(new Double(0), "Float", new Float(0));
      castTest(new Double(1), "Float", new Float(1));
      castTest(new Double(25.5), "Float", new Float(25.5));
      castTest(new Float(0), "Float", new Float(0));
      castTest(new Float(1), "Float", new Float(1));
      castTest(new Float(25.5), "Float", new Float(25.5));
      castTest(new Short((short) 0), "Float", new Float(0));
      castTest(new Short((short) 1), "Float", new Float(1));
      castTest(new Short((short) 25), "Float", new Float(25));
      castTest(new Long(0), "Float", new Float(0));
      castTest(new Long(1), "Float", new Float(1));
      castTest(new Long(2123190123125l), "Float", new Float(2123190123125l));
      castTest(new Integer(0), "Float", new Float(0));
      castTest(new Integer(1), "Float", new Float(1));
      castTest(new Integer(25), "Float", new Float(25));
      castTest("0", "Float", new Float(0));
      castTest("25.5", "Float", new Float(25.5));
    }

    // Double
    @Test
    public void testDoubleCast() {
      castTest(Boolean.TRUE, Double.class, new Double(1));
      castTest(Boolean.FALSE, Double.class, new Double(0));
      castTest(new Double(0), Double.class, new Double(0));
      castTest(new Double(1), Double.class, new Double(1));
      castTest(new Double(25.5), Double.class, new Double(25.5));
      castTest(new Float(0), Double.class, new Double(0));
      castTest(new Float(1), Double.class, new Double(1));
      castTest(new Float(25.5), Double.class, new Double(25.5));
      castTest(new Short((short) 0), Double.class, new Double(0));
      castTest(new Short((short) 1), Double.class, new Double(1));
      castTest(new Short((short) 25), Double.class, new Double(25));
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

      castTest(Boolean.TRUE, Double.class.getName(), new Double(1));
      castTest(Boolean.FALSE, Double.class.getName(), new Double(0));
      castTest(new Double(0), Double.class.getName(), new Double(0));
      castTest(new Double(1), Double.class.getName(), new Double(1));
      castTest(new Double(25.5), Double.class.getName(), new Double(25.5));
      castTest(new Float(0), Double.class.getName(), new Double(0));
      castTest(new Float(1), Double.class.getName(), new Double(1));
      castTest(new Float(25.5), Double.class.getName(), new Double(25.5));
      castTest(new Short((short) 0), Double.class.getName(), new Double(0));
      castTest(new Short((short) 1), Double.class.getName(), new Double(1));
      castTest(new Short((short) 25), Double.class.getName(), new Double(25));
      castTest(new Long(0), Double.class.getName(), new Double(0));
      castTest(new Long(1), Double.class.getName(), new Double(1));
      castTest(new Long(2123190123125l), Double.class.getName(), new Double(2123190123125l));
      castTest(new Integer(0), Double.class.getName(), new Double(0));
      castTest(new Integer(1), Double.class.getName(), new Double(1));
      castTest(new Integer(25), Double.class.getName(), new Double(25));
      castTest("0", Double.class.getName(), new Double(0));
      castTest("25.5", Double.class.getName(), new Double(25.5));
      castTest("RUNNING", SupervisionStatus.class, SupervisionStatus.RUNNING);
      castTest("DOWN", SupervisionStatus.class, SupervisionStatus.DOWN);

      castTest(Boolean.TRUE, "Double", new Double(1));
      castTest(Boolean.FALSE, "Double", new Double(0));
      castTest(new Double(0), "Double", new Double(0));
      castTest(new Double(1), "Double", new Double(1));
      castTest(new Double(25.5), "Double", new Double(25.5));
      castTest(new Float(0), "Double", new Double(0));
      castTest(new Float(1), "Double", new Double(1));
      castTest(new Float(25.5), "Double", new Double(25.5));
      castTest(new Short((short) 0), "Double", new Double(0));
      castTest(new Short((short) 1), "Double", new Double(1));
      castTest(new Short((short) 25), "Double", new Double(25));
      castTest(new Long(0), "Double", new Double(0));
      castTest(new Long(1), "Double", new Double(1));
      castTest(new Long(2123190123125l), "Double", new Double(2123190123125l));
      castTest(new Integer(0), "Double", new Double(0));
      castTest(new Integer(1), "Double", new Double(1));
      castTest(new Integer(25), "Double", new Double(25));
      castTest("0", "Double", new Double(0));
      castTest("25.5", "Double", new Double(25.5));
      castTest("RUNNING", SupervisionStatus.class, SupervisionStatus.RUNNING);
      castTest("DOWN", SupervisionStatus.class, SupervisionStatus.DOWN);
    }

  @Test
  public void testStringArrayCast() {
    // String
    castTest(new Boolean[]{Boolean.TRUE, Boolean.FALSE}, String[].class, new String[]{"true", "false"});
    castTest(new Double[]{new Double(25)}, String[].class, new String[]{"25.0"});
    castTest(new Float[]{new Float(25)}, String[].class, new String[]{"25.0"});
    castTest(new Short[]{new Short((short) 25)}, String[].class, new String[]{"25"});
    castTest(new Long[]{new Long(25)}, String[].class, new String[]{"25"});
    castTest(new Integer[]{new Integer(25)}, String[].class, new String[]{"25"});
    castTest(new String[]{"25"}, String[].class, new String[]{"25"});

    castTest(new Boolean[]{Boolean.TRUE, Boolean.FALSE}, String[].class.getName(), new String[]{"true", "false"});
    castTest(new Double[]{new Double(25)}, String[].class.getName(), new String[]{"25.0"});
    castTest(new Float[]{new Float(25)}, String[].class.getName(), new String[]{"25.0"});
    castTest(new Short[]{new Short((short) 25)}, String[].class.getName(), new String[]{"25"});
    castTest(new Long[]{new Long(25)}, String[].class.getName(), new String[]{"25"});
    castTest(new Integer[]{new Integer(25)}, String[].class.getName(), new String[]{"25"});
    castTest(new String[]{"25"}, String[].class.getName(), new String[]{"25"});
  }

  @Test
  public void testBooleanArrayCast() {
    // Boolean
    castTest(new Boolean[]{Boolean.TRUE, Boolean.FALSE}, Boolean[].class, new Boolean[]{Boolean.TRUE, Boolean.FALSE});
    castTest(new Double[]{new Double(0), new Double(1)}, Boolean[].class, new Boolean[]{Boolean.FALSE, Boolean.TRUE});
    castTest(new Float[]{new Float(0), new Float(1)}, Boolean[].class, new Boolean[]{Boolean.FALSE, Boolean.TRUE});
    castTest(new Short[]{new Short((short)0), new Short((short)1)}, Boolean[].class, new Boolean[]{Boolean.FALSE, Boolean.TRUE});
    castTest(new Long[]{new Long(0), new Long(1)}, Boolean[].class, new Boolean[]{Boolean.FALSE, Boolean.TRUE});
    castTest(new Integer[]{new Integer(0), new Integer(1)}, Boolean[].class, new Boolean[]{Boolean.FALSE, Boolean.TRUE});
    castTest(new String[]{"false", "true"}, Boolean[].class, new Boolean[]{Boolean.FALSE, Boolean.TRUE});

    castTest(new Boolean[]{Boolean.TRUE, Boolean.FALSE}, Boolean[].class.getName(), new Boolean[]{Boolean.TRUE, Boolean.FALSE});
    castTest(new Double[]{new Double(0), new Double(1)}, Boolean[].class.getName(), new Boolean[]{Boolean.FALSE, Boolean.TRUE});
    castTest(new Float[]{new Float(0), new Float(1)}, Boolean[].class.getName(), new Boolean[]{Boolean.FALSE, Boolean.TRUE});
    castTest(new Short[]{new Short((short)0), new Short((short)1)}, Boolean[].class.getName(), new Boolean[]{Boolean.FALSE, Boolean.TRUE});
    castTest(new Long[]{new Long(0), new Long(1)}, Boolean[].class.getName(), new Boolean[]{Boolean.FALSE, Boolean.TRUE});
    castTest(new Integer[]{new Integer(0), new Integer(1)}, Boolean[].class.getName(), new Boolean[]{Boolean.FALSE, Boolean.TRUE});
    castTest(new String[]{"false", "true"}, Boolean[].class.getName(), new Boolean[]{Boolean.FALSE, Boolean.TRUE});

  }

  @Test
  public void testIntegerArrayCast() {
    // Integer
    castTest(new Boolean[]{Boolean.TRUE, Boolean.FALSE}, Integer[].class, new Integer[]{new Integer(1), new Integer(0)});
    castTest(new Double[]{new Double(0), new Double(1), new Double(25.5d)}, Integer[].class, new Integer[]{new Integer(0), new Integer(1), new Integer(26)});
    castTest(new Float[]{new Float(0), new Float(1), new Float(25.5d)}, Integer[].class, new Integer[]{new Integer(0), new Integer(1), new Integer(26)});
    castTest(new Short[]{new Short((short) 0), new Short((short) 1), new Short((short) 25)}, Integer[].class, new Integer[]{new Integer(0), new Integer(1), new Integer(25)});
    castTest(new Long[]{new Long(0), new Long(1), new Long(25L)}, Integer[].class, new Integer[]{new Integer(0), new Integer(1), new Integer(25)});
    castTest(new Integer[]{new Integer(0), new Integer(1), new Integer(25)}, Integer[].class, new Integer[]{new Integer(0), new Integer(1), new Integer(25)});
    castTest(new int[]{0, 1, 25}, Integer[].class, new Integer[]{new Integer(0), new Integer(1), new Integer(25)});
    castTest(new String[]{"0", "1", "25.5"}, Integer[].class, new Integer[]{new Integer(0), new Integer(1), new Integer(26)});

    castTest(new Boolean[]{Boolean.TRUE, Boolean.FALSE}, Integer[].class.getName(), new Integer[]{new Integer(1), new Integer(0)});
    castTest(new Double[]{new Double(0), new Double(1), new Double(25.5d)}, Integer[].class.getName(), new Integer[]{new Integer(0), new Integer(1), new Integer(26)});
    castTest(new Float[]{new Float(0), new Float(1), new Float(25.5d)}, Integer[].class.getName(), new Integer[]{new Integer(0), new Integer(1), new Integer(26)});
    castTest(new Short[]{new Short((short) 0), new Short((short) 1), new Short((short) 25)}, Integer[].class.getName(), new Integer[]{new Integer(0), new Integer(1), new Integer(25)});
    castTest(new Long[]{new Long(0), new Long(1), new Long(25L)}, Integer[].class.getName(), new Integer[]{new Integer(0), new Integer(1), new Integer(25)});
    castTest(new Integer[]{new Integer(0), new Integer(1), new Integer(25)}, Integer[].class.getName(), new Integer[]{new Integer(0), new Integer(1), new Integer(25)});
    castTest(new int[]{0, 1, 25}, Integer[].class.getName(), new Integer[]{new Integer(0), new Integer(1), new Integer(25)});
    castTest(new String[]{"0", "1", "25.5"}, Integer[].class.getName(), new Integer[]{new Integer(0), new Integer(1), new Integer(26)});
  }

  @Test
  public void testShortArrayCast() {
    castTest(new Boolean[]{Boolean.TRUE, Boolean.FALSE}, Short[].class, new Short[]{new Short((short) 1), new Short((short) 0)});
    castTest(new Double[]{new Double(0), new Double(1), new Double(25.5d)}, Short[].class, new Short[]{new Short((short) 0), new Short((short) 1), new Short((short) 26)});
    castTest(new Float[]{new Float(0), new Float(1), new Float(25.5d)}, Short[].class, new Short[]{new Short((short) 0), new Short((short) 1), new Short((short) 26)});
    castTest(new Short[]{new Short((short) 0), new Short((short) 1), new Short((short) 25)}, Short[].class, new Short[]{new Short((short) 0), new Short((short) 1), new Short((short) 25)});
    castTest(new Long[]{new Long(0), new Long(1), new Long(25L)}, Short[].class, new Short[]{new Short((short) 0), new Short((short) 1), new Short((short) 25)});
    castTest(new Integer[]{new Integer(0), new Integer(1), new Integer(25)}, Short[].class, new Short[]{new Short((short) 0), new Short((short) 1), new Short((short) 25)});
    castTest(new short[]{0, 1, 25}, Short[].class, new Short[]{new Short((short) 0), new Short((short) 1), new Short((short) 25)});
    castTest(new String[]{"0", "1", "25.5"}, Short[].class, new Short[]{new Short((short) 0), new Short((short) 1), new Short((short) 26)});

    castTest(new Boolean[]{Boolean.TRUE, Boolean.FALSE}, Short[].class.getName(), new Short[]{new Short((short) 1), new Short((short) 0)});
    castTest(new Double[]{new Double(0), new Double(1), new Double(25.5d)}, Short[].class.getName(), new Short[]{new Short((short) 0), new Short((short) 1), new Short((short) 26)});
    castTest(new Float[]{new Float(0), new Float(1), new Float(25.5d)}, Short[].class.getName(), new Short[]{new Short((short) 0), new Short((short) 1), new Short((short) 26)});
    castTest(new Short[]{new Short((short) 0), new Short((short) 1), new Short((short) 25)}, Short[].class.getName(), new Short[]{new Short((short) 0), new Short((short) 1), new Short((short) 25)});
    castTest(new Long[]{new Long(0), new Long(1), new Long(25L)}, Short[].class.getName(), new Short[]{new Short((short) 0), new Short((short) 1), new Short((short) 25)});
    castTest(new Integer[]{new Integer(0), new Integer(1), new Integer(25)}, Short[].class.getName(), new Short[]{new Short((short) 0), new Short((short) 1), new Short((short) 25)});
    castTest(new short[]{0, 1, 25}, Short[].class.getName(), new Short[]{new Short((short) 0), new Short((short) 1), new Short((short) 25)});
    castTest(new String[]{"0", "1", "25.5"}, Short[].class.getName(), new Short[]{new Short((short) 0), new Short((short) 1), new Short((short) 26)});
  }

  @Test
  public void testByteArrayCast() {
    castTest(new Boolean[]{Boolean.TRUE, Boolean.FALSE}, Byte[].class, new Byte[]{new Byte((byte) 1), new Byte((byte) 0)});
    castTest(new Double[]{new Double(0), new Double(1), new Double(25.5d)}, Byte[].class, new Byte[]{new Byte((byte) 0), new Byte((byte) 1), new Byte((byte) 26)});
    castTest(new Float[]{new Float(0), new Float(1), new Float(25.5d)}, Byte[].class, new Byte[]{new Byte((byte) 0), new Byte((byte) 1), new Byte((byte) 26)});
    castTest(new Byte[]{new Byte((byte) 0), new Byte((byte) 1), new Byte((byte) 25)}, Byte[].class, new Byte[]{new Byte((byte) 0), new Byte((byte) 1), new Byte((byte) 25)});
    castTest(new Long[]{new Long(0), new Long(1), new Long(25L)}, Byte[].class, new Byte[]{new Byte((byte) 0), new Byte((byte) 1), new Byte((byte) 25)});
    castTest(new Integer[]{new Integer(0), new Integer(1), new Integer(25)}, Byte[].class, new Byte[]{new Byte((byte) 0), new Byte((byte) 1), new Byte((byte) 25)});
    castTest(new byte[]{0, 1, 25}, Byte[].class, new Byte[]{new Byte((byte) 0), new Byte((byte) 1), new Byte((byte) 25)});
    castTest(new String[]{"0", "1", "25.5"}, Byte[].class, new Byte[]{new Byte((byte) 0), new Byte((byte) 1), new Byte((byte) 26)});

    castTest(new Boolean[]{Boolean.TRUE, Boolean.FALSE}, Byte[].class.getName(), new Byte[]{new Byte((byte) 1), new Byte((byte) 0)});
    castTest(new Double[]{new Double(0), new Double(1), new Double(25.5d)}, Byte[].class.getName(), new Byte[]{new Byte((byte) 0), new Byte((byte) 1), new Byte((byte) 26)});
    castTest(new Float[]{new Float(0), new Float(1), new Float(25.5d)}, Byte[].class.getName(), new Byte[]{new Byte((byte) 0), new Byte((byte) 1), new Byte((byte) 26)});
    castTest(new Byte[]{new Byte((byte) 0), new Byte((byte) 1), new Byte((byte) 25)}, Byte[].class.getName(), new Byte[]{new Byte((byte) 0), new Byte((byte) 1), new Byte((byte) 25)});
    castTest(new Long[]{new Long(0), new Long(1), new Long(25L)}, Byte[].class.getName(), new Byte[]{new Byte((byte) 0), new Byte((byte) 1), new Byte((byte) 25)});
    castTest(new Integer[]{new Integer(0), new Integer(1), new Integer(25)}, Byte[].class.getName(), new Byte[]{new Byte((byte) 0), new Byte((byte) 1), new Byte((byte) 25)});
    castTest(new byte[]{0, 1, 25}, Byte[].class.getName(), new Byte[]{new Byte((byte) 0), new Byte((byte) 1), new Byte((byte) 25)});
    castTest(new String[]{"0", "1", "25.5"}, Byte[].class.getName(), new Byte[]{new Byte((byte) 0), new Byte((byte) 1), new Byte((byte) 26)});
  }

  @Test
  public void testLongArrayCast() {
    // Integer
    castTest(new Boolean[]{Boolean.TRUE, Boolean.FALSE}, Long[].class, new Long[]{new Long(1), new Long(0)});
    castTest(new Double[]{new Double(0), new Double(1), new Double(25.5d)}, Long[].class, new Long[]{new Long(0), new Long(1), new Long(26)});
    castTest(new Float[]{new Float(0), new Float(1), new Float(25.5d)}, Long[].class, new Long[]{new Long(0), new Long(1), new Long(26)});
    castTest(new Short[]{new Short((short) 0), new Short((short) 1), new Short((short) 25)}, Long[].class, new Long[]{new Long(0), new Long(1), new Long(25)});
    castTest(new Long[]{new Long(0), new Long(1), new Long(25L)}, Long[].class, new Long[]{new Long(0), new Long(1), new Long(25)});
    castTest(new long[]{0, 1, 25}, Long[].class, new Long[]{new Long(0), new Long(1), new Long(25)});
    castTest(new String[]{"0", "1", "25.5"}, Long[].class, new Long[]{new Long(0), new Long(1), new Long(26)});
    castTest(new Integer[]{new Integer(0), new Integer(1), new Integer(25)}, Long[].class, new Long[]{new Long(0), new Long(1), new Long(25)});

    castTest(new Boolean[]{Boolean.TRUE, Boolean.FALSE}, Long[].class.getName(), new Long[]{new Long(1), new Long(0)});
    castTest(new Double[]{new Double(0), new Double(1), new Double(25.5d)}, Long[].class.getName(), new Long[]{new Long(0), new Long(1), new Long(26)});
    castTest(new Float[]{new Float(0), new Float(1), new Float(25.5d)}, Long[].class.getName(), new Long[]{new Long(0), new Long(1), new Long(26)});
    castTest(new Short[]{new Short((short) 0), new Short((short) 1), new Short((short) 25)}, Long[].class.getName(), new Long[]{new Long(0), new Long(1), new Long(25)});
    castTest(new Long[]{new Long(0), new Long(1), new Long(25L)}, Long[].class.getName(), new Long[]{new Long(0), new Long(1), new Long(25)});
    castTest(new long[]{0, 1, 25}, Long[].class.getName(), new Long[]{new Long(0), new Long(1), new Long(25)});
    castTest(new String[]{"0", "1", "25.5"}, Long[].class.getName(), new Long[]{new Long(0), new Long(1), new Long(26)});
    castTest(new Integer[]{new Integer(0), new Integer(1), new Integer(25)}, Long[].class.getName(), new Long[]{new Long(0), new Long(1), new Long(25)});
  }

  @Test
  public void testFloatArrayCast() {
    castTest(new Boolean[]{Boolean.TRUE, Boolean.FALSE}, Float[].class, new Float[]{new Float(1), new Float(0)});
    castTest(new Double[]{new Double(0), new Double(1), new Double(25.5d)}, Float[].class, new Float[]{new Float(0), new Float(1), new Float(25.5f)});
    castTest(new Short[]{new Short((short) 0), new Short((short) 1), new Short((short) 25)}, Float[].class, new Float[]{new Float(0), new Float(1), new Float(25)});
    castTest(new float[]{0, 1, 25.5f}, Float[].class, new Float[]{new Float(0), new Float(1), new Float(25.5)});
    castTest(new String[]{"0", "1", "25.5"}, Float[].class, new Float[]{new Float(0), new Float(1), new Float(25.5f)});
    castTest(new Float[]{new Float(0), new Float(1), new Float(25.5d)}, Float[].class, new Float[]{new Float(0), new Float(1), new Float(25.5f)});
    castTest(new Integer[]{new Integer(0), new Integer(1), new Integer(25)}, Float[].class, new Float[]{new Float(0), new Float(1), new Float(25)});
    castTest(new Long[]{new Long(0), new Long(1), new Long(25L)}, Float[].class, new Float[]{new Float(0), new Float(1), new Float(25)});

    castTest(new Boolean[]{Boolean.TRUE, Boolean.FALSE}, Float[].class.getName(), new Float[]{new Float(1), new Float(0)});
    castTest(new Double[]{new Double(0), new Double(1), new Double(25.5d)}, Float[].class.getName(), new Float[]{new Float(0), new Float(1), new Float(25.5f)});
    castTest(new Short[]{new Short((short) 0), new Short((short) 1), new Short((short) 25)}, Float[].class.getName(), new Float[]{new Float(0), new Float(1), new Float(25)});
    castTest(new float[]{0, 1, 25.5f}, Float[].class.getName(), new Float[]{new Float(0), new Float(1), new Float(25.5)});
    castTest(new String[]{"0", "1", "25.5"}, Float[].class.getName(), new Float[]{new Float(0), new Float(1), new Float(25.5f)});
    castTest(new Float[]{new Float(0), new Float(1), new Float(25.5d)}, Float[].class.getName(), new Float[]{new Float(0), new Float(1), new Float(25.5f)});
    castTest(new Integer[]{new Integer(0), new Integer(1), new Integer(25)}, Float[].class.getName(), new Float[]{new Float(0), new Float(1), new Float(25)});
    castTest(new Long[]{new Long(0), new Long(1), new Long(25L)}, Float[].class.getName(), new Float[]{new Float(0), new Float(1), new Float(25)});
  }

  @Test
  public void testDoubleArrayCast() {
    castTest(new Boolean[]{Boolean.TRUE, Boolean.FALSE}, Double[].class, new Double[]{new Double(1), new Double(0)});
    castTest(new Double[]{new Double(0), new Double(1), new Double(25.5d)}, Double[].class, new Double[]{new Double(0), new Double(1), new Double(25.5d)});
    castTest(new Short[]{new Short((short) 0), new Short((short) 1), new Short((short) 25)}, Double[].class, new Double[]{new Double(0), new Double(1), new Double(25)});
    castTest(new double[]{0, 1, 25.5d}, Double[].class, new Double[]{new Double(0), new Double(1), new Double(25.5)});
    castTest(new String[]{"0", "1", "25.5"}, Double[].class, new Double[]{new Double(0), new Double(1), new Double(25.5d)});
    castTest(new Integer[]{new Integer(0), new Integer(1), new Integer(25)}, Double[].class, new Double[]{new Double(0), new Double(1), new Double(25)});
    castTest(new Long[]{new Long(0), new Long(1), new Long(25L)}, Double[].class, new Double[]{new Double(0), new Double(1), new Double(25)});
    castTest(new Float[]{new Float(0), new Float(1), new Float(25.5d)}, Double[].class, new Double[]{new Double(0), new Double(1), new Double(25.5d)});

    castTest(new Boolean[]{Boolean.TRUE, Boolean.FALSE}, Double[].class.getName(), new Double[]{new Double(1), new Double(0)});
    castTest(new Double[]{new Double(0), new Double(1), new Double(25.5d)}, Double[].class.getName(), new Double[]{new Double(0), new Double(1), new Double(25.5d)});
    castTest(new Short[]{new Short((short) 0), new Short((short) 1), new Short((short) 25)}, Double[].class.getName(), new Double[]{new Double(0), new Double(1), new Double(25)});
    castTest(new double[]{0, 1, 25.5d}, Double[].class.getName(), new Double[]{new Double(0), new Double(1), new Double(25.5)});
    castTest(new String[]{"0", "1", "25.5"}, Double[].class.getName(), new Double[]{new Double(0), new Double(1), new Double(25.5d)});
    castTest(new Integer[]{new Integer(0), new Integer(1), new Integer(25)}, Double[].class.getName(), new Double[]{new Double(0), new Double(1), new Double(25)});
    castTest(new Long[]{new Long(0), new Long(1), new Long(25L)}, Double[].class.getName(), new Double[]{new Double(0), new Double(1), new Double(25)});
    castTest(new Float[]{new Float(0), new Float(1), new Float(25.5d)}, Double[].class.getName(), new Double[]{new Double(0), new Double(1), new Double(25.5d)});
  }

  @Test
  public void multiDimensionalArrayCast(){
    Random random = new Random(42);
    int size = 5;

    Integer[][] array = new Integer[size][size];
    Integer[][] expectedArray = new Integer[size][size];

    for(int i=0; i<size; i++){
      for(int k=0; k<size; k++){
        Integer value = random.nextInt(100);
        array[i][k] = value;
        expectedArray[i][k] = value;
      }
    }

    castTest(array, Integer[][].class, expectedArray);

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
    assertEquals(true, TypeConverter.isConvertible("false", "Long"));
    assertEquals(true, TypeConverter.isConvertible("true", "Long"));
    assertEquals(true, TypeConverter.isConvertible("false", "java.lang.Long"));
    assertEquals(true, TypeConverter.isConvertible("true", "java.lang.Long"));
  }

  @Test
  public void testBooleanStringToIntegerCast() {
    assertEquals(Integer.valueOf(0), TypeConverter.castToType("false", Integer.class));
    assertEquals(Integer.valueOf(1), TypeConverter.castToType("true", Integer.class));
    assertEquals(true, TypeConverter.isConvertible("false", Integer.class));
    assertEquals(true, TypeConverter.isConvertible("true", Integer.class));
    assertEquals(true, TypeConverter.isConvertible("false", "Integer"));
    assertEquals(true, TypeConverter.isConvertible("true", "Integer"));
    assertEquals(true, TypeConverter.isConvertible("false", "java.lang.Integer"));
    assertEquals(true, TypeConverter.isConvertible("true", "java.lang.Integer"));
  }

  @Test
  public void testBooleanStringToFloatCast() {
    assertEquals(Float.valueOf(0f), TypeConverter.castToType("False", Float.class));
    assertEquals(Float.valueOf(1f), TypeConverter.castToType("True", Float.class));
    assertEquals(true, TypeConverter.isConvertible("False", Float.class));
    assertEquals(true, TypeConverter.isConvertible("True", Float.class));
    assertEquals(true, TypeConverter.isConvertible("false", "Float"));
    assertEquals(true, TypeConverter.isConvertible("true", "Float"));
    assertEquals(true, TypeConverter.isConvertible("false", "java.lang.Float"));
    assertEquals(true, TypeConverter.isConvertible("true", "java.lang.Float"));
  }

  @Test
  public void testBooleanStringToDoubleCast() {
    assertEquals(Double.valueOf(0d), TypeConverter.castToType("false", Double.class));
    assertEquals(Double.valueOf(1d), TypeConverter.castToType("true", Double.class));
    assertEquals(true, TypeConverter.isConvertible("false", Double.class));
    assertEquals(true, TypeConverter.isConvertible("true", Double.class));
    assertEquals(true, TypeConverter.isConvertible("false", "Double"));
    assertEquals(true, TypeConverter.isConvertible("true", "Double"));
    assertEquals(true, TypeConverter.isConvertible("false", "java.lang.Double"));
    assertEquals(true, TypeConverter.isConvertible("true", "java.lang.Double"));
  }

  @Test
  public void testBooleanStringToShortCast() {
    assertEquals(Short.valueOf((short) 0), TypeConverter.castToType("false", Short.class));
    assertEquals(Short.valueOf((short) 1), TypeConverter.castToType("true", Short.class));
    assertEquals(true, TypeConverter.isConvertible("false", Short.class));
    assertEquals(true, TypeConverter.isConvertible("true", Short.class));
    assertEquals(true, TypeConverter.isConvertible("false", "Short"));
    assertEquals(true, TypeConverter.isConvertible("true", "Short"));
    assertEquals(true, TypeConverter.isConvertible("false", "java.lang.Short"));
    assertEquals(true, TypeConverter.isConvertible("true", "java.lang.Short"));
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
      if(pExpectedResult.getClass().isArray()) {
        assertArrayEquals("!!! ERROR: Expected result was " + pExpectedResult + " of type " + pExpectedResult.getClass().getName(),(Object[])pExpectedResult,(Object[])resultValue);
      }else {
        assertEquals("!!! ERROR: Expected result was " + pExpectedResult + " of type " + pExpectedResult.getClass().getName(), pExpectedResult, resultValue);
      }
    }
    else {
      assertNull("!!! ERROR: Conversion should not have succeeded", resultValue);
    }
  }

  private static void castTest(final Object pValue, final String pTargetClass, Object pExpectedResult) {
    Object resultValue = null;
    resultValue = TypeConverter.cast(pValue, pTargetClass);
    if (pExpectedResult != null) {
      if(pExpectedResult.getClass().isArray()) {
        assertArrayEquals("!!! ERROR: Expected result was " + pExpectedResult + " of type " + pExpectedResult.getClass().getName(),(Object[])pExpectedResult,(Object[])resultValue);
      }else {
        assertEquals("!!! ERROR: Expected result was " + pExpectedResult + " of type " + pExpectedResult.getClass().getName(), pExpectedResult, resultValue);
      }
    }
    else {
      assertNull("!!! ERROR: Conversion should not have succeeded", resultValue);
    }
  }
}

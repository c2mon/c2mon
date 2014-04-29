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

import java.awt.Color;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.text.DateFormat;

import org.apache.log4j.Logger;

/**
 * This helper class provides methods to cast a given raw type object into
 * another type.
 *
 * @author Matthias Braeger
 */
public final class TypeConverter  {
  private static final Logger LOG = Logger.getLogger(TypeConverter.class);
  
  /**
   * Hidden default constructor
   */
  private TypeConverter() {
    // Do nothing
  }
  
  
  /**
   * Tries to cast the given object value into the speciified class type. In case you are 
   * casting a Float or Double to an Integer or Short, the value will be rounded.
   * @param pValue The object to be casted
   * @param pTargetType the resulting class cast type
   * @return The resulting cast object.
   * @throws ClassCastException In case of a cast exception
   */
  @SuppressWarnings("unchecked")
  public static final <T> T castToType(final Object pValue, final Class<T> pTargetType) {
    return (T) cast(pValue, pTargetType);
  }
  
  /**
   * Tries to cast the given object value into the speciified class type. In case you are 
   * casting a Float or Double to an Integer or Short, the value will be rounded.
   * @param pValue The object to be casted
   * @param pTargetType the resulting class cast type
   * @return The resulting cast object.
   * @throws ClassCastException In case of a cast exception
   * @deprecated Please use method {@link #castToType(Object, Class)} instead
   */
  public static final Object cast(final Object pValue, final Class< ? > pTargetType) throws ClassCastException {
    if (pValue == null || pTargetType == null) {
      return null;
    }
    
    Class< ? > inputType = pValue.getClass();
    Object inputValue = pValue;
    

    // If no cast is necessary, return the original value
    if (inputType.equals(pTargetType)) {
      return pValue;
    }
    if (pTargetType.isInstance(pValue)) {
      return pTargetType.cast(pValue);
    }

    if (String.class.isAssignableFrom(pTargetType)) {
      return pValue.toString();
    }
    

    if (String.class.isAssignableFrom(inputType)) {
      try {
        final Double numberValue = Double.valueOf((String) pValue);
        inputValue = numberValue;
        inputType = Double.class;
      }
      catch (NumberFormatException e) {
        // if we are here, then the String does not represent a number
        // an exception is not needed
      }
    }
    
    
    if (Boolean.class.isAssignableFrom(pTargetType)) {
      if (String.class.isAssignableFrom(inputType)) {
        if (((String)inputValue).equalsIgnoreCase("true")) {
          return Boolean.TRUE;
        }
        else if (((String)inputValue).equalsIgnoreCase("false")) {
          return Boolean.FALSE;
        }
        else {
          throw new ClassCastException(new StringBuffer("Cannot convert String value \"").append(inputValue).append("\" to Boolean").toString());
        }
      }
      else if (Number.class.isAssignableFrom(inputType)) {
        double doubleVal = ((Number)inputValue).doubleValue();
        if (doubleVal == 1.0d) {
          return Boolean.TRUE;
        }
        else if (doubleVal == 0.0d) {
          return Boolean.FALSE;
        }
        else {
          throw new ClassCastException(new StringBuffer("Cannot convert numeric value ").append(inputValue).append(" to Boolean").toString());
        }
      }
      else {
        throw new ClassCastException(new StringBuffer("Cannot convert value of type ").append(inputType.getName()).append(" to ").append(pTargetType.getName()).append(".").toString());
      }
    }
    else if (Integer.class.isAssignableFrom(pTargetType)) {
      if (Number.class.isAssignableFrom(inputType)) {
        long x = Math.round(((Number) inputValue).doubleValue());
        if (x > Integer.MAX_VALUE || x < Integer.MIN_VALUE) {
          throw new ClassCastException(new StringBuffer("Numeric value ").append(x).append(" to big to be converted to Integer.").toString());
        }
        return new Integer((int)x);
      }
      else if (String.class.isAssignableFrom(inputType)) {
        try {
          return Integer.valueOf((String)inputValue);
        }
        catch (Exception e) {
          throw new ClassCastException(new StringBuffer("Cannot convert String value \"").append(inputValue).append("\" to Integer.").toString());
        }
      }
      else if (Boolean.class.isAssignableFrom(inputType)) {
        if (inputValue.equals(Boolean.TRUE)) {
          return new Integer(1);
        }
        else {
          return new Integer(0);
        }          
      }
      else {
        throw new ClassCastException(new StringBuffer("Cannot convert value of type ").append(inputType.getName()).append(" to ").append(pTargetType.getName()).append(".").toString());
      }
    }
    else if (Short.class.isAssignableFrom(pTargetType)) {
      if (Number.class.isAssignableFrom(inputType)) {
        long x = Math.round(((Number) inputValue).doubleValue());
        if (x > Short.MAX_VALUE || x < Short.MIN_VALUE) {
          throw new ClassCastException(new StringBuffer("Numeric value ").append(x).append(" to big to be converted to Short.").toString());
        }
        return new Short((short)x);
      }
      else if (String.class.isAssignableFrom(inputType)) {
        try {
          return Short.valueOf((String)inputValue);
        }
        catch (Exception e) {
          throw new ClassCastException(new StringBuffer("Cannot convert String value \"").append(inputValue).append("\" to Short.").toString());
        }
      }
      else if (Boolean.class.isAssignableFrom(inputType)) {
        if (inputValue.equals(Boolean.TRUE)) {
          return new Short((short)1);
        }
        else {
          return new Short((short)0);
        }          
      }
      else {
        throw new ClassCastException(new StringBuffer("Cannot convert value of type ").append(inputType.getName()).append(" to ").append(pTargetType.getName()).append(".").toString());
      }
    }
    else if (Long.class.isAssignableFrom(pTargetType)) {
      if (Number.class.isAssignableFrom(inputType)) {
        return Long.valueOf(Math.round(((Number) inputValue).doubleValue()));
      }
      else if (String.class.isAssignableFrom(inputType)) {
        try {
          return Long.valueOf((String)inputValue);
        }
        catch (Exception e) {
          throw new ClassCastException(new StringBuffer("Cannot convert String value \"").append(inputValue).append("\" to Long.").toString());
        }
      }
      else if (Boolean.class.isAssignableFrom(inputType)) {
        if (inputValue.equals(Boolean.TRUE)) {
          return new Long(1);
        }
        else {
          return new Long(0);
        }          
      }
      else {
        throw new ClassCastException(new StringBuffer("Cannot convert value of type ").append(inputType.getName()).append(" to ").append(pTargetType.getName()).append(".").toString());
      }
    }
    else if (Float.class.isAssignableFrom(pTargetType)) {
      if (Number.class.isAssignableFrom(inputType)) {
        double x = ((Number)inputValue).doubleValue();
        if (x > Float.MAX_VALUE || x < -Float.MAX_VALUE) {
          throw new ClassCastException(new StringBuffer("Numeric value ").append(x).append(" to big to be converted to Float.").toString());
        }
        else {
          return new Float(x);
        }
      }
      else if (String.class.isAssignableFrom(inputType)) {
        try {
          return Float.valueOf((String)inputValue);
        }
        catch (Exception e) {
          throw new ClassCastException(new StringBuffer("Cannot convert String value \"").append(inputValue).append("\" to Float.").toString());
        }
      }
      else if (Boolean.class.isAssignableFrom(inputType)) {
        if (inputValue.equals(Boolean.TRUE)) {
          return new Float(1);
        }
        else {
          return new Float(0);
        }          
      }
      else {
        throw new ClassCastException(new StringBuffer("Cannot convert value of type ").append(inputType.getName()).append(" to ").append(pTargetType.getName()).append(".").toString());
      }
    }
    else if (boolean.class.isAssignableFrom(pTargetType)) {
      try {
        final boolean result = Boolean.valueOf(inputValue.toString()).booleanValue();
        if (pTargetType == boolean.class) {
          return result;
        }
        else {
          return pTargetType.cast(result);
        }
      }
      catch (Exception e) {
        throw new ClassCastException(String.format("Failed to convert '%s' of type '%s' into a '%s' (%s)", 
            inputValue, inputValue.getClass().getName(), pTargetType.getName(), e.getMessage()));
      }
    }
    else if (Double.class.isAssignableFrom(pTargetType)
        && (Number.class.isAssignableFrom(inputType)
            || String.class.isAssignableFrom(inputType)
            || Boolean.class.isAssignableFrom(inputType))) {
      if (Number.class.isAssignableFrom(inputType)) {
        return new Double(((Number)inputValue).doubleValue());
      }
      else if (String.class.isAssignableFrom(inputType)) {
        try {
          return Double.valueOf((String)inputValue);
        }
        catch (Exception e) {
          throw new ClassCastException(new StringBuffer("Cannot convert String value \"").append(inputValue).append("\" to Double.").toString());
        }
      }
      else if (Boolean.class.isAssignableFrom(inputType)) {
        if (inputValue.equals(Boolean.TRUE)) {
          return new Double(1);
        }
        else {
          return new Double(0);
        }          
      }
      else {
        throw new ClassCastException(new StringBuffer("Cannot convert value of type ").append(inputType.getName()).append(" to ").append(pTargetType.getName()).append(".").toString());
      }
    } else if (pTargetType.isEnum() && String.class.isAssignableFrom(inputType)) {
      try {
        Class< ? extends Enum> enumClass = (Class< ? extends Enum>) pTargetType;
        return Enum.valueOf(enumClass, (String) inputValue);        
      } catch (Exception e) {
        // Tries the incase sensitive method instead 
      } 
      
      final String enumName = String.class.cast(inputValue);
      final Object constants[] = pTargetType.getEnumConstants();
      
      for (final Object constant : constants) {
        if (constant.toString().compareToIgnoreCase(enumName) == 0) {
          return constant;
        }
      }
      
      //only throw cast exception to be consistent with rest of this class...
      throw new ClassCastException(String.format("Unable to convert the string '%s' into an enum of type '%s'",
          inputValue.toString(), pTargetType.getName()));
    }
    else if (pTargetType.isPrimitive() 
        && (Number.class.isAssignableFrom(inputType)
            || inputType == byte.class
            || inputType == short.class
            || inputType == int.class
            || inputType == long.class
            || inputType == float.class
            || inputType == double.class
            )) {
      
      Number numberValue;
      if (inputType.isPrimitive()) {
        if (byte.class == inputType) {
          numberValue = Byte.class.cast(inputValue);
        }
        else if (short.class == inputType) {
          numberValue = Short.class.cast(inputValue);
        }
        else if (int.class == inputType) {
          numberValue = Integer.class.cast(inputValue);
        }
        else if (long.class == inputType) {
          numberValue = Long.class.cast(inputValue);
        }
        else if (float.class == inputType) {
          numberValue = Float.class.cast(inputValue);
        }
        else if (double.class == inputType) {
          numberValue = Double.class.cast(inputValue);
        }
        else {
          throw new ClassCastException(String.format("Failed to convert a '%s' to a '%s'.", inputType.getSimpleName(), pTargetType.getSimpleName()));
        }
      }
      else {
        numberValue = Number.class.cast(inputValue);
      }
      
      if (double.class == pTargetType) {
        return numberValue.doubleValue();
      }
      else if (float.class == pTargetType) {
        return numberValue.floatValue();
      }
      else if (int.class == pTargetType) {
        return numberValue.intValue();
      }
      else if (long.class == pTargetType) {
        return numberValue.longValue();
      }
      else if (short.class == pTargetType) {
        return numberValue.shortValue();
      }
      else if (byte.class == pTargetType) {
        return numberValue.byteValue();
      }
      else {
        throw new ClassCastException(String.format("Failed to convert a '%s' to a '%s'.", inputType.getSimpleName(), pTargetType.getSimpleName()));
      }
      
    }
    else if (inputType.isArray() && pTargetType.isArray()) {
      int inputArrayLength = Array.getLength(inputValue);
      final Class< ? > elementTargetType = pTargetType.getComponentType();
      final Object result = Array.newInstance(elementTargetType, inputArrayLength);
      for (int i = 0; i < inputArrayLength; i++) {
        Array.set(result, i, castToType(Array.get(inputValue, i), elementTargetType));
      }
      return result;
    }
    else if (inputType == java.sql.Timestamp.class) {
      return cast(((java.sql.Timestamp) inputValue).getTime(), pTargetType);
    }
    else if (pTargetType == java.sql.Timestamp.class) {
      if (inputValue instanceof String) {
        try {
          return DateFormat.getDateInstance().format(inputValue);
        }
        catch (Exception e) { }
      }
      try {
        final Long milliseconds = castToType(inputValue, Long.class);
        return new java.sql.Timestamp(milliseconds);
      }
      catch (Exception e) { }
      
      throw new ClassCastException(String.format("Could not convert '%s' into a '%s'", inputValue.toString(), java.sql.Timestamp.class.getName()));
    }
    else if (inputType == String.class && pTargetType == Color.class) {
      String str = (String) inputValue;
      
      // Tests first if the string is a hex string which can be translated into a color
      try {
        String hexStr = str;
        if (hexStr.charAt(0) == '#') {
          hexStr = hexStr.substring(1);
        }
        return new Color(Integer.parseInt(hexStr, 16));
      }
      catch (Exception e) { }
      
      // Checks if the color string is a field in the Color class
      try {
          Field field = Color.class.getField(str);
          return (Color)field.get(null);
      } catch (Exception e) {
      }
      
      throw new ClassCastException(String.format("Could not convert '%s' into a Color", str));
    }
    else {
      throw new ClassCastException(new StringBuffer("Cannot convert value of type ").append(inputType.getName()).append(" to ").append(pTargetType.getName()).append(".").toString());
    }
  }

  /**
   * This method tries to cast any kind of string into the given raw value type. The
   * following type strings are supported: <code>Boolean, Integer, Float, String, 
   * Double, Long, Short</code>
   * @param pValueString The string that shall be casted into the specific raw value type
   * @param pTypeString The raw type class as Sting and without the <code>java.lang.</code>
   *                    prefix
   * @return The casted object or <code>null</code>, if casting wasn't possible
   */
  public static final Object cast(final String pValueString, final String pTypeString) {
    Object result = null;
    if (pValueString != null && pTypeString != null) {
      try {
        if (pTypeString.equals("Boolean")) {
          result = cast(pValueString, Boolean.class);
        }
        else if (pTypeString.equals("Integer")) {
          result = cast(pValueString, Integer.class);
        }
        else if (pTypeString.equals("Float")) {
          result = cast(pValueString, Float.class);
        }
        else if (pTypeString.equals("String")) {
          result = pValueString;
        }
        else if (pTypeString.equals("Double")) {
          result = cast(pValueString, Double.class);
        }
        else if (pTypeString.equals("Long")) {
          result = cast(pValueString, Long.class);
        }
        else if (pTypeString.equals("Short")) {
          result = cast(pValueString, Short.class);
        }
      }
      catch (ClassCastException cce) {
        LOG.error("cast() : Conversion error", cce);
        result = null;
      }
    }
    if (result == null) {
      LOG.warn("cast() returning null.");
    }
    return result;
  }
}
/******************************************************************************
 * Copyright (C) 2010-2021 CERN. All rights not expressly granted are reserved.
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

import java.awt.Color;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

import cern.c2mon.shared.util.json.GsonFactory;

/**
 * This helper class provides methods to cast a given raw type object into
 * another type.
 *
 * @author Matthias Braeger
 * @author Franz Ritter
 */
@Slf4j
public final class TypeConverter  {

  private static final String JAVA_LANG_PREFIX = "java.lang.";
  
  /** Gson instance */
  private static transient Gson gson = GsonFactory.createGson();

  /**
   * Hidden default constructor
   */
  private TypeConverter() {
    // Do nothing
  }

  /**
   * Checks whether the given value can be casted into the given class type.
   * @param value The object to be casted
   * @param clazz the resulting class cast type
   * @return <code>true</code>, if the passed object value can be casted in to the given class type
   */
  public static final <T> boolean isConvertible(final Object value, final Class<T> clazz) {
    try {
      return (castToType(value, clazz) != null);
    } catch (ClassCastException e) {
      return false;
    }
  }

  /**
   * Checks whether the given value can be casted into the given class type. The
   * following shortcut type strings are supported: <code>Boolean, Integer, Float, String,
   * Double, Long, Short</code>
   * @param value The object to be casted
   * @param className the class cast type as name
   * @return <code>true</code>, if the passed object value can be casted in to the given class type
   * @see #cast(Object, String)
   */
  public static final boolean isConvertible(final Object value, final String className) {
      return (cast(value, className) != null);
  }

  /**
   * Tries to cast the given object value into the specified class type. In case you are
   * casting a Float or Double to an Integer or Short, the value will be rounded.
   * @param value The object to be casted
   * @param clazz the resulting class cast type
   * @return The resulting cast object.
   * @throws ClassCastException In case of a cast exception
   */
  @SuppressWarnings("unchecked")
  public static final <T> T castToType(final Object value, final Class<T> clazz) {
    return (T) cast(value, clazz);
  }

  /**
   * Tries to cast the given object value into the specified class type. In case you are
   * casting a Float or Double to an Integer or Short, the value will be rounded.
   * @param pValue The object to be casted
   * @param pTargetType the resulting class cast type
   * @return The resulting cast object.
   * @throws ClassCastException In case of a cast exception
   */
  @SuppressWarnings("unchecked")
  private static final Object cast(final Object pValue, final Class< ? > pTargetType) throws ClassCastException {
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
        if (((String)inputValue).trim().equalsIgnoreCase("false")) {
          return Integer.valueOf(0);
        }
        else if (((String)inputValue).trim().equalsIgnoreCase("true")) {
          return Integer.valueOf(1);
        }
        else {
          try {
            return Integer.valueOf((String)inputValue);
          }
          catch (Exception e) {
            throw new ClassCastException(new StringBuffer("Cannot convert String value \"").append(inputValue).append("\" to Integer.").toString());
          }
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
        if (((String)inputValue).trim().equalsIgnoreCase("false")) {
          return Short.valueOf((short) 0);
        }
        else if (((String)inputValue).trim().equalsIgnoreCase("true")) {
          return Short.valueOf((short) 1);
        }
        else {
          try {
            return Short.valueOf((String)inputValue);
          }
          catch (Exception e) {
            throw new ClassCastException(new StringBuffer("Cannot convert String value \"").append(inputValue).append("\" to Short.").toString());
          }
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
    else if (Byte.class.isAssignableFrom(pTargetType)) {
      if (Number.class.isAssignableFrom(inputType)) {
        long x = Math.round(((Number) inputValue).doubleValue());
        if (x > Byte.MAX_VALUE || x < Byte.MIN_VALUE) {
          throw new ClassCastException(new StringBuffer("Numeric value ").append(x).append(" to big to be converted to Byte.").toString());
        }
        return new Byte((byte)x);
      }
      else if (String.class.isAssignableFrom(inputType)) {
        if (((String)inputValue).trim().equalsIgnoreCase("false")) {
          return Byte.valueOf((byte) 0);
        }
        else if (((String)inputValue).trim().equalsIgnoreCase("true")) {
          return Byte.valueOf((byte) 1);
        }
        else {
          try {
            return Byte.valueOf((String)inputValue);
          }
          catch (Exception e) {
            throw new ClassCastException(new StringBuffer("Cannot convert String value \"").append(inputValue).append("\" to Byte.").toString());
          }
        }
      }
      else if (Boolean.class.isAssignableFrom(inputType)) {
        if (inputValue.equals(Boolean.TRUE)) {
          return new Byte((byte)1);
        }
        else {
          return new Byte((byte)0);
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
        if (((String)inputValue).trim().equalsIgnoreCase("false")) {
          return Long.valueOf(0l);
        }
        else if (((String)inputValue).trim().equalsIgnoreCase("true")) {
          return Long.valueOf(1l);
        }
        else {
          try {
            return Long.valueOf((String)inputValue);
          }
          catch (Exception e) {
            throw new ClassCastException(new StringBuffer("Cannot convert String value \"").append(inputValue).append("\" to Long.").toString());
          }
        }
      }
      else if (Boolean.class.isAssignableFrom(inputType)) {
        if (inputValue.equals(Boolean.TRUE)) {
          return Long.valueOf(1l);
        }
        else {
          return Long.valueOf(0l);
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
        if (((String)inputValue).trim().equalsIgnoreCase("false")) {
          return Float.valueOf(0f);
        }
        else if (((String)inputValue).trim().equalsIgnoreCase("true")) {
          return Float.valueOf(1f);
        }
        else {
          try {
            return Float.valueOf((String)inputValue);
          }
          catch (Exception e) {
            throw new ClassCastException(new StringBuffer("Cannot convert String value \"").append(inputValue).append("\" to Float.").toString());
          }
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
        return Double.valueOf(((Number)inputValue).toString());
      }
      else if (String.class.isAssignableFrom(inputType)) {
        if (((String)inputValue).trim().equalsIgnoreCase("false")) {
          return Double.valueOf(0d);
        }
        else if (((String)inputValue).trim().equalsIgnoreCase("true")) {
          return Double.valueOf(1d);
        }
        else {
          try {
            return Double.valueOf((String)inputValue);
          }
          catch (Exception e) {
            throw new ClassCastException(new StringBuffer("Cannot convert String value \"").append(inputValue).append("\" to Double.").toString());
          }
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

    // Array handling:
    else if (inputType.isArray() && pTargetType.isArray()) {
      int inputArrayLength = Array.getLength(inputValue);
      final Class< ? > elementTargetType = pTargetType.getComponentType();
      final Object result = Array.newInstance(elementTargetType, inputArrayLength);
      for (int i = 0; i < inputArrayLength; i++) {
        Array.set(result, i, cast(Array.get(inputValue, i), elementTargetType.getName()));
      }
      return result;
    } else if (inputType.isArray() && ArrayList.class == pTargetType) {
      return new ArrayList<Object>(Arrays.asList((Object[])inputValue));
    } else if (String.class == inputType && ArrayList.class == pTargetType && pValue.toString().charAt(0) == '[') {
      return gson.fromJson((String) pValue, pTargetType);
    } else if (ArrayList.class == inputType && pTargetType.isArray()) {
      List<Object> inputList = (ArrayList<Object>) inputValue;
      int inputArrayLength = inputList.size();
      final Class< ? > elementTargetType = pTargetType.getComponentType();
      final Object result = Array.newInstance(elementTargetType, inputArrayLength);
      int i = 0;
      for (Object value : inputList) {
        Array.set(result, i++, cast(value, elementTargetType.getName()));
      }
      
      return result;
    } 
      
    // SQL Timestamp handling:
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

    // Color handling:
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
          return field.get(null);
      } catch (Exception e) {
      }

      throw new ClassCastException(String.format("Could not convert '%s' into a Color", str));
    }

    // default:
    else {
      throw new ClassCastException(new StringBuffer("Cannot convert value of type ").append(inputType.getName()).append(" to ").append(pTargetType.getName()).append(".").toString());
    }
  }


  /**
   * This method tries to cast any kind of Object into the given raw value type. The
   * following shortcut type strings are supported: <code>Boolean, Integer, Float, String,
   * Double, Long, Short</code>
   * @param value The object that shall be casted into the specific raw value type
   * @param className The raw type class as Sting and without the <code>java.lang.</code>
   *                    prefix
   * @return The casted object or <code>null</code>, if casting wasn't possible
   */
  public static final Object cast(final Object value, final String className) {
    Object result = null;

    if (value != null && className != null && !className.isEmpty()) {
      try {

        Class<?> type = getType(className);

        if (type != null) {

          if (type.equals(String.class)) {
            result = value.toString();
          } else {
            result = castToType(value, type);
          }
        }

        if (result == null) {
          log.error("Conversion error: Could not cast input value [" + value + "] of type "
              + value.getClass().getName() + " to resulting type " + className);
        }
      } catch (ClassCastException cce) {
        log.error("Conversion error: {}", cce.getMessage());
        result = null;
      }
    }

    return result;
  }


  //===========================================================================
  // Type checking methods based on a type in string representation
  //===========================================================================

  /**
   * Checks if the given data type is a Number.
   * <p>
   * Because of the old representation of data type (non fully-qualified class
   * name) we have to check separately for all subclasses of {@link Number}.
   *
   * @param dataType the data type as string
   * @return returns true if the data type is a subclass of {@link Number}
   */
  public static boolean isNumber(String dataType) {
    Class<?> type = getType(dataType);
    return type != null && Number.class.isAssignableFrom(type);
  }

  public static boolean isKnownClass(String typeName) {
    Class<?> result = typeName != null ? getType(typeName) : null;
    return result != null;
  }

  /**
   * @param typeName a simple class name within the java.lang.* package or the
   *                 fully qualified class name
   * @return the class for the given name if known, {@literal null} otherwise
   */
  public static Class<?> getType(String typeName) {
    if (typeName == null) {
      return null;
    }

    String fullPath = typeName.contains(".") ? typeName : JAVA_LANG_PREFIX + typeName;

    try {
      return Class.forName(fullPath);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  /**
   * Is the given type name a primitive type?
   *
   * @param typeName a simple class name within the java.lang.* package or the
   *                 fully qualified class name
   *
   * @return true if primitive
   */
  public static boolean isPrimitive(String typeName) {
	  String fullPath = !typeName.contains(".") ? JAVA_LANG_PREFIX + typeName : typeName;
	  return fullPath.startsWith(JAVA_LANG_PREFIX) && !fullPath.equals("java.lang.Object");
  }
}

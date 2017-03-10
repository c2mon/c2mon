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
package cern.c2mon.shared.common;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import cern.c2mon.shared.common.type.TypeConverter;
/**
 * A simple class to cover modifications on simple fields of a pojo.
 *
 * @author Andreas Lang
 *
 */
public class SimpleTypeReflectionHandler {
    /**
     * Sets a field of a pojo corresponding to the provided name with to the
     * provided value.
     * @param pojo Plain old Java object.
     * @param fieldName The java name of the field to set.
     * @param value The value to set the field to (unconverted).
     * @throws NoSuchFieldException Throws a {@link NoSuchFieldException} if the
     * field to use the type to parse is not found in the provided class.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSimpleValueParseException This exception is thrown if the field
     * you provide via the field name is not a simple type field.
     */
    public void setSimpleFieldByString(final Object pojo, final String fieldName, final String value) throws NoSuchFieldException, IllegalAccessException, NoSimpleValueParseException {
        Field field = getField(pojo.getClass(), fieldName);
        if (field == null) {
            throw new NoSuchFieldException("Field: '" + fieldName + "' not found "
                    + "in " + pojo.getClass().getName());
        }
        setSimpleField(pojo, fieldName, parse(value, field));
    }

    /**
     * Sets a field of a pojo corresponding to the provided name with to the
     * provided value.
     * @param pojo Plain old Java object.
     * @param fieldName The java name of the field to set.
     * @param value The value to set the field to (unconverted).
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     */
    public void setSimpleField(final Object pojo, final String fieldName, final Object value) throws NoSuchFieldException, IllegalAccessException {
        Class< ? > configClass = pojo.getClass();
        Field field = getField(configClass, fieldName);
        if (field == null) {
            throw new NoSuchFieldException("Field: '" + fieldName + "' not found "
                    + "in " + configClass.getClass().getName());
        }
        field.setAccessible(true);

        if (field.getType().isInstance(value)) {
          field.set(pojo, value);
        }
        else {
          field.set(pojo, TypeConverter.castToType(value, field.getType()));
        }
    }

    /**
     * Parses a value to the type of the field with the same name in the class
     * object.
     * @param value The value to parse.
     * @param javaName The field which is used to identify the value.
     * @param clazz The class object to search the field inside.
     * @return The parsed object.
     * @throws NoSimpleValueParseException Throws a no simple value parse exception
     * if you try to use it with a non simple type.
     * @throws NoSuchFieldException Throws a {@link NoSuchFieldException} if the
     * field to use the type to parse is not found in the provided class.
     */
    public Object parse(final String value, final String javaName, final Class< ? > clazz) throws NoSimpleValueParseException, NoSuchFieldException {
        Field field = getField(clazz, javaName);
        if (field == null) {
            throw new NoSuchFieldException("Field: '" + javaName + "' not found "
                    + "in " + clazz.getClass().getName());
        }
        return parse(value, field);
    }

    /**
     * Parses a value to the type of the type of the provided field.
     * @param value The value to parse.
     * @param field The field which is used to identify the type.
     * @return The parsed object.
     * @throws NoSimpleValueParseException Throws a no simple value parse exception
     * if you try to use it with a non simple type.
     */
    public Object parse(final String value, final Field field) throws NoSimpleValueParseException {
      if (value == null) {
        return null;
      }

      Object parsedValue = TypeConverter.castToType(value, field.getType());

      if (parsedValue == null) {
        throw new NoSimpleValueParseException();
      }
      return parsedValue;
    }

    /**
     * Returns the field with the provided name from the provided class.
     * It will also recursively search the super classes.
     * @param pojoClass The class to search in.
     * @param fieldName The field name to search for.
     * @return The field if found or null if no field with that name exists.
     */
    public Field getField(final Class< ? > pojoClass, final String fieldName) {
        Field field = null;
        try {
            field = pojoClass.getDeclaredField(fieldName);
        }
        catch (NoSuchFieldException exception) {
            Class< ? > clazz = pojoClass.getSuperclass();
            if (clazz != null) {
                return getField(clazz, fieldName);
            }
        }
        return field;
    }

    /**
     * Returns all non transient field of this class and all its superclasses.
     * Not simple and static field are excluded.
     * @param clazz The class to search in.
     * @return All non transient fields of this class and its superclasses.
     */
    public List<Field> getNonTransientSimpleFields(final Class< ? > clazz) {
        List<Field> result = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (!Modifier.isTransient(field.getModifiers())
                    && !Modifier.isStatic(field.getModifiers())
                    && isSimpleTypeOrSimpleObject(field)) {
                result.add(field);
            }
        }
        if (clazz.getSuperclass() != null) {
            result.addAll(getNonTransientSimpleFields(clazz.getSuperclass()));
        }
        return result;
    }

    /**
     * Checks if the provided field is a simple type field or an object like this.
     * @param field The field to check.
     * @return True if the field has a simple type (see list in class comment) else false.
     */
    public boolean isSimpleTypeOrSimpleObject(final Field field) {
        Class< ? > type = field.getType();
        boolean simpleType =
                  (type.isAssignableFrom(Short.class)
                || type.isAssignableFrom(Short.TYPE)
                || type.isAssignableFrom(Integer.class)
                || type.isAssignableFrom(Integer.TYPE)
                || type.isAssignableFrom(Float.class)
                || type.isAssignableFrom(Float.TYPE)
                || type.isAssignableFrom(Double.class)
                || type.isAssignableFrom(Double.TYPE)
                || type.isAssignableFrom(Long.class)
                || type.isAssignableFrom(Long.TYPE)
                || type.isAssignableFrom(Byte.class)
                || type.isAssignableFrom(Byte.TYPE)
                || type.isAssignableFrom(Character.class)
                || type.isAssignableFrom(Character.TYPE)
                || type.isAssignableFrom(Boolean.class)
                || type.isAssignableFrom(Boolean.TYPE)
                || type.isAssignableFrom(String.class))
                || type.isEnum();
        return simpleType;
    }

    /**
     * Checks if the provided field is a simple type field .
     * @param field The field to check.
     * @return True if the field has a simple type (see list in class comment) else false.
     */
    public boolean isSimpleType(final Field field) {
        Class< ? > type = field.getType();
        boolean simpleType =
                   type.isAssignableFrom(Short.TYPE)
                || type.isAssignableFrom(Integer.TYPE)
                || type.isAssignableFrom(Float.TYPE)
                || type.isAssignableFrom(Double.TYPE)
                || type.isAssignableFrom(Long.TYPE)
                || type.isAssignableFrom(Byte.TYPE)
                || type.isAssignableFrom(Character.TYPE)
                || type.isAssignableFrom(Boolean.TYPE);
        return simpleType;
    }
}

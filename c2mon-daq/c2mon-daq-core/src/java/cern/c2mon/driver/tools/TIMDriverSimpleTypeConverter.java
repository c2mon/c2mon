/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2010 CERN This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.driver.tools;

import cern.tim.shared.common.type.TagDataType;
import cern.tim.shared.daq.datatag.ISourceDataTag;

/**
 * This class is used for converting the data types of the data sources to the
 * TIM-data types.
 */
public class TIMDriverSimpleTypeConverter {

    /**
     * The conversion method to convert a boolean to the data type of the
     * source data tag.
     * 
     * @param tag The SourceDataTag to get the data type from.
     * @param value the value to convert.
     * @return The resulting object. If the numeric data type of the source 
     * data tag is not recognized null is returned.
     */
    public static Object convert(final ISourceDataTag tag, final boolean value) {
        int dataType = tag.getDataTypeNumeric();
        Object result;

        switch(dataType) {
        case TagDataType.TYPE_INTEGER:
            if (value) {
                result = Integer.valueOf(1);
            } else {
                result = Integer.valueOf(0);
            }
            break;

        case TagDataType.TYPE_LONG:
            if (value) {
                result = Long.valueOf(1);
            } else {
                result = Long.valueOf(0);
            }
            break;

        case TagDataType.TYPE_FLOAT:
            if (value) {
                result = Float.valueOf(1.0f);
            } else {
                result = Float.valueOf(0.0f);
            }
            break;

        case TagDataType.TYPE_DOUBLE:
            if (value) {
                result = Double.valueOf(1.0);
            } else {
                result = Double.valueOf(0.0);
            }
            break;

        case TagDataType.TYPE_BOOLEAN:
            result = Boolean.valueOf(value);
            break;

        case TagDataType.TYPE_STRING:
            if (value) {
                result = "true";
            } else {
                result = "false";
            }
            break;
        default:
            // if the type is unknown null is returned
            result = null;
            break;
        }
        return result;
    }

    /**
     * The conversion method to convert a byte value to the data type of
     * the data tag.
     * 
     * @param tag SourceDataTag to get the data type from.
     * @param value The value to convert.
     * @return The converted object with the correct data type or null
     * if the data type in the source data tag is not known or the conversion
     * to that data type failed.
     */
    public static Object convert(final ISourceDataTag tag, final byte value) {
        int dataType = tag.getDataTypeNumeric();
        Object result;
        Byte byteValue = Byte.valueOf(value);

        switch (dataType) {
        case TagDataType.TYPE_INTEGER:
            result = Integer.valueOf(byteValue.intValue());
            break;

        case TagDataType.TYPE_LONG:
            result = Long.valueOf(byteValue.longValue());
            break;

        case TagDataType.TYPE_FLOAT:
            result = Float.valueOf(byteValue.floatValue());
            break;

        case TagDataType.TYPE_DOUBLE:
            result = Double.valueOf(byteValue.doubleValue());
            break;

        case TagDataType.TYPE_BOOLEAN:
            if (value == 0) {
                result = Boolean.valueOf(false);
            } else if (value == 1) {
                result = Boolean.valueOf(true);
            } else {
                result = null;
            }
            break;

        case TagDataType.TYPE_STRING:
            try {
                // TODO Why catching this exception?
                result = Byte.toString(value);
            } catch (Exception ex) {
                result = null;
            }
            break;
        default:
            result = null;
            break;

        }
        return result;
    }

    /**
     * The conversion method to convert a short value to the data type of
     * the data tag.
     * 
     * @param tag SourceDataTag to get the data type from.
     * @param value The value to convert.
     * @return The converted object with the correct data type or null
     * if the data type in the source data tag is not known or the conversion
     * to that data type failed.
     */
    public static Object convert(final ISourceDataTag tag, final short value) {
        int dataType = tag.getDataTypeNumeric();
        Object result;
        Short s = Short.valueOf(value);

        switch (dataType) {
        case TagDataType.TYPE_INTEGER:
            result = Integer.valueOf(s.intValue());
            break;

        case TagDataType.TYPE_LONG:
            result = Long.valueOf(s.longValue());
            break;

        case TagDataType.TYPE_FLOAT:
            result = Float.valueOf(s.floatValue());
            break;

        case TagDataType.TYPE_DOUBLE:
            result = Double.valueOf(s.doubleValue());
            break;

        case TagDataType.TYPE_BOOLEAN:
            if (value == 0) {
                result = Boolean.valueOf(false);
            } else if (value == 1) {
                result = Boolean.valueOf(true);
            } else {
                result = null;
            }
            break;

        case TagDataType.TYPE_STRING:
            try {
                // TODO This exception can not happen?!
                result = Short.toString(value);
            } catch (Exception ex) {
                result = null;
            }
            break;
        default:
            result = null;
            break;
        }

        return result;
    }

    /**
     * The conversion method to convert a int value to the data type of
     * the data tag.
     * 
     * @param tag SourceDataTag to get the data type from.
     * @param value The value to convert.
     * @return The converted object with the correct data type or null
     * if the data type in the source data tag is not known or the conversion
     * to that data type failed.
     */
    public static Object convert(final ISourceDataTag tag, final int value) {
        int dataType = tag.getDataTypeNumeric();
        Object result;
        Integer i = Integer.valueOf(value);

        switch (dataType) {

        case TagDataType.TYPE_INTEGER:
            result = i;
            break;

        case TagDataType.TYPE_LONG:
            result = Long.valueOf(i.longValue());
            break;

        case TagDataType.TYPE_FLOAT:
            result = new Float(i.floatValue());
            break;

        case TagDataType.TYPE_DOUBLE:
            result = new Double(i.doubleValue());
            break;

        case TagDataType.TYPE_BOOLEAN:
            if (value == 0) {
                result = Boolean.valueOf(false);
            } else if (value == 1) {
                result = Boolean.valueOf(true);
            } else {
                result = null;
            }
            break;

        case TagDataType.TYPE_STRING:
            try {
                // TODO why exception?
                result = Integer.toString(value);
            } catch (Exception ex) {
                result = null;
            }
            break;
        default:
            result = null;
            break;
        }

        return result;
    }

    /**
     * The conversion method to convert a long value to the data type of
     * the data tag.
     * 
     * @param tag SourceDataTag to get the data type from.
     * @param value The value to convert.
     * @return The converted object with the correct data type or null
     * if the data type in the source data tag is not known or the conversion
     * to that data type failed.
     */
    public static Object convert(final ISourceDataTag tag, final long value) {
        int dataType = tag.getDataTypeNumeric();
        Object result;
        Long l = Long.valueOf(value);

        switch (dataType) {
        case TagDataType.TYPE_INTEGER:
            if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) {
                result = null;
            }
            else {
                result = Integer.valueOf(l.intValue());
            }
            break;

        case TagDataType.TYPE_LONG:
            result = l;
            break;

        case TagDataType.TYPE_FLOAT:
            result = new Float(l.floatValue());
            break;

        case TagDataType.TYPE_DOUBLE:
            result = new Double(l.doubleValue());
            break;

        case TagDataType.TYPE_BOOLEAN:
            if (value == 0) {
                result = Boolean.valueOf(false);
            } else if (value == 1) {
                result = Boolean.valueOf(true);
            } else {
                result = null;
            }
            break;

        case TagDataType.TYPE_STRING:
            try {
                result = Long.toString(value);
            } catch (Exception ex) {
                result = null;
            }
            break;
        default:
            result = null;
            break;

        }
        return result;
    }

    /**
     * The conversion method to convert a float value to the data type of
     * the data tag.
     * 
     * @param tag SourceDataTag to get the data type from.
     * @param value The value to convert.
     * @return The converted object with the correct data type or null
     * if the data type in the source data tag is not known or the conversion
     * to that data type failed.
     */
    public static Object convert(final ISourceDataTag tag, final float value) {
        int dataType = tag.getDataTypeNumeric();
        Object result;
        Float f = new Float(value);

        switch (dataType) {
        case TagDataType.TYPE_INTEGER:
            if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) {
                result = null;
            }
            else {
                result = Integer.valueOf(f.intValue());
            }
            break;

        case TagDataType.TYPE_LONG:
            if (value > Long.MAX_VALUE || value < Long.MIN_VALUE) {
                result = null;
            }
            else {
                result = Long.valueOf(f.longValue());
            }
            break;

        case TagDataType.TYPE_FLOAT:
            result = f;
            break;

        case TagDataType.TYPE_DOUBLE:
            result = new Double(f.doubleValue());
            break;

        case TagDataType.TYPE_BOOLEAN:
            if (value == 0.0f) {
                result = Boolean.valueOf(false);
            } else if (value == 1.0f) {
                result = Boolean.valueOf(true);
            } else {
                result = null;
            }
            break;

        case TagDataType.TYPE_STRING:
            try {
                result = Float.toString(value);
            } catch (Exception ex) {
                result = null;
            }
            break;
        default:
            result = null;
            break;

        }

        return result;
    }

    /**
     * The conversion method to convert a double value to the data type of
     * the data tag.
     * 
     * @param tag SourceDataTag to get the data type from.
     * @param value The value to convert.
     * @return The converted object with the correct data type or null
     * if the data type in the source data tag is not known or the conversion
     * to that data type failed.
     */
    public static Object convert(final ISourceDataTag tag, final double value) {
        int dataType = tag.getDataTypeNumeric();
        Object result;
        Double d = new Double(value);

        switch (dataType) {
        case TagDataType.TYPE_INTEGER:
            if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) {
                result = null;
            }
            else {
                result = Integer.valueOf(d.intValue());
            }
            break;

        case TagDataType.TYPE_LONG:
            if (value > Long.MAX_VALUE || value < Long.MIN_VALUE) {
                result = null;
            }
            else {
                result = Long.valueOf(d.longValue());
            }
            break;

        case TagDataType.TYPE_FLOAT:
            if (value > Float.MAX_VALUE || value < -Float.MAX_VALUE) {
                result = null;
            }
            else {
                result = new Float(d.floatValue());
            }
            break;

        case TagDataType.TYPE_DOUBLE:
            result = d;
            break;

        case TagDataType.TYPE_BOOLEAN:
            if (value == 0.0f) {
                result = Boolean.valueOf(false);
            } else if (value == 1.0f) {
                result = Boolean.valueOf(true);
            } else {
                result = null;
            }
            break;

        case TagDataType.TYPE_STRING:
            try {
                result = Double.toString(value);
            } catch (Exception ex) {
                result = null;
            }
            break;
        default:
            result = null;
            break;

        }
        return result;
    }

    /**
     * The conversion method to convert a String value to the data type of
     * the data tag.
     * 
     * @param tag SourceDataTag to get the data type from.
     * @param value The value to convert.
     * @return The converted object with the correct data type or null
     * if the data type in the source data tag is not known or the conversion
     * to that data type failed.
     */
    public static Object convert(final ISourceDataTag tag, final String value) {
        int dataType = tag.getDataTypeNumeric();
        Object result;

        switch (dataType) {
        case TagDataType.TYPE_INTEGER:
            try {
                result = Integer.valueOf(Integer.parseInt(value));
            } catch (NumberFormatException ex) {
                result = null;
            } catch (Exception ex) {
                result = null;
            }
            break;

        case TagDataType.TYPE_LONG:
            try {
                result = Long.valueOf(Long.parseLong(value));
            } catch (NumberFormatException ex) {
                result = null;
            } catch (Exception ex) {
                result = null;
            }
            break;

        case TagDataType.TYPE_FLOAT:
            try {
                result = new Float(Float.parseFloat(value));
            } catch (NumberFormatException ex) {
                result = null;
            } catch (Exception ex) {
                result = null;
            }
            break;

        case TagDataType.TYPE_DOUBLE:
            try {
                result = new Double(Double.parseDouble(value));
            } catch (NumberFormatException ex) {
                result = null;
            } catch (Exception ex) {
                result = null;
            }
            break;

        case TagDataType.TYPE_BOOLEAN:
            try {
                if (value.equalsIgnoreCase("true")) {
                    result = Boolean.valueOf(true);
                } else if (value.equalsIgnoreCase("false")) {
                    result = Boolean.valueOf(false);
                } else if (Float.parseFloat(value) == 0.0f) {
                    result = Boolean.valueOf(false);
                } else if (Float.parseFloat(value) == 1.0f) {
                    result = Boolean.valueOf(true);
                } else {
                    result = null;
                }
            } catch (Exception ex) {
                result = null;
            }

            break;

        case TagDataType.TYPE_STRING:
            result = value;
            break;
        default:
            result = null;
            break;

        }
        return result;
    }
    
    /**
     * The conversion method to convert Number object to the type of
     * the data tag.
     * 
     * @param tag SourceDataTag to get the data type from.
     * @param value The value to convert.
     * @return The converted object with the correct data type or null
     * if the data type in the source data tag is not known or the conversion
     * to that data type failed.
     */
   public static <T extends Number> Object convert(final ISourceDataTag tag, final T value) {

        if (value instanceof Byte) {
           return convert(tag, value.byteValue());   
        }
       
        if (value instanceof Short) {
           return convert(tag, value.shortValue());   
        }

        if (value instanceof Double) {
            return convert(tag, value.doubleValue());   
        }

        if (value instanceof Float) {
            return convert(tag, value.floatValue());   
        }
        
        if (value instanceof Integer) {
            return convert(tag, value.intValue());   
        }

        if (value instanceof Long) {
            return convert(tag, value.longValue());   
        }

        if (value instanceof Float) {
            return convert(tag, value);   
        }

        return null;
   }
    
}

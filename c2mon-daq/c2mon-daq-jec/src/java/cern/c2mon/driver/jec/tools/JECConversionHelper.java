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
package cern.c2mon.driver.jec.tools;

import cern.c2mon.shared.common.datatag.address.PLCHardwareAddress;
/**
 * This static helper class allows to convert between the data representation
 * of the PLC and the data representation in java.
 * 
 * @author Andreas Lang
 *
 */
public final class JECConversionHelper {
    
    /**
     * Private constructor there should be no instances of this class.
     */
    private JECConversionHelper() {
        
    }
    
    /**
     * This is a table with the resolution factor parameters. Until now, we have
     * just 2 resolution factors but, if needed, we can add some more and the
     * DAQ will work the same way. The actual table has the following structure:
     * ARRAY_LINE | SHRT_MIN | SHRT_MAX
     * ----------------|----------|---------- 
     * 0 | not used | not used 
     * 1 | -32768 | 32767 
     * 2 | 0 | 27648 
     * 3 | 512 | 2560
     * 
     * Be careful the position of the line in the array must match the 
     * configuration value of the resolution factor.
     */
    private static final int[][] RES_FACTOR_TABLE = { 
        { 0, 0 }, 
        { -32768, 32767 }, 
        { 0, 27648 }, 
        { 512, 2560 }, 
        { 0, 2048 }, 
        { -2048, 2048 }, 
        { -27648, 27648 } 
        };

    /**
     * This method is used to check if the SHRT_MIN and SHRT_MAX values from the
     * Resolution Factor Table exist.
     * 
     * @param resFactor - Resolution factor received from the database
     * @return True if resFactor is OK else false
     */
    public static boolean checkSHRTValues(final int resFactor) {
        return RES_FACTOR_TABLE.length > resFactor + 1;
    }
    
    /**
     * This method is used to calculate the Raw Deadband value to be sent to the
     * JECPLC The raw deadband is the conversion result from the Human Readable
     * Values to the PLC Readable Values
     * 
     * @param phMax - Maximum range value in Human Readable Format (ex:
     *        29.5 degrees)
     * @param phMin - Minimum range value in Human Readable Format (ex:
     *        21.5 degrees)
     * @param deadbandValue The deadband to convert.
     * @param resFactor The resolution factor to use.
     * @return short - 16 bit result in PLC Readable Format
     */
    public static short convertDeadbandValueToRawDeadband(final float phMax, 
            final float phMin, final float deadbandValue, final int resFactor) {
        int shrtMin = JECConversionHelper.getSHRTMin(resFactor);
        int shrtMax = JECConversionHelper.getSHRTMax(resFactor);
        // Calculates the range (human readable format)
        float range = phMax - phMin;
        // Calculates raw deadband value
        short rawDB = (short) Math.round((shrtMax - shrtMin) * deadbandValue / range);
        // Return calculated raw deadband value
        return rawDB;
    }
    
    /**
     * Converts a value received from PLC to a java float.
     * 
     * @param value The value to convert.
     * @param hardwareAddress The hardware address to do the right conversion.
     * @return The converted value.
     */
    public static float convertPLCValueToFloat(final int value, 
            final PLCHardwareAddress hardwareAddress) {
        return convertPLCValueToFloat(value, hardwareAddress.getResolutionFactor(), hardwareAddress.getPhysicalMinVal(), hardwareAddress.getPhysicMaxVal());
    }
    
    /**
     * Converts a value received from PLC to a java float.
     * 
     * @param plcValue The value to convert.
     * @param resFactor The resolution factor of the PLC value.
     * @param physicalMin The physical minimum value.
     * @param physicalMax The physical maximum value.
     * @return The converted value.
     */
    public static float convertPLCValueToFloat(final int plcValue, final int resFactor,
            final float physicalMin, final float physicalMax) {
        if (resFactor == 0) {
            return Float.intBitsToFloat(plcValue);
        }
        else {
            return JECConversionHelper.rawPLCToJavaFromat(physicalMax, physicalMin, plcValue, resFactor);
        }
    }
    
    /**
     * Converts a Java float to a PLC float depending on the values defined in 
     * the hardware address.
     * 
     * @param javaValue The java float value to convert.
     * @param hardwareAddress The PLC hardware address to get the setting from.
     * @return The converted int bits.
     */
    public static int convertJavaToPLCValue(final float javaValue, 
            final PLCHardwareAddress hardwareAddress) {
        return convertJavaToPLCValue(javaValue, hardwareAddress.getResolutionFactor(), hardwareAddress.getPhysicalMinVal(), hardwareAddress.getPhysicMaxVal());
    }
    
    /**
     * Converts a Java float to a PLC float depending on the provided values.
     * 
     * @param javaValue The java float value to convert.
     * @param resolutionFactor The resolution factor to convert the float.
     * @param physicalMinVal The physical minimum value.
     * @param physicMaxVal The physical maximum value.
     * @return The converted int bits.
     */
    public static int convertJavaToPLCValue(final float javaValue, final int resolutionFactor, 
            final float physicalMinVal, final float physicMaxVal) {
        if (resolutionFactor == 0) {
            return Float.floatToIntBits(javaValue);
        }
        else {
            return JECConversionHelper.javaToRawPLCFormat(physicMaxVal, physicalMinVal, javaValue, resolutionFactor);
        }
    }


    /**
     * This method is used to calculate the Raw Data value using the value
     * received by TIM The raw data is the measurement converted from Human
     * Readable format to PLC Readable Format This value will be sent up to TIM
     * (ex: JECPLC=0xFA4B)
     * 
     * @param phMax - Maximum range value in Human Readable Format (ex:
     *        29.5 degrees)
     * @param phMin - Minimum range value in Human Readable Format (ex:
     *        21.5 degrees)
     * @param humanReadFormat - Value received from TIM
     * @param resFactor The resolution factor to use for the conversion.
     * @return short - Value converted from Human Readable to PLC format (16
     *         bits)
     */
    private static int javaToRawPLCFormat(final float phMax, final float phMin,
            final float humanReadFormat, final int resFactor) {
        int shrtMin = JECConversionHelper.getSHRTMin(resFactor);
        int shrtMax = JECConversionHelper.getSHRTMax(resFactor);
        // Calculates the slope (increment step)
        float slope = (phMax - phMin) / (shrtMax - shrtMin);
        // Calculates the offset (start point)
        float offset = phMin - (slope * shrtMin);
        // Value received from TIM minus offset adapted to slope
        int rawData = Math.round((humanReadFormat - offset) / slope);
        // Returns calculated value
        return rawData;
    }
    
    /**
     * This method is used to calculate the Human Readable format value using
     * the value received by JECPLC. The raw data is the measurement converted
     * from PLC Readable Format to Human Readable format. This value will be
     * sent up to TIM (ex: 25.4 degrees)
     * 
     * @param phMax - Maximum range value in Human Readable Format (ex:
     *        29.5 degrees)
     * @param phMin - Minimum range value in Human Readable Format (ex:
     *        21.5 degrees)
     * @param rawData - Value received from PLC (WORD)
     * @param resFactor The resolution factor to use for the conversion.
     * @return float - Value converted from PLC format to Human Readable format
     */
    private static float rawPLCToJavaFromat(final float phMax, final float phMin, 
            final int rawData, final int resFactor) {
        int shrtMin = JECConversionHelper.getSHRTMin(resFactor);
        int shrtMax = JECConversionHelper.getSHRTMax(resFactor);
        // Calculates the slope (increment step)
        float slope = (phMax - phMin) / (shrtMax - shrtMin);
        // Calculates the offset (start point)
        float offset = phMin - (slope * shrtMin);
        // Value received from PLC times the calculated scale factor
        float result = rawData * slope + offset;
        // Returns calculated value
        return result;
    }
    
    /**
     * Looks for the SHRT max value int the table and returns it.
     * 
     * @param resFactor The resolution factor to use to search in the table.
     * @return The desired SHRT max value.
     */
    private static int getSHRTMax(final int resFactor) {
        int shrtMax = RES_FACTOR_TABLE[resFactor][1];
        return shrtMax;
    }

    /**
     * Looks for the SHRT max value int the table and returns it.
     * 
     * @param resFactor The resolution factor to use to search in the table.
     * @return The desired SHRT max value or.
     */
    private static int getSHRTMin(final int resFactor) {
        int shrtMin = RES_FACTOR_TABLE[resFactor][0];
        return shrtMin;
    }
    
}

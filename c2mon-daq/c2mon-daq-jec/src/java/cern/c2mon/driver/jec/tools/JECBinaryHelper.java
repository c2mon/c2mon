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

/**
 * This class helps to access parts of a byte array supplied from the
 * PLCDricer. It also allows binary masking operations to encode the
 * filtering type inside a value.
 * 
 * @author Andreas Lang
 *
 */
public final class JECBinaryHelper {
    /**
     * The number to subtract from a small word (inside an integer)
     * to get the same value as integer.</br>
     * Example for 2 byte small word with value -1: 0xFFFF</br>
     * inside an integer: 0x0000FFFF (positive integer)</br>
     * 0x0000FFFF - 0x00010000 is the same as 0x0000FFFF + 0xFFFF0000 (Two's complement)</br>
     * result: 0xFFFFFFFF which is also an integer -1
     */
    private static final int SMALL_WORD_SWITCH = 0x10000;
    /**
     * The number of bytes of a small word.
     */
    private static final int SMALL_WORD_SIZE = 2;
    /**
     * This is the smallest possible value for a small word.
     */
    private static final int SMALL_WORD_MIN_VALUE = 0x8000;
    /**
     * This is the value representing -1 for a small word.
     */
    private static final int SMALL_WORD_MINUS_ONE = 0xFFFF;
    /**
     * The number of bytes of a big word.
     */
    private static final int BIG_WORD_SIZE = 4;
    /**
     * Number of bits to shift for a byte.
     */
    private static final int BYTE_SIZE = 8;
    /**
     * Static helper class. There should be no instances.
     */
    private JECBinaryHelper() {
    }
    
    /**
     * Gets a word (float 32 bit) from the provided array as integer bits.
     * 
     * @param word The position of the word.
     * @param srcArray The array to get the word from.
     * @return The word at this position.
     */
    public static int getAnalogIEEEWord(final int word, final byte[] srcArray) {
        return getWord(word, srcArray, BIG_WORD_SIZE);
    }

    /**
     * Gets a word from the provided array at the provided position.
     * The word is returned as integer bits. The length of the word is 
     * provided in number of bytes.
     * 
     * @param word The position of the word.
     * @param srcArray The array to look in.
     * @param numBytes The number of bytes to put into the word.
     * @return The word at the provided position.
     */
    private static int getWord(final int word, final byte[] srcArray, final int numBytes) {
        int actWord = 0;
        for (int i = 0; i < numBytes - 1; i++) {
            actWord = (actWord | byteToIntUnsigned(srcArray[word * 2 + i])) << BYTE_SIZE;
        }
        actWord = actWord | byteToIntUnsigned(srcArray[word * 2 + numBytes - 1]);
        return actWord;
    }
    
    /**
     * Gets a analog word (float 16 bit) from the provided array as integer bits.
     * 
     * @param wordPos The position of the word.
     * @param srcArray The source array to get the word from.
     * @return The word at this position.
     */
    public static int getAnalogWord(final int wordPos, final byte[] srcArray) {
        int result = getWord(wordPos, srcArray, SMALL_WORD_SIZE);
        /*
         *  Makes sure that if the small word bytes of the integer 
         *  represent a negative value the returned integer is the same 
         *  negative value.
         *  TODO would it not be easier to just check the most significant bit of the word?
         */
        if (result >> (SMALL_WORD_SIZE * BYTE_SIZE - 1) == 1) {
//        if ((result >= SMALL_WORD_MIN_VALUE) && (result <= SMALL_WORD_MINUS_ONE)) {
            result = result - SMALL_WORD_SWITCH;
        }
        return result;
    }

    /**
     * Gets a boolean word (16 bit) from the provided array.
     * 
     * @param wordPos The position of the word.
     * @param srcArray The array to get the word from.
     * @return The word to return.
     */
    public static int getBooleanWord(final int wordPos, final byte[] srcArray) {
        return getWord(wordPos, srcArray, SMALL_WORD_SIZE);
    }
    
    /**
     * This method is used to write a IEEE analog value (32 bit) inside an array 
     * in the position given by the wordID.
     * 
     * @param array Destination array where the value should be written.
     * @param ieeeValue Value to be stored in the array.
     * @param wordPos The position of the word.
     */
    public static void putIEEEAnalogValueIntoArray(final byte[] array, final int ieeeValue, final int wordPos) {
        putValueIntoArray(array, ieeeValue, wordPos, BIG_WORD_SIZE);
    }

    /**
     * This method is used to write an integer value in byte parts into a byte array.
     * The last bytes of the integer value are used. The number is provided with
     * numBytes.
     * 
     * @param array The array to put the value in.
     * @param value The value to put into the array.
     * @param wordPos The position of the word inside the array. (word length is
     * always 16)
     * @param numBytes The number of bytes to insert.
     */
    private static void putValueIntoArray(final byte[] array, final int value, final int wordPos, final int numBytes) {
        for (int i = 0; i < numBytes; i++) {
            array[(wordPos * 2) + (numBytes - (i + 1))] = (byte) (value >> BYTE_SIZE * i);
        }
    }
    
    /**
     * This method is used to write a analog value (16 bit) inside an array in 
     * the position given by the wordID.
     * 
     * @param array Destination table where the values should be written.
     * @param value Value to be stored in the array.
     * @param wordPosition The position of the word.
     */
    public static void putAnalogValueIntoArray(final byte[] array, final short value, final int wordPosition) {
        putValueIntoArray(array, value, wordPosition, SMALL_WORD_SIZE);
    }
    
    /**
     * This method is used to mask the raw value with the absolute filtering type. 
     * This mask is made, putting bit 15=0 and bit 14=0
     * 
     * @param value - Calculated raw value
     * @return short - Value with the right masking value
     */
    public static short maskAbsoluteFilteringType(final short value) {
        if ((value & 0xC000) != 0x0000) 
          throw new IllegalArgumentException("Value deadband is either too large or negative and cannot be masked correctly.");
        short result = (short) (value & 0x3FFF);
        return result;
    }
    
    /**
     * This method is used to mask the raw value with the relative filtering type. 
     * This mask is made, putting bit 15=1 and bit 14=0
     * 
     * @param value - Calculated raw value
     * @return short - Value with the right masking value
     */
    public static short maskRelativeFilteringType(final short value) {
        if ((value & 0xC000) != 0x0000) 
          throw new IllegalArgumentException("Value deadband is either too large or negative and cannot be masked correctly.");
        short result = (short) (value | 0x8000);
        result = (short) (result & 0xBFFF);
        return result;
    }
    
    /**
     * This method is used to mask the deadband value with the relative
     * filtering type. This mask is made by: Put bit 31=1 and bit 30=1
     * 
     * @param value Deadband value in IEEE Float format
     * @return Value with the right masking value.
     */
    public static int maskIEEERelativeFilteringType(final int value) {
        int result = (int) ((value >> 1) | 0xC0000000);
        return result;
    }
    
    /**
     * This method is used to mask the deadband value with the absolute
     * filtering type. This mask is made by: Put bit 31=0 and bit 30=1
     * 
     * @param value Deadband value in IEEE Float format.
     * @return Value with the right masking value.
     */
    public static int maskIEEEAbsoluteFilteringType(final int value) {
        int result = (int) ((value >> 1) | 0x40000000);
        return result;
    }
    
    /**
     * Converts a normal java (signed) byte to an integer ignoring the
     * sign. This is necessary to avoid the higher byte messing up the
     * lower byte (if negative).
     * 
     * Example:
     * byte -1 (0xFF) would equal integer -1 (0xFFFF) if casted or used 
     * inside a binary operation. To arrange our bytes correctly this
     * methods does the following:
     * byte -1 (0xFF) -> integer 255 (0x00FF)
     * 
     * 
     * @param signedByte The signed byte.
     * @return The matching integer.
     */
    public static int byteToIntUnsigned(final byte signedByte) {
        return signedByte & 0x00FF;
    }

}

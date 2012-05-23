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
package ch.cern.tim.driver.jec.frames;

import cern.tim.driver.common.EquipmentLogger;
import cern.tim.driver.common.IEquipmentMessageSender;
import cern.tim.driver.tools.TIMDriverSimpleTypeConverter;
import cern.tim.shared.common.datatag.DataTagDeadband;
import cern.tim.shared.common.datatag.address.PLCHardwareAddress;
import cern.tim.shared.daq.datatag.ISourceDataTag;
import cern.tim.shared.daq.datatag.SourceDataQuality;
import ch.cern.tim.driver.jec.PLCObjectFactory;
import ch.cern.tim.driver.jec.address.AnalogJECAddressSpace;
import ch.cern.tim.driver.jec.tools.JECBinaryHelper;
import ch.cern.tim.driver.jec.tools.JECConversionHelper;
import ch.cern.tim.jec.StdConstants;

/**
 * Data processor for all analog frames (which means containing 16 and 32 Bit floats)
 * 
 * @param <T> The class of the used address space.
 * @author Andreas Lang
 *
 */
public class AnalogDataProcessor<T extends AnalogJECAddressSpace> extends AbstractDataProcessor {
    /**
     * Divisor to get from an percentage value to a decimal number.
     */
    private static final int PERCENTAGE_DIVISOR = 100;
    /**
     * Flag which indicates if a filter should be used for the data or not.
     */
    private boolean filterOn;
    /**
     * Address space of this data processor.
     */
    private T analogJecAddressSpace;
    /**
     * Creates a new analog data processor.
     * 
     * @param supervisedMessagesId The id of the supervised JECPFrames.
     * @param analogJecAddressSpace The JEC address space.
     * @param plcFactory The PLC object factory.
     * @param filterOn If true the messages will be filtered before forwarding them
     * to the core.
     * @param equipmentMessageSender The equipment message sender to send messages 
     * via the core to the server.
     * @param equipmentLogger The equipment logger to use.
     */
    public AnalogDataProcessor(final int supervisedMessagesId,
            final T analogJecAddressSpace,
            final PLCObjectFactory plcFactory, final boolean filterOn, 
            final IEquipmentMessageSender equipmentMessageSender, 
            final EquipmentLogger equipmentLogger) {
        super(supervisedMessagesId, plcFactory, equipmentMessageSender, equipmentLogger);
        this.filterOn = filterOn;
        this.analogJecAddressSpace = analogJecAddressSpace;
    }

    /**
     * Detects the changes between the current and the last array and
     * sends according to that changes messages to the server.
     * 
     * @param blockNumber The number of the block to check.
     * @param timestamp The timestamp to send with the update.
     */
    @Override
    public void detectAndSendArrayChanges(final int blockNumber, final long timestamp) {
        int startWord = blockNumber * (StdConstants.JEC_DATA_SIZE / 2);
        int endWord = (blockNumber + 1) * (StdConstants.JEC_DATA_SIZE / 2);
        for (int wordId = startWord; wordId < endWord; wordId++) {
            int actWord = 0;
            int lastWord = 0;
            actWord = getWord(wordId);
            lastWord = getWord(wordId, getLastValues());
            // Checks if this word has changed (first filtering)
            if (actWord != lastWord) {
                getEquipmentLogger().debug("WORD " + wordId + " HAS CHANGED...");
                // Send the changed byte to TIM (-1 is the bitID for
                // analogs)
                ISourceDataTag sourceDataTag = getTag(wordId, -1);
                if (sourceDataTag == null)
                    getEquipmentLogger().debug("No source datatag found for word " + wordId + " - the change will not be propagated to the server.");
                else if (filterOn)
                    sendFiltered(actWord, lastWord, sourceDataTag, timestamp);
                else
                    convertAndSend(actWord, sourceDataTag, timestamp, false);
            }
        }
    }
    
    /**
     * Gets a IEEE word/float (32 bit) as integer bits from the current values.
     * 
     * @param wordPos The position of the word.
     * @return The word at this position.
     */
    public int getAnalogIEEEWord(final int wordPos) {
        return JECBinaryHelper.getAnalogIEEEWord(wordPos, getCurrentValues());
    }
    
    /**
     * Gets a word/float (16 bit) as integer bits from the current values.
     * 
     * @param wordPos The position of the word.
     * @return The word at the provided position.
     */
    public int getAnalogWord(final int wordPos) {
        return JECBinaryHelper.getAnalogWord(wordPos, getCurrentValues());
    }
    
    /**
     * Gets a word as integer bits from the current values. The number of bits
     * to get from the current values array is determined with the resolution
     * factor of the matching tag. If there is no tag -1 is returned.
     * 
     * @param wordPos The position of the word.
     * @return The word at the provided position or -1 if the tag type could not be determined.
     */
    public int getWord(final int wordPos) {
        return getWord(wordPos, getCurrentValues());
    }
    
    /**
     * Gets a word as integer bits from the current values. The number of bits
     * to get from the source array is determined with the resolution
     * factor of the matching tag. If there is no tag the 16 bit word is returned.
     * 
     * @param wordPos The position of the word.
     * @param srcArray The array to get the word from.
     * @return The word at the provided position. If the tag type could not be 
     * determined a 16 bit word is returned.
     */
    public int getWord(final int wordPos, final byte[] srcArray) {
        int word;
        ISourceDataTag sourceDataTag = getTag(wordPos, -1);
        if (sourceDataTag != null) {
            PLCHardwareAddress plcHardwareAddress = (PLCHardwareAddress) sourceDataTag.getHardwareAddress();
            if (plcHardwareAddress.getResolutionFactor() == 0) {
                word = JECBinaryHelper.getAnalogIEEEWord(wordPos, srcArray);
                getEquipmentLogger().trace("Found IEEE Float (32bit) tag for WORD ID:" + wordPos);
            }
            else {
                word = JECBinaryHelper.getAnalogWord(wordPos, srcArray);
                getEquipmentLogger().trace("Found Float (16bit) tag for WORD ID:" + wordPos);
            }
        }
        else {
            getEquipmentLogger().trace("No tag found for word at position " + wordPos + " - returning 16 bit value.");
            word = JECBinaryHelper.getAnalogWord(wordPos, srcArray);
        }
        return word;
    }
    
    /**
     * Converts the int bits of the word to a tim value.
     * 
     * @param actWord The word to convert.
     * @param sourceDataTag The data tag to use as configuration.
     * @param timestamp The timestamp the to send with the update.
     * @param revalidate If true the tag is revalidated if it is invalid.
     */
    public void convertAndSend(final int actWord, final ISourceDataTag sourceDataTag, 
            final long timestamp, final boolean revalidate) {
        PLCHardwareAddress hardwareAddress = (PLCHardwareAddress) sourceDataTag.getHardwareAddress();
        if (JECConversionHelper.checkSHRTValues(hardwareAddress.getResolutionFactor())) {
            float floatValue = JECConversionHelper.convertPLCValueToFloat(actWord, hardwareAddress);
            Object timValue = TIMDriverSimpleTypeConverter.convert(
                    sourceDataTag, floatValue);
            if (timValue != null) {
                if (revalidate)
                    revalidate(timValue, sourceDataTag, timestamp);
                else
                    send(timValue, sourceDataTag, timestamp);
                getEquipmentLogger().debug("ANALOG DATA TAG VALUE SENT: " + sourceDataTag.getName() + " ID:" + sourceDataTag.getId());
            } else {
                getEquipmentLogger().debug("\tSending INVALIDATE SourceDataTagValue with quality CONVERSION_ERROR, for Tag name : " + sourceDataTag.getName() + " tag id : " + sourceDataTag.getId());
                sendInvalid(sourceDataTag, SourceDataQuality.CONVERSION_ERROR, null, timestamp);
            }
        }
    }

    /**
     * Sends a value after the filters are applied.
     * 
     * @param value The value to send.
     * @param lastValue The value before the last update.
     * @param sourceDataTag The source data tag with the configuration.
     * @param timestamp The timestamp to send with the update.
     */
    public void sendFiltered(final int value, final int lastValue, 
            final ISourceDataTag sourceDataTag, final long timestamp) {
        PLCHardwareAddress hardwareAddress = (PLCHardwareAddress) sourceDataTag.getHardwareAddress();
        if (JECConversionHelper.checkSHRTValues(hardwareAddress.getResolutionFactor())) {
            float convertedValue = JECConversionHelper.convertPLCValueToFloat(value, hardwareAddress);
            float convertedLastValue = JECConversionHelper.convertPLCValueToFloat(lastValue, hardwareAddress);
            if (getEquipmentLogger().isDebugEnabled()) {
                getEquipmentLogger().debug("ACT VAL FROM TABLE: " + value);
                getEquipmentLogger().debug("ACT HRF: " + convertedValue);
                getEquipmentLogger().debug("LAST VAL FROM TABLE: " + lastValue);
                getEquipmentLogger().debug("LAST HRF: " + convertedLastValue);
            }
            if (!isChangeOutOfRange(value, lastValue, sourceDataTag.getValueDeadbandType(), sourceDataTag.getValueDeadband())) {
                Object timValue = TIMDriverSimpleTypeConverter.convert(
                        sourceDataTag, convertedValue);
                if (timValue != null) {
                    send(timValue, sourceDataTag, timestamp);
                    getEquipmentLogger().debug("ANALOG DATA TAG VALUE SENT: " + sourceDataTag.getName() + " ID:" + sourceDataTag.getId());
                } else {
                    getEquipmentLogger().debug("\tSending INVALIDATE SourceDataTagValue with quality CONVERSION_ERROR, for Tag name : " + sourceDataTag.getName() + " tag id : " + sourceDataTag.getId());
                    sendInvalid(sourceDataTag, SourceDataQuality.CONVERSION_ERROR, null, timestamp);
                }
                
            }
        }
        else {
            getEquipmentLogger().debug("Value Change inside range...ignoring data change");
        }
    }

    /**
     * This method is used to check if the analog value is really out of the
     * deadband range. If it is, this function returns true, else returns false.
     * 
     * @param actVal - Actual measurement value (in human readable format)
     * @param lastVal - Last measurement value (in human readable format)
     * @param dbType - Type of deadband filtering used (3:absolute; 4:relative - see 
     * DataTagDeadband constants)
     * @param dbValue - Deadband value received for comparing
     * @return Boolean - TRUE if its out of range; otherwise FALSE
     */
    public boolean isChangeOutOfRange(final float actVal, final float lastVal, 
            final short dbType, final float dbValue) {
        float valueDifference = 0;
        boolean isOutOfRange = false;
        if (dbType == DataTagDeadband.DEADBAND_EQUIPMENT_ABSOLUTE) {
            valueDifference = Math.abs(actVal - lastVal);
            isOutOfRange = (valueDifference > dbValue);
        }
        else if (dbType == DataTagDeadband.DEADBAND_EQUIPMENT_RELATIVE) {
            valueDifference = Math.abs(actVal - lastVal);
            isOutOfRange = (valueDifference > (lastVal * (dbValue / PERCENTAGE_DIVISOR)));
        }
        return isOutOfRange;
    }
    
    /**
     * Sends the tag with the specified word and bit position to the server.
     * 
     * @param wordPosition The position of the word to send.
     * @param bitPosition The position of the bit to send.
     * analog values don't use that so it is ignored when searching for
     * the tag.
     */
    @Override
    public void sendTag(final int wordPosition, final int bitPosition) {
        int word = getWord(wordPosition);
        convertAndSend(word, getTag(wordPosition, -1), System.currentTimeMillis(), false);
    }
    
    /**
     * Revalidates the tag with the specified word and bit position to the server.
     * 
     * @param wordPosition The position of the word to send.
     * @param bitPosition The position of the bit to send.
     * analog values don't use that so it is ignored when searching for
     * the tag.
     */
    @Override
    public void revalidateTag(final int wordPosition, final int bitPosition) {
        int word = getWord(wordPosition);
        convertAndSend(word, getTag(wordPosition, -1), System.currentTimeMillis(), true);
    }

    /**
     * Gets the JEC address space.
     * 
     * @return The JEC address space.
     */
    @Override
    public T getJecAddressSpace() {
        return analogJecAddressSpace;
    }
    
    /**
     * Makes sure that for analog values never the bit id is used in the tag key.
     * 
     * @param wordId The word id.
     * @param bitId The bit id - not used.
     * @return The tag key of this word bit combination.
     */
    @Override
    public String getTagKey(final int wordId, final int bitId) {
        return super.getTagKey(wordId, -1);
    }

}

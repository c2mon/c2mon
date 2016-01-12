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
package cern.c2mon.daq.jec.frames;

import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.jec.PLCObjectFactory;
import cern.c2mon.daq.jec.address.AnalogJECAddressSpace;
import cern.c2mon.daq.jec.plc.StdConstants;
import cern.c2mon.daq.jec.tools.JECBinaryHelper;
import cern.c2mon.daq.jec.tools.JECConversionHelper;
import cern.c2mon.shared.common.datatag.DataTagDeadband;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataQuality;
import cern.c2mon.shared.common.datatag.address.PLCHardwareAddress;

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
                    sendFiltered(actWord, sourceDataTag, timestamp);
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
            Object timValue = Float.valueOf(floatValue);
            if (revalidate) {
              revalidate(timValue, sourceDataTag, timestamp);
            } 
            else {
              send(timValue, sourceDataTag, timestamp);
            }
            getEquipmentLogger().debug("ANALOG DATA TAG VALUE SENT: " + sourceDataTag.getName() + " ID:" + sourceDataTag.getId());
        }
    }

    /**
     * Sends a value after the filters are applied.
     * 
     * @param value The value to send.
     * @param sourceDataTag The source data tag with the configuration.
     * @param timestamp The timestamp to send with the update.
     */
    public void sendFiltered(final int value, final ISourceDataTag sourceDataTag, final long timestamp) {
        PLCHardwareAddress hardwareAddress = (PLCHardwareAddress) sourceDataTag.getHardwareAddress();
        if (JECConversionHelper.checkSHRTValues(hardwareAddress.getResolutionFactor())) {
            float floatValue = JECConversionHelper.convertPLCValueToFloat(value, hardwareAddress);
            if (getEquipmentLogger().isTraceEnabled()) {
              getEquipmentLogger().trace("INCOMING VAL FROM TABLE: " + value);
              getEquipmentLogger().trace("INCOMING HRF: " + floatValue);                              
          }
            if (isChangeOutOfDeadband(Float.valueOf(floatValue), sourceDataTag)) {              
                Object timValue = Float.valueOf(floatValue);                
                if (timValue != null) {                    
                    send(timValue, sourceDataTag, timestamp);
                    getEquipmentLogger().debug("ANALOG DATA TAG VALUE SENT: " + sourceDataTag.getName() + " ID:" + sourceDataTag.getId());
                } 
                else {
                    getEquipmentLogger().debug("\tSending INVALIDATE SourceDataTagValue with quality CONVERSION_ERROR, for Tag name : " + sourceDataTag.getName() + " tag id : " + sourceDataTag.getId());
                    sendInvalid(sourceDataTag, SourceDataQuality.CONVERSION_ERROR, "Error when converting value in JEC DAQ.", timestamp);
                }
                
            } else {
              getEquipmentLogger().debug("Value Change inside range...ignoring data change");
            }
        }
        else {
          getEquipmentLogger().warn("Resolution factor not recognized for tag " + sourceDataTag.getId() + " - unable to process update.");          
        }
    }

    /**
     * This method is used to check if the analog value is really out of the
     * deadband range. If it is, this function returns true, else returns false.
     * 
     * <p>If the sourceDataTag value is currently null or invalid, returns true. 
     * 
     * @param actVal - Actual measurement value (in human readable format)
     * @param lastVal - Last measurement value (in human readable format)
     * @param dbType - Type of deadband filtering used (3:absolute; 4:relative - see 
     * DataTagDeadband constants)
     * @param dbValue - Deadband value received for comparing
     * @return Boolean - TRUE if its out of range; otherwise FALSE
     * @throws NullPointerException if value argument is null
     */
    public <T extends Number> boolean isChangeOutOfDeadband(final T actVal, ISourceDataTag sourceDataTag) {
        if (actVal == null){
          throw new NullPointerException("Called with null value parameter.");
        }
        int deadbandType = sourceDataTag.getValueDeadbandType();
        float deadbandValue = sourceDataTag.getValueDeadband();
        float valueDifference = 0;
        boolean isOutOfDeadband = true;
        if (sourceDataTag.getCurrentValue() != null && sourceDataTag.getCurrentValue().isValid() && sourceDataTag.getCurrentValue().getValue() != null ) {
          Float lastVal = Float.valueOf(sourceDataTag.getCurrentValue().getValue().toString());
          if (deadbandType == DataTagDeadband.DEADBAND_EQUIPMENT_ABSOLUTE) {
              valueDifference = Math.abs(actVal.floatValue() - lastVal.floatValue());
              isOutOfDeadband = (valueDifference > deadbandValue);
          }
          else if (deadbandType == DataTagDeadband.DEADBAND_EQUIPMENT_RELATIVE) {
              valueDifference = Math.abs(actVal.floatValue() - lastVal.floatValue());
              isOutOfDeadband = (valueDifference > (lastVal.floatValue() * (deadbandValue / PERCENTAGE_DIVISOR)));
          }
        }        
        return isOutOfDeadband;
    }
    
    /**
     * Sends the tag with the specified word and bit position to the server.
     *      
     * @param wordPosition The position of the word to send.
     * @param bitPosition The position of the bit to send.
     * analog values don't use that so it is ignored when searching for
     * the tag.
     * @throws NullPointerException if no tag is associated with the word position
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
     * analog values don't use that so it is ignored when searching for     * 
     * the tag.
     * @param sourceTimestamp The timestamp when the request arrived.
     * @throws NullPointerException if no tag is associated with the word position
     */
    @Override
    public void revalidateTag(final int wordPosition, final int bitPosition, final long sourceTimestamp) {
        int word = getWord(wordPosition);
        convertAndSend(word, getTag(wordPosition, -1), sourceTimestamp, true);
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

    @Override
    public void sendAllInBlock(int blockNumber) {
      int startWord = blockNumber * (StdConstants.JEC_DATA_SIZE / 2);
      int endWord = (blockNumber + 1) * (StdConstants.JEC_DATA_SIZE / 2);
      for (int wordId = startWord; wordId < endWord; wordId++) {
        if (getTag(wordId, -1) != null)
          sendTag(wordId, 0); //bit not used here
      }      
    }

}

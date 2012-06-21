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

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cern.tim.driver.common.EquipmentLogger;
import cern.tim.driver.common.IEquipmentMessageSender;
import cern.tim.shared.common.datatag.address.PLCHardwareAddress;
import cern.tim.shared.daq.datatag.ISourceDataTag;
import cern.tim.shared.daq.datatag.SourceDataQuality;
import ch.cern.tim.driver.jec.JECMessageHandler;
import ch.cern.tim.driver.jec.PLCObjectFactory;
import ch.cern.tim.driver.jec.address.AbstractJECAddressSpace;
import ch.cern.tim.jec.JECPFrames;
import ch.cern.tim.jec.StdConstants;

/**
 * Superclass for all data processors. It handles the basics control of the
 * thread and gives some helper methods for implementing classes.
 * 
 * @author Andreas Lang
 * 
 */
public abstract class AbstractDataProcessor extends AbstractJECPFrameProcessor {
    /**
     * The values before the last update.
     */
    private byte[] lastValues;
    /**
     * The current values.
     */
    private byte[] currentValues;
    
    /**
     * Have the initial values been sent to the server.
     */
    private ConcurrentHashMap<Integer, Boolean> initialValuesSent = new ConcurrentHashMap<Integer, Boolean>();
    
    /**
     * The equipment message sender used to send updates to the server.
     */
    private IEquipmentMessageSender equipmentMessageSender;
    /**
     * The tags which are supervised from this data processor.
     */
    private final Map<String, ISourceDataTag> supervisedTags = new ConcurrentHashMap<String, ISourceDataTag>();
    
    /**
     * AbstractDataProcessor constructor called from subclasses.
     * 
     * @param supervisedMessagesId
     *            The message id of the JECPFrames which should be processed.
     * @param plcFactory 
     *            The factory to create connection objects to communicate with
     *            the PLC.
     * @param equipmentMessageSender
     *            The equipment message sender to send updates to the server.
     * @param equipmentLogger
     *            The equipment logger to log equipment specific messages.
     */
    public AbstractDataProcessor(final int supervisedMessagesId, 
            final PLCObjectFactory plcFactory, 
            final IEquipmentMessageSender equipmentMessageSender, 
            final EquipmentLogger equipmentLogger) {
        super(supervisedMessagesId, plcFactory, equipmentLogger);
        this.equipmentMessageSender = equipmentMessageSender;
    }

    /**
     * Subclasses shall check the data at the provided block number in the
     * current values and compare it to the data in the last values.
     * 
     * @param blockNumber
     *            The number of the data block to check.
     * @param timestamp
     *            The timestamp when the data arrived.
     */
    public abstract void detectAndSendArrayChanges(final int blockNumber, final long timestamp);

    /**
     * Sends the value of the provided word and in case of a boolean value at
     * the provided bit.
     * 
     * @param wordPos
     *            The position of the word to send.
     * @param bitPos
     *            The position of the bit to send.
     */
    public abstract void sendTag(final int wordPos, final int bitPos);
    
    /**
     * Sends the current values to the server, for all tags that are contained in this block.
     * 
     * @param blockNumber the number of the data block
     */
    public abstract void sendAllInBlock(final int blockNumber);
    
    /**
     * Revalidates the value of the provided word and in case of a boolean value at
     * the provided bit.
     * 
     * @param wordPos
     *            The position of the word to send.
     * @param bitPos
     *            The position of the bit to send.
     */
    public abstract void revalidateTag(final int wordPos, final int bitPos);

    /**
     * Re-initializes all arrays in the processor. 
     */
    public void initArrays() {
        AbstractJECAddressSpace addressSpace = getJecAddressSpace();
        if (!addressSpace.isEmpty()) {
            initialValuesSent = new ConcurrentHashMap<Integer, Boolean>();
            currentValues = new byte[addressSpace.getJavaByteArraySize()];
            lastValues = new byte[addressSpace.getJavaByteArraySize()];
        }
    }

    /**
     * Returns the current values.
     * 
     * @return The current value array.
     */
    public byte[] getCurrentValues() {
        return currentValues;
    }

    /**
     * Returns the last values.
     * 
     * @return The last values.
     */
    public byte[] getLastValues() {
        return lastValues;
    }

    /**
     * Processes the provided JECPFrames.
     * 
     * @param jecpFrames The frame to process.
     */
    @Override
    public void processJECPFrame(final JECPFrames jecpFrames) {
        try {
            copyJECDataToArray(jecpFrames);
            int blockNumber = jecpFrames.GetDataStartNumber();
            if (initialValuesSent.get(blockNumber) != null && initialValuesSent.get(blockNumber)) {
              detectAndSendArrayChanges(blockNumber, jecpFrames.GetJECCurrTimeMilliseconds());
            } else {
              getEquipmentLogger().debug("Sending initial values for block " + blockNumber);
              sendAllInBlock(blockNumber);
              initialValuesSent.put(blockNumber, true);
            }            
            // TODO Do the replacement while checking for updates.
            copyCurrentValueToLastValues();
        } catch (ArrayIndexOutOfBoundsException indexOutOfBoundsException) {
            getEquipmentLogger().error("Array length: " + currentValues.length + " - There is a serious configuration error with the tags of block type " + getSupervisedMessagesId(),
                    indexOutOfBoundsException);
        }
    }

    /**
     * Copies the data of the JECFrame to the curren value array.
     * 
     * @param jecpFrames
     *            The JECPFrame to use.
     */
    private void copyJECDataToArray(final JECPFrames jecpFrames) {
        int blockID = jecpFrames.GetDataStartNumber();
        int dataOffset = jecpFrames.GetDataOffset();
        if (dataOffset != 0) {
            byte[] jecData = jecpFrames.GetJECData(dataOffset);
            getEquipmentLogger().debug("Copying JEC DATA...");
            getEquipmentLogger().debug("Data received from PLC: " + arrayToString(jecData, 0, jecData.length));
            System.arraycopy(jecData, 0, currentValues, (blockID * StdConstants.JEC_DATA_SIZE), jecData.length);
        }
    }

    /**
     * This method is used to print (in the log or screen) the data from a
     * specific array in hexadecimal format (0x????)
     * 
     * @param array - Array of data to be printed out in HEX format
     * @param startpos - Start position in the array where the data to be
     *        printed is
     * @param length - Offset of data to be printed out
     * @return String - String with the data in HEX format separated with a
     *         space
     */
    public static String arrayToString(final byte[] array, final int startpos, 
            final int length) {
        String tempArray = "";
        for (int k = startpos; k < length; k++) {
            tempArray = tempArray + (" 0x" + Integer.toHexString(array[k]));
        }
        return tempArray;
    }

    /**
     * Sends a value to the server.
     * 
     * @param value
     *            The value to send.
     * @param sourceDataTag
     *            The source data tag the value belongs to.
     * @param timestamp
     *            The timestamp to use.
     */
    public void send(final Object value, final ISourceDataTag sourceDataTag, final long timestamp) {
        PLCHardwareAddress plcHardwareAddress = (PLCHardwareAddress) sourceDataTag.getHardwareAddress();
        if (!"INT999".equalsIgnoreCase(plcHardwareAddress.getNativeAddress()) 
                && sourceDataTag.getCurrentValue() != null 
                && !sourceDataTag.getCurrentValue().isValid()
                && sourceDataTag.getCurrentValue().getQuality().getDescription() != null
                && sourceDataTag.getCurrentValue().getQuality().getDescription().equals(JECMessageHandler.HIERARCHICAL_INVALIDATION_MESSAGE)) {
            getEquipmentLogger().debug("TAG: " + sourceDataTag.getId() + " comes from the DEAD UNIT: " + plcHardwareAddress.getNativeAddress());
        } else {
            getEquipmentLogger().debug("Sending tag value through filter: ID '" + sourceDataTag.getId() + "' value=" + value.toString());
            equipmentMessageSender.sendTagFiltered(sourceDataTag, value, timestamp);
        }
    }
    
    /**
     * Revalidates a value to the server.
     * 
     * @param value
     *            The value to send.
     * @param sourceDataTag
     *            The source data tag the value belongs to.
     * @param timestamp
     *            The timestamp to use.
     */
    public void revalidate(final Object value, final ISourceDataTag sourceDataTag, final long timestamp) {
        equipmentMessageSender.sendTagFiltered(sourceDataTag, value, timestamp);
    }
    
    /**
     * Sends an invalid tag to the server.
     * 
     * @param sourceDataTag
     *            The source data tag to be invalidated.
     * @param qualityCode
     *            The quality code indicating the reason of the invalidation.
     * @param description
     *            A description of the reason for invalidation.
     * @param timestamp
     *            The timestamp to send with the invalidation message.
     */
    public void sendInvalid(final ISourceDataTag sourceDataTag, 
            final short qualityCode, final String description, final long timestamp) {
        equipmentMessageSender.sendInvalidTag(sourceDataTag, qualityCode, description, new Timestamp(timestamp));
    }

    /**
     * Gets the source data tag with the provided word id and bit id or null if
     * there is none.
     * 
     * @param wordId
     *            The word id of the source data tag.
     * @param bitId
     *            The bit id of the source data tag.
     * @return The searched source data tag.
     */
    public ISourceDataTag getTag(final int wordId, final int bitId) {
        return supervisedTags.get(getTagKey(wordId, bitId));
    }

    /**
     * Gets the tag key for a word/bit id combination.
     * 
     * @param wordId
     *            The word id to use.
     * @param bitId
     *            The bit id to use.
     * @return The key for the hash table.
     */
    public String getTagKey(final int wordId, final int bitId) {
        return wordId + ":" + bitId;
    }

    /**
     * This method is used to copy the new values to the last values.
     */
    public void copyCurrentValueToLastValues() {
        System.arraycopy(currentValues, 0, lastValues, 0, currentValues.length);
    }

    /**
     * Adds a source data tag which should be controlled by this data processor.
     * 
     * @param sourceDataTag
     *            The source data tag to add.
     */
    public void addSourceDataTag(final ISourceDataTag sourceDataTag) {
        PLCHardwareAddress plcHardwareAddress = (PLCHardwareAddress) sourceDataTag.getHardwareAddress();
        supervisedTags.put(getTagKey(sourceDataTag), sourceDataTag);
        getJecAddressSpace().updateAddressSpace(plcHardwareAddress);
    }

    /**
     * Gets the JEC address space.
     * 
     * @return The JEC address space.
     */
    public abstract AbstractJECAddressSpace getJecAddressSpace();

    /**
     * Removes a source data tag which should not be controlled by this data
     * processor.
     * 
     * @param sourceDataTag
     *            The source data tag to remove.
     */
    public void removeSourceDataTag(final ISourceDataTag sourceDataTag) {
        supervisedTags.remove(getTagKey(sourceDataTag));
    }

    /**
     * Checks if the source data tag is controlled by this data processor.
     * 
     * @param sourceDataTag
     *            The source data tag to check.
     * @return True if the data tag is controlled by this processor else false.
     */
    public boolean containsSourceDataTag(final ISourceDataTag sourceDataTag) {
        return supervisedTags.containsKey(getTagKey(sourceDataTag));
    }

    /**
     * Removes all supervised data tags from this processor.
     */
    public synchronized void removeAllSupervisedTags() {
        supervisedTags.clear();
        getJecAddressSpace().reset();
    }

    /**
     * Gets the tag key for this source data tag.
     * 
     * @param sourceDataTag
     *            The data tag to get the key for.
     * @return The tag key of this source data tag.
     */
    private String getTagKey(final ISourceDataTag sourceDataTag) {
        PLCHardwareAddress plcHardwareAddress = (PLCHardwareAddress) sourceDataTag.getHardwareAddress();
        return getTagKey(plcHardwareAddress.getWordId(), plcHardwareAddress.getBitId());
    }

    /**
     * Invalidates all tags which belong to the provided unavailable slaves.
     * 
     * @param unavailableSlaves
     *            The unavailable.
     */
    public void invalidateForUnavailableSlaves(final List<String> unavailableSlaves) {
        for (String unavailableSlave : unavailableSlaves) {
            invalidateForUnavailableSlave(unavailableSlave);
        }
    }

    /**
     * Invalidates all tags which belong to the unavailable slave.
     * 
     * @param unavailableSlave
     *            The slave which is unavailable.
     */
    public synchronized void invalidateForUnavailableSlave(final String unavailableSlave) {
        for (ISourceDataTag sourceDataTag : supervisedTags.values()) {
            PLCHardwareAddress plcHardwareAddress = (PLCHardwareAddress) sourceDataTag.getHardwareAddress();
            String nativeAddress = plcHardwareAddress.getNativeAddress();
            if (!"INT999".equalsIgnoreCase(nativeAddress) && nativeAddress.startsWith(unavailableSlave)) {
                getEquipmentLogger().debug("TAG: " + sourceDataTag.getId() + " comes from the DEAD UNIT: " + plcHardwareAddress.getNativeAddress());
                equipmentMessageSender.sendInvalidTag(sourceDataTag, SourceDataQuality.DATA_UNAVAILABLE, JECMessageHandler.HIERARCHICAL_INVALIDATION_MESSAGE);
            }
        }
    }

    /**
     * Revalidates all tags which belong to the provided slave address.
     * 
     * @param slaveAddress The slave address to send to.
     */
    public synchronized void revalidateForUnavailableSlave(final String slaveAddress) {
        for (ISourceDataTag sourceDataTag : supervisedTags.values()) {
            PLCHardwareAddress plcTagAddress = (PLCHardwareAddress) sourceDataTag.getHardwareAddress();
            String natAddr = plcTagAddress.getNativeAddress();
            if (natAddr != null && natAddr.startsWith(slaveAddress)) {
                if (sourceDataTag.getCurrentValue() != null) {
                    if (!sourceDataTag.getCurrentValue().isValid()) {
                        revalidateTag(plcTagAddress.getWordId(), plcTagAddress.getBitId());
                    }
                }
            }
        }
    }

    /**
     * For test use only!!
     * @param initialValuesSent the initialValuesSent to set
     */
    public void setInitialValuesSent(int blockNumber, boolean initialValuesSent) {
      this.initialValuesSent.put(blockNumber, initialValuesSent);
    }
}

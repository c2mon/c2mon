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
import cern.tim.shared.daq.datatag.ISourceDataTag;
import ch.cern.tim.driver.jec.PLCObjectFactory;
import ch.cern.tim.driver.jec.address.BooleanJECAdressSpace;
import ch.cern.tim.driver.jec.tools.JECBinaryHelper;
import ch.cern.tim.jec.StdConstants;

/**
 * The boolean data processor handles boolean JECFrames.
 * 
 * @param <T> The class of the used address space.
 * @author Andreas Lang
 * 
 */
public class BooleanDataProcessor<T extends BooleanJECAdressSpace> extends AbstractDataProcessor {
    
    /**
     * Helps to calculate and access details to the BooleanJECProfibusWagoAddressSpace.
     * Boolean data tags use this address space.
     */
    private T booleanAddressSpace;

    /**
     * The length of a word in the used adress space.
     */
    private static final int WORD_LENGTH = 16;

    /**
     * Create a boolean data processor.
     * 
     * @param supervisedMessagesId The supervised message id of the JECFrames.
     * @param booleanAddressSpace The JEC address space.
     * @param plcFactory The PLC object factory.
     * @param equipmentMessageSender The equipment message sender to send updates
     * to the server.
     * @param equipmentLogger The equipment logger to use.
     */
    public BooleanDataProcessor(final int supervisedMessagesId,
            final T booleanAddressSpace,
            final PLCObjectFactory plcFactory, 
            final IEquipmentMessageSender equipmentMessageSender,
            final EquipmentLogger equipmentLogger) {
        super(supervisedMessagesId, plcFactory, equipmentMessageSender, equipmentLogger);
        this.booleanAddressSpace = booleanAddressSpace;
    }

    /**
     * Searches for changes between the current and the last values at the specified
     * position and sends them to the server.
     * 
     * @param blockNumber The number of the block to check.
     * @param timestamp The timestamp to send with the update.
     */
    @Override
    public void detectAndSendArrayChanges(final int blockNumber, final long timestamp) {
        int startWord = blockNumber * (StdConstants.JEC_DATA_SIZE / 2);
        int endWord = (blockNumber + 1) * (StdConstants.JEC_DATA_SIZE / 2);
        getEquipmentLogger().debug("Searching array in BLOCK " + blockNumber + " from word " + startWord + " to word " + (endWord - 1));
        for (int word = startWord; word < endWord; word++) {
            int actWord = JECBinaryHelper.getBooleanWord(word, getCurrentValues());
            int lastWord = JECBinaryHelper.getBooleanWord(word, getLastValues());

            if (actWord != lastWord) {
                getEquipmentLogger().debug("WORD " + word + " HAS CHANGED...");
                // Check which bits have changed inside the byte (from right to left)
                for (int bit = 0; bit < WORD_LENGTH; bit++) {
                    // If this test is true, the bit j has changed
                    if ((actWord & 0x01) != (lastWord & 0x01)) {
                        getEquipmentLogger().debug("BIT " + bit + " from WORD " + word + " HAS CHANGED!!");
                        // Send the changed bit to TIM
                        ISourceDataTag sourceDataTag = getTag(word, bit);
                        if (sourceDataTag != null) {
                            send((actWord & 0x01) == 1, sourceDataTag, timestamp);
                        }
                        else {
                            getEquipmentLogger().debug("No source data tag found for word " + word + " and bit " + bit);
                        }
                    }
                    // Shift the byte one position to the right to test the last bit
                    actWord = (short) (actWord >> 0x01);
                    // Shift the byte one position to the right to test the last bit
                    lastWord = (short) (lastWord >> 0x01);
                }
            }
        }
    }

    /**
     * Sends the value of the tag with the provided word and bit position to
     * the server.
     * @param wordPos The position of the word the tag belongs to.
     * @param bitPos the position of the bit the tag belongs to.
     */
    @Override
    public void sendTag(final int wordPos, final int bitPos) {
        int word = JECBinaryHelper.getBooleanWord(wordPos, getCurrentValues());
        boolean value = ((word >> bitPos) & 1) == 1;
        ISourceDataTag sourceDataTag = getTag(wordPos, bitPos);
        if (sourceDataTag != null) {
          getEquipmentLogger().trace("Sending boolean datatag with word " + wordPos + " bit " + bitPos);
          send(value, sourceDataTag, System.currentTimeMillis());
        } else
          getEquipmentLogger().trace("No source data tag for boolean word " + wordPos + " bit " + bitPos);
    }
    
    /**
     * Sends the value of the tag with the provided word and bit position to
     * the server.
     * @param wordPos The position of the word the tag belongs to.
     * @param bitPos the position of the bit the tag belongs to.
     */
    @Override
    public void revalidateTag(final int wordPos, final int bitPos) {
        int word = JECBinaryHelper.getBooleanWord(wordPos, getCurrentValues());
        boolean value = ((word >> bitPos) & 1) == 1;
        ISourceDataTag sourceDataTag = getTag(wordPos, bitPos);
        if (sourceDataTag != null)
            revalidate(value, sourceDataTag, System.currentTimeMillis());
        else
            getEquipmentLogger().warn("revalidateTag() - Cannot send revalidation Source data tag for boolean word " + wordPos + " bit " + bitPos + ". Reason: tag unknown!");
    }

    /**
     * Gets the JEC address space.
     * 
     * @return The JEC address space.
     */
    @Override
    public T getJecAddressSpace() {
        return booleanAddressSpace;
    }

    @Override
    public void sendAllInBlock(int blockNumber) {
      int startWord = blockNumber * (StdConstants.JEC_DATA_SIZE / 2);
      int endWord = (blockNumber + 1) * (StdConstants.JEC_DATA_SIZE / 2);
      for (int word = startWord; word < endWord; word++) {
        for (int bit = 0; bit < WORD_LENGTH; bit++) {
          sendTag(word, bit);
        }
      }
      
    }

}

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
package cern.c2mon.daq.jec.address;

import cern.c2mon.shared.common.datatag.address.PLCHardwareAddress;
import cern.c2mon.daq.jec.plc.StdConstants;
/**
 * This is the abstract base class for all address spaces. It handles the
 * increasing of the maximum word id.
 * 
 * @author Andreas Lang
 *
 */
public abstract class AbstractJECAddressSpace {

    /**
     * The id of the highest word.
     */
    private int maxWordId = -1;

    /**
     * Returns the size a java byte array has to must hold the full address.
     * space.
     * 
     * @return The size the java byte array must have.
     */
    public int getJavaByteArraySize() {
        return StdConstants.JEC_DATA_SIZE * (1 + (maxWordId * 2 / StdConstants.JEC_DATA_SIZE));
    }
    
    /**
     * Updates the address space.
     * 
     * @param hardwareAddress The PLC hardware address to update the address
     * space.
     */
    public abstract void updateAddressSpace(final PLCHardwareAddress hardwareAddress);
    
    /**
     * Checks if the provided values are in the current address range.
     * 
     * @param hardwareAddress he PLC hardware address.
     * @return True if the values are in range else false.
     */
    public abstract boolean isInRange(final PLCHardwareAddress hardwareAddress);

    /**
     * This will set the max word id. But only! if the provided word id is bigger.
     * 
     * @param wordId the maxWordId to set
     * @return True if the word is greater or equals then the max word before.
     */
    public boolean setMaxWordId(final int wordId) {
        boolean greaterOrEquals = false;
        if (wordId > maxWordId) {
            maxWordId = wordId;
            greaterOrEquals = true;
        }
        else if (maxWordId == wordId) {
            greaterOrEquals = true;
        }
        return greaterOrEquals;
    }

    /**
     * Returns the highest word id of this address space.
     * 
     * @return the maxWordId The highest word id.
     */
    public int getMaxWordId() {
        return maxWordId;
    }
    
    /**
     * Resets this address space.
     */
    public void reset() {
        maxWordId = -1;
    }
    
    /**
     * Checks if this address is empty.
     * 
     * @return True if it is empty else false.
     */
    public boolean isEmpty() {
        return maxWordId < 0;
    }
    
}

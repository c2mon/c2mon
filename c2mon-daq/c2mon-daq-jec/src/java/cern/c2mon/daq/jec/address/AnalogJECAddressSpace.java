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
/**
 * This is the class for the analog address space. In an analog address
 * space we have to deal with 16 and 32 Bit words.
 * 
 * @author Andreas Lang
 *
 */
public class AnalogJECAddressSpace extends AbstractJECAddressSpace {
    /**
     * The max word id on the PLC this number does not include the on DAQ level 
     * required increase if the last word is a 32 bit float.
     */
    private int maxWordIdPLC = -1;
    
    /**
     * Updates the address space.
     * 
     * @param plcHardwareAddress The PLC hardware address to update the address
     * space.
     */
    public void updateAddressSpace(final PLCHardwareAddress plcHardwareAddress) {
        int wordId = plcHardwareAddress.getWordId();
        updateAddressSpace(plcHardwareAddress.getNativeAddress(), wordId);
        if (plcHardwareAddress.getResolutionFactor() == 0) {
            setMaxWordId(getMaxWordId() + 1);
        }
    }
    
    /**
     * Updates the address space with the provided values.
     * 
     * @param nativeAddress The native address to use.
     * @param wordId The word id to use.
     */
    public void updateAddressSpace(final String nativeAddress, final int wordId) {
        setMaxWordIdPLC(wordId);
        setMaxWordId(wordId);
    }

    /**
     * @param wordId the maxWordIdPLC to set
     * @return Returns true if the max word id PLC was greater or equals.
     */
    public boolean setMaxWordIdPLC(final int wordId) {
        boolean greaterOrEquals = false;
        if (wordId > maxWordIdPLC) {
            maxWordIdPLC = wordId;
            greaterOrEquals = true;
        }
        else if (wordId == maxWordIdPLC) {
            greaterOrEquals = true;
        }
        return greaterOrEquals;
    }

    /**
     * @return the maxWordIdPLC
     */
    public int getMaxWordIdPLC() {
        return maxWordIdPLC;
    }
    
    /**
     * Resets this address space.
     */
    @Override
    public void reset() {
        maxWordIdPLC = -1;
        super.reset();
    }
    
    /**
     * Checks if these values are inside the address range.
     * 
     * @param nativeAddress The native address to check.
     * @param wordId The word id to check.
     * @return True if the values are in range else false.
     */
    public boolean isInRange(final String nativeAddress, final int wordId) {
        return getMaxWordId() >= wordId;
    }
    
    /**
     * Checks if the provided values are in the current address range.
     * 
     * @param hardwareAddress he PLC hardware address.
     * @return True if the values are in range else false.
     */
    @Override
    public boolean isInRange(final PLCHardwareAddress hardwareAddress) {
        return isInRange(hardwareAddress.getNativeAddress(), hardwareAddress.getWordId());
    }

}

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
/**
 * Class to update an analog address space to deal with normal addresses
 * as well as profibus wago addresses.
 * 
 * @author Andreas Lang
 *
 */
public class AnalogJECProfibusWagoAddressSpace extends AnalogJECAddressSpace {
    /**
     * Prefix for profibus wago addresses.
     */
    private static final String PROFIBUS_WAGO_PREFIX = "PWA";

    /**
     * The maximum MMD module word id or -1 if there was no address of this tye updated.
     */
    private int maxMMDWordId = -1;
    
    /**
     * Updates this address space.
     * 
     * @param nativeAddress The native address to use.
     * @param wordPos The word position to use.
     */
    @Override
    public void updateAddressSpace(final String nativeAddress, final int wordPos) {
        super.updateAddressSpace(nativeAddress, wordPos);
        if (nativeAddress != null 
                && nativeAddress.startsWith(PROFIBUS_WAGO_PREFIX)) {
            setMaxMMDWordId(wordPos);
        }
    }
    
    /**
     * Returns the number of MMD modules required on the PLC.
     * 
     * @return Number of MMD modules required on the PLC.
     */
    public int getNumberOfMMDModules() {
        int numberOfModules = 0;
        if (maxMMDWordId != -1) {
            numberOfModules = (maxMMDWordId / 2) + 1;
        }
        return numberOfModules;
    }
    
    /**
     * This will set the max word id. But only! if the provided word id is bigger
     * or equals.
     * 
     * @param wordId the maxMMDWordId to set
     * @return True if the word id is greater or equals else false.
     */
    public boolean setMaxMMDWordId(final int wordId) {
        boolean greaterOrEquals = false;
        if (wordId > maxMMDWordId) {
            maxMMDWordId = wordId;
            greaterOrEquals = true;
        }
        else if (wordId == maxMMDWordId) {
            greaterOrEquals = true;
        }
        return greaterOrEquals;
    }

    /**
     * @return the maxMMDWordId
     */
    public int getMaxMMDWordId() {
        return maxMMDWordId;
    }
    
    /**
     * Resets this address space.
     */
    @Override
    public void reset() {
        maxMMDWordId = -1;
        super.reset();
    }
    
    /**
     * Checks if the provided values are inside the range of this address space.
     * 
     * @param nativeAddress The native address to check for.
     * @param wordId The word id to check for.
     * @return True if it in range else false.
     */
    @Override
    public boolean isInRange(final String nativeAddress, final int wordId) {
        boolean isInRange;
        if (nativeAddress != null && nativeAddress.startsWith(PROFIBUS_WAGO_PREFIX)) {
            isInRange = maxMMDWordId >= wordId;
        }
        else {
            isInRange = getMaxWordId() >= wordId;
        }
        return isInRange;
    }

}

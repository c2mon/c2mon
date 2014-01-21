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
package cern.c2mon.daq.jec.address;
/**
 * Boolean address space which is able to deal with normal addresses as well
 * as profibus wago addresses.
 * 
 * @author Andreas Lang
 *
 */
public class BooleanJECProfibusWagoAddressSpace extends BooleanJECAdressSpace {
    /**
     * The number of MMD modules per word.
     */
    private static final int MMD_MODULES_PER_WORD = 4;
    /**
     * Address prefix of profibus wago addresses.
     */
    private static final String PROFIBUS_WAGO_PREFIX = "PWA";
    /**
     * Maximum MMD word id. It is -1 if there was no tag of type profibus wago was added.
     */
    private int maxMMDWordId = -1;
    /**
     * Maximum MMD bit id. It is -1 if there was no tag of type profibus wago was added.
     */
    private int maxMMDBitId = -1;
    
    /**
     * Updates the address space.
     * 
     * @param nativeAddress The native address to update.
     * @param wordId The word id to update.
     * @param bitId The bit id to update.
     */
    public void updateAddressSpace(final String nativeAddress, 
            final int wordId, final int bitId) {
        super.updateAddressSpace(nativeAddress, wordId, bitId);
        if (nativeAddress != null 
                && nativeAddress.startsWith(PROFIBUS_WAGO_PREFIX)) {
            if (setMaxMMDWordId(wordId)) {
                setMaxMMDBitId(bitId);
            }
        }
    }
    
    /**
     * Resets this address space.
     */
    @Override
    public void reset() {
        maxMMDWordId = -1;
        maxMMDBitId = -1;
        super.reset();
    }
    
    /**
     * Returns the number of MMD modules needed on the PLC.
     * 
     * @return Number of MMD modules needed on the PLC.
     */
    public int getNumberOfMMDModules() {
        int numberOfModules = 0;
        if (maxMMDWordId != -1) {            
            numberOfModules = maxMMDWordId * MMD_MODULES_PER_WORD + maxMMDBitId * MMD_MODULES_PER_WORD / 16 + 1;
        }
        return numberOfModules;
    }

    /**
     * This will set the max word id. But only! if the provided word id is bigger
     * or equals. If the max word id is set by this method, the max bit id is reset.
     * 
     * @param wordId the maxMMDWordId to set
     * @return True if the word was greater or equals else false.
     */
    public boolean setMaxMMDWordId(final int wordId) {
        boolean greaterOrEquals = false;
        if (wordId > maxMMDWordId) {
            maxMMDWordId = wordId;
            maxMMDBitId = -1; //reset as new largest word id 
            greaterOrEquals = true;
        }
        else if (wordId == maxMMDWordId) {
            greaterOrEquals = true; //need to check bit ID also
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
     * @param maxMMDBitId the maxMMDBitId to set
     */
    public void setMaxMMDBitId(final int maxMMDBitId) {
        if (maxMMDBitId > this.maxMMDBitId)
          this.maxMMDBitId = maxMMDBitId;
    }

    /**
     * @return the maxMMDBitId
     */
    public int getMaxMMDBitId() {
        return maxMMDBitId;
    }
    
    /**
     * Checks if the provided address is in range.
     * 
     * @param nativeAddress The native address to check.
     * @param bitId The bit id to check.
     * @param wordId The word id to check.
     * @return True if it is inside the currently configured range else false. 
     */
    @Override
    public boolean isInRange(final String nativeAddress, final int wordId, 
            final int bitId) {
        boolean isInRange;
        if (nativeAddress != null && nativeAddress.startsWith(PROFIBUS_WAGO_PREFIX)) {
            isInRange = (maxMMDWordId > wordId) 
                || (maxMMDWordId == wordId && maxMMDBitId >= bitId);
        }
        else {
            isInRange = getMaxWordId() >= wordId;
        }
        return isInRange;
    }
}

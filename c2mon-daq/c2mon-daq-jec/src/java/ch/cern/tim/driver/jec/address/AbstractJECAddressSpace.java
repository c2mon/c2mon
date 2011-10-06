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
package ch.cern.tim.driver.jec.address;

import cern.tim.shared.common.datatag.address.PLCHardwareAddress;
import ch.cern.tim.jec.StdConstants;
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

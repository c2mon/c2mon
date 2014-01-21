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

import cern.c2mon.shared.common.datatag.address.PLCHardwareAddress;
/**
 * Boolean address space.
 * 
 * @author Andreas Lang
 *
 */
public class BooleanJECAdressSpace extends AbstractJECAddressSpace {

    /**
     * Updates the boolean address space.
     * 
     * @param nativeAddress The native address to use.
     * @param wordId The word id to use.
     * @param bitId The bit id to use.
     */
    public void updateAddressSpace(final String nativeAddress, final int wordId, 
            final int bitId) {
        setMaxWordId(wordId);
    }

    /**
     * The PLC hardware address to update this address space.
     * 
     * @param plcHardwareAddress The address to update this address sapce.
     */
    public void updateAddressSpace(final PLCHardwareAddress plcHardwareAddress) {
        updateAddressSpace(
                plcHardwareAddress.getNativeAddress(), 
                plcHardwareAddress.getWordId(), 
                plcHardwareAddress.getBitId()
                );
    }

    /**
     * Checks if the provided values are in the current address range.
     * 
     * @param nativeAddress The native address to check.
     * @param wordId The word id to check.
     * @param bitId The bit id to check.
     * @return True if the values are in range else false.
     */
    public boolean isInRange(final String nativeAddress, 
            final int wordId, final int bitId) {
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
        return isInRange(hardwareAddress.getNativeAddress(),
                hardwareAddress.getWordId(), hardwareAddress.getBitId());
    }
}

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

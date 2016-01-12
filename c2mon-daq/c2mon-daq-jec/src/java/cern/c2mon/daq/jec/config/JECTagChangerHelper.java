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
package cern.c2mon.daq.jec.config;

import cern.c2mon.shared.common.datatag.address.PLCHardwareAddress;
/**
 * Abstract JECTagChanger which implements the just hardware address related check
 * if changes to it affect the PLC address space.
 * 
 * @author Andreas Lang
 *
 */
public final class JECTagChangerHelper {
    
    /**
     * There should be no instances of this class.
     */
    private JECTagChangerHelper() {
        
    }

    /**
     * Checks if changes to the hardware address might affect the 
     * address space. This means it checks if one of the values 
     * has important for the address has changed.
     * 
     * @param hardwareAddress The new hardware address.
     * @param oldHardwareAddress The old hardware address.
     * @return True if the changes might affect the PLC address space.
     */
    public static boolean mightAffectPLCAddressSpace(final PLCHardwareAddress hardwareAddress,
            final PLCHardwareAddress oldHardwareAddress) {
        boolean mightAffectPLCAddressSpace;
        if (hardwareAddress.getWordId() != oldHardwareAddress.getWordId() 
                || hardwareAddress.getBitId() != oldHardwareAddress.getBitId()) {
            mightAffectPLCAddressSpace = true;
        }
        else if (hardwareAddress.getNativeAddress() == null && oldHardwareAddress.getNativeAddress() == null) {
            mightAffectPLCAddressSpace = false;
        }
        else if (hardwareAddress.getNativeAddress() != null && hardwareAddress.getNativeAddress().equals(oldHardwareAddress.getNativeAddress())) {
            mightAffectPLCAddressSpace = false;
        }
        else if (oldHardwareAddress.getNativeAddress() != null && oldHardwareAddress.getNativeAddress().equals(hardwareAddress.getNativeAddress())) {
            mightAffectPLCAddressSpace = false;
        }
        else {
            mightAffectPLCAddressSpace = true;
        }
        return mightAffectPLCAddressSpace;
    }

}

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
package ch.cern.tim.driver.jec.config;

import cern.tim.shared.common.datatag.address.PLCHardwareAddress;
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

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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.address.PLCHardwareAddress;
import cern.c2mon.shared.common.datatag.address.impl.PLCHardwareAddressImpl;

public class JECTagChangerHelperTest {
    
    @Test
    public void testMightAffectPLCAddressSpace() throws ConfigurationException {
        PLCHardwareAddressImpl hardwareAddress = new PLCHardwareAddressImpl(PLCHardwareAddress.STRUCT_ANALOG, 0, 0, 0, 10, 100, null, 1000);
        PLCHardwareAddressImpl oldHardwareAddress = new PLCHardwareAddressImpl(PLCHardwareAddress.STRUCT_ANALOG, 0, 0, 0, 10, 100, null, 1000);;
        
        assertFalse(JECTagChangerHelper.mightAffectPLCAddressSpace(hardwareAddress, oldHardwareAddress));
        hardwareAddress.setBitId(10);
        assertTrue(JECTagChangerHelper.mightAffectPLCAddressSpace(hardwareAddress, oldHardwareAddress));
        hardwareAddress.setBitId(0);
        hardwareAddress.setWordId(10);
        assertTrue(JECTagChangerHelper.mightAffectPLCAddressSpace(hardwareAddress, oldHardwareAddress));
        hardwareAddress.setWordId(0);
        hardwareAddress.setNativeAddress("asd");
        assertTrue(JECTagChangerHelper.mightAffectPLCAddressSpace(hardwareAddress, oldHardwareAddress));
        oldHardwareAddress.setNativeAddress("asd2");
        assertTrue(JECTagChangerHelper.mightAffectPLCAddressSpace(hardwareAddress, oldHardwareAddress));
        oldHardwareAddress.setNativeAddress("asd");
        assertFalse(JECTagChangerHelper.mightAffectPLCAddressSpace(hardwareAddress, oldHardwareAddress));
    }

}

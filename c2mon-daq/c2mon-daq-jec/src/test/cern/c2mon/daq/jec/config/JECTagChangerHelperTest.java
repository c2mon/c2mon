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

package ch.cern.tim.driver.jec.config;

import static junit.framework.Assert.*;

import org.junit.Test;

import cern.tim.shared.common.ConfigurationException;
import cern.tim.shared.common.datatag.address.PLCHardwareAddress;
import ch.cern.tim.shared.datatag.address.impl.PLCHardwareAddressImpl;

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

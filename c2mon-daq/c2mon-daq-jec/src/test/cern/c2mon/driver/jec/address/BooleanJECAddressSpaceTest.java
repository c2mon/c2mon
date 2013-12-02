package cern.c2mon.driver.jec.address;

import org.junit.Test;

import cern.c2mon.driver.jec.address.BooleanJECAdressSpace;
import static org.junit.Assert.*;

public class BooleanJECAddressSpaceTest {

    private BooleanJECAdressSpace addressSpace = new BooleanJECAdressSpace();
    
    @Test
    public void testWordAndBitId() {
        addressSpace.updateAddressSpace(new TestPLCHardwareAddress("asd", 1, 10, 5));
        assertEquals(10, addressSpace.getMaxWordId());
        addressSpace.updateAddressSpace(new TestPLCHardwareAddress("asd", 0, 2, 3));
        assertEquals(10, addressSpace.getMaxWordId());
        addressSpace.updateAddressSpace(new TestPLCHardwareAddress("asd", 1, 11, 4));
        assertEquals(11, addressSpace.getMaxWordId());
        addressSpace.updateAddressSpace(new TestPLCHardwareAddress("asd", 1, 12, 8));
        assertEquals(12, addressSpace.getMaxWordId());
        addressSpace.reset();
        assertEquals(-1, addressSpace.getMaxWordId());
    }
    
}

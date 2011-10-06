package ch.cern.tim.driver.jec.address;

import org.junit.Test;
import static org.junit.Assert.*;

public class BooleanJECProfibusWagoAddressSpaceTest {

    private BooleanJECProfibusWagoAddressSpace addressSpace = new BooleanJECProfibusWagoAddressSpace();
    
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
    
    @Test
    public void testWordAndBitIdMMD() {
        addressSpace.updateAddressSpace(new TestPLCHardwareAddress("asd", 1, 10, 5));
        assertEquals(-1, addressSpace.getMaxMMDWordId());
        assertEquals(-1, addressSpace.getMaxMMDBitId());
        addressSpace.updateAddressSpace(new TestPLCHardwareAddress("PWA", 0, 2, 3));
        assertEquals(2, addressSpace.getMaxMMDWordId());
        assertEquals(3, addressSpace.getMaxMMDBitId());
        addressSpace.updateAddressSpace(new TestPLCHardwareAddress("asd", 1, 11, 4));
        assertEquals(2, addressSpace.getMaxMMDWordId());
        assertEquals(3, addressSpace.getMaxMMDBitId());
        addressSpace.updateAddressSpace(new TestPLCHardwareAddress("PWA", 1, 12, 2));
        assertEquals(12, addressSpace.getMaxMMDWordId());
        assertEquals(2, addressSpace.getMaxMMDBitId());
        addressSpace.reset();
        assertEquals(-1, addressSpace.getMaxMMDWordId());
        assertEquals(-1, addressSpace.getMaxMMDBitId());
    }
    
    
}

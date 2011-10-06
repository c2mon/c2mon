package ch.cern.tim.driver.jec.address;

import org.junit.Test;
import static org.junit.Assert.*;

public class AnalogJECProfibusWagoAddressSpaceTest {

    private AnalogJECProfibusWagoAddressSpace addressSpace = new AnalogJECProfibusWagoAddressSpace();
    
    @Test
    public void testWordId() {
        addressSpace.updateAddressSpace(new TestPLCHardwareAddress("asd", 1, 10, 5));
        assertEquals(10, addressSpace.getMaxWordId());
        addressSpace.updateAddressSpace(new TestPLCHardwareAddress("asd", 0, 2, 3));
        assertEquals(11, addressSpace.getMaxWordId());
        addressSpace.updateAddressSpace(new TestPLCHardwareAddress("asd", 1, 11, 4));
        assertEquals(11, addressSpace.getMaxWordId());
        addressSpace.updateAddressSpace(new TestPLCHardwareAddress("asd", 1, 12, 8));
        assertEquals(12, addressSpace.getMaxWordId());
        addressSpace.reset();
        assertEquals(-1, addressSpace.getMaxWordId());
    }
    
    @Test
    public void testWordIdPLC() {
        addressSpace.updateAddressSpace(new TestPLCHardwareAddress("asd", 1, 10, 5));
        assertEquals(10, addressSpace.getMaxWordIdPLC());
        addressSpace.updateAddressSpace(new TestPLCHardwareAddress("asd", 0, 2, 3));
        assertEquals(10, addressSpace.getMaxWordIdPLC());
        addressSpace.updateAddressSpace(new TestPLCHardwareAddress("asd", 1, 11, 4));
        assertEquals(11, addressSpace.getMaxWordIdPLC());
        addressSpace.updateAddressSpace(new TestPLCHardwareAddress("asd", 1, 12, 8));
        assertEquals(12, addressSpace.getMaxWordIdPLC());
        addressSpace.reset();
        assertEquals(-1, addressSpace.getMaxWordIdPLC());
    }
    
    @Test
    public void testWordIdMMD() {
        addressSpace.updateAddressSpace(new TestPLCHardwareAddress("asd", 1, 10, 5));
        assertEquals(-1, addressSpace.getMaxMMDWordId());
        addressSpace.updateAddressSpace(new TestPLCHardwareAddress("PWA", 0, 2, 3));
        assertEquals(2, addressSpace.getMaxMMDWordId());
        addressSpace.updateAddressSpace(new TestPLCHardwareAddress("asd", 1, 11, 4));
        assertEquals(2, addressSpace.getMaxMMDWordId());
        addressSpace.updateAddressSpace(new TestPLCHardwareAddress("PWA", 1, 12, 8));
        assertEquals(12, addressSpace.getMaxMMDWordId());
        addressSpace.reset();
        assertEquals(-1, addressSpace.getMaxMMDWordId());
    }
    
    
}

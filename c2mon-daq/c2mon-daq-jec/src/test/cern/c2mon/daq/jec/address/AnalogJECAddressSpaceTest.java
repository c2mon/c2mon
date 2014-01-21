package cern.c2mon.daq.jec.address;

import org.junit.Test;

import cern.c2mon.daq.jec.address.AnalogJECAddressSpace;
import static org.junit.Assert.*;

public class AnalogJECAddressSpaceTest {

    private AnalogJECAddressSpace addressSpace = new AnalogJECAddressSpace();
    
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
    public void testMMDWordId() {
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
    
}

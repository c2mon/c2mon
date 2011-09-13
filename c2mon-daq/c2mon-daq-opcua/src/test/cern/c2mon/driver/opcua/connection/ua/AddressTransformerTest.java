package cern.c2mon.driver.opcua.connection.ua;

import static org.junit.Assert.*;

import java.net.MalformedURLException;

import org.junit.Test;

import cern.c2mon.driver.opcua.connection.ua.UAAddressTransformer;

public class AddressTransformerTest {
    
    private String siemensClassicAddress = "S7:[@LOCALSERVER]DB1,INT1.5";
    
    private String siemensUAAddress = "s=@localserver.db1.1,i5";
    
    @Test
    public void testTransformSiemensDataType() throws MalformedURLException {
        assertEquals("i", UAAddressTransformer.transformSiemensDataType("INT"));
        assertEquals("x", UAAddressTransformer.transformSiemensDataType("X"));
    }
    
    @Test
    public void testMatchesSiemensClassic() {
        assertTrue(
                UAAddressTransformer.matchesSiemensClassic(
                        siemensClassicAddress));
        assertFalse(
                UAAddressTransformer.matchesSiemensClassic(
                        siemensUAAddress));
    }
    
    @Test
    public void testTransformSiemensClassic() {
        assertEquals(
                siemensUAAddress,
                UAAddressTransformer.transformSiemensClassic(
                        siemensClassicAddress));
        assertEquals(
                siemensUAAddress,
                UAAddressTransformer.transformSiemensClassic(
                        siemensUAAddress));
    }
    
    @Test
    public void testTransform() {
        assertEquals(
                siemensUAAddress,
                UAAddressTransformer.transform(siemensClassicAddress));
        assertEquals(
                siemensUAAddress,
                UAAddressTransformer.transform(siemensUAAddress));
    }

}

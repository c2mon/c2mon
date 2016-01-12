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
package cern.c2mon.daq.opcua.connection.ua;

import static org.junit.Assert.*;

import java.net.MalformedURLException;

import org.junit.Test;

import cern.c2mon.daq.opcua.connection.ua.UAAddressTransformer;

public class AddressTransformerTest {
    
    private String siemensClassicAddress = "S7:[@LOCALSERVER]DB1,INT1.5";
    
    private String siemensUAAddress = "@localserver.db1.1,i5";
    
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

package cern.c2mon.daq.opcua.connection.dcom;

import static org.junit.Assert.*;

import org.junit.Test;

import cern.c2mon.daq.opcua.connection.dcom.DADCOMItemDefintion;
import cern.c2mon.daq.opcua.connection.dcom.DADCOMItemDefintionFactory;

public class DADCOMItemDefinitionFactoryTest {
    
    private DADCOMItemDefintionFactory factory = new DADCOMItemDefintionFactory();
    
    @Test
    public void testCreateItemDefintion() {
        String address = "address";
        DADCOMItemDefintion defintion = factory.createItemDefinition(1L, address);
        assertEquals(address, defintion.getAddress());
        assertEquals(1L, defintion.getId());
    }
    
    @Test
    public void testCreateItemDefintionWithRedundantAddress() {
        String address = "address";
        String redAdddress = "address";
        DADCOMItemDefintion defintion = factory.createItemDefinition(
                1L, address, redAdddress);
        assertEquals(address, defintion.getAddress());
        assertEquals(redAdddress, defintion.getRedundantAddress());
        assertEquals(1L, defintion.getId());
    }

}

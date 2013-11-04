package cern.c2mon.daq.opcua.connection.common.impl;

import static org.junit.Assert.*;

import org.junit.Test;

import cern.c2mon.daq.opcua.connection.common.impl.ItemDefinition;

public class ItemDefintionTest {
    
    @Test
    public void testEquals() {
        ItemDefinition<String> address1 = new ItemDefinition<String>(1L, "asd");
        ItemDefinition<String> address2 = new ItemDefinition<String>(1L, "asd2");
        ItemDefinition<String> address3 = new ItemDefinition<String>(2L, "asd");
        ItemDefinition<String> addressNull = null;
        
        assertEquals(address1, address1);
        assertEquals(address1, address2);
        assertFalse(address1.equals(address3));
        assertFalse(address1.equals(addressNull));
    }
    
    @Test
    public void testHash() {
        ItemDefinition<String> address1 = new ItemDefinition<String>(1L, "asd");
        ItemDefinition<String> address2 = new ItemDefinition<String>(1L, "asd2");
        ItemDefinition<String> address3 = new ItemDefinition<String>(2L, "asd");
        
        assertEquals(address1.hashCode(), address1.hashCode());
        assertEquals(address1.hashCode(), address2.hashCode());
        assertFalse(address1.hashCode() == address3.hashCode());
    }
}

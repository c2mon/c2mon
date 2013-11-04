package cern.c2mon.daq.opcua.connection.soap;

import static org.junit.Assert.*;

import org.junit.Test;

import cern.c2mon.daq.opcua.connection.soap.DASoapItemDefintion;
import cern.c2mon.daq.opcua.connection.soap.DASoapItemDefintionFactory;

public class DASoapItemDefinitionFactoryTest {
    
    private DASoapItemDefintionFactory factory = new DASoapItemDefintionFactory();
    
    @Test
    public void testCreateItemDefintion() {
        DASoapItemDefintion definition = factory.createItemDefinition(1L, "asd");
        assertEquals(1L, definition.getId());
        assertEquals("asd", definition.getAddress());
        assertFalse(definition.hasRedundantAddress());
    }
    
    @Test
    public void testCreateItemDefintionRedundant() {
        DASoapItemDefintion definition = factory.createItemDefinition(1L, "asd", "red");
        assertEquals(1L, definition.getId());
        assertEquals("asd", definition.getAddress());
        assertEquals("red", definition.getRedundantAddress());
        assertTrue(definition.hasRedundantAddress());
    }

}

package cern.c2mon.daq.opcua.connection.ua;

import static org.junit.Assert.*;

import org.junit.Test;
import org.opcfoundation.ua.core.IdType;

import cern.c2mon.daq.opcua.connection.ua.UAItemDefintion;
import cern.c2mon.daq.opcua.connection.ua.UaItemDefintionFactory;
import cern.tim.shared.common.ConfigurationException;
import cern.tim.shared.common.datatag.address.HardwareAddress;
import cern.tim.shared.common.datatag.address.OPCHardwareAddress.ADDRESS_TYPE;
import ch.cern.tim.shared.datatag.address.impl.HardwareAddressImpl;
import ch.cern.tim.shared.datatag.address.impl.OPCHardwareAddressImpl;

public class UAItemDefintionFactoryTest {
    
    private UaItemDefintionFactory factory = new UaItemDefintionFactory();
    
    @Test
    public void testCreateItemDefinitionString() throws ConfigurationException {
        OPCHardwareAddressImpl address = 
            new OPCHardwareAddressImpl("S7:[@LOCALSERVER]DB1,INT1.5");
        address.setAddressType(ADDRESS_TYPE.STRING);
        address.setNamespace(3);
        UAItemDefintion definition = factory.createItemDefinition(1L, address);
        assertFalse(definition.hasRedundantAddress());
        assertEquals(IdType.String, definition.getAddress().getIdType());
        assertEquals(3, definition.getAddress().getNamespaceIndex());
    }
    
    @Test
    public void testCreateItemDefinitionNumeric() throws ConfigurationException {
        OPCHardwareAddressImpl address = 
            new OPCHardwareAddressImpl("12");
        address.setAddressType(ADDRESS_TYPE.NUMERIC);
        address.setNamespace(3);
        UAItemDefintion definition = factory.createItemDefinition(1L, address);
        assertFalse(definition.hasRedundantAddress());
        assertEquals(IdType.Numeric, definition.getAddress().getIdType());
        assertEquals(3, definition.getAddress().getNamespaceIndex());
    }
    
    @Test
    public void testCreateItemDefinitionUUID() throws ConfigurationException {
        OPCHardwareAddressImpl address = 
            new OPCHardwareAddressImpl("550e8400-e29b-11d4-a716-446655440000");
        address.setAddressType(ADDRESS_TYPE.GUID);
        address.setNamespace(3);
        UAItemDefintion definition = factory.createItemDefinition(1L, address);
        assertFalse(definition.hasRedundantAddress());
        assertEquals(IdType.Guid, definition.getAddress().getIdType());
        assertEquals(3, definition.getAddress().getNamespaceIndex());
    }
    
    @Test
    public void testCreateItemDefinitionStringRedundant() throws ConfigurationException {
        OPCHardwareAddressImpl address = 
            new OPCHardwareAddressImpl("S7:[@LOCALSERVER]DB1,INT1.5");
        address.setAddressType(ADDRESS_TYPE.STRING);
        address.setNamespace(3);
        address.setOpcRedundantItemName("S7:[@LOCALSERVER]DB1,X1.5");
        UAItemDefintion definition = factory.createItemDefinition(1L, address);
        assertTrue(definition.hasRedundantAddress());
        assertEquals(IdType.String, definition.getAddress().getIdType());
        assertEquals(3, definition.getAddress().getNamespaceIndex());
    }
    
    @Test
    public void testCreateItemDefinitionNumericRedundant() throws ConfigurationException {
        OPCHardwareAddressImpl address = 
            new OPCHardwareAddressImpl("12");
        address.setAddressType(ADDRESS_TYPE.NUMERIC);
        address.setNamespace(3);
        address.setOpcRedundantItemName("13");
        UAItemDefintion definition = factory.createItemDefinition(1L, address);
        assertTrue(definition.hasRedundantAddress());
        assertEquals(IdType.Numeric, definition.getAddress().getIdType());
        assertEquals(3, definition.getAddress().getNamespaceIndex());
    }
    
    @Test
    public void testCreateItemDefinitionUUIDRedundant() throws ConfigurationException {
        OPCHardwareAddressImpl address = 
            new OPCHardwareAddressImpl("550e8400-e29b-11d4-a716-446655440000");
        address.setAddressType(ADDRESS_TYPE.GUID);
        address.setNamespace(3);
        address.setOpcRedundantItemName("520e8400-e29b-11d4-a716-446655440000");
        UAItemDefintion definition = factory.createItemDefinition(1L, address);
        assertTrue(definition.hasRedundantAddress());
        assertEquals(IdType.Guid, definition.getAddress().getIdType());
        assertEquals(3, definition.getAddress().getNamespaceIndex());
    }
    
    @Test
    public void testCreateItemDefinitionWrongOPCAddress() throws ConfigurationException {
        HardwareAddress address = new HardwareAddressImpl();
        UAItemDefintion definition = factory.createItemDefinition(1L, address);
        assertNull(definition);
    }
    
    @Test
    public void testCreateItemDefinitionTypeNULL() throws ConfigurationException {
        OPCHardwareAddressImpl address = 
            new OPCHardwareAddressImpl("550e8400-e29b-11d4-a716-446655440000");
        address.setAddressType(null);
        address.setNamespace(3);
        UAItemDefintion definition = factory.createItemDefinition(1L, address);
        assertNull(definition);
    }

}

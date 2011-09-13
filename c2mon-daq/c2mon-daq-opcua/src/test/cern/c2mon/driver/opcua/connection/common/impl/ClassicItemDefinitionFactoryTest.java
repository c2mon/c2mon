package cern.c2mon.driver.opcua.connection.common.impl;

import static org.easymock.classextension.EasyMock.*;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.driver.opcua.connection.common.impl.ClassicItemDefinitionFactory;
import cern.c2mon.driver.opcua.connection.common.impl.ItemDefinition;
import cern.tim.shared.common.ConfigurationException;
import ch.cern.tim.shared.datatag.address.impl.OPCHardwareAddressImpl;

public class ClassicItemDefinitionFactoryTest {
    
    private ClassicItemDefinitionFactory<ItemDefinition<String>> factory;
        
    
    @Before
    public void setUp() throws SecurityException, NoSuchMethodException {
        factory = createMock(ClassicItemDefinitionFactory.class, 
                ClassicItemDefinitionFactory.class.getMethod(
                        "createItemDefinition", Long.TYPE, String.class),
                ClassicItemDefinitionFactory.class.getMethod(
                        "createItemDefinition", Long.TYPE, String.class,
                        String.class) 
        );
    }
    
    @Test
    public void testCreateItemDefinitonNotRedundant() throws ConfigurationException {
        String name = "name";
        OPCHardwareAddressImpl hwimpl = new OPCHardwareAddressImpl(name);
        
        expect(factory.createItemDefinition(1L, hwimpl.getOPCItemName()))
            .andReturn(new ItemDefinition<String>(1L, name));
        
        replay(factory);
        factory.createItemDefinition(1L, hwimpl);
        verify(factory);
    }
    
    @Test
    public void testCreateItemDefinitonRedundant() throws ConfigurationException {
        String opcItemName = "name";
        OPCHardwareAddressImpl hwimpl = new OPCHardwareAddressImpl(opcItemName);
        String secondaryOpcItemName = "name2";
        hwimpl.setOpcRedundantItemName(secondaryOpcItemName);
        
        expect(factory.createItemDefinition(1L, opcItemName, secondaryOpcItemName))
                .andReturn(new ItemDefinition<String>(
                        1L, opcItemName, secondaryOpcItemName));
        
        replay(factory);
        factory.createItemDefinition(1L, hwimpl);
        verify(factory);
    }

}

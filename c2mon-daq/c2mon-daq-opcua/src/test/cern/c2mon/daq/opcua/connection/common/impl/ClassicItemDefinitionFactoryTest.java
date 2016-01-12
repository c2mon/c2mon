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
package cern.c2mon.daq.opcua.connection.common.impl;

import static org.easymock.classextension.EasyMock.*;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.daq.opcua.connection.common.impl.ClassicItemDefinitionFactory;
import cern.c2mon.daq.opcua.connection.common.impl.ItemDefinition;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.address.impl.OPCHardwareAddressImpl;

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

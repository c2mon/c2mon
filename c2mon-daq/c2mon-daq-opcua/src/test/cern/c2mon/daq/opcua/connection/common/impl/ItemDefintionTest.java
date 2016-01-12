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

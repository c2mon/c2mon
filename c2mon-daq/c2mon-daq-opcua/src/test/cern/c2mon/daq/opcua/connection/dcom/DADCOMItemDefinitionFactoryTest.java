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

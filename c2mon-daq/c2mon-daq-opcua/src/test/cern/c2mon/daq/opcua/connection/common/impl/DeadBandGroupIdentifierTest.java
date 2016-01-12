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

import cern.c2mon.daq.opcua.connection.common.impl.DeadBandGroupIdentifier;

public class DeadBandGroupIdentifierTest {
    
    private DeadBandGroupIdentifier identifier = 
        new DeadBandGroupIdentifier(0.2f, 100);
    
    private DeadBandGroupIdentifier sameIdentifier = 
        new DeadBandGroupIdentifier(0.2f, 100);
    
    private DeadBandGroupIdentifier differentIdentifier = 
        new DeadBandGroupIdentifier(0.2f, 101);
    
    private DeadBandGroupIdentifier differentIdentifier2 = 
        new DeadBandGroupIdentifier(0.3f, 100);
    
    private DeadBandGroupIdentifier completlyDifferentIdentifier = 
        new DeadBandGroupIdentifier(0.234f, 23423);

    @Test
    public void testEquals() {
        assertTrue(identifier.equals(identifier));
        assertTrue(identifier.equals(sameIdentifier));
        assertFalse(identifier.equals(null));
        assertFalse(identifier.equals(differentIdentifier));
        assertFalse(identifier.equals(differentIdentifier2));
        assertFalse(identifier.equals(completlyDifferentIdentifier));
    }
    
    @Test
    public void testHashCode() {
        assertEquals(identifier.hashCode(), identifier.hashCode());
        assertEquals(identifier.hashCode(), sameIdentifier.hashCode());
        assertFalse(identifier.hashCode() == differentIdentifier.hashCode());
        assertFalse(identifier.hashCode() == differentIdentifier2.hashCode());
        assertFalse(
                identifier.hashCode() == completlyDifferentIdentifier.hashCode()
                );
    }
}

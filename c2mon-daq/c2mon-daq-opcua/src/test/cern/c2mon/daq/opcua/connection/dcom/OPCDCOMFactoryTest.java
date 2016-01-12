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

import java.io.IOException;

import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Test;

import cern.c2mon.daq.opcua.connection.dcom.OPCDCOMFactory;
import cern.c2mon.daq.opcua.jintegraInterface.IOPCGroup;
import cern.c2mon.daq.opcua.jintegraInterface.IOPCGroups;
import cern.c2mon.daq.opcua.jintegraInterface.OPCItem;
import cern.c2mon.daq.opcua.jintegraInterface.OPCItems;

public class OPCDCOMFactoryTest {
    
    @Test
    public void testCreateOPCGroup() throws IOException {
        IOPCGroups groups = createMock(IOPCGroups.class);
        IOPCGroup groupMock = createMock(IOPCGroup.class);
        String groupName = "asdasd";
        int updateRate = 123;
        float valueDeadband = 24.0f;
        
        expect(groups.add(groupName)).andReturn(groupMock);
        groupMock.setUpdateRate(updateRate);
        groupMock.setDeadBand(valueDeadband);
        
        replay(groups, groupMock);
        IOPCGroup group = OPCDCOMFactory.createOPCGroup(groups, 
                groupName, updateRate, valueDeadband);
        verify(group, groupMock);
    }
    
    @Test
    public void testCreateOPCItem() throws IOException {
        OPCItems items = createMock(OPCItems.class);
        int clientHandle = 23;
        String itemAddress = "asdada";
        OPCItem itemMock = createMock(OPCItem.class);
        expect(items.addItem(itemAddress, clientHandle))
            .andReturn(itemMock);
        
        replay(items);
        OPCItem item =
            OPCDCOMFactory.createOPCItem(items, clientHandle, itemAddress);
        assertEquals(itemMock, item);
        verify(items);
    }
}

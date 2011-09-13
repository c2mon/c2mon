package cern.c2mon.driver.opcua.connection.dcom;

import java.io.IOException;

import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Test;

import cern.c2mon.driver.opcua.connection.dcom.OPCDCOMFactory;
import ch.cern.tim.driver.jintegraInterface.IOPCGroup;
import ch.cern.tim.driver.jintegraInterface.IOPCGroups;
import ch.cern.tim.driver.jintegraInterface.OPCItem;
import ch.cern.tim.driver.jintegraInterface.OPCItems;

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

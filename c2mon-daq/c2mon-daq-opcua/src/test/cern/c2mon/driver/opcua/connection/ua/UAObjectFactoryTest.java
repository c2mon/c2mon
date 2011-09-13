package cern.c2mon.driver.opcua.connection.ua;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.core.DataChangeFilter;

import cern.c2mon.driver.opcua.connection.ua.UAObjectFactory;

import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.client.MonitoredItem;
import com.prosysopc.ua.client.Subscription;

public class UAObjectFactoryTest {
    
    @Test
    public void testCreateSubscription() {
        Subscription subscription1 = UAObjectFactory.createSubscription();
        Subscription subscription2 = UAObjectFactory.createSubscription();
        assertNotSame(subscription1, subscription2);
    }
    
    @Test
    public void testCreateMonitoredItem() throws ServiceException, StatusException {
        NodeId nodeId = new NodeId(2, "asd");
        float valueDeadband = 0.1f;
        int timeDeadband = 100;
        MonitoredItem item = UAObjectFactory.createMonitoredItem(
                nodeId, valueDeadband, timeDeadband);
        double deadBandDif = Math.abs(
                    Float.valueOf(valueDeadband) -
                    ((DataChangeFilter)item.getFilter()).getDeadbandValue());
        assertTrue(deadBandDif < 0.000000000001);
        assertEquals(UnsignedInteger.valueOf(2),
                ((DataChangeFilter)item.getFilter()).getDeadbandType());
        assertEquals(nodeId, item.getNodeId());
    }

}

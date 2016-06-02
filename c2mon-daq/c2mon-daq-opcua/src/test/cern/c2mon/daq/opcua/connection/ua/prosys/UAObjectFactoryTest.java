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
package cern.c2mon.daq.opcua.connection.ua.prosys;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.core.DataChangeFilter;

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

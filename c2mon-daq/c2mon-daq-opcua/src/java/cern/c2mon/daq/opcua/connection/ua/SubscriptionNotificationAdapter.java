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
package cern.c2mon.daq.opcua.connection.ua;

import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.DiagnosticInfo;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.Variant;
import org.opcfoundation.ua.core.MonitoredItemNotification;
import org.opcfoundation.ua.core.NotificationData;

import com.prosysopc.ua.SubscriptionBase;
import com.prosysopc.ua.client.MonitoredItem;
import com.prosysopc.ua.client.SubscriptionNotificationListener;

/**
 * Adapter to allow to specificly select events which are interesting.
 * 
 * @author Andreas Lang
 *
 */
public abstract class SubscriptionNotificationAdapter 
        implements SubscriptionNotificationListener {

    
    /**
     * Called after subscribed data on the OPC server has changed.
     * 
     * @param subscriptionBase The subscription in which the data changed.
     * @param item The monitored item which changed.
     * @param value The new value.
     */
    @Override
    public void onDataChange(final SubscriptionBase subscriptionBase, 
            final MonitoredItem item, final DataValue value) {
        // default: do nothing
    }

    /**
     * Called after an error in the UaClient.
     * 
     * @param subscriptionBase The subscription in which the data changed.
     * @param object {@link MonitoredItemNotification} or EventList
     * @param exception The exception which happend
     */
    @Override
    public void onError(
            final SubscriptionBase subscriptionBase, final Object object,
            final Exception exception) {
        // default: do nothing
    }

    @Override
    public void onEvent(final MonitoredItem item, final Variant[] variant) {
        // default: do nothing
    }

    @Override
    public void onNotificationData(final SubscriptionBase subscriptionBase,
            final NotificationData notificationData) {
        // default: do nothing
    }

    @Override
    public void onStatusChange(final SubscriptionBase subscriptionBase,
            final StatusCode oldStatusCode, final StatusCode newStatusCode,
            final DiagnosticInfo info) {
        // default: do nothing
    }

}

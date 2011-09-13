package cern.c2mon.driver.opcua.connection.ua;

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

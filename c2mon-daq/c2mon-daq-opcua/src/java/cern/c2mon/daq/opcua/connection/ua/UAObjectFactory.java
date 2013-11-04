package cern.c2mon.daq.opcua.connection.ua;

import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.core.Attributes;
import org.opcfoundation.ua.core.DataChangeFilter;
import org.opcfoundation.ua.core.MonitoringMode;

import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.client.MonitoredDataItem;
import com.prosysopc.ua.client.MonitoredItem;
import com.prosysopc.ua.client.Subscription;

/**
 * Helper class to create OPC UA objects.
 * 
 * @author Andreas Lang
 *
 */
public final class UAObjectFactory {
    
    /**
     * There should be no objects of a helper class.
     */
    private UAObjectFactory() { }
    
    /**
     * Creates a new Subscription object.
     * 
     * @return The new subscription.
     */
    public static Subscription createSubscription() {
        Subscription subscription = new Subscription();
        return subscription;
    }
    
    /**
     * Creates a new MonitoredItem object.
     * 
     * @param nodeId The node id of the item to monitor.
     * @return The new subscription.
     */
    public static MonitoredItem createMonitoredItem(final NodeId nodeId) {
        return createMonitoredDataItem(nodeId);
    }
    
    /**
     * Creates a new MonitoredDataItem object.
     * 
     * @param nodeId The node id of the item to monitor.
     * @return The new subscription.
     */
    private static MonitoredDataItem createMonitoredDataItem(
            final NodeId nodeId) {
        MonitoredDataItem monitoredItem = new MonitoredDataItem(nodeId,
                Attributes.Value, MonitoringMode.Reporting);
        return monitoredItem;
    }
    
    /**
     * Creates a new MonitoredItem object.
     * 
     * @param nodeId The node id of the item to monitor.
     * @param valueDeadband The value deadband of the item.
     * @param timeDeadband The time deadband of the item.
     * @return The new subscription.
     * @throws ServiceException Might throw a service exception.
     * @throws StatusException Might throw a status exception.
     */
    public static MonitoredItem createMonitoredItem(final NodeId nodeId,
            final float valueDeadband, final int timeDeadband) 
            throws ServiceException, StatusException {
        MonitoredDataItem monitoredItem = createMonitoredDataItem(nodeId);
        monitoredItem.setSamplingInterval(timeDeadband);
        DataChangeFilter filter = new DataChangeFilter();
        filter.setDeadbandType(UnsignedInteger.valueOf(2));
        filter.setDeadbandValue(Double.valueOf(valueDeadband));
        monitoredItem.setDataChangeFilter(filter);
        return monitoredItem;
    }
    
    
}

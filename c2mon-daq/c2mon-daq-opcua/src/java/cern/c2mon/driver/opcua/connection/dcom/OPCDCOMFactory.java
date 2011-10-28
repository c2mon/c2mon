package cern.c2mon.driver.opcua.connection.dcom;

import java.io.IOException;

import com.linar.jintegra.AutomationException;

import cern.c2mon.driver.opcua.connection.common.impl.OPCCommunicationException;
import cern.c2mon.driver.opcua.connection.common.impl.OPCCriticalException;
import ch.cern.tim.driver.jintegraInterface.IOPCAutoServer;
import ch.cern.tim.driver.jintegraInterface.IOPCGroup;
import ch.cern.tim.driver.jintegraInterface.IOPCGroups;
import ch.cern.tim.driver.jintegraInterface.OPCItem;
import ch.cern.tim.driver.jintegraInterface.OPCItems;
import ch.cern.tim.driver.jintegraInterface.OPCServer;

/**
 * Helper class to create initialized DCOM objects.
 * 
 * @author Andreas Lang
 *
 */
public final class OPCDCOMFactory implements IDCOMErrorConstants {
    
    /**
     * There should be no instances of this class. This is why it is made 
     * private.
     */
    private OPCDCOMFactory() { }
    
    /**
     * Creates a new opc server object.
     * 
     * @param hostName The hostname the server object should correspond to.
     * @return The new OPC server object.
     * @throws IOException Throws and IOException if there are general problems
     * with the connection.
     */
    public static IOPCAutoServer createOPCServer(final String hostName) 
            throws IOException {
        return new OPCServer(hostName);
    }
    
    /**
     * Creates a new IOPCGroup.
     * 
     * @param opcGroups The opc groups object the new group will belong to.
     * @param groupName The name for the new group.
     * @param updateRate The update rate for all items in the group. The update
     * rate specifies how often the cache on the server is updated. This means
     * taht updates happen not more often than this time.
     * @param valueDeadband The value deadband specifies the relative difference
     * a new value has to have to be worth notified about. E.g. a value of 0.3
     * means that the value has to differ at least 30%.
     * @return The new OPCGroup object.
     * @throws IOException Throws and IOException if there are general problems
     * with the connection.
     */
    public static IOPCGroup createOPCGroup(final IOPCGroups opcGroups, 
            final String groupName, final int updateRate,
            final float valueDeadband) throws IOException {
        IOPCGroup group = opcGroups.add(groupName);
        group.setDeadBand(valueDeadband);
        group.setUpdateRate(updateRate);
        group.setTimeBias(1000 * 60 * 60 * 2);
        return group;
    }

    /**
     * Creates a new OPCItem.
     * 
     * @param items The items the new item will belong to.
     * @param clientHandle The client handle used to identify value updates.
     * @param itemAddress The item address inside the OPC server.
     * @return The new OPC item.
     * @throws IOException Throws and IOException if there are general problems
     * with the connection.
     */
    public static OPCItem createOPCItem(final OPCItems items,
            final int clientHandle, final String itemAddress) throws IOException {
        OPCItem item = items.addItem(itemAddress, clientHandle);
        return item;
    }

    /**
     * Creates a new OPCCommunication exception which reflects the quality.
     * 
     * @param quality The quality to get the exception for.
     * @return The new exception.
     */
    public static OPCCommunicationException createQualityException(final int quality) {
        String substatusMessage = getSubsctatusMessage(quality);
        OPCCommunicationException exception;
        // bad
        if ((quality & 0xC0) == 0x00) {
            exception = new OPCCommunicationException("Bad quality: " + substatusMessage);
        }
        // uncertain
        else {
            exception = new OPCCommunicationException("Uncertain quality: " + substatusMessage);
        }
        return exception;
    }

    /**
     * Gets the substatus message for a quality.
     * 
     * @param quality
     *            The quality to get the message for.
     * @return The substatus message.
     */
    private static String getSubsctatusMessage(final int quality) {
        int substatus = (quality & 0x3C) >> 2;
        String reason;
        switch (substatus) {
        case 0x00:
            reason = "Non-specific: The value is bad but no specific reason is known";
            break;
        case 0x01:
            reason = "Configuration Error: There is some server specific problem with the " 
                + "configuration. For example the item is question " 
                + "has been deleted from the configuration.";
            break;
        case 0x02:
            reason = "Not Connected: The input is required to be logically connected" 
                + "to something but is not. This quality may "
                + "reflect that no value is available at this time, "
                + "for reasons like the value may have not been " 
                + "provided by the data source.";
            break;
        case 0x03:
            reason = "Device Failure: A device failure has been detected.";
            break;
        case 0x04:
            reason = "Sensor Failure: A sensor failure had been detected (the " 
                + "Limits field can provide additional diagnostic "
                + "information in some situations.)";
            break;
        case 0x05:
            reason = "Last known value: Communications have failed. However, the last "
                + "known value is available. Note that the age of "
                + "the value may be determined from the "
                + "TIMESTAMP in the OPCITEMSTATE.";
            break;
        case 0x06:
            reason = "Communication Failure: Communications have failed. There is no " 
                + "last known value is available.";
            break;
        case 0x07:
            reason = "Out of service: The block is off scan or otherwise locked This "
                + "quality is also used when the active state of the "
                + "item or the group containing the item is InActive.";
            break;
        case 0x08:
            reason = "After Items are added to a group, it may take "
                + "some time for the server to actually obtain "
                + "values for these items. In such cases the client "
                + "might perform a read (from cache), or " 
                + "establish a ConnectionPoint based " 
                + "subscription and/or execute a Refresh on such "
                + "a subscription before the values are available. "
                + "This substatus is only available from OPC DA 3.0 or newer servers.";
            break;
        default:
            reason = "N/A: quality was in a state not defined by the OPC specification,";
            break;
        }
        return reason;
    }

    /**
     * Creates a OPC communication exception which wraps an automation exception
     * and adds a readable error message.
     * 
     * @param cause
     *            The original automation exception.
     * @return The OPCCommunication exception.
     */
    public static RuntimeException createWrappedAutomationException(
            final AutomationException cause) {
        RuntimeException exception;
        long code = cause.getCode();
        if (code == OPC_E_INVALIDHANDLE) {
            exception = new OPCCriticalException("The value of the handle is invalid.", cause);
        } else if (code == OPC_E_BADTYPE) {
            exception = new OPCCriticalException("The server cannot convert the data between the" + "requested data type and the canonical data type.", cause);
        } else if (code == OPC_E_PUBLIC) {
            exception = new OPCCriticalException("The requested operation cannot be done on a public group.", cause);
        } else if (code == OPC_E_BADRIGHTS) {
            exception = new OPCCriticalException("The Items AccessRights do not allow the operation.", cause);
        } else if (code == OPC_E_UNKNOWNITEMID) {
            exception = new OPCCriticalException("The item is no longer available in the server address space", cause);
        } else if (code == OPC_E_INVALIDITEMID) {
            exception = new OPCCriticalException("The item definition doesn't conform to the server's syntax.", cause);
        } else if (code == OPC_E_INVALIDFILTER) {
            exception = new OPCCriticalException("The filter string was not valid.", cause);
        } else if (code == OPC_E_UNKNOWNPATH) {
            exception = new OPCCriticalException("The item's access path is not known to the server.", cause);
        } else if (code == OPC_E_RANGE) {
            exception = new OPCCriticalException("The value was out of range.", cause);
        } else if (code == OPC_E_DUPLICATENAME) {
            exception = new OPCCriticalException("Duplicate name not allowed. ", cause);
        } else if (code == OPC_S_UNSUPPORTEDRATE) {
            exception = new OPCCriticalException("The server does not support the requested data rate" + "but will use the closest available rate.", cause);
        } else if (code == OPC_S_CLAMP) {
            exception = new OPCCommunicationException("A value passed to WRITE was accepted but the output was clamped. ", cause);
        } else if (code == OPC_S_INUSE) {
            exception = new OPCCommunicationException("The operation cannot be completed because the" + "object still has references that exist.", cause);
        } else if (code == OPC_E_INVALIDCONFIGFILE) {
            exception = new OPCCommunicationException("The server's configuration file is an invalid format.", cause);
        } else if (code == OPC_E_NOTFOUND) {
            exception = new OPCCriticalException("The server could not locate the requested object.", cause);
        } else if (code == OPC_E_INVALID_PID) {
            exception = new OPCCriticalException("The server does not recognise the passed property ID.", cause);
        } else if (code == OPC_E_DEADBANDNOTSET) {
            exception = new OPCCriticalException("The item deadband has not been set for this item.", cause);
        } else if (code == OPC_E_DEADBANDNOTSUPPORTED) {
            exception = new OPCCriticalException("The item does not support deadband.", cause);
        } else if (code == OPC_E_NOBUFFERING) {
            exception = new OPCCriticalException("The server does not support buffering of data items that are" + "collected at a faster rate than the group update rate.", cause);
        } else if (code == OPC_E_INVALIDCONTINUATIONPOINT) {
            exception = new OPCCriticalException("The continuation point is not valid.", cause);
        } else if (code == OPC_S_DATAQUEUEOVERFLOW) {
            exception = new OPCCommunicationException("Data Queue Overflow - Some value transitions were lost.", cause);
        } else if (code == OPC_E_RATENOTSET) {
            exception = new OPCCriticalException("Server does not support requested rate.", cause);
        } else if (code == OPC_E_NOTSUPPORTED) {
            exception = new OPCCriticalException("The server does not support writing of quality and/or timestamp.", cause);
        } else {
            exception = new OPCCommunicationException("Unknown automation error.", cause);
        }
        return exception;
    }
}

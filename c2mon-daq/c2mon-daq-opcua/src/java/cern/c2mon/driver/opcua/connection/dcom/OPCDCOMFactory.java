package cern.c2mon.driver.opcua.connection.dcom;

import java.io.IOException;

import org.apache.log4j.Logger;

import cern.c2mon.driver.opcua.connection.common.impl.OPCCommunicationException;
import cern.c2mon.driver.opcua.connection.common.impl.OPCCriticalException;
import ch.cern.tim.driver.jintegraInterface.IOPCAutoServer;
import ch.cern.tim.driver.jintegraInterface.IOPCGroup;
import ch.cern.tim.driver.jintegraInterface.IOPCGroups;
import ch.cern.tim.driver.jintegraInterface.OPCItem;
import ch.cern.tim.driver.jintegraInterface.OPCItems;
import ch.cern.tim.driver.jintegraInterface.OPCServer;

import com.linar.jintegra.AutomationException;

/**
 * Helper class to create initialized DCOM objects.
 * 
 * @author Andreas Lang
 *
 */
public final class OPCDCOMFactory {
    
    /**
     * logger of this class.
     */
    private final static Logger logger = Logger.getLogger(OPCDCOMFactory.class);
  
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
        logger.debug("Creating OPC Group: name=" + groupName 
                     + ", value-deadband=" + valueDeadband
                     + ", update-rate(ms)=" + updateRate);
        IOPCGroup group = opcGroups.add(groupName);
        group.setDeadBand(valueDeadband);
        group.setUpdateRate(updateRate);
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
      return createWrappedAutomationException(cause, null);
    }
    
    /**
     * Creates a OPC communication exception which wraps an automation exception
     * and adds a readable error message.
     * 
     * @param cause
     *            The original automation exception.
     * @param itemAddress The opc item address, or <code>null</code> if no
     *        specific item address can be given. 
     * @return The OPCCommunication exception.
     */
    public static RuntimeException createWrappedAutomationException(
            final AutomationException cause, final String itemAddress) {
        long code = cause.getCode();
        
        // checks whether the error is a critical OPC error
        for (DCOMErrorConstant error : DCOMErrorConstant.values()) {
          if (code == error.getCode().longValue()) {
            return new OPCCriticalException(error.toString(), cause);
          }
        }
        
        // checks whether the automation exception code is known
        // and creates a communication exception.
        for (COMErrorConstant error : COMErrorConstant.values()) {
          if (code == error.getCode().longValue()) {
            return new OPCCommunicationException(error.toString(), cause);
          }
        }
        
        // If unknown, we use directly the description of the automation exception
        // to create the communication exception
        return new OPCCommunicationException(cause.getDescription(), cause);
    }
}

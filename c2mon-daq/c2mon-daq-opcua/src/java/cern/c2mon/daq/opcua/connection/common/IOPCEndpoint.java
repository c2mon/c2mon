package cern.c2mon.daq.opcua.connection.common;

import java.util.Collection;

import cern.c2mon.daq.opcua.OPCUAAddress;
import cern.c2mon.daq.opcua.connection.common.impl.OPCCommunicationException;
import cern.c2mon.daq.opcua.connection.common.impl.OPCCriticalException;
import cern.tim.shared.common.datatag.address.OPCHardwareAddress;
import cern.tim.shared.daq.command.ISourceCommandTag;
import cern.tim.shared.daq.command.SourceCommandTagValue;
import cern.tim.shared.daq.datatag.ISourceDataTag;

/**
 * Defines an OPC endpoint. An OPC endpoint has exactly the functionality
 * required for the TIM system. It is used to abstract from the different
 * connection types and their libraries.
 * 
 * @author Andreas Lang
 *
 */
public interface IOPCEndpoint {
    
    /**
     * States of the OPC endpoint.
     * <ul>
     *  <li>
     *      <b>NOT_INTIALIZED:</b>
     *      Endpoint has not been injected with the OPCaddress yet. 
     *      Usage will cause exceptions.
     *  </li>
     *  <li>
     *      <b>INTIALIZED:</b>
     *      Endpoint has been injected with the OPCaddress. It can be used.
     *  </li>
     * </ul>
     * @author Andreas Lang
     *
     */
    enum STATE { 
        /**
         * Endpoint has not been injected with the OPCaddress yet.
         * Usage will cause exceptions.
         */
        NOT_INITIALIZED, 
        /**
         * Endpoint has been injected with the OPCaddress. It can be used.
         */
        INITIALIZED,
        /**
         * Endpoint is correctly injected and subscribed to OPC items
         */
        OPERATIONAL
    }
    
    /**
     * Returns the state of the endpoint. See the STATE enumeration for details.
     * 
     * @return The current state of the endpoint.
     */
    STATE getState();
    
    /**
     * Sets the state of the endpoint to operational which means that the 
     * Endpoint is correctly injected and subscribed to OPC items
     */
    void setStateOperational();
    
    /**
     * Checks the connection of the endpoint. Will return without exception if
     * the connection is alive.
     * 
     * @throws OPCCommunicationException Thrown if the connection is not
     * reachable but might be back later on.
     * @throws OPCCriticalException Thrown if the connection is not
     * reachable and can most likely not be restored.
     */
    void checkConnection();
    
    /**
     * This method should be the first to be called. It gives the endpoint
     * all properties which are specific to the enpoint type.
     * 
     * @param address The properties specific to this endpoint.
     */
    void initialize(OPCUAAddress address);
    
    /**
     * Adds a data tag to this endpoint.
     * 
     * @param sourceDataTag The data tag to add.
     */
    void addDataTag(ISourceDataTag sourceDataTag);
    
    /**
     * Removes a data tag from this endpoint.
     * 
     * @param sourceDataTag The data tag to remove.
     */
    void removeDataTag(ISourceDataTag sourceDataTag);

    /**
     * Adds the provided data tags to the endpoint. The endpoint will send
     * updates for all added data tags.
     * 
     * @param dataTags The data tags from which updates should be received.
     */
    void addDataTags(Collection<ISourceDataTag> dataTags);
    
    /**
     * Refreshes the values for the provided data tags. This will work only 
     * for already added data tags.
     * 
     * @param dataTags The data tags which should be updated.
     */
    void refreshDataTags(Collection<ISourceDataTag> dataTags);

    /**
     * Adds the provided command tags to the endpoint. The endpoint will only
     * execute added commands.
     * 
     * @param commandTags The commands which can be executed.
     */
    void addCommandTags(Collection<ISourceCommandTag> commandTags);
    
    /**
     * Adds the provided command tag to the endpoint. The endpoint will only
     * execute added commands.
     * 
     * @param commandTag The command to add.
     */
    void addCommandTag(final ISourceCommandTag commandTag);
    
    /**
     * Removes the provided command tag from the endpoint.
     * 
     * @param commandTag The command to remove.
     */
    void removeCommandTag(final ISourceCommandTag commandTag);
    
    /**
     * Executes a command based on the information of the provided
     * SourceCommandTagValue.
     * 
     * @param hardwareAddress The properties of the command.
     * @param command The value of the command.
     */
    void executeCommand(final OPCHardwareAddress hardwareAddress,
            SourceCommandTagValue command);
    
    /**
     * Writes to the specified address in the OPC.
     * 
     * @param address The address to write to.
     * @param value The value to write.
     */
    void write(final OPCHardwareAddress address, final Object value);

    /**
     * Registers an endpoint listener. The endpoint will inform this listener
     * about changes.
     * 
     * @param endpointListener The listener to add.
     */
    void registerEndpointListener(IOPCEndpointListener endpointListener);

    /**
     * Unregisters an endpoint listener. The listener will no longer be informed
     * about updates.
     * 
     * @param endpointListener The listener to remove.
     */
    void unRegisterEndpointListener(IOPCEndpointListener endpointListener);
    
    /**
     * Stops everything in the endpoint and clears all configuration states.
     */
    void reset();
}

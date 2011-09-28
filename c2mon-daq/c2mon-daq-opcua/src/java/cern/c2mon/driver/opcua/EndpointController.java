package cern.c2mon.driver.opcua;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;

import cern.c2mon.driver.opcua.connection.common.IOPCEndpoint;
import cern.c2mon.driver.opcua.connection.common.IOPCEndpointFactory;
import cern.c2mon.driver.opcua.connection.common.IOPCEndpointListener;
import cern.c2mon.driver.opcua.connection.common.IOPCEndpoint.STATE;
import cern.c2mon.driver.opcua.connection.common.impl.AliveWriter;
import cern.c2mon.driver.opcua.connection.common.impl.OPCCommunicationException;
import cern.c2mon.driver.opcua.connection.common.impl.OPCCriticalException;
import cern.c2mon.driver.opcua.connection.common.impl.StatusChecker;
import cern.tim.driver.common.EquipmentLogger;
import cern.tim.driver.common.EquipmentLoggerFactory;
import cern.tim.driver.common.IEquipmentMessageSender;
import cern.tim.driver.common.conf.equipment.ICommandTagChanger;
import cern.tim.driver.common.conf.equipment.IDataTagChanger;
import cern.tim.driver.common.conf.equipment.IEquipmentConfiguration;
import cern.tim.driver.tools.TIMDriverSimpleTypeConverter;
import cern.tim.shared.common.datatag.address.OPCHardwareAddress;
import cern.tim.shared.daq.command.ISourceCommandTag;
import cern.tim.shared.daq.command.SourceCommandTagValue;
import cern.tim.shared.daq.config.ChangeReport;
import cern.tim.shared.daq.config.ChangeReport.CHANGE_STATE;
import cern.tim.shared.daq.datatag.ISourceDataTag;
import cern.tim.shared.daq.datatag.SourceDataQuality;

/**
 * The endpoint controller is responsible for the handling of a single endpoint.
 * It initializes the connection, registers and unregisters data and command
 * tags and fires commands.
 * 
 * @author Andreas Lang
 * 
 */
public class EndpointController implements IOPCEndpointListener, ICommandTagChanger, IDataTagChanger {
    /**
     * Properties for the opc endpoint.
     */
    private final List<OPCAddress> opcAddresses;

    /**
     * The OPCAddress of the current endpoint.
     */
    private OPCAddress currentAddress;

    /**
     * Factory to create OPCEndpoints.
     */
    private final IOPCEndpointFactory opcEndpointFactory;

    /**
     * The currently controlled endpoint.
     */
    private IOPCEndpoint endpoint;

    /**
     * The EquipmentMessageSender to send updates to.
     */
    private IEquipmentMessageSender sender;

    /**
     * The equipment specific logger factory.
     */
    private final EquipmentLoggerFactory factory;

    /**
     * The equipment specific logger.
     */
    private final EquipmentLogger logger;

    /**
     * Event listener and logger for OPC endpoints to the C2MON equipment
     * logger.
     */
    private final EndpointEquipmentLogListener logListener;

    /**
     * Thread used to reconnect to the endpoint if an exception occurred.
     */
    private Thread reconnectThread;

    /**
     * The alive writer to write to the OPC.
     */
    private AliveWriter writer;

    /**
     * The equipment configuration for his controller.
     */
    private IEquipmentConfiguration equipmentConfiguration;
    
    private Timer statusCheckTimer;

    /**
     * Creates a new EndpointController
     * 
     * @param endPointFactory
     *            The endpoint factory to create OPC endpoints.
     * @param sender
     *            The equipment message sender to send updates to.
     * @param factory
     *            Factory to crate equipmen bound loggers.
     * @param opcAddresses
     *            The addresses for the endpoints.
     * @param equipmentConfiguration
     *            The equipment configuration for this controller.
     */
    public EndpointController(final IOPCEndpointFactory endPointFactory,
            final IEquipmentMessageSender sender,
            final EquipmentLoggerFactory factory,
            final List<OPCAddress> opcAddresses,
            final IEquipmentConfiguration equipmentConfiguration) {
        this.opcEndpointFactory = endPointFactory;
        this.sender = sender;
        this.factory = factory;
        this.logger = factory.getEquipmentLogger(getClass());
        logListener = new EndpointEquipmentLogListener(
                factory.getEquipmentLogger(EndpointEquipmentLogListener.class));
        this.opcAddresses = opcAddresses;
        this.equipmentConfiguration = equipmentConfiguration;
    }

    /**
     * Starts this controllers endpoint. After this method is called the
     * controller will receive updates.
     */
    public synchronized void startEndpoint() {
        try {
            createEndpoint();
            endpoint.addDataTags(equipmentConfiguration.getSourceDataTags().values());
            endpoint.addCommandTags(equipmentConfiguration.getSourceCommandTags().values());
            endpoint.registerEndpointListener(this);
            endpoint.registerEndpointListener(logListener);
            startAliveTimer();
            sender.confirmEquipmentStateOK();
            setUpStatusChecker();
        } catch (OPCCommunicationException e) {
            logger.error(
                    "Endpoint creation failed. Controller will try again. ", e);
            triggerEndpointRestart();
        }
    }

    /**
     * Sets up and schedules a regular status check.
     */
    protected void setUpStatusChecker() {
        stopStatusChecker();
        statusCheckTimer = new Timer("OPCStatusChecker");
        statusCheckTimer.schedule(new StatusChecker(endpoint) {
            
            @Override
            public void onOPCUnknownException(
                    final IOPCEndpoint endpoint, final Exception e) {
                // critical should shutdown
                logger.error("Status of and endpoint could not be determined "
                        + "because of an unexpected exception. Shutting down.", e);
                stop();
            }
            
            @Override
            public void onOPCCriticalException(
                    final IOPCEndpoint endpoint, final OPCCriticalException e) {
                // critical should shutdown
                logger.error("Status of and endpoint could not be determined "
                        + "because of an critical OPC exception. Shutting down.", e);
                stop();
            }
            
            @Override
            public void onOPCCommunicationException(
                    final IOPCEndpoint endpoint, final OPCCommunicationException e) {
                logger.error("OPCCommunication exception try to restart.", e);
                triggerEndpointRestart();
            }
        }, 0, getCurrentOPCAddress().getServerTimeout());
    }

    /**
     * 
     */
    protected void stopStatusChecker() {
        if (statusCheckTimer != null) {
            statusCheckTimer.cancel();
        }
    }

    /**
     * Starts the alive timer.
     */
    public synchronized void startAliveTimer() {
        ISourceDataTag targetTag = equipmentConfiguration.getSourceDataTag(
                equipmentConfiguration.getAliveTagId());
        if (targetTag != null) {
            writer = new AliveWriter(
                    endpoint, equipmentConfiguration.getAliveTagInterval() / 2,
                    targetTag, factory.getEquipmentLogger(AliveWriter.class));
            writer.startWriter();
        }
    }

    /**
     * Stops the alive timer.
     */
    public synchronized void stopAliveTimer() {
        if (writer != null) {
            writer.stopWriter();
        }
    }

    /**
     * Makes sure there is a created and initialized endpoint.
     */
    private void createEndpoint() {
        if (endpoint == null || endpoint.getState() == STATE.NOT_INITIALIZED) {
            OPCAddress address = getNextOPCAddress();
            logger.info("Trying to create endpoint '" + address.getUriString() + "'");
            endpoint = opcEndpointFactory.createEndpoint(address.getProtocol());
            if (endpoint == null && opcAddresses.size() > 1) {
                logger.warn("Endpoint creation for '" + address.getUriString() + "' failed. Trying alternative address.");
                // try alternative address
                address = getNextOPCAddress();
                endpoint = opcEndpointFactory.createEndpoint(address.getProtocol());
            }
            if (endpoint != null) {
                endpoint.initialize(address);
                logger.info("Endpoint '" + address.getUriString() + "' created and initialized");
            } else {
                // There was no type matching an implemented endpoint
                logger.error("Endpoint creation for '" + address.getUriString() + "' failed. Stop Startup.");
                throw new EndpointTypesUnknownException();
            }
        }
    }

    /**
     * Stops this endpoint and goes back to the state before the start method
     * was called.
     */
    public synchronized void stop() {
        stopAliveTimer();
        stopStatusChecker();
        if (endpoint != null)
            endpoint.reset();
        currentAddress = null;
    }

    /**
     * Returns the next available OPCAddress. If there is no second address the
     * first one will be returned.
     * 
     * @return The next available OPCAddress.
     */
    private synchronized OPCAddress getNextOPCAddress() {
        if (currentAddress == null) {
            currentAddress = opcAddresses.get(0);
        } else if (opcAddresses.size() > 1) {
            if (opcAddresses.get(0).equals(currentAddress)) {
                currentAddress = opcAddresses.get(1);
            } else {
                currentAddress = opcAddresses.get(0);
            }
        }
        return currentAddress;
    }
    
    /**
     * Returns the current OPC Address.
     * 
     * @return The OPCAddress used at the moment.
     */
    private synchronized OPCAddress getCurrentOPCAddress() {
    	return currentAddress;
    }

    /**
     * Implementation of the IOPCEndpointListener interface. The endpoint
     * controller will forward updates to the core (EquipmentMessageSender).
     * 
     * @param dataTag
     *            The data tag which has a changed value.
     * @param timestamp
     *            The timestamp when the value was updated.
     * @param tagValue
     *            The changed value.
     */
    @Override
    public void onNewTagValue(final ISourceDataTag dataTag, 
            final long timestamp, final Object tagValue) {
        Object convertedValue;
        if (tagValue == null)
            convertedValue = tagValue;
        else if (tagValue instanceof Number)
            convertedValue = TIMDriverSimpleTypeConverter.convert(
                    dataTag, Double.valueOf(tagValue.toString()));
        else
            convertedValue = TIMDriverSimpleTypeConverter.convert(
                    dataTag, tagValue.toString());
        sender.sendTagFiltered(dataTag, convertedValue, timestamp);
        logger.debug("Tag value sent.");
    }

    /**
     * Refreshes the values of all added data tags.
     */
    public synchronized void refresh() {
        requiresEndpoint();
        logger.info("Refreshing values of all data tags.");
        endpoint.refreshDataTags(equipmentConfiguration.getSourceDataTags().values());
    }

    /**
     * Refreshes the values of the provided source data tag.
     * 
     * @param sourceDataTag
     *            The source data tag to refresh the value for.
     */
    public synchronized void refresh(final ISourceDataTag sourceDataTag) {
        requiresEndpoint();
        Collection<ISourceDataTag> tags = new ArrayList<ISourceDataTag>(1);
        tags.add(sourceDataTag);
        logger.info("Refreshing value of data tag with id '" + sourceDataTag.getId() + "'.");
        endpoint.refreshDataTags(tags);
    }

    /**
     * Invalidates the tag which caused an exception in an endpoint.
     * 
     * @param dataTag
     *            The data tag which caused an exception.
     * @param cause
     *            The exception which was caused in the endpoint.
     */
    @Override
    public void onTagInvalidException(final ISourceDataTag dataTag, final Throwable cause) {
        String decription = "Tag invalid: " + cause.getClass().getSimpleName() + ": " 
            + cause.getMessage();
        logger.debug(decription);
		sender.sendInvalidTag(dataTag, (short) SourceDataQuality.UNKNOWN,
        			decription);
    }

    /**
     * When this is called a serious error happened for our subscriptions. A
     * full restart is required.
     * 
     * @param cause
     *            The cause of the subscription loss.
     */
    @Override
    public void onSubscriptionException(final Throwable cause) {
        logger.error("Subscription failed. Restarting endpoint.", cause);
        triggerEndpointRestart();
    }

    /**
     * Triggers the restart of this endpoint.
     */
    private synchronized void triggerEndpointRestart() {
        if (reconnectThread == null || !reconnectThread.isAlive()) {
            reconnectThread = new Thread() {
                @Override
                public void run() {
                    while (endpoint.getState() != STATE.INITIALIZED) {
                        endpoint.reset();
                        sender.confirmEquipmentStateIncorrect();
                        try {
                            Thread.sleep(getCurrentOPCAddress().getServerRetryTimeout());
                        } catch (InterruptedException e) {
                            logger.error("Subscription restart interrupted!", e);
                        }
                        try {
                            startEndpoint();
                            refresh();
                        } catch (Exception e) {
                            logger.error("Error restarting subscription");
                        }
                    }
                }
            };
            reconnectThread.start();
        }
    }

    /**
     * Runs a command on the current endpoint.
     * 
     * @param commandTag
     *            The command to run.
     * @param sourceCommandTagValue
     *            The value description of the command to run.
     */
    public void runCommand(final ISourceCommandTag commandTag, final SourceCommandTagValue sourceCommandTagValue) {
        requiresEndpoint();
        endpoint.executeCommand((OPCHardwareAddress) commandTag.getHardwareAddress(), sourceCommandTagValue);
    }

    /**
     * Adds a command tag to the controller.
     * 
     * @param sourceCommandTag
     *            The command tag to add.
     * @param changeReport
     *            The report to append information.
     */
    @Override
    public void onAddCommandTag(final ISourceCommandTag sourceCommandTag, final ChangeReport changeReport) {
        logger.info("Adding command tag " + sourceCommandTag.getId());
        requiresEndpoint();
        endpoint.addCommandTag(sourceCommandTag);
        changeReport.appendInfo("CommandTag added.");
        changeReport.setState(CHANGE_STATE.SUCCESS);
        logger.info("Added command tag " + sourceCommandTag.getId());
    }

    /**
     * Removes a command tag from the controller.
     * 
     * @param sourceCommandTag
     *            The command tag to remove.
     * @param changeReport
     *            The report to append information.
     */
    @Override
    public void onRemoveCommandTag(final ISourceCommandTag sourceCommandTag, final ChangeReport changeReport) {
        logger.info("Removing command tag " + sourceCommandTag.getId());
        requiresEndpoint();
        endpoint.removeCommandTag(sourceCommandTag);
        changeReport.appendInfo("CommandTag removed.");
        changeReport.setState(CHANGE_STATE.SUCCESS);
        logger.info("Removed command tag " + sourceCommandTag.getId());

    }

    /**
     * Updates a command tag.
     * 
     * @param sourceCommandTag
     *            The command tag to updates.
     * @param oldSourceCommandTag
     *            A copy of the former command tag state.
     * @param changeReport
     *            The report to append information.
     */
    @Override
    public void onUpdateCommandTag(final ISourceCommandTag sourceCommandTag, final ISourceCommandTag oldSourceCommandTag, final ChangeReport changeReport) {
        logger.info("Updating command tag " + sourceCommandTag.getId());
        requiresEndpoint();
        if (!sourceCommandTag.getHardwareAddress().equals(oldSourceCommandTag.getHardwareAddress())) {
            endpoint.removeCommandTag(oldSourceCommandTag);
            endpoint.addCommandTag(sourceCommandTag);
            changeReport.appendInfo("CommandTag updated.");
        } else {
            changeReport.appendInfo("No changes for OPC necessary.");
        }
        changeReport.setState(CHANGE_STATE.SUCCESS);
        logger.info("Updated command tag " + sourceCommandTag.getId());
    }

    /**
     * Adds a data tag.
     * 
     * @param sourceDataTag
     *            The data tag to add.
     * @param changeReport
     *            The change report object which needs to be filled.
     */
    @Override
    public void onAddDataTag(final ISourceDataTag sourceDataTag, final ChangeReport changeReport) {
        logger.info("Adding data tag " + sourceDataTag.getId());
        requiresEndpoint();
        endpoint.addDataTag(sourceDataTag);
        refresh(sourceDataTag);
        changeReport.appendInfo("DataTag added.");
        changeReport.setState(CHANGE_STATE.SUCCESS);
        logger.info("Added data tag " + sourceDataTag.getId());
    }

    /**
     * Removes a data tag.
     * 
     * @param sourceDataTag
     *            The data tag to remove.
     * @param changeReport
     *            The change report object which needs to be filled.
     */
    @Override
    public void onRemoveDataTag(final ISourceDataTag sourceDataTag, final ChangeReport changeReport) {
        logger.info("Removing data tag " + sourceDataTag.getId());
        requiresEndpoint();
        endpoint.removeDataTag(sourceDataTag);
        changeReport.appendInfo("DataTag removed.");
        changeReport.setState(CHANGE_STATE.SUCCESS);
        logger.info("Removed data tag " + sourceDataTag.getId());
    }

    /**
     * Updates a data tag.
     * 
     * @param sourceDataTag
     *            The data tag to update.
     * @param oldSourceDataTag
     *            A copy of the former state of the data tag.
     * @param changeReport
     *            The change report object which needs to be filled.
     */
    @Override
    public void onUpdateDataTag(final ISourceDataTag sourceDataTag, final ISourceDataTag oldSourceDataTag, final ChangeReport changeReport) {
        logger.info("Updating data tag " + sourceDataTag.getId());
        requiresEndpoint();
        if (!sourceDataTag.getHardwareAddress().equals(oldSourceDataTag.getHardwareAddress())) {
            endpoint.removeDataTag(oldSourceDataTag);
            endpoint.addDataTag(sourceDataTag);
            changeReport.appendInfo("Data tag updated.");
        } else {
            changeReport.appendInfo("No changes for OPC necessary.");
        }
        changeReport.setState(CHANGE_STATE.SUCCESS);
        logger.info("Updated data tag " + sourceDataTag.getId());
    }

    /**
     * Checks if there is an endpoint created. If not an exception is thrown.
     */
    private void requiresEndpoint() {
        if (endpoint == null || endpoint.getState() == STATE.NOT_INITIALIZED)
            throw new OPCCriticalException(
                    "No Endpoint was created or Endpoint was not initialized/started.");
    }

}

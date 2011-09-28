package ch.cern.tim.driver.opc;

import java.util.List;

import cern.c2mon.driver.opcua.EndpointController;
import cern.c2mon.driver.opcua.EndpointTypesUnknownException;
import cern.c2mon.driver.opcua.OPCAddress;
import cern.c2mon.driver.opcua.OPCAddressException;
import cern.c2mon.driver.opcua.OPCAddressParser;
import cern.c2mon.driver.opcua.connection.common.IOPCEndpointFactory;
import cern.c2mon.driver.opcua.connection.common.impl.DefaultOPCEndpointFactory;
import cern.c2mon.driver.opcua.connection.common.impl.OPCCriticalException;
import cern.tim.driver.common.EquipmentMessageHandler;
import cern.tim.driver.common.ICommandRunner;
import cern.tim.driver.common.conf.equipment.IEquipmentConfiguration;
import cern.tim.driver.common.conf.equipment.IEquipmentConfigurationChanger;
import cern.tim.driver.tools.equipmentexceptions.EqCommandTagException;
import cern.tim.driver.tools.equipmentexceptions.EqIOException;
import cern.tim.shared.daq.command.ISourceCommandTag;
import cern.tim.shared.daq.command.SourceCommandTagValue;
import cern.tim.shared.daq.config.ChangeReport;
import cern.tim.shared.daq.config.ChangeReport.CHANGE_STATE;
import cern.tim.shared.daq.datatag.ISourceDataTag;
/**
 * The OPCMessageHandler is the entry point of the application. It is created
 * and called by the core. Here the OPC module can access the configuration and
 * register listeners for optional events.
 * 
 * @author Andreas Lang
 *
 */
public class OPCMessageHandler extends EquipmentMessageHandler
        implements ICommandRunner, IEquipmentConfigurationChanger {
    
    /**
     * Delay to restart the DAQ after an equipment change.
     */
    private static final long RESTART_DELAY = 2000L;

    /**
     * This parser helps to split up the string provided as equipment address
     * from the core.
     */
    private final OPCAddressParser opcAddressParser = new OPCAddressParser();
    
    /**
     * The endpoint controller for this module.
     */
    private EndpointController controller;

    /**
     * Called when the core wants the OPC module to start up and connect to the
     * OPC server.
     * 
     * @throws EqIOException Throws an {@link EqIOException} if there is an IO
     * problem during startup.
     */
    @Override
    public synchronized void connectToDataSource() throws EqIOException {
        IEquipmentConfiguration config = getEquipmentConfiguration();
        getEquipmentLogger().debug("starting connect to OPC data source");
        try {
            List<OPCAddress> opcAddresses = 
                opcAddressParser.parseAddressString(config.getAddress());
            getEquipmentLogger().debug("creating endpoint");
            IOPCEndpointFactory endpointFactory = new DefaultOPCEndpointFactory();
            controller = new EndpointController(
                    endpointFactory, getEquipmentMessageSender(), 
                    getEquipmentLoggerFactory(),
                    opcAddresses, config);
            getEquipmentLogger().debug("starting endpoint");
            controller.startEndpoint();
            getEquipmentLogger().debug("endpoint started");
        } catch (OPCAddressException e) {
            throw new EqIOException(
                    "OPC address configuration string is invalid.", e);
        } catch (EndpointTypesUnknownException e) {
            throw new EqIOException(
                    "The configured protocol(s) could not be matched to an "
                    + "endpoint implementation.", e);
        } catch (OPCCriticalException e) {
            throw new EqIOException("Endpoint creation failed.", e);
        }
        getEquipmentCommandHandler().setCommandRunner(this);
        getEquipmentConfigurationHandler().setCommandTagChanger(controller);
        getEquipmentConfigurationHandler().setDataTagChanger(controller);
        getEquipmentConfigurationHandler().setEquipmentConfigurationChanger(this);
    }

    /**
     * Called when the core wants the OPC module to disconnect from the OPC
     * server and discard all configuration.
     * 
     * @throws EqIOException Throws an {@link EqIOException} if there is an IO
     * problem during stop.
     */
    @Override
    public synchronized void disconnectFromDataSource() throws EqIOException {
        getEquipmentLogger().debug("disconnecting from OPC data source...");
        controller.stop();
        getEquipmentLogger().debug("disconnected");
    }

    /**
     * Triggers the refresh of all values directly from the OPC server.
     */
    @Override
    public synchronized void refreshAllDataTags() {
        getEquipmentLogger().debug("refreshing data tags");
        controller.refresh();
    }

    /**
     * Triggers the refresh of a single value directly from the OPC server.
     * 
     * @param dataTagId The id of the data tag to refresh.
     */
    @Override
    public synchronized void refreshDataTag(final long dataTagId) {
        getEquipmentLogger().debug("refreshing data tag " + dataTagId);
        ISourceDataTag sourceDataTag = 
            getEquipmentConfiguration().getSourceDataTag(dataTagId);
        if (sourceDataTag == null)
            throw new OPCCriticalException("SourceDataTag with id '" + dataTagId 
                    + "' unknown.");
        controller.refresh(sourceDataTag);
    }
    
    /**
     * Runs a command on the current endpoint.
     * 
     * @param sourceCommandTagValue the value for the command
     * @throws EqCommandTagException This exception is thrown if the command
     * fails.
     * @return String with command result.
     */
    @Override
    public synchronized String runCommand(
            final SourceCommandTagValue sourceCommandTagValue) 
            throws EqCommandTagException {
        Long commandId = sourceCommandTagValue.getId();
        ISourceCommandTag commandTag = 
            getEquipmentConfiguration().getSourceCommandTag(commandId);
        if (commandTag == null) {
            throw new EqCommandTagException("Command tag with id '" + commandId
                    + "' unknown!");
        }
        try {
            getEquipmentLogger().debug("running command " + commandId
                    + " with value " + sourceCommandTagValue.getValue());
            controller.runCommand(commandTag, sourceCommandTagValue);
        } catch (EndpointTypesUnknownException e) {
            throw new EqCommandTagException("The configuration contained no "
                    + "usable endpoint addresses.");
        } catch (Exception e) {
            throw new EqCommandTagException("Unexpected exception while "
                    + "executing command.", e);
        }
        return null;
    }
    
    /**
     * Makes sure the changes to the equipment are applied on OPC level.
     * 
     * @param equipmentConfiguration The new equipment configuration.
     * @param oldEquipmentConfiguration A clone of the old equipment configuration.
     * @param changeReport Report object to fill.
     */
    @Override
    public synchronized void onUpdateEquipmentConfiguration(
            final IEquipmentConfiguration equipmentConfiguration,
            final IEquipmentConfiguration oldEquipmentConfiguration,
            final ChangeReport changeReport) {
        if (equipmentConfiguration.getAddress().equals(
                oldEquipmentConfiguration.getAddress())) {
            try {
                disconnectFromDataSource();
                Thread.sleep(RESTART_DELAY);
                connectToDataSource();
                changeReport.appendInfo("DAQ restarted.");
            } catch (EqIOException e) {
                changeReport.appendError("Restart of DAQ failed.");
            } catch (InterruptedException e) {
                changeReport.appendError("Restart delay interrupted. DAQ will not connect.");
            }
        }
        else if (equipmentConfiguration.getAliveTagId() 
                != oldEquipmentConfiguration.getAliveTagId()
            || equipmentConfiguration.getAliveTagInterval() 
                != oldEquipmentConfiguration.getAliveTagInterval()) {
            controller.stopAliveTimer();
            controller.startAliveTimer();
            changeReport.appendInfo("Alive Timer updated.");
        }
        changeReport.setState(CHANGE_STATE.SUCCESS);
    }

}

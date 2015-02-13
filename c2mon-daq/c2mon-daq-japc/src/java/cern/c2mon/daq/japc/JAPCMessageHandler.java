// TIM. CERN. All rights reserved.
//
// T Nick: Date: Info:
// -------------------------------------------------------------------------
// D jstowisek --/May/2006 Implementation of the prototype
// P wbuczak 23/May/2006 handler implementation
// P wbuczak 25/Jun/2010  upgrade to newest JAPC. Refactoring.
//                                      Support for various JAPC protocols.
//
//
// -------------------------------------------------------------------------

package cern.c2mon.daq.japc;

import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.common.ICommandRunner;
import cern.c2mon.daq.tools.equipmentexceptions.EqCommandTagException;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.shared.common.command.ISourceCommandTag;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.address.JAPCHardwareAddress;
import cern.c2mon.shared.daq.command.SourceCommandTagValue;
import cern.japc.Parameter;
import cern.japc.ParameterValue;
import cern.japc.Selector;
import cern.japc.factory.ParameterFactory;
import cern.japc.factory.ParameterValueFactory;
import cern.japc.spi.ParameterUrl;
import cern.japc.spi.ParameterUrlImpl;

/**
 * This is a specialized subclass of the general EquipmentMessageHandler. The class implements an
 * EquipmentMessageHandler for TIM DAQ for JAPC protocol.
 */
public class JAPCMessageHandler extends EquipmentMessageHandler implements ICommandRunner {

    /**
     * JAPC controller
     */
    private JAPCController japcController;

    // private final TIMDriverSimpleTypeConverter converter = new TIMDriverSimpleTypeConverter();

    public JAPCMessageHandler() {
        super();
    }

    @Override
    public void connectToDataSource() throws EqIOException {
        getEquipmentLogger().debug("entering connectToDataSource()..");

        // If this is the first time this method is called (on start-up), create
        // a JAPC controller
        if (this.japcController == null) {
            this.japcController = new JAPCController(getEquipmentLoggerFactory(), getEquipmentConfiguration(),
                    getEquipmentMessageSender());
        }


        // If this is the first time this method is called (on start-up), create
        // a JAPC parameter factory
        if (this.japcController.getParameterFactory() == null) {
            try {
                this.japcController.setParameterFactory(ParameterFactory.newInstance());
                // We do not really have an Equipment but it indicates at least that
                // the factory creates went fine.
                getEquipmentMessageSender().confirmEquipmentStateOK();
            } catch (Exception e) {
                getEquipmentMessageSender().confirmEquipmentStateIncorrect(
                        "Unexpected problem occured when trying to create a JAPC ParameterFactory instance");

                getEquipmentLogger().error(
                                "connectToDataSource() : Unexpected problem occured when trying to create a JAPC ParameterFactory", e);
                throw new EqIOException("Unexpected problem occured while creating instance of ParameterFactory: "
                        + e.getMessage());
            }
        }

        // Add Data Tag Changer
        JAPCDataTagChanger dataTagChanger = new JAPCDataTagChanger(this.japcController, getEquipmentLogger(JAPCDataTagChanger.class));
        getEquipmentConfigurationHandler().setDataTagChanger(dataTagChanger);

        // Connection
        for (ISourceDataTag sourceDataTag : getEquipmentConfiguration().getSourceDataTags().values()) {
            this.japcController.connection(sourceDataTag, null);
        }

        getEquipmentLogger().debug("leaving connectToDataSource()");
    }

    @Override
    public void disconnectFromDataSource() throws EqIOException {
        getEquipmentLogger().debug("entering diconnectFromDataSource()..");

        if (this.japcController != null) {
            for (String key : this.japcController.getJAPCSubscriptions().keySet()) {
                // The Key is the TopicName and the Value the DipSubscription
                this.japcController.disconnection(this.japcController.getJAPCSubscriptions().get(key), null);

            }
        }
        else {
            getEquipmentLogger().debug("disconnectFromDataSource - japcController was not initialice (null)");
        }

        getEquipmentLogger().debug("leaving diconnectFromDataSource()");
    }


    @Override
    public void refreshAllDataTags() {
        // TODO Implement this method at the moment it might be part of the connectToDataSourceMehtod
    }

    @Override
    public void refreshDataTag(long dataTagId) {
        // TODO Implement this method.
    }

    @Override
    public String runCommand(SourceCommandTagValue sourceCommandTagValue) throws EqCommandTagException {
        sendCommand(sourceCommandTagValue);
        return null;
    }

    // TODO move to implementation of ICommandRunner
    protected void sendCommand(SourceCommandTagValue p0) throws EqCommandTagException {
        getEquipmentLogger().debug("entering sendCommand()..");

        ISourceCommandTag sct = getEquipmentConfiguration().getSourceCommandTags().get(p0.getId());

        if (sct == null) {
            throw new EqCommandTagException(String.format(
                    "command #%d is not registered. Please check DAQ configuration", p0.getId()));
        }

        JAPCHardwareAddress addr = (JAPCHardwareAddress) sct.getHardwareAddress();
        ParameterUrl pUrl = null;
        try {

            // make sure protocol and service are correctly specified
            String protocol = JAPCController.checkProtocol(addr.getProtocol());
            String service = JAPCController.checkService(addr.getService());

            String dataField = addr.getDataFieldName();
            if (dataField != null && dataField.length() == 0) {
                dataField = null;
            }

            pUrl = new ParameterUrlImpl(protocol, service, addr.getDeviceName(), addr.getPropertyName(), dataField);

            Parameter parameter = ParameterFactory.newInstance().newParameter(pUrl);

            // Create a selector for the parameter
            Selector selector = ParameterValueFactory.newSelector(addr.getCycleSelector());
            ParameterValue pv = ParameterValueFactory.newParameterValue(p0.getValue());

            parameter.setValue(selector, pv);

        } catch (Exception e) {
            throw new EqCommandTagException("command execution failed. could not set value: " + p0.getValue()
                    + " for parameter: " + pUrl + " Error: " + e.getMessage());
        }

        getEquipmentLogger().debug("leaving sendCommand()");
    }
}

/*
 * $Id $
 * 
 * This class implements C2MON Equipment Handler for BIC
 * 
 * Copyright CERN 2012, All Rights Reserved.
 */
package cern.c2mon.driver.bic;

import cern.tim.driver.common.EquipmentMessageHandler;
import cern.tim.driver.common.conf.equipment.ICommandTagChanger;
import cern.tim.driver.common.conf.equipment.IDataTagChanger;
import cern.tim.driver.common.conf.equipment.IEquipmentConfiguration;
import cern.tim.driver.common.conf.equipment.IEquipmentConfigurationChanger;
import cern.tim.driver.tools.equipmentexceptions.EqIOException;
import cern.tim.shared.daq.command.ISourceCommandTag;
import cern.tim.shared.daq.config.ChangeReport;
import cern.tim.shared.daq.datatag.ISourceDataTag;

public class BICMessageHandler extends EquipmentMessageHandler implements ICommandTagChanger, IDataTagChanger,
        IEquipmentConfigurationChanger {

    /**
     * This method is called by the DAQ core right after an instance of the BICMessageHandler is created You need to
     * implement it in order to open subscriptions for all DataTags assigned to this handler
     * 
     * @throws EqIOException - is proper configuration is not possible for any reason (e.g. missing some mandatory data
     *             in the configuration)
     */
    @Override
    public void connectToDataSource() throws EqIOException {
        // TODO Auto-generated method stub

    }

    /**
     * This method is called by the DAQ core when the DAQ is about to be stopped. You can use it as a place to put the
     * code for closing any previously allocated resources (e.g. connection/subscription handlers etc..)
     * 
     * @throws EqIOException - is proper disconnection is not possible for any reason 
     */
    @Override
    public void disconnectFromDataSource() throws EqIOException {
        // TODO Auto-generated method stub

    }

    /**
     * This method is called when a request is received from the server to add new CommandTag. You should implement that
     * method so that your handler would be ready to receive and execute this new command.
     */
    @Override
    public void onAddCommandTag(ISourceCommandTag arg0, ChangeReport arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onRemoveCommandTag(ISourceCommandTag arg0, ChangeReport arg1) {
        // TODO Auto-generated method stub

    }

    /**
     * This method is called when a request is received from the server to change CommandTag's configuration
     */
    @Override
    public void onUpdateCommandTag(ISourceCommandTag arg0, ISourceCommandTag arg1, ChangeReport arg2) {
        // TODO Auto-generated method stub

    }

    /**
     * This method is called when a request is received from the server to add new DataTag. You should implement proper
     * code to open a subscription for that DataTag etc. so that updates of that DataTag would be sent to the server,
     * from that moment on.
     */
    @Override
    public void onAddDataTag(ISourceDataTag arg0, ChangeReport arg1) {
        // TODO Auto-generated method stub

    }

    /**
     * This method is called when a request is received from the server to remove particular DataTag. You should
     * implement proper code to close a subscription for that DataTag etc. so that updates of that DataTag would no
     * longer be sent to the server.
     */
    @Override
    public void onRemoveDataTag(ISourceDataTag arg0, ChangeReport arg1) {
        // TODO Auto-generated method stub

    }

    /**
     * This method is called when a request is received from the server to change DataTag's configuration
     */
    @Override
    public void onUpdateDataTag(ISourceDataTag arg0, ISourceDataTag arg1, ChangeReport arg2) {
        // TODO Auto-generated method stub

    }

    /**
     * This method is called when a request is received from the server to change the equipment handler's configuration
     */
    @Override
    public void onUpdateEquipmentConfiguration(IEquipmentConfiguration arg0, IEquipmentConfiguration arg1,
            ChangeReport arg2) {
        // TODO Auto-generated method stub

    }

    /**
     * This method is called when the server requests all DataTags to be resent
     */
    @Override
    public void refreshAllDataTags() {
        // TODO Auto-generated method stub

    }

    /**
     * This method is called when the server requests a particular tag to be resent
     */
    @Override
    public void refreshDataTag(long arg0) {
        // TODO Auto-generated method stub

    }

}

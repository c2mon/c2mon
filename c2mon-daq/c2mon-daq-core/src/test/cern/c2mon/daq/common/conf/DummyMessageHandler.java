package cern.c2mon.daq.common.conf;

import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;

/**
 * this message handler is used for testing runtime equipment-units adding and removing
 */
public class DummyMessageHandler extends EquipmentMessageHandler {

    @Override
    public void connectToDataSource() throws EqIOException {
        
        // just send commfault tag
        //getEquipmentMessageSender().confirmEquipmentStateOK();
    }

    @Override
    public void disconnectFromDataSource() throws EqIOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void refreshAllDataTags() {
        // TODO Auto-generated method stub

    }

    @Override
    public void refreshDataTag(long dataTagId) {
        // TODO Auto-generated method stub

    }
    
}

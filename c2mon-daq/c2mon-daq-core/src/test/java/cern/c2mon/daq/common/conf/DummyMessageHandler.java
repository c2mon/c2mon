/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
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

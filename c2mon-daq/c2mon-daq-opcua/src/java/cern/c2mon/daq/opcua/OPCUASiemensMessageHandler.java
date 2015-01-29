/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005 - 2014 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/

package cern.c2mon.daq.opcua;

import java.util.List;

import cern.c2mon.daq.opcua.connection.common.AbstractOPCUAMessageHandler;
import cern.c2mon.daq.opcua.connection.common.IOPCEndpointFactory;
import cern.c2mon.daq.opcua.connection.common.impl.DefaultOPCEndpointFactory;
import cern.c2mon.daq.opcua.connection.common.impl.EndpointControllerSiemens;
import cern.c2mon.daq.opcua.connection.common.impl.OPCCriticalException;
import cern.c2mon.daq.opcua.connection.common.impl.OPCUASiemensAddress;
import cern.c2mon.daq.opcua.connection.common.impl.OPCUASiemensAddressParser;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.shared.common.process.IEquipmentConfiguration;


/**
 * The OPCSiemensMessageHandler is a special case for Automatic 
 * Master/Slave failover detection for Siemens OPCs
 * 
 * @author Nacho Vilches
 *
 */
public class OPCUASiemensMessageHandler extends AbstractOPCUAMessageHandler {
    
    /**
     * This parser helps to split up the string provided as equipment address
     * from the core.
     */
    private final OPCUASiemensAddressParser siemensAddressParser = new OPCUASiemensAddressParser();
    
    
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
        getEquipmentLogger().debug("connectToDataSource - starting connect to Siemens OPCUA data source");
        try {
            List<OPCUASiemensAddress> opcuaSiemensAddresses = this.siemensAddressParser.createOPCAddressFromAddressString(config.getAddress());
            getEquipmentLogger().debug("connectToDataSource - creating endpoint");
            IOPCEndpointFactory endpointFactory = new DefaultOPCEndpointFactory();
            this.controller = new EndpointControllerSiemens(endpointFactory, getEquipmentMessageSender(), getEquipmentLoggerFactory(),
                    opcuaSiemensAddresses, config);
            getEquipmentLogger().debug("connectToDataSource - starting endpoint");
            if (!this.controller.startEndpoint()) {
                getEquipmentLogger().debug("connectToDataSource - endpoint NOT started");
            } else {
                getEquipmentLogger().debug("connectToDataSource - endpoint started");
            }
        } catch (OPCAUAddressException e) {
            throw new EqIOException(
                    "OPCUA Siemens address configuration string is invalid.", e);
        } catch (EndpointTypesUnknownException e) {
            throw new EqIOException(
                    "The configured protocol(s) could not be matched to an "
                    + "Siemens endpoint implementation.", e);
        } catch (OPCCriticalException e) {
            throw new EqIOException("Siemens Endpoint creation failed.", e);
        }
        getEquipmentCommandHandler().setCommandRunner(this);
        getEquipmentConfigurationHandler().setCommandTagChanger(this.controller);
        getEquipmentConfigurationHandler().setDataTagChanger(this.controller);
        getEquipmentConfigurationHandler().setEquipmentConfigurationChanger(this);
    }

}

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
package cern.c2mon.daq.opcua;

import java.util.List;

import cern.c2mon.daq.opcua.connection.common.AbstractOPCUAMessageHandler;
import cern.c2mon.daq.opcua.connection.common.IOPCEndpointFactory;
import cern.c2mon.daq.opcua.connection.common.impl.DefaultOPCEndpointFactory;
import cern.c2mon.daq.opcua.connection.common.impl.EndpointControllerDefault;
import cern.c2mon.daq.opcua.connection.common.impl.OPCCriticalException;
import cern.c2mon.daq.opcua.connection.common.impl.OPCUADefaultAddress;
import cern.c2mon.daq.opcua.connection.common.impl.OPCUADefaultAddressParser;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.shared.common.process.IEquipmentConfiguration;
/**
 * The OPCMessageHandler is the entry point of the application. It is created
 * and called by the core. Here the OPC module can access the configuration and
 * register listeners for optional events.
 * 
 * @author Andreas Lang, Nacho Vilches
 *
 */
public class OPCUAMessageHandler extends AbstractOPCUAMessageHandler {
  
    /**
     * This parser helps to split up the string provided as equipment address
     * from the core.
     */
    private final OPCUADefaultAddressParser opcDefaultAddressParser = new OPCUADefaultAddressParser();

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
        getEquipmentLogger().debug("connectToDataSource - starting connect to OPC data source");
        try {
            List<OPCUADefaultAddress> opcuaDefaultAddresses = this.opcDefaultAddressParser.createOPCAddressFromAddressString(config.getAddress());
            getEquipmentLogger().debug("connectToDataSource - creating endpoint");
            IOPCEndpointFactory endpointFactory = new DefaultOPCEndpointFactory();
            controller = new EndpointControllerDefault(
                    endpointFactory, getEquipmentMessageSender(), 
                    getEquipmentLoggerFactory(),
                    opcuaDefaultAddresses, config);
            getEquipmentLogger().debug("connectToDataSource - starting endpoint");
            if (!controller.startEndpoint()) {
                getEquipmentLogger().debug("connectToDataSource - endpoint NOT started");
            } else {
                getEquipmentLogger().debug("connectToDataSource - endpoint started");
            }
        } catch (OPCAUAddressException e) {
            throw new EqIOException(
                    "OPC address configuration string is invalid.", e);
        } catch (EndpointTypesUnknownException e) {
            throw new EqIOException(
                    "The configured protocol(s) could not be matched to an endpoint implementation.", e);
        } catch (OPCCriticalException e) {
            throw new EqIOException("Endpoint creation failed. Reason: " + e.getMessage(), e);
        }
        getEquipmentCommandHandler().setCommandRunner(this);
        getEquipmentConfigurationHandler().setCommandTagChanger(controller);
        getEquipmentConfigurationHandler().setDataTagChanger(controller);
        getEquipmentConfigurationHandler().setEquipmentConfigurationChanger(this);
    }
}

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

package cern.c2mon.daq.opcua.connection.common.impl;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import cern.c2mon.daq.opcua.OPCAUAddressException;
import cern.c2mon.daq.opcua.connection.common.AbstractOPCUAAddressParser;

/**
 * The OPCAUASiemensAdress parser extends OPCAUAddressParser and takes a String 
 * in the format provided from the core and translates it to a list of properties
 * for Siemens PLCs
 * 
 * @author Nacho Vilches
 * 
 */
public class OPCUASiemensAddressParser extends AbstractOPCUAAddressParser {

    /**
     * REDUNDANT_SERVER_STATE_NAME
     * 
     * Internal value provided by Siemens that is set to true 
     * when the OPC is in the Role of the Master
     */
    public static final String REDUNDANT_SERVER_STATE_NAME = "redundantServerStateName";
    
    /**
     * 
     */
    private String redundantServerStateName;
    
    /**
     * A Siemens OPC address String has the form:
     * 
     * <pre>
     * URI=protocol1://host1[:port1]/[path1][,protocol2://host2[:port2]/[path2]];
     * user=user1[@domain1][,user2[@domain2]];password=password1[,password2];
     * serverTimeout=serverTimeout;serverRetryTimeout=serverRetryTimeout
     * [;aliveWriter=true|false];redundantServerStateName=redundantServerStateName
     * </pre>
     * 
     * The parts in brackets are optional.
     */
    @Override
    public List<OPCUASiemensAddress> createOPCAddressFromAddressString(String address) {
        List<OPCUASiemensAddress> addresses = new ArrayList<OPCUASiemensAddress>(2);
        
        try {    
            // Parse the properties from the Address string
            Properties properties = parsePropertiesFromString(address);
            // Get the property variables
            getProperties(properties);
            
            OPCUASiemensAddress primaryAddress = createOPCAddress(this.uris[0], this.usersAtDomains[0], this.passwords[0]);
            addresses.add(primaryAddress);
            if (uris.length > 1) {
                OPCUASiemensAddress alternativeAddress = createOPCAddress(this.uris[1], 
                        this.usersAtDomains.length > 1 ? this.usersAtDomains[1] : null,
                        this.passwords.length > 1 ? this.passwords[1] : null);
                addresses.add(alternativeAddress);
            }
        } catch (Exception ex) {
            throw new OPCCriticalException(
                    "Address parsing failed. Address: " + address, ex);
        }
        return addresses;
    }
    
    @Override
    protected OPCUASiemensAddress createOPCAddress(final String uri, final String userAtDomain, final String password) {
        OPCUASiemensAddress opcuaSiemensAddress;
        try {
            opcuaSiemensAddress = (OPCUASiemensAddress) new OPCUASiemensAddress.BuilderSiemens(uri.trim(), this.serverTimeout, this.serverRetryTimeout, 
                    this.redundantServerStateName)
                .userAtDomain(userAtDomain != null ? userAtDomain.trim() : null)
                .password(password.trim())
                .aliveWriter(this.aliveWriter)
                .build();
        } catch (URISyntaxException e) {
            throw new OPCAUAddressException("Syntax of Siemens OPC URI is incorrect: " + uri, e);
        }
        return opcuaSiemensAddress;
    }
    
    @Override
    protected void getProperties (final Properties properties) {
        super.getProperties(properties);
        this.redundantServerStateName = properties.getProperty(REDUNDANT_SERVER_STATE_NAME);
    }
}

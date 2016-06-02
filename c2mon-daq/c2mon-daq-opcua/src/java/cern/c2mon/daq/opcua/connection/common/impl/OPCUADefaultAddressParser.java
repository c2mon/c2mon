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

package cern.c2mon.daq.opcua.connection.common.impl;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import cern.c2mon.daq.opcua.OPCAUAddressException;
import cern.c2mon.daq.opcua.connection.common.AbstractOPCUAAddressParser;

/**
 *
 *
 * @author vilches
 */
public class OPCUADefaultAddressParser extends AbstractOPCUAAddressParser {

    /**
     * A Default OPC address String has the form:
     *
     * <pre>
     * URI=protocol1://host1[:port1]/[path1][,protocol2://host2[:port2]/[path2]];
     * user=user1[@domain1][,user2[@domain2]];password=password1[,password2];
     * serverTimeout=serverTimeout;serverRetryTimeout=serverRetryTimeout
     * [;aliveWriter=true|false]
     * </pre>
     *
     */
    @Override
    public List<OPCUADefaultAddress> createOPCAddressFromAddressString(String address) {
        List<OPCUADefaultAddress> addresses = new ArrayList<OPCUADefaultAddress>(2);

        try {
            // Parse the properties from the Address string
            Properties properties = parsePropertiesFromString(address);
            // Get the property variables
            getProperties(properties);

            OPCUADefaultAddress primaryAddress = createOPCAddress(this.uris[0], this.usersAtDomains[0], this.passwords[0]);
            addresses.add(primaryAddress);
            if (uris.length > 1) {
                OPCUADefaultAddress alternativeAddress = createOPCAddress(this.uris[1],
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
    protected OPCUADefaultAddress createOPCAddress(final String uri, final String userAtDomain, final String password) {
        OPCUADefaultAddress opcuaDefaultAddress;
        try {
            opcuaDefaultAddress = (OPCUADefaultAddress) new OPCUADefaultAddress.DefaultBuilder(uri.trim(), this.serverTimeout, this.serverRetryTimeout)
                .vendor(vendor)
                .userAtDomain(userAtDomain != null ? userAtDomain.trim() : null)
                .password(password.trim())
                .aliveWriterEnabled(this.aliveWriter)
                .build();
        } catch (URISyntaxException e) {
            throw new OPCAUAddressException("Syntax of OPC URI is incorrect: " + uri, e);
        }
        return opcuaDefaultAddress;
    }
}

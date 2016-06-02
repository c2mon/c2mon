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

package cern.c2mon.daq.opcua.connection.common;

import java.util.List;
import java.util.Properties;

/**
 * AbstractOPCUAAddressParser abstract class (no implementation of this class)
 *
 * @author vilches
 */
public abstract class AbstractOPCUAAddressParser {

    public enum AddressKeys {
      /**
       * URI key in the address string.
       */
      URI,
      /**
       * user key in the address string.
       */
      user,
      /**
       * password key in the address string.
       */
      password,
      /**
       * serverTimeout key in the address string.
       */
      serverTimeout,
      /**
       * serverRetryTimeout key in the address string.
       */
      serverRetryTimeout,

      /**
       * aliveWriter key in the address string
       */
      aliveWriter,

      /**
       * Optional property to define the vendor implementation
       */
      vendor;
    }



    protected String[] uris;
    protected String[] usersAtDomains;
    protected String[] passwords;
    protected int serverTimeout;
    protected int serverRetryTimeout;
    /** optional value which is set to true, if not specified.*/
    protected boolean aliveWriter;
    /** optional value for tcp.ua protocol */
    protected String vendor;

    /**
     * Creates a properties object which has the properties defined in the
     * provided address String.
     *
     * @param address The address String.
     *
     * @return The properties object with the properties from the provided
     *         address String.
     */
    public Properties parsePropertiesFromString (final String address) {
        Properties properties = new Properties();

        String[] keyValues = address.split(";");
        for (String keyValueString : keyValues) {
            String[] keyValuePair = keyValueString.trim().split("=");
            // if there is nothing to split ignore it
            if (keyValuePair.length > 1) {
                String key = keyValuePair[0].trim();
                String value = keyValuePair[1].trim();
                properties.put(key, value);
            }
        }

        return properties;
    }

    /**
     * Fill the local variables with the Properties parsed from the String
     *
     * @param properties
     */
    protected void getProperties (final Properties properties) {
        this.uris = properties.getProperty(AddressKeys.URI.name()).split(",");
        this.usersAtDomains = properties.getProperty(AddressKeys.user.name(),"").split(",");
        this.passwords = properties.getProperty(AddressKeys.password.name(),"").split(",");
        this.serverTimeout = Integer.valueOf(properties.getProperty(AddressKeys.serverTimeout.name()));
        this.serverRetryTimeout = Integer.valueOf(properties.getProperty(AddressKeys.serverRetryTimeout.name()));
        this.aliveWriter = Boolean.valueOf(properties.getProperty(AddressKeys.aliveWriter.name(), "true"));
        this.vendor = properties.getProperty(AddressKeys.vendor.name(), "");
    }

    /**
     * Creates a properties object which has the properties defined in the
     * provided address String
     *
     * @param address The address String.
     *
     * @return The Address object with the properties from the provided address String.
     */
    public abstract List<? extends AbstractOPCUAAddress> createOPCAddressFromAddressString(final String address);

    /**
     * Creates an OPCUA address.
     *
     * @param uri The URI for the address.
     * @param userAtDomain The user and domain in the format user@domain.
     * @param password The password for authentication.
     *
     * @return The OPCUA address
     */
    protected abstract AbstractOPCUAAddress createOPCAddress(final String uri, final String userAtDomain, final String password);

}

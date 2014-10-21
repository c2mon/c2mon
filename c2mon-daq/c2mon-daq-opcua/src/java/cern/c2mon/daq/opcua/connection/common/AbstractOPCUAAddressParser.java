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

package cern.c2mon.daq.opcua.connection.common;

import java.util.List;
import java.util.Properties;

/**
 * AbstractOPCUAAddressParser abstract class (no implementation of this class)
 * 
 * @author vilches
 */
public abstract class AbstractOPCUAAddressParser {
    /**
     * URI key in the address string.
     */
    public static final String URI_KEY = "URI";
    /**
     * user key in the address string.
     */
    public static final String USER_KEY = "user";
    /**
     * password key in the address string.
     */
    public static final String PASSWORD_KEY = "password";
    /**
     * serverTimeout key in the address string.
     */
    public static final String SERVER_TIMEOUT_KEY = "serverTimeout";
    /**
     * serverRetryTimeout key in the address string.
     */
    public static final String SERVER_RETRY_TIMEOUT_KEY = "serverRetryTimeout";
    
    /**
     * aliveWriter key in the address string
     */
    public static final String ALIVE_WRITER_KEY = "aliveWriter";
    
    protected String[] uris;
    protected String[] usersAtDomains;
    protected String[] passwords;
    protected int serverTimeout;
    protected int serverRetryTimeout;
    // optional value which is set to true, if not specified.
    protected boolean aliveWriter;
    
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
        this.uris = properties.getProperty(URI_KEY).split(",");
        this.usersAtDomains = properties.getProperty(USER_KEY).split(",");
        this.passwords = properties.getProperty(PASSWORD_KEY).split(",");
        this.serverTimeout = Integer.valueOf(properties.getProperty(SERVER_TIMEOUT_KEY));
        this.serverRetryTimeout = Integer.valueOf(properties.getProperty(SERVER_RETRY_TIMEOUT_KEY));
        this.aliveWriter = Boolean.valueOf(properties.getProperty(ALIVE_WRITER_KEY, "true"));
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

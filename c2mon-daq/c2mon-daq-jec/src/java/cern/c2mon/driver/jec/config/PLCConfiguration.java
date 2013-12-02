/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005 - 2011 CERN This program is free software; you can
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
package cern.c2mon.driver.jec.config;

import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;
/**
 * This is the class which contains the parsed PLC equipment address String.
 * 
 * @author Andreas Lang
 *
 */
public class PLCConfiguration {
    /**
     * The default handler period used if there is no handler period defined.
     */
    public static final int DEFAULT_HANDLER_PERIOD = 5000;
    
    /**
     * Logger to use while parsing the configuration.
     */
    private static final Logger LOGGER = Logger.getLogger(PLCConfiguration.class);
    
    /**
     * String to store PLC name eg. "PLCSTAA03"
     */
    private String plcName = "";

    /**
     * String to store Redundant PLC name eg. "PLCSTAA02"
     */
    private String plcNameRed = "";

    /**
     * String to store the PLC Type eg. "SiemensISO" means that the PLC used is
     * a Siemens PLC connceted using ISO-ON-TCP protocol.
     */
    private String protocol = "";

    /**
     * Port number in the PLC side (Server)
     */
    private int port = 0;

    /**
     * String to store the synchronization modes used by the PLC MODES: Jec or
     * Ntp
     */
    private String timeSync = "";

    /**
     * Strings used to store the Alive Handler Period in milliseconds. This
     * value will be sent to the PLC during the Initialization and is used by
     * the PLC to send Alive messages to the driver with this predefined period.
     */
    private int handlerPeriod = DEFAULT_HANDLER_PERIOD;

    /**
     * String to store Source TSAP (only in ISO-on-TCP protocol) TSAP can only
     * have a maximum of 8 characters
     */
    private String sTsap = "";

    /**
     * String to store Destination TSAP (only in ISO-on-TCP protocol) TSAP can
     * only have a maximum of 8 characters
     */
    private String dTsap = "";
    
    /**
     * Short array (vector) to store the DP slave address(es) (max. 32 slaves)
     * In string from XML, each slave address is separated by comma ','
     */
    private Vector<Byte> dpSlaveAddresses = new Vector<Byte>();
    
    /**
     * Fills the PLCConfiguration object.
     * 
     * @param addressString The PLC equipment address String to parse.
     * @throws Exception Throws an exception if the parsing fails.
     */
    public void parsePLCAddress(final String addressString) throws Exception {
        // make sure dp slave addresses are empty
        getDpSlaveAddresses().clear();
        // example address :
        // Plc_name=plcstaa01,plcstaa02;Protocol=SiemensISO;Port=102;Time_sync=JEC;S_tsap=TCP-1;D_tsap=TCP-2;Dp_slave_address=1234,5678;

        // Extract the config data string from XML - delimiter for each element
        // is ';'
        StringTokenizer tokens = new StringTokenizer(addressString, ";");
        // As there are tree kinds of separator, this strings are used to define
        // each one of them
        // While there are some more string parts delimited by ';'
        while (tokens.hasMoreTokens()) {
            // Takes the substring separated by ';' (first field+value)
            String configurationProperty = tokens.nextToken();
            // For this substring, divides it in other substrings delimited by '='
            StringTokenizer configurationPropertyTokens = new StringTokenizer(configurationProperty, "=");
            // While there are more string parts delimited by '='
            if (configurationPropertyTokens.hasMoreTokens()) {
                String propertyKey = configurationPropertyTokens.nextToken();
                if (propertyKey.equalsIgnoreCase("Plc_name")) {
                    String plcNames = configurationPropertyTokens.nextToken();
                    // For the PLC name, check how many are delimited by ','
                    StringTokenizer plcNameTokens = new StringTokenizer(plcNames, ",");
                    setPlcName(plcNameTokens.nextToken());
                    if (plcNameTokens.hasMoreTokens()) {
                        setPlcNameRed(plcNameTokens.nextToken());
                    }
                }
                else if (propertyKey.equalsIgnoreCase("Protocol"))
                    setProtocol(configurationPropertyTokens.nextToken());
                else if (propertyKey.equalsIgnoreCase("Port"))
                    setPort(Integer.parseInt(configurationPropertyTokens.nextToken()));
                else if (propertyKey.equalsIgnoreCase("Time_sync"))
                    setTimeSync(configurationPropertyTokens.nextToken());
                else if (propertyKey.equalsIgnoreCase("Alive_handler_period"))
                    // Assigns the value to corresponding variable
                    try {
                        setHandlerPeriod(Integer.parseInt(configurationPropertyTokens.nextToken()));
                    } catch (NumberFormatException nfe) {
                        setHandlerPeriod(DEFAULT_HANDLER_PERIOD);
                        LOGGER.error("ERROR parsing Handler period. Setting default value: " + DEFAULT_HANDLER_PERIOD);
                    }
                else if (propertyKey.equalsIgnoreCase("S_tsap"))
                    setsTsap(configurationPropertyTokens.nextToken());
                else if (propertyKey.equalsIgnoreCase("D_tsap"))
                    setdTsap(configurationPropertyTokens.nextToken());
                // If this token has DP SLAVE ADDRESS
                else if (propertyKey.equalsIgnoreCase("Dp_slave_address")) {
                    // If there are no more tokens, means no slaves
                    if (!configurationPropertyTokens.hasMoreTokens()) {
                        LOGGER.info("No DP slave address found in configuration");
                    }
                    // If there are more tokens, but ',' doesn't appear, there's
                    // only one slave
                    else {
                        // Extracts what's after 'Dp_slave_address' string
                        String slaveList = configurationPropertyTokens.nextToken();
                        // If the extracted string from Dp_slave_address is NULL, 
                        // it means that there are no DP Slaves defined in the
                        // configuration
                        if (slaveList.equalsIgnoreCase("null")) {
                            LOGGER.warn("NO DP SLAVES REGISTERED FOR THIS PROCESS");
                        }
                        else {
                            // Creates a tokenizer to detect the different DP
                            // slaves in the list
                            StringTokenizer dpSlaveTokens = new StringTokenizer(slaveList, ",");
                            // While there are some more slave ID's
                            while (dpSlaveTokens.hasMoreTokens()) {
                                // Add the new extracted value to the Vector
                                getDpSlaveAddresses().add(new Byte(dpSlaveTokens.nextToken()));
                            }
                        }
                    }
                }
            }
            else {
                throw new Exception("Error parsing configuration properties.");
            }
        }
    }

    /**
     * @return the plcName
     */
    public String getPlcName() {
        return plcName;
    }

    /**
     * @param plcName the plcName to set
     */
    public void setPlcName(final String plcName) {
        this.plcName = plcName;
    }

    /**
     * @return the plcNameRed
     */
    public String getPlcNameRed() {
        return plcNameRed;
    }

    /**
     * @param plcNameRed the plcNameRed to set
     */
    public void setPlcNameRed(final String plcNameRed) {
        this.plcNameRed = plcNameRed;
    }

    /**
     * @return the protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * @param protocol the protocol to set
     */
    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(final int port) {
        this.port = port;
    }

    /**
     * @return the timeSync
     */
    public String getTimeSync() {
        return timeSync;
    }

    /**
     * @param timeSync the timeSync to set
     */
    public void setTimeSync(final String timeSync) {
        this.timeSync = timeSync;
    }

    /**
     * @return the handlerPeriod
     */
    public int getHandlerPeriod() {
        return handlerPeriod;
    }

    /**
     * @param handlerPeriod the handlerPeriod to set
     */
    public void setHandlerPeriod(final int handlerPeriod) {
        this.handlerPeriod = handlerPeriod;
    }

    /**
     * @return the sTsap
     */
    public String getsTsap() {
        return sTsap;
    }

    /**
     * @param sTsap the sTsap to set
     */
    public void setsTsap(final String sTsap) {
        this.sTsap = sTsap;
    }

    /**
     * @return the dTsap
     */
    public String getdTsap() {
        return dTsap;
    }

    /**
     * @param dTsap the dTsap to set
     */
    public void setdTsap(final String dTsap) {
        this.dTsap = dTsap;
    }

    /**
     * Returns the DP slave addresses.
     * 
     * @return Vector of DP slave addresses.
     */
    public Vector<Byte> getDpSlaveAddresses() {
        return dpSlaveAddresses;
    }

}

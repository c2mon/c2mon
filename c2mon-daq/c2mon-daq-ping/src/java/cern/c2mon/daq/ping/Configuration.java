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

package cern.c2mon.daq.ping;

/**
 * @author wbuczak
 */
public class Configuration {

    /**
     * defines the number of threads that run the ping tasks
     */
    public static final int POLLING_THREADS = Integer.parseInt(System.getProperty("dmn2.daq.ping.threads", "60"));

    /**
     * defines the polling interval
     */
    public static final int POLLING_INTERVAL = Integer.parseInt(System.getProperty("dmn2.daq.ping.interval", "60"));

    /**
     * defines the ping response timeout
     */
    public static final int PING_TIMEOUT = Integer.parseInt(System.getProperty("dmn2.daq.ping.timeout", "3000"));

    /**
     * defines after how many iterations the DAQ should re-ask the DNS for the IP address resolution
     */
    public static final int REFRESH_DNS_ADDRESS = Integer.parseInt(System.getProperty(
            "dmn2.daq.ping.dns_refresh_address", "5"));
}

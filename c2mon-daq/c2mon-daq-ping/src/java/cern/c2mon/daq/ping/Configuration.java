/**
 * Copyright (c) 2013 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

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

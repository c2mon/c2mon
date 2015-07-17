/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.monit;

import static org.junit.Assert.*;

import org.junit.Test;

import cern.c2mon.daq.monit.MonitHardwareAddress;

/**
 * Check that a valid Json String is correctly converted into an address with the 
 * expected hostname.
 * 
 * @author mbuttner
 */
public class MonitHardwareAddressTest 
{

    private static final String addr = "{\n\"hostname\": \"cs-ccr-diam1\"\n}\"";
    
    @Test
    public void testHardwareAddress()
    {
        MonitHardwareAddress a = MonitHardwareAddress.fromJson(addr);
        assertEquals("cs-ccr-diam1", a.getHostname());
    }
    
}

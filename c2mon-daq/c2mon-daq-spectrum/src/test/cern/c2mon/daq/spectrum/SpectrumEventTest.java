/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SpectrumEventTest {

    public static final String server = "cs-ccs-src44";
    public static final String eventStr =
            "CLR 172.18.201.214 01/29/2015 11:03:47 u2485-r-pb14-bhp42-1 21082124 10009 0x20661c0 \"DEVICE HAS ...\"";    
    
    @Test
    public void testSpectrumEvent()
    {
        SpectrumEvent event = new SpectrumEvent(server, eventStr);
        event.prepare();
        assertEquals(server, event.getServerName());
        assertEquals("CS-CCR-DIAM1", event.getHostname());
    }
    
}

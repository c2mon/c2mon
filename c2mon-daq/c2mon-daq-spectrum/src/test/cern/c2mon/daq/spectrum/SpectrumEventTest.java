/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.spectrum.SpectrumEvent.HostnameQuality;

public class SpectrumEventTest {

    Logger LOG = LoggerFactory.getLogger(SpectrumEventTest.class);

    
    public static final String server = "cs-ccs-src44";

    public static final String eventStr1 =
            "CLR 172.18.201.214 01/29/2015 11:03:47 u2485-r-pb14-bhp42-1 21082124 10009 0x20661c0 \"DEVICE HAS ...\"";    
    
    public static final String eventStr2 =
            "CLR 172.19.201.214 01/29/2015 11:03:47 u2485-r-pb14-bhp42-1 21082124 10009 0x20661c0 \"DEVICE HAS ...\"";    
    
    @Test
    public void testSpectrumEventCorrect()
    {
        SpectrumEvent event = new SpectrumEvent(server, eventStr1);
        event.prepare();
        LOG.info("Date: " + new Date(event.getUserTimestamp()));        
        LOG.info("Alarm id:      " + event.getAlarmId());
        LOG.info("Cause id:       " + event.getCauseId());
        LOG.info("Context URL:    " + event.getContextURL());
        LOG.info("Pb description: " + event.getProblemDescription());
        LOG.info("Event type:     " + event.getType());
        LOG.info("is keep alive:  " + event.isKeepAlive());
        LOG.info("is notifier ok: " + event.isSpectrumNotifierOk());
        LOG.info("activate:       " + event.toActivate());
        LOG.info("terminate:      " + event.toTerminate());
        LOG.info("reset:          " + event.toReset());
        
        assertEquals(server, event.getServerName());
        assertEquals("CS-CCR-DIAM1", event.getHostname());
        assertEquals(HostnameQuality.FOUND, event.getHostnameQuality());
        assertEquals(1422525827000L, event.getUserTimestamp());
        assertEquals(SpectrumEvent.SpectrumEventType.CLR, event.getType());
        assertEquals("10009", event.getCauseId());
        assertEquals(Long.parseLong("10009", 16), 65545);
        
        assertFalse(event.isKeepAlive());
        assertFalse(event.toReset());
        assertFalse(event.toActivate());
        assertTrue(event.toTerminate());
        assertTrue(event.isSpectrumNotifierOk());
    }

    @Test
    public void testSpectrumEventInvalidIp()
    {
        SpectrumEvent event = new SpectrumEvent(server, eventStr2);
        event.prepare();
        assertEquals(server, event.getServerName());
        assertEquals(HostnameQuality.NOT_FOUND, event.getHostnameQuality());        
    }
    
}

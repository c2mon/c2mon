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

    public static final String eventStrClearIpOk =
            "CLR 172.18.201.214 01/29/2015 11:03:47 u2485-r-pb14-bhp42-1 21082124 10009 0x20661c0 \"DEVICE HAS ...\"";    

    public static final String eventStrSet =
            "SET 172.18.201.214 01/29/2015 11:03:47 u2485-r-pb14-bhp42-1 21082124 10009 0x20661c0 \"DEVICE HAS PB\"";    

    public static final String eventStrUpd =
            "UPD 172.18.201.214 01/29/2015 11:03:47 u2485-r-pb14-bhp42-1 21082124 10009 0x20661c0 \"DEVICE HAS PB\"";    

    public static final String eventStrReset =
            "RST - 01/29/2015 11:03:47 - 0 0 - \"RESET!\"";    

    public static final String eventStrUnknown =
            "xxx 172.18.201.214 01/29/2015 11:03:47 u2485-r-pb14-bhp42-1 21082124 10009 0x20661c0 \"DEVICE HAS PB\"";    
    
    public static final String eventStrClearIpWrong =
            "CLR 172.19.201.214 01/29/2015 11:03:47 u2485-r-pb14-bhp42-1 21082124 10009 0x20661c0 \"DEVICE HAS ...\"";    

    public static final String eventStrKalOk =
            "KAL OK 31/05/2015 23:35:01 - - - - \"[AlarmNotifier process is running on cs-srv-44]\"";    
    public static final String eventStrKalNotOk =
            "KAL xx 31/05/2015 23:35:01 - - - - \"[AlarmNotifier process is running on cs-srv-44]\"";    

    @Test
    public void testSpectrumEventSet()
    {
        SpectrumEvent event = new SpectrumEvent(server, eventStrSet);
        event.prepare();
        printEvent(event);
        
        assertEquals(server, event.getServerName());
        assertEquals("CS-CCR-DIAM1", event.getHostname());
        assertEquals(HostnameQuality.FOUND, event.getHostnameQuality());
        assertEquals(1422525827000L, event.getUserTimestamp());
        assertEquals(SpectrumEvent.SpectrumEventType.SET, event.getType());
        assertEquals("10009", event.getCauseId());
        assertEquals(Long.parseLong("10009", 16), 65545);
        
        assertFalse(event.isKeepAlive());
        assertFalse(event.toReset());
        assertTrue(event.toActivate());
        assertFalse(event.toTerminate());
        assertTrue(event.isSpectrumNotifierOk());
    }

    @Test
    public void testSpectrumEvenUpd()
    {
        SpectrumEvent event = new SpectrumEvent(server, eventStrUpd);
        event.prepare();
        printEvent(event);
        
        assertEquals(server, event.getServerName());
        assertEquals("CS-CCR-DIAM1", event.getHostname());
        assertEquals(HostnameQuality.FOUND, event.getHostnameQuality());
        assertEquals(1422525827000L, event.getUserTimestamp());
        assertEquals(SpectrumEvent.SpectrumEventType.UPD, event.getType());
        assertEquals("10009", event.getCauseId());
        assertEquals(Long.parseLong("10009", 16), 65545);
        
        assertFalse(event.isKeepAlive());
        assertFalse(event.toReset());
        assertTrue(event.toActivate());
        assertFalse(event.toTerminate());
        assertTrue(event.isSpectrumNotifierOk());
    }

    @Test
    public void testSpectrumEvenUnknown()
    {
        SpectrumEvent event = new SpectrumEvent(server, eventStrUnknown);
        event.prepare();
        printEvent(event);
        
        assertEquals(SpectrumEvent.SpectrumEventType.INVALID_MESSAGE, event.getType());
    }


    
    @Test
    public void testSpectrumEventClear()
    {
        SpectrumEvent event = new SpectrumEvent(server, eventStrClearIpOk);
        event.prepare();
        printEvent(event);
        
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
    public void testSpectrumEventReset()
    {
        SpectrumEvent event = new SpectrumEvent(server, eventStrReset);
        event.prepare();
        printEvent(event);
                
        assertFalse(event.isKeepAlive());
        assertTrue(event.toReset());
        assertFalse(event.toActivate());
        assertFalse(event.toTerminate());
    }

    
    @Test
    public void testSpectrumEventKeepAliveOk()
    {
        SpectrumEvent event = new SpectrumEvent(server, eventStrKalOk);
        event.prepare();
        printEvent(event);
                
        assertTrue(event.isKeepAlive());
        assertFalse(event.toReset());
        assertFalse(event.toActivate());
        assertFalse(event.toTerminate());
        assertTrue(event.isSpectrumNotifierOk());
    }

    @Test
    public void testSpectrumEventKeepAliveNotOk()
    {
        SpectrumEvent event = new SpectrumEvent(server, eventStrKalNotOk);
        event.prepare();
        printEvent(event);
                
        assertTrue(event.isKeepAlive());
        assertFalse(event.toReset());
        assertFalse(event.toActivate());
        assertFalse(event.toTerminate());
        assertFalse(event.isSpectrumNotifierOk());
    }

    
    @Test
    public void testSpectrumEventInvalidIp()
    {
        SpectrumEvent event = new SpectrumEvent(server, eventStrClearIpWrong);
        event.prepare();
        assertEquals(server, event.getServerName());
        assertEquals(HostnameQuality.NOT_FOUND, event.getHostnameQuality());        
    }

    private void printEvent(SpectrumEvent event)
    {
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
    }
    
}

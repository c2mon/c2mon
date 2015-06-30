/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum.util;

import static org.junit.Assert.assertEquals;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.spectrum.SpectrumAlarm;
import cern.c2mon.daq.spectrum.SpectrumEvent.SpectrumEventType;
import cern.c2mon.daq.spectrum.SpectrumMessageHandler;
import cern.c2mon.daq.spectrum.SpectrumTestUtil;
import cern.c2mon.daq.test.GenericMessageHandlerTst;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;

/**
 *  Check the DiskBuffer, the mechanism which perists the alarm status to disk in order to get
 *  the latest status of all configured alarms after a restart.
 */
@UseHandler(SpectrumMessageHandler.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DiskBufferTest extends GenericMessageHandlerTst {

    Logger LOG = LoggerFactory.getLogger(DiskBufferTest.class);
    protected static SpectrumMessageHandler theHandler;
    
    private static final String primaryServer = "cs-srv-44.cern.ch";

    
    //
    // --- TEST --------------------------------------------------------------------------------
    //   
    /*
     * Send some alarm messages to generate a given status, stop the message handler and restart
     * it in order to dump the situation to disk and reloa dit, check that the alarm status is as
     * expected.
     * 
     */
    @UseConf("spectrum_test_1.xml")
    @Test
    public void testBasicInterface() throws EqIOException {
        LOG.info("Operating test ...");
        
        // activate an alarm
        SpectrumTestUtil.sendMessage(primaryServer, SpectrumEventType.SET, "cs-ccr-diam1", 10009);                
        SpectrumTestUtil.sendMessage(primaryServer, SpectrumEventType.SET, "cs-ccr-diam1", 10010);
        SpectrumTestUtil.sendMessage(primaryServer, SpectrumEventType.SET, "cs-ccr-dmnp1", 10010);
        SpectrumTestUtil.sendMessage(primaryServer, SpectrumEventType.SET, "cs-ccr-dmnp2", 10010);
        SpectrumTestUtil.sendMessage(primaryServer, SpectrumEventType.CLR, "cs-ccr-dmnp2", 10010);
  
        SpectrumTestUtil.trySleepSec(3);                   
        theHandler.shutdown();
        
        SpectrumTestUtil.trySleepSec(3);           
        theHandler.connectToDataSource();
        
        SpectrumTestUtil.trySleepSec(3);           
        // now we should have two alarms for acmnnr,1 for cs-ccr-dmnp1, and none for cs-ccr-dmnp2
        SpectrumAlarm a1 = theHandler.getProcessor().getAlarm("cs-ccr-diam1");
        SpectrumAlarm a2 = theHandler.getProcessor().getAlarm("cs-ccr-dmnp1");
        SpectrumAlarm a3 = theHandler.getProcessor().getAlarm("cs-ccr-dmnp2");

        LOG.warn("" + a1.getTag().getName() + " -> " + a1.getAlarmCount());
        LOG.warn("" + a2.getTag().getName() + " -> " + a2.getAlarmCount());
        LOG.warn("" + a3.getTag().getName() + " -> " + a3.getAlarmCount());
        
        assertEquals(2, a1.getAlarmCount());        
        assertEquals(1, a2.getAlarmCount());        
        assertEquals(0, a3.getAlarmCount());
        
        LOG.info("Test completed.");
    }

    
    //
    // --- SETUP --------------------------------------------------------------------------------
    //
    @Override
    protected void beforeTest() throws Exception {
        LOG.info("Init ...");
        System.setProperty("spectrum.mode", "junit");
        theHandler = (SpectrumMessageHandler) msgHandler;        
        SpectrumMessageHandler.profile = "TEST";
        theHandler.connectToDataSource();
                
        LOG.info("Init done.");
    }

    @Override
    protected void afterTest() throws Exception {
        if (theHandler != null) {
            theHandler.shutdown();
        }
    }

}

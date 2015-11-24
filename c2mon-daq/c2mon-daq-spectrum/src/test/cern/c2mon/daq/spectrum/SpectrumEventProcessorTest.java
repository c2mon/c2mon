/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum;

import static org.junit.Assert.*;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.spectrum.SpectrumEvent.SpectrumEventType;
import cern.c2mon.daq.spectrum.listener.SpectrumListenerJunit;
import cern.c2mon.daq.spectrum.util.DiskBufferTest;
import cern.c2mon.daq.test.GenericMessageHandlerTst;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;
import cern.c2mon.shared.common.datatag.SourceDataTag;
import cern.c2mon.shared.util.parser.SimpleXMLParser;

/**
 * TODO problem timestamps (buffer, update, )
 * 
 * Check add/remove/get of a host into the internal data structure of the processr
 * Check that one minute without backup or backup with NOT OK ends in connection not ok
 * 
 * approach: create events, put events on queue, check that event processor establishes correct situation
 *  * 
 * Scenario 2:
 * - send activate
 * - check tha alarm is active
 * - send terminate
 * - check that alarm is terminated
 * ...
 * 
 * @author mbuttner
 */
@UseHandler(SpectrumMessageHandler.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SpectrumEventProcessorTest extends GenericMessageHandlerTst {

    Logger LOG = LoggerFactory.getLogger(DiskBufferTest.class);
    protected static SpectrumMessageHandler theHandler;
    
    private static final String primaryServer = "cs-srv-44.cern.ch";
    private static final String secondaryServer = "cs-srv-45.cern.ch";

    
    //
    // --- TEST --------------------------------------------------------------------------------
    //    
    /*
     * Check add and remove hosts to/from event processor, and subsequent retrieving of config
     * data
     * 
     */
    @UseConf("spectrum_test_1.xml")
    @Test
    public void testInternalDataStruct() throws ParserConfigurationException {
        SpectrumEventProcessor proc = theHandler.getProcessor();
        SimpleXMLParser parser = new SimpleXMLParser();
        
        String xml = SpectrumTestUtil.getConfigTag("cs-ccr-diam1", 1L);
        SourceDataTag tag = SourceDataTag.fromConfigXML(parser.parse(xml).getDocumentElement());
        proc.add("cs-ccr-diam1.cern.ch", tag);
        
        SpectrumAlarm alarm = proc.getAlarm("cs-CCR-diaM1");
        assertNotNull(alarm);
        
        proc.del("cs-ccr-diam1");
        alarm = proc.getAlarm("cs-ccr-diam1");
        assertNull(alarm);
    }
    
    /*
     * Check a RST message clears all alarms from the message processor. Also verifies the correct
     * processing of a simple set and a simple clr.
     * 
     */
    @UseConf("spectrum_test_1.xml")
    @Test
    public void testMessageProcessing() {
        SpectrumTestUtil.sendMessage(primaryServer, SpectrumEventType.SET, "cs-ccr-diam1", 1);
        SpectrumTestUtil.sendMessage(primaryServer, SpectrumEventType.SET, "cs-ccr-dmnp1", 1);
        SpectrumTestUtil.sendMessage(primaryServer, SpectrumEventType.SET, "cs-ccr-dmnp2", 1);
        
        SpectrumTestUtil.trySleepSec(3);
        SpectrumEventProcessor proc = theHandler.getProcessor();
        SpectrumAlarm alarm = proc.getAlarm("cs-CCR-diaM1");
        assertEquals(1, alarm.getAlarmCount());
        assertEquals(1, proc.getAlarm("cs-ccr-dmnp1").getAlarmCount());
        assertEquals(1, proc.getAlarm("cs-ccr-dmnp2").getAlarmCount());

        SpectrumTestUtil.sendMessage(primaryServer, SpectrumEventType.CLR, "cs-ccr-dmnp2", 1);
        SpectrumTestUtil.trySleepSec(3);
        assertEquals(0, proc.getAlarm("cs-ccr-dmnp2").getAlarmCount());
        
        SpectrumTestUtil.sendMessage(primaryServer, SpectrumEventType.RST, "cs-ccr-diam1", 1);
        SpectrumTestUtil.trySleepSec(3);
        assertEquals(0, alarm.getAlarmCount());
        assertEquals(0, proc.getAlarm("cs-ccr-dmnp1").getAlarmCount());
    }
    
    /**
     * Make sure the connection status changes if no keep alive is received from the client.
     */
    @UseConf("spectrum_test_1.xml")
    @Test
    public void testConnectionLost() {
        SpectrumEventProcessor proc = theHandler.getProcessor();
        proc.setBackupControl(10,10);       // set control to 10s freq with max. interval to 10s to make test fast
        SpectrumTestUtil.trySleepSec(3);
        assertTrue(proc.isConnectionOk());
        SpectrumTestUtil.trySleepSec(10);
        assertFalse(proc.isConnectionOk());
        
        SpectrumTestUtil.sendKeepAlive(primaryServer, "OK");
        SpectrumTestUtil.trySleepSec(5);
        assertTrue(proc.isConnectionOk());
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
        SpectrumTestUtil.trySleepSec(3);
        LOG.info("Init done.");
        
        SpectrumListenerJunit spectrumListenerJunit = new SpectrumListenerJunit();
        spectrumListenerJunit.setProcessor(theHandler.getProcessor());
        SpectrumTestUtil.sendMessage(primaryServer, SpectrumEventType.RST, "-", 1);
        SpectrumTestUtil.sendMessage(secondaryServer, SpectrumEventType.RST, "-", 1);
        SpectrumTestUtil.trySleepSec(3);
    }

    @Override
    protected void afterTest() throws Exception {
        if (theHandler != null) {
            theHandler.shutdown();
        }
    }

}

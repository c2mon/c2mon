/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.FixMethodOrder;
import org.junit.Test;

import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.spectrum.SpectrumEvent.SpectrumEventType;
import cern.c2mon.daq.spectrum.listener.impl.SpectrumListenerJunit;
import cern.c2mon.daq.test.GenericMessageHandlerTst;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration("/resources/dmn-spectrum-config.xml")
@UseHandler(SpectrumMessageHandler.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SpectrumAlarmTest extends GenericMessageHandlerTst {

    Logger LOG = LoggerFactory.getLogger(SpectrumAlarmTest.class);
    protected static SpectrumMessageHandler theHandler;
    
    String primaryServer = "cs-srv-44.cern.ch";
    String secondaryServer = "cs-srv-45.cern.ch";
    int port = 12001;
    
    //
    // --- TEST --------------------------------------------------------------------------------
    //    
    @UseConf("spectrum_test_1.xml")
//    @Test
    public void testBasicInterface() {
        LOG.info("Operating test ...");

        // test simple on/off
        SpectrumTestUtil.sendMessage(primaryServer, SpectrumEventType.SET, "acmmnr", 10009);        
        sleep(3);        
        assertTrue(SpectrumTestUtil.getValue(theHandler,  1L));
        
        SpectrumAlarm alarm = SpectrumListenerJunit.getListener().getAlarm("acmmnr");
        assertTrue(alarm.isAlarmOn());
        assertTrue(alarm.getAlarmCount() == 1);
        
        SpectrumTestUtil.sendMessage(primaryServer, SpectrumEventType.CLR, "acmmnr", 10009);
        
        sleep(3);
        assertFalse(SpectrumTestUtil.getValue(theHandler,  1L));
        assertTrue(!alarm.isAlarmOn());
        assertTrue(alarm.getAlarmCount() == 0);
        
        SpectrumTestUtil.sendMessage(primaryServer, SpectrumEventType.SET, "acmmnr", 10009);        
        SpectrumTestUtil.sendMessage(primaryServer, SpectrumEventType.SET, "acmmnr", 10010);
        SpectrumTestUtil.sendMessage(primaryServer, SpectrumEventType.SET, "cs-ccr-dmnp1", 10010);
        sleep(3);        
        assertTrue(SpectrumTestUtil.getValue(theHandler,  1L));
        assertNull(SpectrumListenerJunit.getListener().getAlarm("cs-ccr-dmnp1"));
        assertTrue(alarm.getAlarmCount() == 2);
        assertTrue(SpectrumTestUtil.getValue(alarm.getTag()));

        
        SpectrumTestUtil.sendMessage(primaryServer, SpectrumEventType.CLR, "acmmnr", 10009);                
        sleep(3);
        assertTrue(SpectrumTestUtil.getValue(theHandler,  1L));
        assertTrue(alarm.getAlarmCount() == 1);

        SpectrumTestUtil.sendMessage(primaryServer, SpectrumEventType.CLR, "acmmnr", 10010);                
        sleep(3);
        assertFalse(SpectrumTestUtil.getValue(theHandler,  1L));
        assertFalse(SpectrumTestUtil.getValue(alarm.getTag()));
        assertTrue(alarm.getAlarmCount() == 0);

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
        theHandler.connectToDataSource();
                
//        IEquipmentConfiguration eqCfg = theHandler.getEquipmentConfiguration();
//        config = JsonUtils.fromJson(eqCfg.getAddress(), SpectrumEquipConfig.class);
        
        LOG.info("Init done.");
    }

    @Override
    protected void afterTest() throws Exception {
        LOG.info("Clean ...");
        theHandler.disconnectFromDataSource();
        LOG.info("Completed.");
    }

    //
    // --- UTIL ----------------------------------------------------------------------------------
    //
    private void sleep(int n) {
        try {
            Thread.sleep(n * 1000);
        } catch (Exception e) {
            LOG.warn("Sleep interrupted", e);
        }
    }
}

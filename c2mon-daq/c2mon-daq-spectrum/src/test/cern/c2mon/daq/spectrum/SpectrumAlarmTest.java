/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum;

import static org.junit.Assert.*;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.daq.spectrum.SpectrumEvent.SpectrumEventType;
import cern.c2mon.daq.spectrum.listener.impl.SpectrumListenerJunit;
import cern.c2mon.daq.spectrum.util.JsonUtils;
import cern.c2mon.daq.test.GenericMessageHandlerTst;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;
import cern.c2mon.shared.common.process.IEquipmentConfiguration;

//@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/resources/dmn-spectrum-config.xml")
@UseHandler(SpectrumMessageHandler.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SpectrumAlarmTest extends GenericMessageHandlerTst {

    Logger LOG = LoggerFactory.getLogger(SpectrumAlarmTest.class);
    static { System.setProperty("spectrum.mode", "junit"); }
    protected static SpectrumMessageHandler theHandler;
    private static SpectrumEquipConfig config;
    
    //
    // --- TEST --------------------------------------------------------------------------------
    //
    @UseConf("spectrum_test_1.xml")
    @Test
    public void testConfig() {
        assertNotNull(config.getPrimaryServer());
        assertNotNull(config.getSecondaryServer());
        assertTrue(config.getPort() > 1000);
    }
    
    @UseConf("spectrum_test_1.xml")
    @Test
    public void testBasicInterface() {
        LOG.info("Operating test ...");

        // test simple on/off
        SpectrumTestUtil.sendMessage(config.getPrimaryServer(), SpectrumEventType.SET, "acmmnr", 10009);        
        sleep(3);        
        assertTrue(SpectrumTestUtil.getValue(theHandler,  1L));
        
        SpectrumAlarm alarm = SpectrumListenerJunit.getListener().getAlarm("acmmnr");
        assertTrue(alarm.isAlarmOn());
        assertTrue(alarm.getAlarmCount() == 1);
        
        SpectrumTestUtil.sendMessage(config.getPrimaryServer(), SpectrumEventType.CLR, "acmmnr", 10009);
        
        sleep(3);
        assertFalse(SpectrumTestUtil.getValue(theHandler,  1L));
        assertTrue(!alarm.isAlarmOn());
        assertTrue(alarm.getAlarmCount() == 0);
        
        SpectrumTestUtil.sendMessage(config.getPrimaryServer(), SpectrumEventType.SET, "acmmnr", 10009);        
        SpectrumTestUtil.sendMessage(config.getPrimaryServer(), SpectrumEventType.SET, "acmmnr", 10010);
        SpectrumTestUtil.sendMessage(config.getPrimaryServer(), SpectrumEventType.SET, "cs-ccr-dmnp1", 10010);
        sleep(3);        
        assertTrue(SpectrumTestUtil.getValue(theHandler,  1L));
        assertNull(SpectrumListenerJunit.getListener().getAlarm("cs-ccr-dmnp1"));
        assertTrue(alarm.getAlarmCount() == 2);
        assertTrue(SpectrumTestUtil.getValue(alarm.getTag()));

        
        SpectrumTestUtil.sendMessage(config.getPrimaryServer(), SpectrumEventType.CLR, "acmmnr", 10009);                
        sleep(3);
        assertTrue(SpectrumTestUtil.getValue(theHandler,  1L));
        assertTrue(alarm.getAlarmCount() == 1);

        SpectrumTestUtil.sendMessage(config.getPrimaryServer(), SpectrumEventType.CLR, "acmmnr", 10010);                
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
        theHandler = (SpectrumMessageHandler) msgHandler;        
        theHandler.connectToDataSource();
                
        IEquipmentConfiguration eqCfg = theHandler.getEquipmentConfiguration();
        config = JsonUtils.fromJson(eqCfg.getAddress(), SpectrumEquipConfig.class);
        
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

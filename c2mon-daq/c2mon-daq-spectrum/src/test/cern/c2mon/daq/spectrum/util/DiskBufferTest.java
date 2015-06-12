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
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cern.c2mon.daq.spectrum.SpectrumAlarm;
import cern.c2mon.daq.spectrum.SpectrumEvent.SpectrumEventType;
import cern.c2mon.daq.spectrum.SpectrumEventProcessor;
import cern.c2mon.daq.spectrum.SpectrumMessageHandler;
import cern.c2mon.daq.spectrum.SpectrumTestUtil;
import cern.c2mon.daq.test.GenericMessageHandlerTst;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;

@UseHandler(SpectrumMessageHandler.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DiskBufferTest  extends GenericMessageHandlerTst {

    Logger LOG = LoggerFactory.getLogger(DiskBufferTest.class);
    protected static SpectrumMessageHandler theHandler;
    private static ClassPathXmlApplicationContext ctx;
    
    private static final String primaryServer = "cs-srv-44.cern.ch";

    
    //
    // --- TEST --------------------------------------------------------------------------------
    //    
    @UseConf("spectrum_test_1.xml")
    @Test
    public void testBasicInterface() throws EqIOException {
        LOG.info("Operating test ...");
//        if (ctx == null) {
//            ctx = new ClassPathXmlApplicationContext("classpath:dmn-spectrum-config.xml");
//            ctx.getEnvironment().setDefaultProfiles("TEST");
//            ctx.refresh();
//        }
        
//        SpectrumEventProcessor proc = ctx.getBean("eventProc", SpectrumEventProcessor.class);
        
        // activate an alarm
        SpectrumTestUtil.sendMessage(primaryServer, SpectrumEventType.SET, "acmmnr", 10009);        
//        SpectrumTestUtil.trySleepSec(3);        
//        assertTrue(SpectrumTestUtil.getValue(theHandler,  1L));
        
//        SpectrumAlarm alarm = SpectrumListenerJunit.getListener().getAlarm("acmmnr");
//        assertTrue(alarm.isAlarmOn());
//        assertTrue(alarm.getAlarmCount() == 1);
        
        SpectrumTestUtil.sendMessage(primaryServer, SpectrumEventType.SET, "acmmnr", 10010);
        SpectrumTestUtil.sendMessage(primaryServer, SpectrumEventType.SET, "cs-ccr-dmnp1", 10010);
        SpectrumTestUtil.sendMessage(primaryServer, SpectrumEventType.SET, "cs-ccr-dmnp2", 10010);
        SpectrumTestUtil.sendMessage(primaryServer, SpectrumEventType.CLR, "cs-ccr-dmnp2", 10010);
  
        theHandler.shutdown();
        SpectrumTestUtil.trySleepSec(30);           
        theHandler.connectToDataSource();
        
        // now we should have two alarms for acmnnr,1 for cs-ccr-dmnp1, and none for cs-ccr-dmnp2
        SpectrumAlarm a1 = theHandler.getProcessor().getAlarm("acmmnr");
        SpectrumAlarm a2 = theHandler.getProcessor().getAlarm("cs-ccr-dmnp1");
        SpectrumAlarm a3 = theHandler.getProcessor().getAlarm("cs-ccr-dmnp2");
        assertEquals(2, a1.getAlarmCount());
        assertEquals(1, a2.getAlarmCount());
        assertEquals(0, a3.getAlarmCount());
        
        
//        SpectrumTestUtil.trySleepSec(3);
//        assertFalse(SpectrumTestUtil.getValue(theHandler,  1L));
//        assertTrue(!alarm.isAlarmOn());
//        assertTrue(alarm.getAlarmCount() == 0);
/*        
        SpectrumTestUtil.sendMessage(primaryServer, SpectrumEventType.SET, "acmmnr", 10009);        
        SpectrumTestUtil.sendMessage(primaryServer, SpectrumEventType.SET, "acmmnr", 10010);
        SpectrumTestUtil.sendMessage(primaryServer, SpectrumEventType.SET, "cs-ccr-dmnp1", 10010);
        SpectrumTestUtil.trySleepSec(3);           
        assertTrue(SpectrumTestUtil.getValue(theHandler,  1L));
        assertNull(SpectrumListenerJunit.getListener().getAlarm("cs-ccr-dmnp1"));
        assertTrue(alarm.getAlarmCount() == 2);
        assertTrue(SpectrumTestUtil.getValue(alarm.getTag()));

        
        SpectrumTestUtil.sendMessage(primaryServer, SpectrumEventType.CLR, "acmmnr", 10009);                
        SpectrumTestUtil.trySleepSec(3);
        assertTrue(SpectrumTestUtil.getValue(theHandler,  1L));
        assertTrue(alarm.getAlarmCount() == 1);

        SpectrumTestUtil.sendMessage(primaryServer, SpectrumEventType.CLR, "acmmnr", 10010);                
        SpectrumTestUtil.trySleepSec(3);
        assertFalse(SpectrumTestUtil.getValue(theHandler,  1L));
        assertFalse(SpectrumTestUtil.getValue(alarm.getTag()));
        assertTrue(alarm.getAlarmCount() == 0);
*/
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
                
//        IEquipmentConfiguration eqCfg = theHandler.getEquipmentConfiguration();
//        config = JsonUtils.fromJson(eqCfg.getAddress(), SpectrumEquipConfig.class);
        
        LOG.info("Init done.");
    }

    @Override
    protected void afterTest() throws Exception {
        if (theHandler != null) {
            theHandler.shutdown();
        }
    }

}

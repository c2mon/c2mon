/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum;

import static org.junit.Assert.assertEquals;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cern.c2mon.daq.test.GenericMessageHandlerTst;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;

@UseHandler(SpectrumMessageHandler.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SpectrumConfigTest extends GenericMessageHandlerTst {

    Logger LOG = LoggerFactory.getLogger(SpectrumConfigTest.class);
    protected static SpectrumMessageHandler theHandler;
    
    private static final String primaryServer = "cs-srv-44.cern.ch";
    private static final String secondaryServer = "cs-srv-45.cern.ch";
    int port = 12001;
    
    private static ClassPathXmlApplicationContext ctx;
    
    //
    // --- TEST --------------------------------------------------------------------------------
    //    
    @UseConf("spectrum_test_1.xml")
    @Test
    public void testBasicInterface() {
        LOG.info("Operating test ...");
        if (ctx == null) {
            ctx = new ClassPathXmlApplicationContext("classpath:dmn-spectrum-config.xml");
            ctx.getEnvironment().setDefaultProfiles("PRO");
            ctx.refresh();
        }
        
        SpectrumEventProcessor proc = ctx.getBean("eventProc", SpectrumEventProcessor.class);
        assertEquals(primaryServer, proc.getPrimaryServer());
        assertEquals(secondaryServer, proc.getSecondaryServer());
        assertEquals(port, proc.getPort());

        /*
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
        theHandler.connectToDataSource();
                
//        IEquipmentConfiguration eqCfg = theHandler.getEquipmentConfiguration();
//        config = JsonUtils.fromJson(eqCfg.getAddress(), SpectrumEquipConfig.class);
        
        LOG.info("Init done.");
    }

    //
    // --- UTIL ----------------------------------------------------------------------------------
    //

    @Override
    protected void afterTest() throws Exception {
        // TODO Auto-generated method stub
        
    }
}

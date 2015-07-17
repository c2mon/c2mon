/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.monit;

import static org.junit.Assert.assertEquals;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cern.c2mon.daq.monit.MonitEventProcessor;
import cern.c2mon.daq.monit.MonitMessageHandler;
import cern.c2mon.daq.test.GenericMessageHandlerTst;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;

@UseHandler(MonitMessageHandler.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MonitConfigTest extends GenericMessageHandlerTst {

    Logger LOG = LoggerFactory.getLogger(MonitConfigTest.class);
    protected static MonitMessageHandler theHandler;
    
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
        
        MonitEventProcessor proc = ctx.getBean("eventProc", MonitEventProcessor.class);
//        assertEquals(primaryServer, proc.getPrimaryServer());
//        assertEquals(secondaryServer, proc.getSecondaryServer());
//        assertEquals(port, proc.getPort());

        LOG.info("Test completed.");
    }

    //
    // --- SETUP --------------------------------------------------------------------------------
    //
    @Override
    protected void beforeTest() throws Exception {
        LOG.info("Init ...");
        System.setProperty("spectrum.mode", "junit");
        theHandler = (MonitMessageHandler) msgHandler;        
        theHandler.connectToDataSource();
        LOG.info("Init done.");
    }

    @Override
    protected void afterTest() throws Exception {
        //        
    }
}

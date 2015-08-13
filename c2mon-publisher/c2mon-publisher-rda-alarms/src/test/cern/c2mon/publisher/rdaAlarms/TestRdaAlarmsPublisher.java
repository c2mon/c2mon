/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.rdaAlarms;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

// TODO test RdaPublisher update of values (subscribe, get, set, use demo code for that)

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:cern/c2mon/publisher/rdaAlarms/alarms_publisher.xml")
@ActiveProfiles(profiles = "TEST")
public class TestRdaAlarmsPublisher extends TestBaseClass {

    @Test
    public void testLifeCycle() throws InterruptedException {
        getLogger().info("Retrieving th epublisher instance ...");
        RdaAlarmsPublisher publisher = RdaAlarmsPublisher.getPublisher();
        getLogger().info("Boot ...");
        publisher.start();
        
        // give the publisher a bit of time to asnchronously start the RDA server, than check it is really up
        Thread.sleep(1000);
        assertTrue(publisher.isRunning());
        
        getLogger().info("Let it run for 10 seconds ...");
        Thread.sleep(10 * 1000);
        getLogger().info("Shutdown (was started on {}) ...", publisher.getStartTime());
        publisher.shutdown();
        
        getLogger().info("halt.");        
        assertFalse(publisher.isRunning());
    }
    
}

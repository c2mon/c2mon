/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.rdaAlarms;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class TestRdaAlarmsPublisher extends TestBaseClass {
    
    @Test
    public void testLifeCycle() throws InterruptedException {
        getLogger().info("Starting testLifeCycle() ----------------- ");

        getLogger().info("Retrieving the publisher instance ...");
        RdaAlarmsPublisher publisher = RdaAlarmsPublisher.getPublisher();
        getLogger().info("Boot ...");
        startTestPublisher();
        
        // give the publisher a bit of time to asnchronously start the RDA server, than check it is really up
        Thread.sleep(1000);
        assertTrue(publisher.isRunning());
        
        getLogger().info("Let it run for 10 seconds ...");
        Thread.sleep(10 * 1000);
        getLogger().info("Shutdown (was started on {}) ...", publisher.getStartTime());
        stopTestPublisher();
        
        getLogger().info("halt.");        
        assertFalse(publisher.isRunning());
        getLogger().info("Completed testLifeCycle() ---------------- ");
    }
    
}

/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/

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

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

package cern.c2mon.publisher.mobicall;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Check the confitguration load/reload mechanism. For this test we reduce the default interval
 * of 5 minutes to 1. The, it checks that:
 * - the initial load is immediately operated and expected alarms are available
 * - the reload is done after 1mn1s (check two cycles)
 * - the replacement of an alarm by another appears to the query
 * 
 * @author mbuttner
 */
public class TestConfigurationReload {

    //
    // --- SETUP test ------------------------------------------------------------------------------
    //
    @BeforeClass
    public static void initClass() throws Exception {        
        // set config reloading interval to 1 minute, otherwise testing it would be too long
        System.setProperty("mobicall.config.refresh","1");
        
        TestUtil.init();
        TestUtil.startTestPublisher();
    }
 
    @AfterClass
    public static void cleanupClass() throws Exception {
        TestUtil.stopTestPublisher();
    }
    
    //
    // --- TESTS  -----------------------------------------------------------------------------------
    //    
    @Test
    public void testReloadTrigger() throws Exception {
        // after boot, the config load should have been done once, the alarms available
        assertEquals(1, TestUtil.loader.getCount());
        assertNotNull(TestUtil.loader.find("FF:FM:1"));
        assertNotNull(TestUtil.loader.find("FF:FM:2"));
        assertNull(TestUtil.loader.find("FF:FM:3"));
        
        // after 1mn1s, the configuration process should have been done another time, the
        // content of the alarm map slightly changed
        Thread.sleep(61 * 1000);
        assertEquals(2, TestUtil.loader.getCount());
        assertNull(TestUtil.loader.find("FF:FM:1"));
        assertNotNull(TestUtil.loader.find("FF:FM:2"));
        assertNotNull(TestUtil.loader.find("FF:FM:3"));
        
        // after 2mn2s, the configuration reload should have been done another time
        Thread.sleep(61 * 1000);
        assertEquals(3, TestUtil.loader.getCount());
    }
}

/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.mobicall;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

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
        assertEquals(1, TestUtil.loader.getCount());
        assertNotNull(TestUtil.loader.find("FF:FM:1"));
        assertNotNull(TestUtil.loader.find("FF:FM:2"));
        assertNull(TestUtil.loader.find("FF:FM:3"));
        
        Thread.sleep(61 * 1000);
        assertEquals(2, TestUtil.loader.getCount());
        assertNull(TestUtil.loader.find("FF:FM:1"));
        assertNotNull(TestUtil.loader.find("FF:FM:2"));
        assertNotNull(TestUtil.loader.find("FF:FM:3"));
        
        Thread.sleep(61 * 1000);
        assertEquals(3, TestUtil.loader.getCount());
    }
}

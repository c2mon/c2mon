/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.mobicall;

import static org.junit.Assert.*;

import java.sql.Timestamp;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.alarm.AlarmValueImpl;

public class TestConfiguration {

    //
    // --- SETUP test ------------------------------------------------------------------------------
    //
    @BeforeClass
    public static void initClass() throws Exception {
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
    public void testConfig() {
        assertNotNull(TestUtil.loader.find("FF:FM:1"));
        assertNotNull(TestUtil.loader.find("FF:FM:2"));
        assertNull(TestUtil.loader.find("FF:FM:3"));
        
        AlarmValue av = new AlarmValueImpl(1L, 1, "FM", "FF", "Info", 1L, new Timestamp(System.currentTimeMillis()), true);
        assertEquals(MobicallAlarmsPublisher.getAlarmId(av), "FF:FM:1");

        String expected ="FF FM 1 [111]  Dummy problem description ACTIVE";
        assertEquals(expected, MobicallAlarmsPublisher.composeTrapMessage(TestUtil.loader.find("FF:FM:1"), av));        
    }
        
}

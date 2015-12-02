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

public class ConnectionTest {

    //
    // --- SETUP test ------------------------------------------------------------------------------
    //
    @BeforeClass
    public static void initClass() throws Exception {
        TestBaseClass.init();
        TestBaseClass.startTestPublisher();
    }
 
    @AfterClass
    public static void cleanupClass() throws Exception {
        TestBaseClass.init();
        TestBaseClass.startTestPublisher();
    }
    
    //
    // --- TESTS  -----------------------------------------------------------------------------------
    //
    @Test
    public void testLoader() {
        assertNotNull(TestBaseClass.loader.find("FF:FM:1"));
        assertNull(TestBaseClass.loader.find("FF:FM:2"));
        
        AlarmValue av = new AlarmValueImpl(1L, 1, "FM", "FF", "Info", 1L, new Timestamp(System.currentTimeMillis()), true);
        assertEquals(MobicallAlarmsPublisher.getAlarmId(av), TestBaseClass.ALARM_ID);

        String expected ="1 FF FM 1 [111]  Dummy problem description ACTIVE";
        assertEquals(MobicallAlarmsPublisher.composeTrapMessage(TestBaseClass.loader.find("FF:FM:1"), av), expected);        
    }
    
    @Test
    public void testSender() {
        TestBaseClass.c2mon.activateAlarm("FF", "FM", 1, false);
        assertEquals(TestBaseClass.sender.getCount(), 0);
        TestBaseClass.c2mon.activateAlarm("FF", "FM", 1, true);
        assertEquals(TestBaseClass.sender.getCount(), 1);
        TestBaseClass.c2mon.activateAlarm("FF", "FM", 2, true);
        assertEquals(TestBaseClass.sender.getCount(), 1);
        TestBaseClass.c2mon.activateAlarm("FF", "FM", 2, false);
        assertEquals(TestBaseClass.sender.getCount(), 1);
    }
    
    // TODO test configuration thread triggering reload on request
    // TODO in mock loader, add an alarm and checl it is there
    
    // TODO test stop procedure
    
    // TODO test safety lock on reconfig ?
    
    @Test
    public void testConnection() {
        try {
            synchronized (TestBaseClass.publisher) {
                TestBaseClass.publisher.wait(15 * 1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

}

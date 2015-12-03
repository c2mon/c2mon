/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.mobicall;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Check that notifications are sent only for valid and configured alarms, i.e. that nothing is
 * sent if we do not have a notification id, or the underlying datatag is not vali. 
 * 
 * @author mbuttner
 */
public class TestSender {

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
    public void testSender() {
        // this one should be rejected as it is not valid, counter must nevertheless be one due to 
        // the send of the initial selection
        TestUtil.c2mon.activateAlarm("FF", "FM", 1, false);
        assertEquals(1, TestUtil.sender.getCount());
        
        // this one should be notified
        TestUtil.c2mon.activateAlarm("FF", "FM", 1, true);
        assertEquals(2, TestUtil.sender.getCount());
        
        // no mobicall definition for this alarm, no send
        TestUtil.c2mon.activateAlarm("FF", "FM", 3, true);
        assertEquals(2, TestUtil.sender.getCount());
        
        // no mobicall definition and alarm not valid, so no additional send
        TestUtil.c2mon.activateAlarm("FF", "FM", 3, false);
        assertEquals(2, TestUtil.sender.getCount());
    }


}

/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Verify activating/termination mechanism of SpectrumAlarm object. The logic behind
 * this object is to keep the alarm up as long as at least one distinct fault is known
 * for the same target. If the alarm count is down to 0, the alarm should be terminated.
 * 
 * @author mbuttner
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SpectrumAlarmTest
{

    Logger LOG = LoggerFactory.getLogger(SpectrumAlarmTest.class);
    
    //
    // --- TEST --------------------------------------------------------------------------------
    //    
    @Test
    public void testSpectrumAlarm()
    {
        SpectrumAlarm sa = new SpectrumAlarm("x", null);
        assertFalse(sa.isAlarmOn());
        
        sa.activate(1);
        assertTrue(sa.isAlarmOn());
        assertEquals(1, sa.getAlarmCount());
        
        sa.activate(2);
        assertTrue(sa.isAlarmOn());
        assertEquals(2, sa.getAlarmCount());
        
        sa.terminate(2);
        assertTrue(sa.isAlarmOn());
        assertEquals(1, sa.getAlarmCount());

        sa.terminate(2);
        assertTrue(sa.isAlarmOn());
        assertEquals(1, sa.getAlarmCount());
        
        sa.terminate(1);
        assertFalse(sa.isAlarmOn());
        assertEquals(0, sa.getAlarmCount());
        
        sa.terminate(3);
        assertFalse(sa.isAlarmOn());
        assertEquals(0, sa.getAlarmCount());
        
        sa.activate(1);
        assertTrue(sa.isAlarmOn());
        assertEquals(1, sa.getAlarmCount());
    }
}

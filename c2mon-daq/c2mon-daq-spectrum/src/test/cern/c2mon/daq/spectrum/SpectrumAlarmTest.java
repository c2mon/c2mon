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

import cern.c2mon.daq.test.GenericMessageHandlerTst;
import cern.c2mon.daq.test.UseHandler;

@UseHandler(SpectrumMessageHandler.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SpectrumAlarmTest extends GenericMessageHandlerTst {

    Logger LOG = LoggerFactory.getLogger(SpectrumAlarmTest.class);
    
    //
    // --- TEST --------------------------------------------------------------------------------
    //    
    @Test
    public void testSpectrumAlarm()
    {
        SpectrumAlarm sa = new SpectrumAlarm(null);
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
    }
    
    

    //
    // --- SETUP --------------------------------------------------------------------------------
    //
    @Override
    protected void beforeTest() throws Exception {
        //
    }

    @Override
    protected void afterTest() throws Exception {
        //
    }
}

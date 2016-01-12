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

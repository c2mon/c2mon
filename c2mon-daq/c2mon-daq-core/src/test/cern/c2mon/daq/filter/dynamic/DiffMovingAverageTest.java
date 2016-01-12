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
package cern.c2mon.daq.filter.dynamic;

import static junit.framework.Assert.assertTrue;

import org.junit.Test;

import cern.c2mon.daq.filter.dynamic.DiffMovingAverage;

public class DiffMovingAverageTest {
    private DiffMovingAverage timeDiffMovingAverage = new DiffMovingAverage(5);
    
    @Test
    public void testAverage() {
        int counter = 0;
        while (counter < 5) {
            timeDiffMovingAverage.recordTimestamp();
            try {
                Thread.sleep(100);
                counter++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        long currentAverage = timeDiffMovingAverage.getCurrentAverage();
        assertTrue(currentAverage >= 90 && currentAverage <= 110);
    }
    
    @Test
    public void testAverageAfterFirstAdd() {
        timeDiffMovingAverage.recordTimestamp();
        long currentAverage = timeDiffMovingAverage.getCurrentAverage();
        assertTrue(currentAverage == -1);
    }
}

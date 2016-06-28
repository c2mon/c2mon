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

import static junit.framework.Assert.*;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.daq.filter.dynamic.CounterTimeDeadbandActivator;
import cern.c2mon.shared.common.datatag.SourceDataTag;

public class CounterTimeDeadbandActivatorTest extends AbstractTestDyanmicTimeDeadbandActivator {
    
    private static final long CHECK_INTERVAL = 100;
    private static final int NUMBER_OF_COUNTERS = 3;
    private static final int MAX_TAGS = 10;
    private static final int NORMAL_TAGS = 5;
    private static final int DEADBANDTIME = 30;

    @Before
    public void setUp() {
        setActivator(new CounterTimeDeadbandActivator(NUMBER_OF_COUNTERS, 
                CHECK_INTERVAL, MAX_TAGS, NORMAL_TAGS, DEADBANDTIME));
        for (SourceDataTag sourceDataTag : getSourceDataTags().values()) {
            getActivator().addDataTag(sourceDataTag);
        }
    }
    
    @Test
    public void testOnOff() throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            getActivator().newTagValueSent(getTestKey());
        }
        Thread.sleep(CHECK_INTERVAL + 10L);
        assertTrue(getTestTag().getAddress().isTimeDeadbandEnabled());
        Thread.sleep(CHECK_INTERVAL * NUMBER_OF_COUNTERS);
        assertTrue(!getTestTag().getAddress().isTimeDeadbandEnabled());
    }
}

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

package cern.c2mon.publisher.rdaAlarms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class TestDataProvider extends TestBaseClass {
        
    @Test
    public void testGetSource() throws Exception {
        getLogger().info("Starting testGetSource() ----------------- ");

        DataProviderIntf dpi = applicationContext.getBean("dataProvider", DataProviderIntf.class);
        assertNotNull(dpi.getSource(TestBaseClass.ALARM_ID));
        assertNull(dpi.getSource(TestBaseClass.MISSING_ALARM_ID));
                
        getLogger().info("Completed testGetSource() ---------------- ");
    }

    @Test
    public void testGetSourceNames() throws Exception {
        getLogger().info("Starting testGetSourceNames() ----------------- ");
        DataProviderIntf dpi = applicationContext.getBean("dataProvider", DataProviderIntf.class);
        Collection<String> sourceNames = dpi.getSourceNames();
        assertTrue(1 <= sourceNames.size());
        assertTrue(sourceNames.contains(TestBaseClass.SOURCE_ID));

        getLogger().info("Completed testGetSourceNames() ---------------- ");
    }

    @Test
    public void testInitSourceMap() throws Exception {
        getLogger().info("Starting testInitSourceMap() ----------------- ");
        DataProviderIntf dpi = applicationContext.getBean("dataProvider", DataProviderIntf.class);
        HashSet<String> alarmIds = new HashSet<String>();
        alarmIds.add(TestBaseClass.ALARM_ID);
        alarmIds.add(TestBaseClass.MISSING_ALARM_ID);
        Map<String,String> alarmSource = dpi.initSourceMap(alarmIds);
        assertEquals(TestBaseClass.SOURCE_ID, alarmSource.get(TestBaseClass.ALARM_ID));
        assertNull(alarmSource.get(TestBaseClass.MISSING_ALARM_ID));

        getLogger().info("Completed testInitSourceMap() ---------------- ");
    }
}

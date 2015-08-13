/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.rdaAlarms;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:cern/c2mon/publisher/rdaAlarms/alarms_publisher.xml")
@ActiveProfiles(profiles = "TEST")
public class TestDataProvider extends TestBaseClass {
        
    @Test
    public void testGetSource() throws Exception {
        getLogger().info("Starting testGetSource() ----------------- ");

        DataProviderIntf dpi = applicationContext.getBean("dataProvider", DataProviderIntf.class);
        assertNotNull(dpi.getSource(TestBaseClass.EXISTING_ALARM_ID));
        assertNull(dpi.getSource(TestBaseClass.NOT_EXISTING_ALARM_ID));
                
        getLogger().info("Completed testGetSource() ---------------- ");
    }

    @Test
    public void testGetSourceNames() throws Exception {
        getLogger().info("Starting testGetSourceNames() ----------------- ");
        DataProviderIntf dpi = applicationContext.getBean("dataProvider", DataProviderIntf.class);
        Collection<String> sourceNames = dpi.getSourceNames();
        assertTrue(1 <= sourceNames.size());
        assertTrue(sourceNames.contains(TestBaseClass.EXISTING_SOURCE_ID));

        getLogger().info("Completed testGetSourceNames() ---------------- ");
    }

    @Test
    public void testInitSourceMap() throws Exception {
        getLogger().info("Starting testInitSourceMap() ----------------- ");
        DataProviderIntf dpi = applicationContext.getBean("dataProvider", DataProviderIntf.class);
        HashSet<String> alarmIds = new HashSet<String>();
        alarmIds.add(TestBaseClass.EXISTING_ALARM_ID);
        alarmIds.add(TestBaseClass.NOT_EXISTING_ALARM_ID);
        Map<String,String> alarmSource = dpi.initSourceMap(alarmIds);
        assertEquals(TestBaseClass.EXISTING_SOURCE_ID, alarmSource.get(TestBaseClass.EXISTING_ALARM_ID));
        assertNull(alarmSource.get(TestBaseClass.NOT_EXISTING_ALARM_ID));

        getLogger().info("Completed testInitSourceMap() ---------------- ");
    }
}

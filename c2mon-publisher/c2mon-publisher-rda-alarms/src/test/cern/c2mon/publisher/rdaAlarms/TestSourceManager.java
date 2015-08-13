/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.rdaAlarms;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:cern/c2mon/publisher/rdaAlarms/alarms_publisher.xml")
@ActiveProfiles(profiles = "TEST")
public class TestSourceManager extends TestBaseClass {

    @Test
    public void testSourceCount() throws Exception {
        getLogger().info("Starting testSourceCount() ----------------- ");

        SourceManager smgr = applicationContext.getBean("sourceMgr", SourceManager.class);
        C2monConnectionIntf c2mon = applicationContext.getBean("c2mon", C2monConnectionIntf.class);
        smgr.initialize(c2mon.getActiveAlarms());

        assertTrue(1 <= smgr.getSourceCount());
        
        assertEquals(TestBaseClass.EXISTING_SOURCE_ID, smgr.getSourceNameForAlarm(TestBaseClass.EXISTING_ALARM_ID));
        assertNull(smgr.getSourceNameForAlarm(TestBaseClass.NOT_EXISTING_ALARM_ID));
        
        assertNotNull(smgr.findProp(TestBaseClass.EXISTING_ALARM_ID));
        assertNull(smgr.findProp(TestBaseClass.NOT_EXISTING_ALARM_ID));
        getLogger().info("Completed testSourceCount() ---------------- ");
    }

    // TODO add alarms to source manager as they arrive!
    // TODO check the alarm count, including after some activation
    // TODO test the garbage collector 
    
    
    
}

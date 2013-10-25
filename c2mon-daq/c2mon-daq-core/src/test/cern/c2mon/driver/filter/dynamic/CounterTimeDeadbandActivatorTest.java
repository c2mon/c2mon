package cern.c2mon.driver.filter.dynamic;

import static junit.framework.Assert.*;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.driver.filter.dynamic.CounterTimeDeadbandActivator;
import cern.tim.shared.daq.datatag.SourceDataTag;

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

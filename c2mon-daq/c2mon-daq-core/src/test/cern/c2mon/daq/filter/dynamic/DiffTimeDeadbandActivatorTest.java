package cern.c2mon.daq.filter.dynamic;


import static junit.framework.Assert.*;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.daq.filter.dynamic.TimeDifferenceMovingAverageTimeDeadbandActivator;
import cern.c2mon.shared.daq.datatag.SourceDataTag;


public class DiffTimeDeadbandActivatorTest extends AbstractTestDyanmicTimeDeadbandActivator {
    
    @Before
    public void setUp() {
        setActivator(new TimeDifferenceMovingAverageTimeDeadbandActivator(3, 50, 100, 30));
        for (SourceDataTag sourceDataTag : getSourceDataTags().values()) {
            getActivator().addDataTag(sourceDataTag);
        }
    }
    
    @Test
    public void testOnOff() throws InterruptedException {
        for (int i = 0; i < 3; i++) {
            getActivator().newTagValueSent(getTestKey());
        }
        assertTrue(getTestTag().getAddress().isTimeDeadbandEnabled());
        Thread.sleep(600);
        getActivator().newTagValueSent(getTestKey());
        assertFalse(getTestTag().getAddress().isTimeDeadbandEnabled());
    }
    
    @Test
    public void testNoActivationOnFirstRecord() {
        getActivator().newTagValueSent(getTestKey());
        assertFalse(getTestTag().getAddress().isTimeDeadbandEnabled());
    }
}

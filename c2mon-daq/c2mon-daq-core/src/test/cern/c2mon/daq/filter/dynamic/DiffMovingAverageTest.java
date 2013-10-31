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

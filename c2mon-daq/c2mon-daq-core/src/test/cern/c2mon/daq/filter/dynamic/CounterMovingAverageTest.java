package cern.c2mon.daq.filter.dynamic;

import static junit.framework.Assert.*;
import org.junit.Test;

import cern.c2mon.daq.filter.dynamic.CounterMovingAverage;

public class CounterMovingAverageTest {
    private CounterMovingAverage counterMovingAverage = new CounterMovingAverage(5);
    
    @Test
    public void testAverage() {
        int counterSwitches = 5;
        int numberOfCounts = 50;
        for (int i = 0; i < counterSwitches; i++) {
            for (int j = 0; j < numberOfCounts; j++) {
                counterMovingAverage.increaseCurrentCounter();
            }
            counterMovingAverage.switchCurrentCounter();
        }
        assertTrue(counterMovingAverage.getCurrentAverage() == 40);
    }

}

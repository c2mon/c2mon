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
/**
 * Calculates an average over counters and allows to increase and to switch them.
 * @author alang
 *
 */
public class CounterMovingAverage {
    /**
     * The default number of counters used if not specified.
     */
    public static final int DEFAULT_NUMBER_OFCOUNTERS = 10;
    /**
     * The array containing all counters.
     */
    private int[] counters;
    /**
     * The currently used counter.
     */
    private int currentCounter = 0;
    
    /**
     * Creates a new CounterMovingAverage with a default number of counters.
     */
    public CounterMovingAverage() {
        this(DEFAULT_NUMBER_OFCOUNTERS);
    }
    
    /**
     * Creates a new CounterMovingAverage with the provided number of counters.
     * @param numberOfCounters The number of counters.
     */
    public CounterMovingAverage(final int numberOfCounters) {
        counters = new int[numberOfCounters];
    }

    /**
     * Returns the current average over all counters.
     * @return The current average over all counters.
     */
    public synchronized int getCurrentAverage() {
        int sum = 0;
        for (int counter : counters) {
            sum += counter;
        }
        return sum / counters.length;
    }

    /**
     * Switches to the next counter and resets it. If the current counter is the
     * last one it switches to he first.
     */
    public synchronized void switchCurrentCounter() {
        currentCounter = (currentCounter + 1) % counters.length;
        counters[currentCounter] = 0;
    }
    
    /**
     * Increases the value of the current counter by one.
     */
    public synchronized void increaseCurrentCounter() {
        counters[currentCounter] = counters[currentCounter] + 1;
    }
    
    /**
     * Creates a String representation of the contained array.
     * @return String representation of the contained array.
     */
    @Override
    public String toString() {
        StringBuilder buffer =  new StringBuilder();
        for (int counter : counters) {
            buffer.append("[" + counter + "]");
        }
        return buffer.toString();
    }

}

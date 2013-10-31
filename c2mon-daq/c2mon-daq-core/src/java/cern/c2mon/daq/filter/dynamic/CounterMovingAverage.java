/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005 - 2010 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
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

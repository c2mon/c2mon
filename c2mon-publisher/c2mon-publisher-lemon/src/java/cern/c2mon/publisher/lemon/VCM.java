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

package cern.c2mon.publisher.lemon;

import java.util.Timer;
import java.util.TimerTask;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * The VCM (Value Change Monitor) provides the difference between the last acquisitions for a given
 * long value. It also keeps the total value sync'd with the vcm value (i.e. the sum of the reported
 * changes). The time period is defined by construction. A timer is than started to update the values
 * as returned by the getters. 
 * 
 * The increments are counted into a temporary attribute until the period is elapsed. The timer
 * than runs a task which places the temporary counter into the vcm value attribute and updates the
 * total. The corresponding setters are sync'd to make the VCM thread safe.
 * 
 * After the first period, the total will be the same as the difference. Until there, both values
 * will be 0. After further periods, total will always be gt the vcm value
 * 
 * @author mbuttner
 */
@ManagedResource(objectName = "cern.c2mon.publisher.lemon:name=VCM", description = "Published events")
public class VCM extends TimerTask {
    
    private long currentCounter;        // track of the increments during the current period
    private long lastValue;             // value observed during last complete period
    private long totalValue;            // value observed since the start of the VCM timer

    
    //
    // --- CONSTRUCTION -----------------------------------------------------------------
    //
    /**
     * Setter for construction by Spring.
     * @param seconds in between two acquisitions of the vcm value
     */
    public void setVcmSeconds(int seconds) {
        Timer timer = new Timer() ;
        long millis = seconds * 1000;
        timer.schedule(this, millis, millis) ;            
    }
    
    //
    // --- Implements TimerTask ----------------------------------------------------------
    //
    @Override
    public void run () {
        switchValues();
    }

    //
    // --- PRIVATE METHODS ---------------------------------------------------------------
    //
    private synchronized void switchValues()
    {
        lastValue = currentCounter;
        totalValue += currentCounter;
        currentCounter = 0;
    }
    
    //
    // --- PUBLIC METHODS ----------------------------------------------------------------
    //
    @ManagedAttribute
    public long getPublishedLastPeriod() {
        return lastValue;
    }

    @ManagedAttribute
    public long getPublishedTotal() {
        return totalValue;
    }

    public synchronized void increment() {
        currentCounter++;
    }
}

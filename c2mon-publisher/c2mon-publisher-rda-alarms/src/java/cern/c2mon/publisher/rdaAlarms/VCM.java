/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.rdaAlarms;

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
 * will be 0. After further periods, total will always be >= the vcm value
 * 
 * @author mbuttner
 */
@ManagedResource(description = "VCM counter object")
public class VCM {
    
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
        timer.schedule(new Updater(), millis, millis) ;            
    }
    
    //
    // --- Implements TimerTask ----------------------------------------------------------
    //
    class Updater extends TimerTask {
        @Override
        public void run () {
            switchValues();
        }
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
    public long getValue() {
        return lastValue;
    }

    @ManagedAttribute
    public long getTotal() {
        return totalValue;
    }

    public synchronized void increment() {
        currentCounter++;
    }
}
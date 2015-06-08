/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum;

import java.util.ArrayList;
import java.util.Collection;

import cern.c2mon.shared.common.datatag.ISourceDataTag;

/***
 * The alarm for a given host is considered to be "on" if Spectrum signaled at least one
 * error for it. The alarm goes off only once the entire list of various errors signaled
 * by Spectrum was signaled to be terminated. For this purpose, each instance keeps a list
 * of errors actually active for a given host.
 * 
 * @author mbuttner
 */
public class SpectrumAlarm {
    
    private ArrayList<Long> spectrumAlarmIds ;
    private ISourceDataTag tag;
    private boolean alarmOn;
    
    //
    // --- CONSTRUCTION --------------------------------------------------------------
    //
    public SpectrumAlarm(ISourceDataTag tag)
    {
        spectrumAlarmIds = new ArrayList<Long>();
        alarmOn = false;
        this.tag = tag;
    }

    //
    // --- PUBLIC METHODS ------------------------------------------------------------
    //
    public int getAlarmCount()
    {
        return spectrumAlarmIds.size();
    }

    public ISourceDataTag getTag() {
        return tag;
    }

    public boolean isAlarmOn() {
        return alarmOn;
    }
    
    /**
     * When the Spectrum server operates a reset, we have to clean all known errors at once
     */
    public void clear() {
        spectrumAlarmIds.clear();
        alarmOn = false;
    }
    
    /**
     * If the specified Spectrum error is not yet known in our list for this host, it
     * is added. As the list contains at least one error after the call, the global
     * alarm for us is always true.
     * 
     * @param spectrumAlarmId <code>long</code> id of the alarm as defined by Spectrum
     */
    public void activate(long spectrumAlarmId)
    {
        if (!spectrumAlarmIds.contains(spectrumAlarmId))
        {
            spectrumAlarmIds.add(spectrumAlarmId);
        }
        alarmOn = true;
    }

    
    /**
     * Remove the specified error. If after the call the list is empty, the alarms is
     * considered to be off
     * 
     * @param spectrumAlarmId <code>long</code> id of the alarm as defined by Spectrum
     */
    public void terminate(long spectrumAlarmId)
    {
        if (spectrumAlarmIds.contains(spectrumAlarmId))
        {
            spectrumAlarmIds.remove(spectrumAlarmId);
        }        
        if (this.getAlarmCount() == 0)
        {
            alarmOn = false;
        }
    }

    public Collection<Long> getAlarmIds() {
        return this.spectrumAlarmIds;
    }
    
}

/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum;

import java.util.ArrayList;

import cern.c2mon.shared.common.datatag.ISourceDataTag;

public class SpectrumAlarm {
    
    private String hostname;
    private ArrayList<Long> spectrumAlarmIds ;
    private ISourceDataTag tag;
    private boolean alarmOn;
    
    //
    // --- CONSTRUCTION --------------------------------------------------------------
    //
    public SpectrumAlarm(String hostname, ISourceDataTag tag)
    {
        this.hostname = hostname;
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

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public ISourceDataTag getTag() {
        return tag;
    }

    public void setTag(ISourceDataTag tag) {
        this.tag = tag;
    }

    public boolean isAlarmOn() {
        return alarmOn;
    }

    public void setAlarmOn(boolean alarmOn) {
        this.alarmOn = alarmOn;
    }
    
    public void activate(long spectrumAlarmId)
    {
        if (!spectrumAlarmIds.contains(spectrumAlarmId))
        {
            spectrumAlarmIds.add(spectrumAlarmId);
        }
        alarmOn = true;
    }

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
    
}

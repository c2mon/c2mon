/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.monit;

import cern.c2mon.daq.monit.util.JsonUtils;

public class MonitHardwareAddress {

    private String hostname;
    private String metricname;
    
    //
    // --- CONSTRUCTION ----------------------------------------------------------------------
    //
    private MonitHardwareAddress() 
    {
    }

    public static MonitHardwareAddress fromJson(final String json) 
    {
        MonitHardwareAddress addr = JsonUtils.fromJson(json, MonitHardwareAddress.class);
        if (addr == null) 
        {
            throw new IllegalArgumentException("Address creation failed. Check the JSON declaration");
        }
        return addr;
    }

    //
    // --- PUBLIC METHODS --------------------------------------------------------------------
    //
    public String getHostname() 
    {
        return hostname;
    }

    public String getMetricname() {
        return this.metricname;
    }
    
    //
    // --- Overrides java.lang.Object ---------------------------------------------------------
    //
    @Override
    public String toString() 
    {
        return JsonUtils.toJson(this);
    }

    

}

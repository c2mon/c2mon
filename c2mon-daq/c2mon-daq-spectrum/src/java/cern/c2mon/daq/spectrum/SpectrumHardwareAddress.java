/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum;

import cern.c2mon.daq.spectrum.util.JsonUtils;

public class SpectrumHardwareAddress {

    private String hostname;
    
    private SpectrumHardwareAddress() 
    {
    }

    public String getHostname() 
    {
        return hostname;
    }

    @Override
    public String toString() 
    {
        return JsonUtils.toJson(this);
    }

    public static SpectrumHardwareAddress fromJson(final String json) 
    {
        SpectrumHardwareAddress addr = JsonUtils.fromJson(json, SpectrumHardwareAddress.class);
        if (addr == null) 
        {
            throw new IllegalArgumentException("Address creation failed. Check the JSON declaration");
        }
        return addr;
    }
    

}

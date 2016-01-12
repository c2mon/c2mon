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

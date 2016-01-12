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

import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.shared.common.datatag.ISourceDataTag;

public class MonitUpdateEvent {

    private static IEquipmentMessageSender equipmentMessageSender;

    private String hostname;
    private String metric;
    private ISourceDataTag tag;

    private long ts;
    private Object value;
    private String descr;
    
    //
    // --- CONSTRUCTION -----------------------------------------------------------------------
    //

    public MonitUpdateEvent(String hostname, String metric, ISourceDataTag tag) {
        this.hostname = hostname;
        this.tag = tag;
        this.metric = metric;
    }

    //
    // --- PUBLIC METHODS ----------------------------------------------------------------------
    //
    public static void setSender(IEquipmentMessageSender equipmentMessageSender) {
        MonitUpdateEvent.equipmentMessageSender = equipmentMessageSender;
    }
    
    public String getHostname() {
        return hostname;
    }

    public String getMetricname() {
        return this.metric;
    }
    
    public ISourceDataTag getTag() {
        return tag;
    }

    public long getUserTimestamp() {
        return ts;
    }

    public void setValue(Object value, String descr) {
        this.value = value;
        this.ts = System.currentTimeMillis();
        this.descr = descr;
    }
    

    public void sendUpdate() {
        equipmentMessageSender.sendTagFiltered(tag, value, ts, descr);        
    }
    
    public Object getValue() {
        return this.value;
    }
    
}

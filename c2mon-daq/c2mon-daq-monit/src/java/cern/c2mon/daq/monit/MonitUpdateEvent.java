/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

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

/**
 * Copyright (c) 2013 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.common;

/**
 * The <code>ValueChangeMonitorEngine</code> class represents events that can be registered inside the
 * <code>ValueChangeMonitorEngine</code>
 * 
 * @author wbuczak
 */
public class ValueChangeMonitorEvent {
    private long id;
    private double value;
    private String valueDescr;
    private long timestamp;

    public ValueChangeMonitorEvent(final long id, double value, final String valueDescr, final long timestamp) {
        this.id = id;
        this.value = value;
        this.valueDescr = valueDescr;
        this.timestamp = timestamp;
    }

    public long getId() {
        return id;
    }

    public double getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getValueDescription() {
        return valueDescr;
    }
}

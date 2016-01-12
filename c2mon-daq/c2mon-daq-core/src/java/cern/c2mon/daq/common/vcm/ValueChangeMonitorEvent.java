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

package cern.c2mon.daq.common.vcm;

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

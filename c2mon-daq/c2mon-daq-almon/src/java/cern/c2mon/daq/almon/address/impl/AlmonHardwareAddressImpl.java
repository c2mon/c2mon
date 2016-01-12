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

package cern.c2mon.daq.almon.address.impl;

import static java.lang.String.format;
import cern.c2mon.daq.almon.address.AlarmTriplet;
import cern.c2mon.daq.almon.address.AlarmType;
import cern.c2mon.daq.almon.address.AlmonHardwareAddress;
import cern.c2mon.daq.almon.util.JsonUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This is the implementation of the <code>AlmonHardwareAddress</code>
 * 
 * @author wbuczak
 */
public class AlmonHardwareAddressImpl implements AlmonHardwareAddress {

    // required by jackson as it creates objects via reflection
    @SuppressWarnings("unused")
    private AlmonHardwareAddressImpl() {

    }

    public AlmonHardwareAddressImpl(final AlarmType type, final String device, final String property,
            final String field, final AlarmTriplet triplet) {
        this.type = type;
        this.device = device;
        this.property = property;
        this.field = field;
        this.alarmTriplet = triplet;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((alarmTriplet == null) ? 0 : alarmTriplet.hashCode());
        result = prime * result + ((device == null) ? 0 : device.hashCode());
        result = prime * result + ((field == null) ? 0 : field.hashCode());
        result = prime * result + ((property == null) ? 0 : property.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AlmonHardwareAddressImpl other = (AlmonHardwareAddressImpl) obj;
        if (alarmTriplet == null) {
            if (other.alarmTriplet != null)
                return false;
        } else if (!alarmTriplet.equals(other.alarmTriplet))
            return false;
        if (device == null) {
            if (other.device != null)
                return false;
        } else if (!device.equals(other.device))
            return false;
        if (field == null) {
            if (other.field != null)
                return false;
        } else if (!field.equals(other.field))
            return false;
        if (property == null) {
            if (other.property != null)
                return false;
        } else if (!property.equals(other.property))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    private AlarmType type;
    private AlarmTriplet alarmTriplet;

    private String device;
    private String property;
    private String field;

    @Override
    public AlarmType getType() {
        return type;
    }

    @Override
    public String getDevice() {
        return device;
    }

    @Override
    public String getProperty() {
        return property;
    }

    @Override
    public String getField() {
        return field;
    }

    @Override
    public AlarmTriplet getAlarmTriplet() {
        return alarmTriplet;
    }

    @JsonIgnore
    @Override
    public boolean hasCycle() {
        boolean hasCycle = false;
        switch (type) {
        case GM:
            hasCycle = true;
            break;
        case FESA:
            hasCycle = false;
            break;
        }
        return hasCycle;
    }

    @JsonIgnore
    @Override
    public String getCycle() {
        String cycle = null;

        switch (type) {
        case GM:
            cycle = GM_JAPC_ALARM_SELECTOR;
            break;
        case FESA:
            break;
        }
        return cycle;
    }

    /**
     * @return JAPC parameter in format DEVICE/PROPERTY#field
     */
    @JsonIgnore
    @Override
    public String getJapcParameterName() {
        return format("%s/%s#%s", getDevice(), getProperty(), getField());
    }

    @Override
    public String toString() {
        return toJson();
    }

    public String toJson() {
        return JsonUtils.toJson(this);
    }

    public static AlmonHardwareAddressImpl fromJson(final String json) {
        if (json == null) {
            return null;
        }

        return JsonUtils.fromJson(json, AlmonHardwareAddressImpl.class);
    }

}

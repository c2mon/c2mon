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

package cern.c2mon.daq.almon.address;

import cern.c2mon.daq.almon.util.JsonUtils;

/**
 * Represents an alarm
 * 
 * @author wbuczak
 */
public class AlarmTriplet {

    private String faultFamily;
    private String faultMember;
    private int faultCode;

    public String getFaultFamily() {
        return faultFamily;
    }

    public String getFaultMember() {
        return faultMember;
    }

    public int getFaultCode() {
        return faultCode;
    }

    // jackson needs it!
    @SuppressWarnings("unused")
    private AlarmTriplet() {
    }

    public AlarmTriplet(final String faultFamily, final String faultMember, final int faultCode) {
        this.faultFamily = faultFamily;
        this.faultMember = faultMember;
        this.faultCode = faultCode;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + faultCode;
        result = prime * result + ((faultFamily == null) ? 0 : faultFamily.hashCode());
        result = prime * result + ((faultMember == null) ? 0 : faultMember.hashCode());
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
        AlarmTriplet other = (AlarmTriplet) obj;
        if (faultCode != other.faultCode)
            return false;
        if (faultFamily == null) {
            if (other.faultFamily != null)
                return false;
        } else if (!faultFamily.equals(other.faultFamily))
            return false;
        if (faultMember == null) {
            if (other.faultMember != null)
                return false;
        } else if (!faultMember.equals(other.faultMember))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return String.format("%s:%s:%d", faultFamily, faultMember, faultCode);
    }

    public String toJson() {
        return JsonUtils.toJson(this);
    }

    public static AlarmTriplet fromJson(final String json) {
        if (json == null) {
            return null;
        }

        return JsonUtils.fromJson(json, AlarmTriplet.class);
    }

    public static AlarmTriplet fromString(String json) {
        return fromJson(json);
    }
}

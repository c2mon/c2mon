/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.almon.address;

import cern.c2mon.daq.almon.util.JsonUtils;

/**
 * Represents an alarm
 * 
 * @author wbuczak
 */
public class AlarmTripplet {

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
    private AlarmTripplet() {
    }

    public AlarmTripplet(final String faultFamily, final String faultMember, final int faultCode) {
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
        AlarmTripplet other = (AlarmTripplet) obj;
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

    public static AlarmTripplet fromJson(final String json) {
        if (json == null) {
            return null;
        }

        return JsonUtils.fromJson(json, AlarmTripplet.class);
    }

    /**
     * @param tripplet
     * @return
     */
    public static AlarmTripplet fromString(String json) {
        return fromJson(json);
    }
}
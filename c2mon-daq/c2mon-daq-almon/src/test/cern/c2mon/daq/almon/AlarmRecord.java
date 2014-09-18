/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.almon;

import cern.c2mon.daq.almon.address.UserProperties;

/**
 * @author wbuczak
 */
public class AlarmRecord {
    private AlarmState alarmState;
    private UserProperties userProperties;
    private long userTimestamp;

    public AlarmRecord(AlarmState alarmState, long userTimestamp) {
        this(alarmState, userTimestamp, null);
    }

    public AlarmRecord(AlarmState alarmState, long userTimestamp, UserProperties userProperties) {
        this.alarmState = alarmState;
        this.userProperties = userProperties;
        this.userTimestamp = userTimestamp;
    }

    public AlarmState getAlarmState() {
        return alarmState;
    }

    public UserProperties getUserProperties() {
        return userProperties;
    }

    public long getUserTimestamp() {
        return userTimestamp;
    }

}
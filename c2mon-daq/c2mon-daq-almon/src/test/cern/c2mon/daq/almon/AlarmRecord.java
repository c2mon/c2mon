/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.almon;

import java.util.Properties;

import cern.c2mon.daq.almon.AlarmState;

/**
 * @author wbuczak
 */
public class AlarmRecord {
    private AlarmState alarmState;
    private Properties userProperties;
    private long userTimestamp;

    public AlarmRecord(AlarmState alarmState, long userTimestamp) {
        this(alarmState, userTimestamp, null);
    }

    public AlarmRecord(AlarmState alarmState, long userTimestamp, Properties userProperties) {
        this.alarmState = alarmState;
        this.userProperties = userProperties;
        this.userTimestamp = userTimestamp;
    }

    public AlarmState getAlarmState() {
        return alarmState;
    }

    public Properties getUserProperties() {
        return userProperties;
    }

    public long getUserTimestamp() {
        return userTimestamp;
    }

}
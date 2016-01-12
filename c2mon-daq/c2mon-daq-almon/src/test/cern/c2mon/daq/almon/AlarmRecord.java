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

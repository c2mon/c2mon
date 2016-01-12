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

package cern.c2mon.publisher.rdaAlarms;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

import javax.jms.JMSException;

import cern.c2mon.client.jms.AlarmListener;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.alarm.AlarmValueImpl;

public class C2monConnectionMock implements C2monConnectionIntf{

    AlarmListener listener;
    
    @Override
    public void setListener(AlarmListener listener) {
        this.listener = listener;
    }


    @Override
    public Collection<AlarmValue> getActiveAlarms() {
        AlarmValue av = new AlarmValueImpl(1L, 1, "FM", "FF", "Info", 1L, new Timestamp(System.currentTimeMillis()), true);
        ArrayList<AlarmValue> activeAlarms = new ArrayList<AlarmValue>();
        activeAlarms.add(av);
        return activeAlarms;
    }

    public void activateAlarm(String ff, String fm, int fc) {
        AlarmValue av = new AlarmValueImpl(1L, fc, fm, ff, "Activation", 1L, getSystemTs(), true);
        listener.onAlarmUpdate(av);
    }

    public void terminateAlarm(String ff, String fm, int fc) {
        AlarmValue av = new AlarmValueImpl(1L, fc, fm, ff, "Activation", 1L, getSystemTs(), false);
        listener.onAlarmUpdate(av);
    }

    private Timestamp getSystemTs() {
        return new Timestamp(System.currentTimeMillis());
    }
    
    @Override
    public int getQuality(long alarmTagId) {
        int qual = Quality.EXISTING | Quality.VALID;
        return qual;
    }

    @Override
    public void connectListener() throws JMSException {
        // not needed for mock
    }
    
    @Override
    public void start() throws Exception {
        // not needed for mock
    }

    @Override
    public void stop() {
        // not needed for mock
    }
    
}

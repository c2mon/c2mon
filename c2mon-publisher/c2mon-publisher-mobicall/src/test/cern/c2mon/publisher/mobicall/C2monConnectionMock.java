/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.mobicall;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

import javax.jms.JMSException;

import cern.c2mon.client.jms.AlarmListener;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.alarm.AlarmValueImpl;

public class C2monConnectionMock implements C2monConnectionIntf {

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

    public void activateAlarm(String ff, String fm, int fc, boolean valid) {
        long tagId = 2;
        if (valid) {
            tagId = 1;
        }
        AlarmValue av = new AlarmValueImpl(1L, fc, fm, ff, "Activation", tagId, getSystemTs(), true);
        listener.onAlarmUpdate(av);
    }

    public void terminateAlarm(String ff, String fm, int fc, boolean valid) {
        long tagId = 2;
        if (valid) {
            tagId = 1;
        }
        AlarmValue av = new AlarmValueImpl(1L, fc, fm, ff, "Activation", tagId, getSystemTs(), false);
        listener.onAlarmUpdate(av);
    }

    private Timestamp getSystemTs() {
        return new Timestamp(System.currentTimeMillis());
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
    

    @Override
    public boolean isTagValid(Long tagId) {
        return true;
    }
        
}

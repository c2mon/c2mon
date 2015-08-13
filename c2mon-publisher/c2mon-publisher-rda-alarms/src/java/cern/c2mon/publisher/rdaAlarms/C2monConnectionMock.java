/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.rdaAlarms;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

import cern.c2mon.client.jms.AlarmListener;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.alarm.AlarmValueImpl;

// TODO add a thread to generate some test events
public class C2monConnectionMock implements C2monConnectionIntf{

    AlarmListener listener;
    
    @Override
    public void setListener(AlarmListener listener) {
        this.listener = listener;
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
    public Collection<AlarmValue> getActiveAlarms() {
        AlarmValue av = new AlarmValueImpl(1L, 1, "FM", "FF", "Info", 1L, new Timestamp(System.currentTimeMillis()), true);
        ArrayList<AlarmValue> activeAlarms = new ArrayList<AlarmValue>();
        activeAlarms.add(av);
        return activeAlarms;
    }

}

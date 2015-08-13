/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.rdaAlarms;

import java.util.Collection;

import cern.c2mon.client.jms.AlarmListener;
import cern.c2mon.shared.client.alarm.AlarmValue;

public interface C2monConnectionIntf {

    void setListener(AlarmListener listener);
    void start() throws Exception;
    void stop();
    Collection<AlarmValue> getActiveAlarms();

}

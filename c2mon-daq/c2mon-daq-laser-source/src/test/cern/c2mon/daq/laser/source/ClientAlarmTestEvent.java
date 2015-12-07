/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.laser.source;

import cern.diamon.alarms.client.ClientAlarmEvent;

class ClientAlarmTestEvent extends ClientAlarmEvent {

    public ClientAlarmTestEvent(String deviceClass, String deviceName, int faultCode) {
        super(deviceClass, deviceName, faultCode);
    }

    static ClientAlarmEvent createAlarm(boolean active, String deviceClass, String deviceName, int faultCode) {
        ClientAlarmTestEvent alarm = new ClientAlarmTestEvent(deviceClass, deviceName, faultCode);
        alarm.setActive(active);
        alarm.setUserTs(System.currentTimeMillis());
        alarm.setProperty("ASI_PREFIX", "");

        return alarm;

    }
}
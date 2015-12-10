/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.laser.source.testutil;

import cern.diamon.alarms.client.ClientAlarmEvent;

/**
 * Builder for the test alarm events.
 * 
 * @author mbuttner
 */
class AlarmTestEvent extends ClientAlarmEvent {

    private AlarmTestEvent(String deviceClass, String deviceName, int faultCode) {
        super(deviceClass, deviceName, faultCode);
    }

    static ClientAlarmEvent createAlarm(boolean active, String deviceClass, String deviceName, int faultCode) {
        AlarmTestEvent alarm = new AlarmTestEvent(deviceClass, deviceName, faultCode);
        alarm.setActive(active);
        alarm.setUserTs(System.currentTimeMillis());
        alarm.setProperty("ASI_PREFIX", "");

        return alarm;

    }
}
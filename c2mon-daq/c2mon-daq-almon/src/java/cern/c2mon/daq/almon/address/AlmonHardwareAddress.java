/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.almon.address;

/**
 * @author wbuczak
 */
public interface AlmonHardwareAddress {

    String GM_JAPC_ALARM_SELECTOR = "ASYNC.PERIODIC.30000";

    AlarmType getType();

    String getDevice();

    String getProperty();

    String getField();

    AlarmTripplet getAlarmTripplet();

    boolean hasCycle();

    String getCycle();

    String getJapcParameterName();
}

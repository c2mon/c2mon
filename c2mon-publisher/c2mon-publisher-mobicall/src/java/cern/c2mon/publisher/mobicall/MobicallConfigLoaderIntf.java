/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.mobicall;

public interface MobicallConfigLoaderIntf {
    
    // called on startup and than from time to time to update the configuration
    void loadConfig();
    
    // should return a MobicallAlarm object if this alarm has a notification id, null otherwise
    MobicallAlarm find(String alarmId);

    // for smooth shutdown of process
    void close();

}

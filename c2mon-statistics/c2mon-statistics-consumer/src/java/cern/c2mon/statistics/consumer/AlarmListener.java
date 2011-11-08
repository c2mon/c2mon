package cern.c2mon.statistics.consumer;

import java.io.File;

import cern.c2mon.pmanager.IAlarmListener;

/**
 * The alarm listener class for the fallback package.
 * Methods are not implemented yet.
 * 
 * @author mbrightw
 *
 */
public class AlarmListener implements IAlarmListener {

    public void dbUnavailable(boolean alarmUp, String exceptionMsg, String dbInfo) {        
    }
    
    public void diskFull(boolean alarmUp, String directoryName) {        
    }
    
    public void fileNotReachable(boolean alarmUp, File file) {
        
    }



}

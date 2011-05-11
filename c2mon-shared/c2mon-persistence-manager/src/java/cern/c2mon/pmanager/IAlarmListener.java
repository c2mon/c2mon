package cern.c2mon.pmanager;

import java.io.File;

import cern.c2mon.pmanager.alarm.FallbackAlarmsInterface;

/**
 * Interface defining the methods that every class willing to send an alarm or any error notification directly from the server 
 * should implement
 * @author mruizgar
 *
 */
public interface IAlarmListener extends FallbackAlarmsInterface {
    
    /** Alarm status when it is still activated in the alarm monitoring system*/
    //boolean ACTIVATED = true;
    
    /** Alarm status when it is terminated or not alarm has been sent*/
    //boolean DOWN = false;
    
   
    
    /** 
     * Notifies that the connection to the DB was lost
     * @param alarmUp It indicates whether the alarm or notification method has to be activated or deactivated
     * @param exceptionMsg Contains the exception detailed message defining the DB problem
     * @param dbInfo A string identifying a user database account
     */
    void dbUnavailable(boolean alarmUp, String exceptionMsg, String dbInfo);

    /**
     * Notifies that the disk to which the fallback data is written is near to fill up
     * @param alarmUp It indicates whether the alarm or notification method has to be activated or deactivated (the problem has been sorted out)
     * @param directoryName The name of the system directory where the fallback file is stored
     */
    void diskFull(boolean alarmUp, String directoryName);
    
    /**
     * Notifies that the fallback file is not reachable, i.e., it is not possible to read or delete contents from it    
     * @param alarmUp It indicates whether the alarm or notification method has to be activated or deactivated (the problem has been sorted out)
     * @param file The name of the file that was access when the problem arose
     */
    void fileNotReachable(boolean alarmUp, File file);    
    
}

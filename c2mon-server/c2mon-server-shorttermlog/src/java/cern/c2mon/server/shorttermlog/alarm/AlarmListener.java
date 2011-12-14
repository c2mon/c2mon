package cern.c2mon.server.shorttermlog.alarm;

import java.io.File;

import org.apache.log4j.Logger;

import cern.c2mon.pmanager.IAlarmListener;

/**
 * Fallback alarm listener that send emails via log4j.
 * 
 * @author Mark Brightwell
 *
 */
public class AlarmListener implements IAlarmListener {

  /**
   * Mail logger.
   */
  private final static Logger LOGGER = Logger.getLogger("AdminMailLogger");
  
  /**
   * Flags for not sending repeated error messages.
   */
  private volatile boolean dbAlarm = false;
  private volatile boolean diskAlarm = false;
  private volatile boolean fileAlarm = false;
  
  
  @Override
  public void dbUnavailable(boolean alarmUp, String exceptionMsg, String dbInfo) {
    if (alarmUp && !dbAlarm) {
      dbAlarm = true;
      LOGGER.error("Error in logging to Short-Term-Log: DB unavailable with error message " + exceptionMsg + ", for DB " + dbInfo);
    } else if (!alarmUp && dbAlarm) {
      dbAlarm = false;
      LOGGER.error("DB unavailable error has resolved itself");
    }    
  }

  @Override
  public void diskFull(boolean alarmUp, String directoryName) {   
    if (alarmUp && !diskAlarm) {
      diskAlarm = true;
      LOGGER.error("Error in logging to Short-Term-Log - the following directory is full: " + directoryName);
    } else if (!alarmUp && diskAlarm) {
      diskAlarm = false;
      LOGGER.error("Disk full error has resolved itself");
    }    
  }

  @Override
  public void fileNotReachable(boolean alarmUp, File file) {
    if (alarmUp && !fileAlarm) {
      fileAlarm = true;
      LOGGER.error("Error in logging to Short-Term-Log - the following file is not reachable: " + file.getName());
    } else if (!alarmUp && fileAlarm) {
      fileAlarm = false;
      LOGGER.error("File unreachable error has resolved itself");
    }
  }

}

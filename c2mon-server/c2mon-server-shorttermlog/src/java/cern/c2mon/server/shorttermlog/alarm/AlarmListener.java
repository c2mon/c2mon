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
  private final static Logger EMAIL_LOGGER = Logger.getLogger("AdminMailLogger");
  
  /**
   * SMS logger.
   */
  private final static Logger SMS_LOGGER = Logger.getLogger("AdminSmsLogger");
  
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
      EMAIL_LOGGER.error("Error in logging to Short-Term-Log: DB unavailable with error message " + exceptionMsg + ", for DB " + dbInfo);
      SMS_LOGGER.error("Error in logging to Short-Term-Log: DB unavailable. See email for details.");
    } else if (!alarmUp && dbAlarm) {
      dbAlarm = false;
      EMAIL_LOGGER.error("DB unavailable error has resolved itself");
      SMS_LOGGER.error("DB unavailable error has resolved itself");
    }    
  }

  @Override
  public void diskFull(boolean alarmUp, String directoryName) {   
    if (alarmUp && !diskAlarm) {
      diskAlarm = true;
      EMAIL_LOGGER.error("Error in logging to Short-Term-Log fallback - the disk is nearly full, directory is " + directoryName);
      SMS_LOGGER.error("Error in logging to Short-Term-Log fallback - the disk nearly is full.");
    } else if (!alarmUp && diskAlarm) {
      diskAlarm = false;
      EMAIL_LOGGER.error("Disk full error has resolved itself");
      SMS_LOGGER.error("Disk full error has resolved itself");
    }    
  }

  @Override
  public void fileNotReachable(boolean alarmUp, File file) {
    if (alarmUp && !fileAlarm) {
      fileAlarm = true;
      EMAIL_LOGGER.error("Error in logging to Short-Term-Log - the following file is not reachable: " + file.getName());
      SMS_LOGGER.error("Error in logging to Short-Term-Log - the following file is not reachable: " + file.getName());
    } else if (!alarmUp && fileAlarm) {
      fileAlarm = false;
      EMAIL_LOGGER.error("File unreachable error has resolved itself");
      SMS_LOGGER.error("File unreachable error has resolved itself");
    }
  }

}

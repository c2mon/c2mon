package cern.c2mon.server.shorttermlog.alarm;

import java.io.File;

import cern.c2mon.pmanager.IAlarmListener;

/**
 * Not implemented yet; TODO once alarm module is defined.
 * 
 * @author Mark Brightwell
 *
 */
public class DummyAlarmListener implements IAlarmListener {

  @Override
  public void dbUnavailable(boolean alarmUp, String exceptionMsg, String dbInfo) {
    // TODO Auto-generated method stub

  }

  @Override
  public void diskFull(boolean alarmUp, String directoryName) {
    // TODO Auto-generated method stub

  }

  @Override
  public void fileNotReachable(boolean alarmUp, File file) {
    // TODO Auto-generated method stub

  }

}

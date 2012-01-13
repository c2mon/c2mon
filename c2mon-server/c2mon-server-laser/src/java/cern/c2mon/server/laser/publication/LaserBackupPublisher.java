package cern.c2mon.server.laser.publication;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import cern.tim.server.cache.AlarmCache;
import cern.tim.server.cache.exception.CacheElementNotFoundException;
import cern.tim.server.common.alarm.Alarm;
import cern.tim.server.common.config.ServerConstants;

/**
 * Sends regular backups of all active alarms to LASER.
 * 
 * @author Mark Brightwell
 * 
 */
@Service
@ManagedResource(objectName = "cern.c2mon:type=LaserPublisher,name=LaserBackupPublisher")
public class LaserBackupPublisher extends TimerTask implements SmartLifecycle {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(LaserBackupPublisher.class);

  /**
   * Time (ms) between backups.
   */
  private static final int BACKUP_INTERVAL = 60000;

  /**
   * Initial delay before sending backups (ms).
   */
  private static final int INITIAL_BACKUP_DELAY = BACKUP_INTERVAL;

  /**
   * Flag for lifecycle calls.
   */
  private volatile boolean running = false;

  /**
   * Timer scheduling publication.
   */
  private Timer timer;

  /**
   * Ref to alarm cache.
   */
  private AlarmCache alarmCache;

  /**
   * Constructor.
   * 
   * @param alarmCache ref to Alarm cache bean
   */
  @Autowired
  public LaserBackupPublisher(AlarmCache alarmCache) {
    super();
    this.alarmCache = alarmCache;
  }

  @Override
  public void run() {
    try {
      List<Alarm> alarmList = new ArrayList<Alarm>();
      for (Long alarmId : alarmCache.getKeys()) {
        try {
          Alarm alarm = alarmCache.getCopy(alarmId);
          if (alarm.isActive()) {
            alarmList.add(alarm);
          }
        } catch (CacheElementNotFoundException e) {
          // should only happen if concurrent re-configuration of the server
          LOGGER.warn("Unable to locate alarm " + alarmId + " in cache during LASER backup: not included in backup.", e);
        }
        if (!alarmList.isEmpty())
          publishAlarmBackUp(alarmList);
      }
    } catch (Exception e) {
      LOGGER.error("Exception caught while publishing active Alarm backup list", e);
    }
  }

  /**
   * Publishes the alarm list as backup to LASER
   * 
   * @param alarmList list of active alarms
   */
  private void publishAlarmBackUp(List<Alarm> alarmList) {
    // TODO publish alarms: * use alarm.getTimestamp() as LASER user timestamp! *
  }

  @Override
  public boolean isAutoStartup() {
    return true;
  }

  @Override
  public void stop(Runnable callback) {
    stop();
    callback.run();
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  @ManagedOperation(description="starts the backups publisher.")
  public void start() {
    LOGGER.info("Starting LASER backup mechanism.");
    // DOES ANYTHING ELSE NEEDS STARTING?
    timer = new Timer();
    timer.scheduleAtFixedRate(this, INITIAL_BACKUP_DELAY, BACKUP_INTERVAL);
    running = true;
  }

  @Override
  @ManagedOperation(description="Stops the backups publisher.")
  public void stop() {
    LOGGER.info("Stopping LASER backup mechanism.");
    // DOES ANYTHING ELSE NEEDS STOPPING?
    timer.cancel();
    running = false;
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_START_LAST;
  }

}

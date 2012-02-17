package cern.c2mon.server.laser.publication;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.SmartLifecycle;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import cern.laser.source.alarmsysteminterface.ASIException;
import cern.laser.source.alarmsysteminterface.AlarmSystemInterface;
import cern.laser.source.alarmsysteminterface.AlarmSystemInterfaceFactory;
import cern.laser.source.alarmsysteminterface.FaultState;
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
@ManagedResource(objectName = "cern.c2mon:type=LaserPublisher,name=LaserBackupPublisher")
public class LaserBackupPublisher extends TimerTask implements SmartLifecycle {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(LaserBackupPublisher.class);
  
  /**
   * Time between connection attempts at start-up (in millis)
   */
  private static final long SLEEP_BETWEEN_CONNECT = 3000;

  /**
   * Time (ms) between backups.
   */
  private int backupInterval;

  /**
   * Initial delay before sending first backup (ms).
   */
  private static final int INITIAL_BACKUP_DELAY = 60000;

  /**
   * Lock used to only allow one backup to run at any time across a server
   * cluster.
   */
  private ReentrantReadWriteLock backupLock = new ReentrantReadWriteLock();
  
  /**
   * Is the connect thread already running?
   */
  private volatile boolean connectThreadRunning = false;

  /**
   * Flag for lifecycle calls.
   */
  private volatile boolean running = false;

  /**
   * Module shutdown request (on server shutdown f.eg.)
   */
  private volatile boolean shutdownRequested = false;
  
  /**
   * Timer scheduling publication.
   */
  private Timer timer;

  /**
   * Ref to alarm cache.
   */
  private AlarmCache alarmCache;

  /**
   * Our reference to the {@link LaserPublisher} as we need it to use the
   * {@link LaserPublisher#getSourceName()} method.<br>
   * <br>
   * This is because we want to be aligned (sourcename-wise) with the
   * LaserPublisher instance. Otherwise we may end up sending backups with a
   * different sourcename.
   */
  private LaserPublisher publisher = null;

  /** Reference to the LASER alarm system interface. */
  private AlarmSystemInterface asi = null;

  /**
   * Constructor.
   * 
   * @param alarmCache
   *          ref to Alarm cache bean
   */
  @Autowired
  public LaserBackupPublisher(AlarmCache alarmCache, LaserPublisher publisher) {
    super();
    this.alarmCache = alarmCache;
    this.publisher = publisher;
  }

  @Override
  public void run() {
    publisher.getTmpLock().writeLock().lock();
    try {
   // lock to only allow a single backup at a time
      if (running){
        backupLock.writeLock().lock();
        try {
          LOGGER.debug("Creating LASER active alarm backup list.");
          List<Alarm> alarmList = new ArrayList<Alarm>();
          for (Long alarmId : alarmCache.getKeys()) {
            if (!shutdownRequested) {            
              try {
                Alarm alarm = alarmCache.getCopy(alarmId);
                if (alarm.isActive()) {
                  alarmList.add(alarm);
                }
              } catch (CacheElementNotFoundException e) {
                // should only happen if concurrent re-configuration of the server
                LOGGER.warn("Unable to locate alarm " + alarmId + " in cache during LASER backup: not included in backup.", e);
              }
            } else {
              // interrupt alarm sending as shutting down
              return;
            }
          }
          LOGGER.debug("Sending active alarm backup to LASER.");
          if (!alarmList.isEmpty()) {
            publishAlarmBackUp(alarmList);
          }
          LOGGER.debug("Finished sending LASER active alarm backup.");
        } catch (Exception e) {
          LOGGER.error("Exception caught while publishing active Alarm backup list", e);
        } finally {
          backupLock.writeLock().unlock();
        }
      } else {
        LOGGER.warn("Unable to publish LASER backup as module not running.");
      }
      //sleep to give laser time to get backup!
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    } finally {
      publisher.getTmpLock().writeLock().unlock();
    }       
  }

  /**
   * Publishes the alarm list as backup to LASER
   * 
   * @param alarmList list of active alarms
   */
  private void publishAlarmBackUp(List<Alarm> alarmList) {
    ArrayList<FaultState> toSend = new ArrayList<FaultState>();

    // iterate over list and transform them into Laser fault states
    for (Alarm timAlarm : alarmList) {
      FaultState fs = null;

      fs = AlarmSystemInterfaceFactory.createFaultState(timAlarm.getFaultFamily(), timAlarm.getFaultMember(), timAlarm.getFaultCode());
      fs.setUserTimestamp(new Timestamp(System.currentTimeMillis()));
      fs.setDescriptor(timAlarm.getState());
      if (timAlarm.getInfo() != null) {
        Properties prop = null;
        prop = fs.getUserProperties();
        prop.put(FaultState.ASI_PREFIX_PROPERTY, timAlarm.getInfo());
        fs.setUserProperties(prop);
      }

      toSend.add(fs);
    }
    try {
      asi.pushActiveList(toSend);
    } catch (ASIException e) {
      LOGGER.error("Cannot create backup list : ", e);
      e.printStackTrace();
    }
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
  @ManagedOperation(description = "starts the backups publisher.")
  public void start() {
    if (!running && !connectThreadRunning){
      connectThreadRunning = true;
      new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            while (!running && !shutdownRequested) {
              try {
                LOGGER.info("Starting LASER backup mechanism.");
                asi = AlarmSystemInterfaceFactory.createSource(publisher.getSourceName());            
                timer = new Timer();
                timer.scheduleAtFixedRate(LaserBackupPublisher.this, INITIAL_BACKUP_DELAY, backupInterval);
                running = true;              
              } catch (ASIException e) {
                LOGGER.error("Failed to start LASER backup publisher - will try again in 5 seconds", e);
                try {
                  Thread.sleep(SLEEP_BETWEEN_CONNECT);
                } catch (InterruptedException e1) {
                  LOGGER.error("Interrupted during sleep", e1);
                }            
              }
            }
          } finally {
            connectThreadRunning = false;
          }           
        }
      }).start();
    }       
  }

  @Override
  @ManagedOperation(description = "Stops the backups publisher.")
  public void stop() {
    if (running) {
      LOGGER.info("Stopping LASER backup mechanism.");
      shutdownRequested = true;
      //wait for connect thread to end
      try {
        Thread.sleep(SLEEP_BETWEEN_CONNECT);
      } catch (InterruptedException e) {
        LOGGER.error("Interrupted during sleep", e);
      } 
      if (timer != null){
        timer.cancel();
      }    
      if (asi != null) {
        asi.close();
      }
      running = false;
      shutdownRequested = false;
    }    
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_START_LAST;
  }

  /**
   * Setter method
   * 
   * @param backupInterval the time between successive LASER backups (in milliseconds)
   */
  @Required
  public void setBackupInterval(int backupInterval) {
    this.backupInterval = backupInterval;
  }

  /**
   * Getter method.
   * 
   * @return the backupInterval
   */
  public int getBackupInterval() {
    return backupInterval;
  }

}

package cern.c2mon.server.laser.publication;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.SmartLifecycle;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import cern.laser.source.alarmsysteminterface.ASIException;
import cern.laser.source.alarmsysteminterface.AlarmSystemInterfaceFactory;
import cern.laser.source.alarmsysteminterface.FaultState;
import cern.tim.server.cache.AlarmCache;
import cern.tim.server.common.alarm.Alarm;
import cern.tim.server.common.alarm.AlarmPublication;
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
   * Max number of minutes allowed for backup to complete. If takes
   * longer, will fail and be re-attempted in backupInterval ms.
   */
  private static final long MAX_BACKUP_TIME = 3;
  
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
   * For generating the backup.
   */
  private ThreadPoolExecutor backupExecutor;
  
  /**
   * Number of threads used when creating the backup.
   */  
  private int nbBackupThreads;

  /**
   * Our reference to the {@link LaserPublisherImpl} as we need it to use the
   * {@link LaserPublisherImpl#getSourceName()} method.<br>
   * <br>
   * This is because we want to be aligned (sourcename-wise) with the
   * LaserPublisher instance. Otherwise we may end up sending backups with a
   * different sourcename.
   */
  private LaserPublisher publisher = null;

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
  
  @PostConstruct
  public void init() {
    backupExecutor = new ThreadPoolExecutor(nbBackupThreads, nbBackupThreads, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    backupExecutor.allowCoreThreadTimeOut(true);
  }

  @Override
  public void run() {    
    // lock to only allow a single backup at a time, and no concurrent publication of alarms
    if (running){
      publisher.getBackupLock().writeLock().lock();
      try {
        LOGGER.debug("Creating LASER active alarm backup list.");                     
        LinkedList<BackupTask> tasks = new LinkedList<BackupTask>();
        for (Long alarmId : alarmCache.getKeys()) {
          tasks.add(new BackupTask(alarmId));            
        }
        List<Future<FaultState>> taskResults = backupExecutor.invokeAll(tasks, MAX_BACKUP_TIME, TimeUnit.MINUTES);
        ArrayList<FaultState> toSend = new ArrayList<FaultState>();
        for (Future<FaultState> result : taskResults) {
          try {
            FaultState fs = result.get();
            if (fs != null) {
              toSend.add(fs);
            }                      
          } catch (ExecutionException e) {
            LOGGER.error("Backup fault state computation threw an exception - unable to include in backup.", e);
          }
        }          
        LOGGER.debug("Sending active alarm backup to LASER with " + toSend.size() + " alarms");
        if (!toSend.isEmpty()) {
          try {
            publisher.getAsi().pushActiveList(toSend);
            LOGGER.debug("Finished sending LASER active alarm backup.");
          } catch (ASIException e) {
            LOGGER.error("Cannot send backup list to LASER", e);
            e.printStackTrace();
          }            
        }          
      } catch (Exception e) {
        LOGGER.error("Exception caught while publishing active Alarm backup list", e);
      } finally {
        publisher.getBackupLock().writeLock().unlock();
      }
    } else {
      LOGGER.warn("Unable to publish LASER backup as module not running.");
    }         
  }

  @Override
  public boolean isAutoStartup() {
    return false;
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
    shutdownRequested = false;
    if (!running && !connectThreadRunning){      
      connectThreadRunning = true;
      new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            while (!running && !shutdownRequested) {              
              LOGGER.info("Starting LASER backup mechanism.");
              if (publisher.getAsi() != null) {
                timer = new Timer();
                timer.scheduleAtFixedRate(LaserBackupPublisher.this, INITIAL_BACKUP_DELAY, backupInterval);
                running = true;
              } else {
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
      if (connectThreadRunning) {
        try {
          Thread.sleep(SLEEP_BETWEEN_CONNECT);
        } catch (InterruptedException e) {
          LOGGER.error("Interrupted during sleep", e);
        }
      }       
      if (timer != null){
        timer.cancel();
      }         
      running = false;      
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
  
  /**
   * Getter method.
   * 
   * @return the nbBackupThreads
   */
  public int getNbBackupThreads() {
    return nbBackupThreads;
  }

  /**
   * Setter method.
   * 
   * @param nbBackupThreads the nbBackupThreads to set
   */
  @Required
  public void setNbBackupThreads(int nbBackupThreads) {
    this.nbBackupThreads = nbBackupThreads;
  }
  
  /**
   * Generates the FaultState for a single alarm if active.
   * @author Mark Brightwell
   *
   */
  private class BackupTask implements Callable<FaultState> {

    private Long id;
    
    public BackupTask(Long id) {
      super();
      this.id = id;
    }

    @Override
    public FaultState call() throws Exception {   
      FaultState fs = null;
      if (!shutdownRequested) {
        Alarm timAlarm = alarmCache.getCopy(id);
        AlarmPublication alarmPublication = timAlarm.getLastPublication();
        if (alarmPublication != null && alarmPublication.isActivePublication()) {
          fs = AlarmSystemInterfaceFactory.createFaultState(timAlarm.getFaultFamily(), timAlarm.getFaultMember(), timAlarm.getFaultCode());
          fs.setUserTimestamp(alarmPublication.getPublicationTime());              
          fs.setDescriptor(alarmPublication.getState());
          if (alarmPublication.getInfo() != null) {
            Properties prop = null;
            prop = fs.getUserProperties();
            prop.put(FaultState.ASI_PREFIX_PROPERTY, alarmPublication.getInfo());
            fs.setUserProperties(prop);
          }
        }
      }     
      return fs;
    }   
  }
  
}

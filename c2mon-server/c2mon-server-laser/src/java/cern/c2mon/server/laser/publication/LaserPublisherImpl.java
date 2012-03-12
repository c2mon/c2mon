package cern.c2mon.server.laser.publication;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.SmartLifecycle;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import cern.laser.source.alarmsysteminterface.ASIException;
import cern.laser.source.alarmsysteminterface.AlarmSystemInterface;
import cern.laser.source.alarmsysteminterface.AlarmSystemInterfaceFactory;
import cern.laser.source.alarmsysteminterface.FaultState;
import cern.tim.server.cache.AlarmCache;
import cern.tim.server.cache.CacheRegistrationService;
import cern.tim.server.cache.TimCacheListener;
import cern.tim.server.cachepersistence.common.BatchPersistenceManager;
import cern.tim.server.common.alarm.Alarm;
import cern.tim.server.common.component.Lifecycle;
import cern.tim.server.common.config.ServerConstants;

/**
 * Bean responsible for submitting C2MON alarms to LASER.
 */
@ManagedResource(objectName = "cern.c2mon:type=LaserPublisher,name=LaserPublisher")
public class LaserPublisherImpl implements TimCacheListener<Alarm>, SmartLifecycle, LaserPublisherMBean, LaserPublisher {

  /**
   * Lock used to only allow one backup to run at any time across a server
   * cluster.
   */
  private ReentrantReadWriteLock backupLock = new ReentrantReadWriteLock();
  
  /**
   * Time between connection attempts at start-up (in millis)
   */
  private static final long SLEEP_BETWEEN_CONNECT = 3000;

  /**
   * Time before republication checks of failed LASER publications from this bean.
   */
  private long republishDelay = 60000;

  /**
   * Period between republication checks.
   */
  private static final long REPUBLISH_PERIOD = 120000;

  /**
   * Nb of seconds given to this module to start-up in it's own thread: this gives
   * the LASER connection some time to be established, before calls are
   * made to this module for alarm publications. If no connection is made after this
   * time, server start-up will continue and failed publications will be stored.
   */
  private static final short START_UP_SECONDS= 3;

  /**
   * The alarm source name this publisher is called.
   */
  private String sourceName;

  /**
   * Our Logger
   */
  private Logger log = Logger.getLogger(LaserPublisherImpl.class);

  /**
   * yet another logger, that will be configured to output to a file every alarm
   * pushed to LASER
   */
  private Logger laserLog = Logger.getLogger("LaserAlarmsLogger");

  /** Reference to the LASER alarm system interface. */
  private AlarmSystemInterface asi = null;

  /**
   * Is the connect thread already running?
   */
  private volatile boolean connectThreadRunning = false;
  
  /**
   * Flag for lifecycle calls.
   */
  private volatile boolean running = false;
  
  /**
   * Was the initial connection successful at startup.
   */
  private volatile boolean initialConnection = false;
  
  /**
   * Module shutdown request (on server shutdown f.eg.)
   */
  private volatile boolean shutdownRequested = false;

  /**
   * Service for registering as listener to C2MON caches.
   */
  private CacheRegistrationService cacheRegistrationService;
  
  /**
   * Reference to cache.
   */
  private AlarmCache alarmCache;
  
  /**
   * For persisting changes to alarm publication details in cache object.
   */
  private BatchPersistenceManager alarmPersistenceManager;
  
  /**
   * Timer that re-publishes alarms that failed to publish successfully to LASER.
   */
  private Timer republishTimer;
  
  /**
   * Task run on timer.
   */
  private TimerTask republishTask;
  
  /**
   * Ids of alarms that need re-publishing as publication failed (map is used as set)
   */
  private ConcurrentHashMap<Long, Long> toBePublished = new ConcurrentHashMap<Long, Long>();

  private StatisticsModule stats = new StatisticsModule();
  
  /**
   * Listener container for stop/starting;
   */
  private Lifecycle listenerContainer;

  /**
   * Autowired constructor.
   * 
   * @param cacheRegistrationService the C2MON cache registration service bean
   */
  @Autowired
  public LaserPublisherImpl(final CacheRegistrationService cacheRegistrationService, final AlarmCache alarmCache,
                        @Qualifier("alarmPersistenceManager") final BatchPersistenceManager batchPersistenceManager) {
    super();
    this.cacheRegistrationService = cacheRegistrationService;
    this.alarmCache = alarmCache;
    this.alarmPersistenceManager = batchPersistenceManager;
  }

  /**
   * @param sourceName the alarm source name this publisher should be called.
   */
  @Required
  public void setSourceName(String sourceName) {
    if (log.isInfoEnabled()) {
      log.info("Setting Alarm Sourcename to " + sourceName);
    }
    this.sourceName = sourceName;
  }

  /**
   * @return the alarm source name this publisher is called.
   */
  @Override
  public String getSourceName() {
    return sourceName;
  }

  /**
   * Called at server startup.
   * 
   * @throws Exception in case the underlying alarm system could not be initiated.
   */
  @PostConstruct
  public void init() throws Exception {
    listenerContainer = cacheRegistrationService.registerToAlarms(this);    
  }


  /**
   * Only uses the id of the passed alarm: fresh copy is accessed in cache.
   */
  @Override
  public void notifyElementUpdated(Alarm alarmCopy) {  
    if (running && initialConnection) {
      backupLock.readLock().lock();
      try {
        //get most recent alarm in cache and lock access
        Alarm alarm = alarmCache.get(alarmCopy.getId());
        alarm.getWriteLock().lock();
        try {
          if (!alarm.isPublishedToLaser());
            try {
              publishToLaser(alarm);
              toBePublished.remove(alarm.getId());
            } catch (Exception e) {              
              StringBuilder str = new StringBuilder("Exception caught while publishing alarm to LASER: ");
              str.append(alarm.getTimestamp());
              str.append("\t");
              str.append(alarm.getFaultFamily());
              str.append(':');
              str.append(alarm.getFaultMember());
              str.append(':');
              str.append(alarm.getFaultCode());
              str.append('\t');
              str.append(alarm.getState());
              if (alarm.getInfo() != null) {
                str.append('\t');
                str.append(alarm.getInfo());
              }             
              log.error(str, e);
              toBePublished.put(alarm.getId(), alarm.getId());              
            }                       
        } finally {
          alarm.getWriteLock().unlock();
        }         
      } finally {
        backupLock.readLock().unlock();
      }
    } else {
      log.warn("Unable to publish alarm as LASER publisher module not running/connected - adding to re-publication list (alarm id " + alarmCopy.getId() + ")");
      toBePublished.put(alarmCopy.getId(), alarmCopy.getId());
    }                       
  }

  /**
   * Call within write lock.
   * @param alarm in cache
   * @throws ASIException 
   */
  private void publishToLaser(Alarm alarm) throws ASIException {
    FaultState fs = null;    
    fs = AlarmSystemInterfaceFactory.createFaultState(alarm.getFaultFamily(), alarm.getFaultMember(), alarm.getFaultCode());
    Timestamp laserPublicationTime = new Timestamp(System.currentTimeMillis());        
    stats.update(alarm);

    if (alarm.isActive()) {
      fs.setDescriptor(alarm.getState());
      fs.setUserTimestamp(laserPublicationTime);

      if (alarm.getInfo() != null) {
        Properties prop = fs.getUserProperties();
        prop.put(FaultState.ASI_PREFIX_PROPERTY, alarm.getInfo());
        fs.setUserProperties(prop);
      }
    } else {
      fs.setDescriptor(FaultState.TERMINATE);
      fs.setUserTimestamp(laserPublicationTime);
    }

    if (log.isDebugEnabled()) {
      log.debug("Pushing alarm to LASER :\n" + fs);
    }          
    getAsi().push(fs);    
    log(alarm);  
    alarm.hasBeenPublished(laserPublicationTime); 
    alarmPersistenceManager.addElementToPersist(alarm.getId());
  }

  // below server lifecycle methods: complete start/stop (no need to allow for
  // stop/restart, just final shutdown)
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
  @ManagedOperation(description = "Starts the alarm publisher (will continue in own thread until successful)")
  public void start() {
    if (!running) {
      republishTask = new PublicationTask();
      republishTimer = new Timer("LASER re-publication timer");
      republishTimer.schedule(republishTask, republishDelay, REPUBLISH_PERIOD);
      if(!connectThreadRunning) {
        connectThreadRunning = true;
        new Thread(new Runnable() {
          @Override
          public void run() {
            log.info("Starting " + LaserPublisherImpl.class.getName() + " (in own thread)");
            try {            
              while (!initialConnection && !shutdownRequested) {
                try {
                  log.info("Attempting LASER connection.");
                  setAsi(AlarmSystemInterfaceFactory.createSource(getSourceName()));                  
                  initialConnection = true;                
                } catch (ASIException e) {
                  log.error("Failed to start LASER publisher - will try again in 5 seconds", e);
                  try {
                    Thread.sleep(SLEEP_BETWEEN_CONNECT);
                  } catch (InterruptedException e1) {
                    log.error("Interrupted during sleep", e1);
                  }            
                }
              }
            } finally {
              connectThreadRunning = false;
            }                  
          }
        }).start();
        short waited = 0;
        while (!initialConnection && !shutdownRequested && waited < START_UP_SECONDS) {
          try {
            Thread.sleep(1000);
            waited++;
          } catch (InterruptedException e) {
            log.error("Interrupted during start-up check");
          }
        }                
        running = true;
        listenerContainer.start();
      }
    }
             
  }

  @Override
  @ManagedOperation(description = "Stops the alarm publisher.")
  public void stop() {    
    if (running) {          
      log.info("Stopping LASER publisher " + LaserPublisherImpl.class.getName());
      listenerContainer.stop();
      shutdownRequested = true; 
      while (!toBePublished.isEmpty()) {
        log.warn("Unpublished alarms at shutdown - be sure to restart server in recovery mode to guarantee all alarm publications! (or run 'republish alarms' in Jconsole RecoveryManager)");
        log.warn("If LASER connection is not re-established, the C2MON server will need killing!");
        try {
          Thread.sleep(5000);
        } catch (InterruptedException e) {
          log.error("Interrupted while shutting down LASER publisher.", e);
        }
      }
      republishTimer.cancel();           
      //wait for connect thread to end
      if (connectThreadRunning) {
        try {
          Thread.sleep(SLEEP_BETWEEN_CONNECT);
        } catch (InterruptedException e) {
          log.error("Interrupted during sleep", e);
        } 
      }      
      if (getAsi() != null) { 
        getAsi().close();
      }      
      running = false;
      shutdownRequested = false;
      initialConnection = false;
    }    
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_STOP_LAST - 1;
  }

  @Override
  @ManagedOperation(description = "Return the total number of alarms processed since last reset().")
  public long getProcessedAlarms() {
    return stats.getTotalProcessed();
  }

  @Override
  @ManagedOperation(description = "Resets the internal statistics for all alarms.")
  public void resetStatistics() {
    if (log.isTraceEnabled()) {
      log.trace("Entering resetStatistics()");
    }
    stats.resetStatistics();
  }

  @Override
  @ManagedOperation(description = "Resets the internal statistics for a specific alarm.")
  public void resetStatistics(String alarmID) {
    if (log.isTraceEnabled()) {
      log.trace("Entering resetStatistics('" + alarmID + "')");
    }
    stats.resetStatistics(alarmID);
  }

  @Override
  @ManagedOperation(description = "Return a list of alarm for which statistics are collected.")
  public List<String> getRegisteredAlarms() {
    if (log.isTraceEnabled()) {
      log.trace("Entering getRegisteredAlarms()");
    }
    return stats.getStatsList();
  }

  @Override
  @ManagedOperation(description = "Returns a string representation of the statistics for an alarm.")
  public String getStatsForAlarm(String id) {
    if (stats.getStatsForAlarm(id) == null) {
      return "Not found!";
    } else {
      return stats.getStatsForAlarm(id).toString();
    }
  }

  private void log(final Alarm alarm) {
    if (laserLog != null && laserLog.isInfoEnabled()) {
      laserLog.info(alarm);
    }
  }

  public void confirmStatus(Alarm cacheable) {
    notifyElementUpdated(cacheable);
  }

  /**
   * @return the backupLock
   */
  @Override
  public ReentrantReadWriteLock getBackupLock() {
    return backupLock;
  }
  
  @ManagedOperation(description = "Does this publisher have failed publications waiting to be published again?")
  @Override
  public boolean hasUnpublishedAlarms() {
    return !toBePublished.isEmpty();
  }
  
  @Override
  public void setRepublishDelay(long republishDelay) {
    this.republishDelay = republishDelay;
  }

  /**
   * Checks if un-published alarms need publishing. If so, will publish them.
   * 
   * @author Mark Brightwell
   *
   */
  private class PublicationTask extends TimerTask {
        
    /**
     * Constructor
     * @param alarmCopy alarm to republish (only id is used as latest alarm is accessed)
     */
    public PublicationTask() {
      super();      
    }

    @Override
    public void run() {
      try {
        log.debug("Checking for LASER re-publications");      
        if (!toBePublished.isEmpty()) {
          log.info("Detected alarms that failed to be published - will attempt republication of these.");
          for (Long alarmId : new ArrayList<Long>(toBePublished.keySet())) {  //take copy as these tasks also add to this map if publication fails again        
            notifyElementUpdated(alarmCache.getCopy(alarmId));
          }
        }
      } catch (Exception e) {
        log.error("Unexpected exception caught while checking for failed LASER publications", e);
      }      
    }    
  }

  /**
   * @return the asi
   */
  @Override
  public synchronized AlarmSystemInterface getAsi() {
    return asi;
  }

  /**
   * @param asi the asi to set
   */
  private synchronized void setAsi(AlarmSystemInterface asi) {
    this.asi = asi;
  }
  
  

}

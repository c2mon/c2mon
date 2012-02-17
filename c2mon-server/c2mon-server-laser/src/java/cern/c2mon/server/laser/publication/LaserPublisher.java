package cern.c2mon.server.laser.publication;

import java.sql.Timestamp;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;

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
import cern.tim.server.cache.CacheRegistrationService;
import cern.tim.server.cache.TimCacheListener;
import cern.tim.server.common.alarm.Alarm;
import cern.tim.server.common.config.ServerConstants;

/**
 * Bean responsible for submitting C2MON alarms to LASER.
 */
@ManagedResource(objectName = "cern.c2mon:type=LaserPublisher,name=LaserPublisher")
public class LaserPublisher implements TimCacheListener<Alarm>, SmartLifecycle, LaserPublisherMBean {

  private static ReentrantReadWriteLock tmpLock = new ReentrantReadWriteLock();
  
  /**
   * Time between connection attempts at start-up (in millis)
   */
  private static final long SLEEP_BETWEEN_CONNECT = 3000;

  /**
   * The alarm source name this publisher is called.
   */
  private String sourceName;

  /**
   * Our Logger
   */
  private Logger log = Logger.getLogger(LaserPublisher.class);

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
   * Module shutdown request (on server shutdown f.eg.)
   */
  private volatile boolean shutdownRequested = false;

  /**
   * Service for registering as listener to C2MON caches.
   */
  private CacheRegistrationService cacheRegistrationService;

  /**
	 */
  private StatisticsModule stats = new StatisticsModule();

  /**
   * Autowired constructor.
   * 
   * @param cacheRegistrationService the C2MON cache registration service bean
   */
  @Autowired
  public LaserPublisher(final CacheRegistrationService cacheRegistrationService) {
    super();
    this.cacheRegistrationService = cacheRegistrationService;
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
    cacheRegistrationService.registerToAlarms(this);    
  }

  /**
     * 
     */
  @Override
  public void notifyElementUpdated(Alarm cacheable) {
    tmpLock.writeLock().lock();
    try {      
      if (running) {
        FaultState fs = null;
        fs = AlarmSystemInterfaceFactory.createFaultState(cacheable.getFaultFamily(), cacheable.getFaultMember(), cacheable.getFaultCode());

        stats.update(cacheable);

        if (cacheable.isActive()) {
          fs.setDescriptor(cacheable.getState());
          fs.setUserTimestamp(new Timestamp(System.currentTimeMillis()));

          if (cacheable.getInfo() != null) {
            Properties prop = fs.getUserProperties();
            prop.put(FaultState.ASI_PREFIX_PROPERTY, cacheable.getInfo());
            fs.setUserProperties(prop);
          }
        } else {
          fs.setDescriptor(FaultState.TERMINATE);
          fs.setUserTimestamp(new Timestamp(System.currentTimeMillis()));
        }

        if (log.isDebugEnabled()) {
          log.debug("Pushing alarm to LASER :\n" + fs);
        }        
        try {
          asi.push(fs);     
          log(cacheable);
          // Keep track of the sent alarm in the Alarm log
          StringBuilder str = new StringBuilder();
          str.append(cacheable.getTimestamp());
          str.append("\t");
          str.append(cacheable.getFaultFamily());
          str.append(':');
          str.append(cacheable.getFaultMember());
          str.append(':');
          str.append(cacheable.getFaultCode());
          str.append('\t');
          str.append(cacheable.getState());
          if (cacheable.getInfo() != null) {
            str.append('\t');
            str.append(cacheable.getInfo());
          }
          log.info(str);      
        } catch (ASIException e) {
          // Ooops, didn't work. log the exception.
          StringBuilder str = new StringBuilder("Alarm System Interface Exception. Unable to send FaultState ");
          str.append(cacheable.getFaultFamily());
          str.append(':');
          str.append(cacheable.getFaultMember());
          str.append(':');
          str.append(cacheable.getFaultCode());
          str.append(" to LASER.");
          log.error(str, e);
        } catch (Exception e) {
          StringBuilder str = new StringBuilder("sendFaultState() : Unexpected Exception. Unable to send FaultState ");
          str.append(cacheable.getFaultFamily());
          str.append(':');
          str.append(cacheable.getFaultMember());
          str.append(':');
          str.append(cacheable.getFaultCode());
          str.append(" to LASER.");
          log.error(str, e);
        } 
      } else {
        log.warn("Unable to publish alarm as LASER publisher module not running: alarm id " + cacheable.getId());
      }  
    } finally {
      tmpLock.writeLock().unlock();
    }                 
  }

  // below server lifecycle methods: complete start/stop (no need to allow for
  // stop/restart, just final shutdown)
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
  @ManagedOperation(description = "Starts the alarm publisher (will continue in own thread until successful)")
  public void start() {
    if (!running && !connectThreadRunning) {
      connectThreadRunning = true;
      new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            while (!running && !shutdownRequested) {
              try {
                log.info("Starting " + LaserPublisher.class.getName() + " (in own thread)");
                asi = AlarmSystemInterfaceFactory.createSource(getSourceName());
                running = true;
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
    }      
  }

  @Override
  @ManagedOperation(description = "Stops the alarm publisher.")
  public void stop() {
    if (running) {
      log.info("Stopping LASER publisher" + LaserPublisher.class.getName());   
      shutdownRequested = true;
      //wait for connect thread to end
      try {
        Thread.sleep(SLEEP_BETWEEN_CONNECT);
      } catch (InterruptedException e) {
        log.error("Interrupted during sleep", e);
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
    return ServerConstants.PHASE_STOP_LAST;
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

  public static ReentrantReadWriteLock getTmpLock() {
    return tmpLock;
  }

}

package cern.tim.server.lifecycle;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import cern.tim.server.cache.AlarmCache;
import cern.tim.server.cache.ControlTagCache;
import cern.tim.server.cache.DataTagCache;
import cern.tim.server.common.alarm.Alarm;
import cern.tim.server.common.config.ServerConstants;
import cern.tim.server.common.control.ControlTag;
import cern.tim.server.common.datatag.DataTag;
import cern.tim.server.daqcommunication.out.DataRefreshManager;
import cern.tim.server.supervision.SupervisionFacade;

/**
 * Runs when the server is started with the c2mon.recovery
 * property set to true.
 * 
 * <p>Performs cleanup operations to recover from a server
 * crash.
 * 
 * @author Mark Brightwell
 *
 */
@Service
@ManagedResource(objectName = "cern.c2mon:name=recoveryManager")
public class RecoveryManager implements SmartLifecycle {

  /**
   * Class logger.
   */
  public static final Logger LOGGER = Logger.getLogger(RecoveryManager.class);
  
  /**
   * Delay at server start-up before current supervision status saved to the DB.
   */
  public static final int INITIAL_LOGGING_DELAY = 2;
  
  /**
   * Flag for interrupting the initial logging thread if shutdown requested.
   */
  private volatile boolean stopRequested = false;  
  
  /**
   * Lifecycle flag.
   */
  private volatile boolean running = false;
  
  /**
   * For notifying current status.
   */
  private SupervisionFacade supervisionFacade;
  
  /**
   * For getting latest values from DAQ.
   */
  private DataRefreshManager dataRefreshManager;
  
  /**
   * Caches for notifying listeners.
   */
  private DataTagCache dataTagCache;  
  private ControlTagCache controlTagCache;
  
  private AlarmCache alarmCache;
   
  
  /**
   * Constructor
   * @param supervisionFacade facade
   * @param dataRefreshManager refresh manager
   * @param dataTagCache datatag cache
   * @param controlTagCache controltag cache
   */
  @Autowired
  public RecoveryManager(final SupervisionFacade supervisionFacade, 
                          final DataRefreshManager dataRefreshManager, 
                          final DataTagCache dataTagCache, 
                          final ControlTagCache controlTagCache,
                          final AlarmCache alarmCache) {
    super();
    this.supervisionFacade = supervisionFacade;
    this.dataRefreshManager = dataRefreshManager;
    this.dataTagCache = dataTagCache;
    this.controlTagCache = controlTagCache;
    this.alarmCache = alarmCache;
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
  public void start() {
    if (!running && !stopRequested && System.getProperty("c2mon.recovery") != null && System.getProperty("c2mon.recovery").equals("true")) {
      new Thread(new Runnable() {        
        @Override
        public void run() {
          LOGGER.info("Running server recovery tasks.");
          refresh();          
        }
      }).start();      
      running = true;
    }
  }

  /**
   * Runs all refresh actions.
   */
  @ManagedOperation(description = "Runs all refresh actions.")
  public void refresh() {
    if (!stopRequested) {      
      publishUnpublishedAlarms(); //unpublished alarms are sent to LASER
    }
    if (!stopRequested) {
      refreshSupervisionStatus(); //generates new events with the current status; includes alarm callbacks!
    }
    if (!stopRequested) {
      refreshStateTags(); //updates the tags with the current status
    }
    if (!stopRequested) {
      refreshDataTags(); //gets latest values from DAQ cache
    }
    if (!stopRequested) {      
      notifyAllTagCacheListeners(); //also refreshes rules but not alarms (done with supervision)
    }    
  }
  
  /**
   * Refresh the supervision status.
   */
  @ManagedOperation(description = "Refreshes all supervision status.")
  public void refreshSupervisionStatus() {        
    LOGGER.info("Recovery task: notifying all supervision listeners of current status.");
    supervisionFacade.refreshAllSupervisionStatus();
    LOGGER.info("Recovery task: finished notifying supervision status (notice all alarms are now re-evaluated on a separate thread"
    		+ " - this may take some time!)");
  } 
  
  /**
   * Refresh all state tags with new timestamps.
   */
  @ManagedOperation(description = "Refreshes all state tags (new timestamp).")
  public void refreshStateTags() {
    LOGGER.info("Recovery task: refreshing state tags.");
    supervisionFacade.refreshStateTags();
    LOGGER.info("Recovery task: finished refreshing state tags.");
  }
  
  /**
   * Asks for tag refresh from DAQ level (DAQ cache refresh).
   * Value already in cache will be filtered out.
   */
  @ManagedOperation(description = "Refreshes DataTags from DAQ cache.")
  public void refreshDataTags() {
    LOGGER.info("Recovery task: refreshing DataTags from DAQ (using DAQ cache).");
    dataRefreshManager.refreshTagsForAllProcess();  
    LOGGER.info("Recovery task: finished refreshing all DataTags from DAQ.");
  }
  
  /**
   * Notifies all Tag cache listeners with the confirmStatus notification,
   * so that all listeners receive up to date notifications (notice alarm
   * cache listeners are notified as these are all re-evaluated, both via
   * Tag and supervision status notification; the RuleTag cache is also left
   * out here, as all rules are refreshes through DataTag and ControlTag
   * status confirmations).
   */
  @ManagedOperation(description = "Notifies all Tag cache listeners (status confirmation).")
  public void notifyAllTagCacheListeners() {
    LOGGER.info("Recovery task: notifying all tag listeners.");
    for (Long key : controlTagCache.getKeys()) {
      ControlTag controlTag = controlTagCache.get(key);
      controlTag.getWriteLock().lock();      
      try {        
        long eventTime = System.currentTimeMillis();
        controlTagCache.notifyListenerStatusConfirmation(controlTag, eventTime);
      } finally {
        controlTag.getWriteLock().unlock();
      }      
    }
    for (Long key : dataTagCache.getKeys()) {
      DataTag dataTag = dataTagCache.get(key);
      dataTag.getWriteLock().lock();
      try {
        long eventTime = System.currentTimeMillis();
        dataTagCache.notifyListenerStatusConfirmation(dataTag, eventTime);
      } finally {
        dataTag.getWriteLock().unlock();
      }      
    }
    LOGGER.info("Recovery task: finished notifying all tag listeners.");
  }
  
  /**
   * If the alarm publication thread does not manage to publish all alarms in the queue before
   * shutdown, the server must check all non-published alarms are published on start-up.
   * 
   * <p>If published alarms were not persisted before a crash and then the restart involves a
   * cache clean (or in single server mode), then these alarms will be re-published to LASER with
   * a new timestamp.
   * 
   * <p>Alarm cache persistence is also notified for these alarms, although this will only result
   * in saving the current state twice.
   */
  @ManagedOperation(description="Republish all non-published alarms (use if alarm publication thread did not shutdown correctly)")
  public void publishUnpublishedAlarms() {
    for (Long key : alarmCache.getKeys()) {
      Alarm alarm = alarmCache.get(key);
      alarm.getReadLock().lock();
      try {        
        alarmCache.notifyListenersOfUpdate(alarm);      
      } catch (Exception e) {
        LOGGER.error("Exception caught while checking for unpublished alarms", e);
      } finally {
        alarm.getReadLock().unlock();      
      }
    }
  }

  @Override
  public void stop() {
    stopRequested = true;
    running = false;
  }

  @Override
  public int getPhase() {    
    return ServerConstants.PHASE_START_LAST;
  }

  
}

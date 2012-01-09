package cern.tim.server.lifecycle;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import cern.tim.server.cache.ControlTagCache;
import cern.tim.server.cache.DataTagCache;
import cern.tim.server.common.config.ServerConstants;
import cern.tim.server.common.control.ControlTag;
import cern.tim.server.common.datatag.DataTag;
import cern.tim.server.common.thread.Event;
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
                          final ControlTagCache controlTagCache) {
    super();
    this.supervisionFacade = supervisionFacade;
    this.dataRefreshManager = dataRefreshManager;
    this.dataTagCache = dataTagCache;
    this.controlTagCache = controlTagCache;
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
      refreshSupervisionStatus(); //includes alarm callbacks!
    }
    if (!stopRequested) {
      refreshStateTags();
    }
    if (!stopRequested) {
      refreshDataTags(); //gets latest values from DAQ cache
    }
    if (!stopRequested) {      
      notifyAllTagCacheListeners(); //also refreshes rules and alarms!
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
    LOGGER.info("Recovery task: refreshing Process state tags.");
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

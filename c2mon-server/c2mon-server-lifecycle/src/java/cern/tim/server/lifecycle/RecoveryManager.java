package cern.tim.server.lifecycle;

import org.apache.log4j.Logger;
import org.springframework.context.SmartLifecycle;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import cern.tim.server.cache.ControlTagCache;
import cern.tim.server.cache.DataTagCache;
import cern.tim.server.cache.RuleTagCache;
import cern.tim.server.common.config.ServerConstants;
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
@ManagedResource(objectName="cern.c2mon:name=recoveryManager")
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
  private volatile boolean recoveryRunning = true;  
  
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
  
  private DataTagCache dataTagCache;  
  private ControlTagCache controlTagCache;
   
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
    if (!running && System.getProperty("c2mon.recovery") != null && System.getProperty("c2mon.recovery").equals("true")) {
      new Thread(new Runnable() {        
        @Override
        public void run() {
          LOGGER.info("Running server recovery tasks.");
          refreshSupervisionStatus(); //includes alarm callbacks!
          refreshStateTags();
          refreshDataTags(); //gets latest values from DAQ cache
          notifyAllTagCacheListeners(); //also refreshes rules and alarms!
          recoveryRunning = false;
        }
      }).start();      
      running = true;
    }
  }

  /**
   * Refresh the supervision status.
   */
  private void refreshSupervisionStatus() {        
    LOGGER.info("Recovery task: notifying all supervision listeners of current status.");
    supervisionFacade.refreshAllSupervisionStatus();
  } 
  
  /**
   * Refresh all state tags with new timestamps.
   */
  private void refreshStateTags() {
    LOGGER.info("Recovery task: refreshing Process state tags.");
    supervisionFacade.refreshStateTags();
  }
  
  /**
   * Asks for tag refresh from DAQ level (DAQ cache refresh).
   * Value already in cache will be filtered out.
   */
  private void refreshDataTags() {
    LOGGER.info("Recovery task: refreshing DataTags from DAQ (using DAQ cache).");
    dataRefreshManager.refreshTagsForAllProcess();    
  }
  
  /**
   * Notifies all Tag cache listeners with the confirmStatus notification,
   * so that all listeners receive up to date notifications (notice alarm
   * cache listeners are notified as these are all re-evaluated, both via
   * Tag and supervision status notification; the RuleTag cache is also left
   * out here, as all rules are refreshes through DataTag and ControlTag
   * status confirmations).
   */
  private void notifyAllTagCacheListeners() {
    for (Long key : dataTagCache.getKeys()) {
      dataTagCache.notifyListenerStatusConfirmation(dataTagCache.get(key));
    }
    for (Long key : controlTagCache.getKeys()) {
      controlTagCache.notifyListenerStatusConfirmation(controlTagCache.get(key));
    }
  }

  @Override
  public void stop() {
    recoveryRunning = false;
    running = false;
  }

  @Override
  public int getPhase() {    
    return ServerConstants.PHASE_START_LAST;
  }

  
}

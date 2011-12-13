package cern.tim.server.lifecycle;

import org.apache.log4j.Logger;
import org.springframework.context.SmartLifecycle;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import cern.tim.server.common.config.ServerConstants;
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
   * Refresh the supervision status.
   */
  private void refreshSupervisionStatus() {        
    LOGGER.info("Notifying all supervision listeners of current status.");
    supervisionFacade.refreshAllSupervisionStatus();
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
    if (!running && System.getProperty("c2mon.recovery") != null && System.getProperty("c2mon.recovery").equals("true")) {
      new Thread(new Runnable() {        
        @Override
        public void run() {
          LOGGER.info("Running server recovery tasks.");
          refreshSupervisionStatus();                    
          recoveryRunning = false;
        }
      }).start();      
      running = true;
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

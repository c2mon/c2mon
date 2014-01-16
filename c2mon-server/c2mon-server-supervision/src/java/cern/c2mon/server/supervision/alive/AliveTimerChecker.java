package cern.c2mon.server.supervision.alive;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.AliveTimerCache;
import cern.c2mon.server.cache.AliveTimerFacade;
import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.alive.AliveTimer;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.supervision.SupervisionManager;

/**
 * Timer that regularly checks all the active alive timers monitoring
 * the connections to the DAQs, Equipment and SubEquipment.
 * 
 * <p>Notice that an alive timer is considered expired when alive-interval
 *  + alive-interval/3 milliseconds have expired since the last alive
 *  message arrived, where alive-interval is specific to the AliveTimer
 *  object (see <code>hasExpired</code> in {@link AliveTimerFacade}).
 * 
 * @author Mark Brightwell
 *
 */
@Service
public class AliveTimerChecker extends TimerTask implements SmartLifecycle {

  /**
   * Log4j Logger for this class.
   */
  private static final Logger LOGGER = Logger.getLogger(AliveTimerChecker.class);

  /**
   * SMS logger for warnings.
   */
  private static final Logger SMS_LOGGER = Logger.getLogger("AdminSmsLogger");
  
  /**
   * Cluster cache key for retrieving the time of last check
   * of the alives. Across server cluster it assures that
   * the alive check only takes place on a single server.
   */
  private static final String LAST_ALIVE_TIMER_CHECK_LONG = "c2mon.supervision.AliveTimerChecker.lastAliveTimerCheck";
  
  /**
   * How often the timer checks whether the alive
   * timer have expired.
   */
  private static final int SCAN_INTERVAL = 10000;

  /**
   * The time the server waits before doing first
   * checks at start up (this gives time for incoming
   * alives to be processed).
   */
  private static final int INITIAL_SCAN_DELAY = 120000;
  
  /**
   * Lifecycle flag.
   */
  private volatile boolean running = false;
  
  /**
   * Timer object
   */
  private Timer timer;
  
  /**
   * Reference to alive timer facade.
   */
  private AliveTimerFacade aliveTimerFacade;

  /**
   * Reference to alive timer cache.
   */
  private AliveTimerCache aliveTimerCache;
  
  /**
   * Reference to the SupervisionManager bean.
   */
  private SupervisionManager supervisionManager;

  /**
   * Threshold of DAQ/Equipment/SubEqu. down when warning is sent to admin. 
   */
  private static final short WARNING_THRESHOLD = 50;
  
  /**
   * Warning has been sent.
   */
  private boolean alarmActive = false;
  
  /**
   * Count down to alarm switch off.
   */
  private AtomicInteger warningSwitchOffCountDown = new AtomicInteger(SWITCH_OFF_COUNTDOWN);

  /** Reference to the clusterCache to share values accross teh cluster nodes */
  private final ClusterCache clusterCache;
  
  private static final int SWITCH_OFF_COUNTDOWN = 60; //10mins 
  
  /**
   * Constructor.
   * @param cache the alive timer cache
   * @param aliveTimerFacade the alive timer facade bean
   * @param supervisionManager the supervision manager bean
   * @param clusterCache Reference to the clusterCache to share values accross teh cluster nodes
   */
  @Autowired 
  public AliveTimerChecker(final AliveTimerCache cache,
                           final AliveTimerFacade aliveTimerFacade,
                           final SupervisionManager supervisionManager,
                           final ClusterCache clusterCache) {
    super();
    this.aliveTimerCache = cache;
    this.aliveTimerFacade = aliveTimerFacade;
    this.supervisionManager = supervisionManager;
    this.clusterCache = clusterCache;
  }
  
  /**
   * Initialises the clustered values
   */
  @PostConstruct
  public void init() {
    clusterCache.acquireWriteLockOnKey(LAST_ALIVE_TIMER_CHECK_LONG);
    try {
      if (!clusterCache.hasKey(LAST_ALIVE_TIMER_CHECK_LONG)) {
        clusterCache.put(LAST_ALIVE_TIMER_CHECK_LONG, Long.valueOf(0L));
      }
    } finally {
      clusterCache.releaseWriteLockOnKey(LAST_ALIVE_TIMER_CHECK_LONG);
    }
  }

  /**
   * Starts the timer. Alive timers will be checked from then on.
   */
  @Override
  public synchronized void start() {   
    LOGGER.info("Starting the C2MON alive timer mechanism.");
    timer = new Timer("C2MON-alive-timer");
    timer.schedule(this, INITIAL_SCAN_DELAY, SCAN_INTERVAL);
    running = true;
  }

  /**
   * Stops the timer mechanism. No more checks are made on the
   * alive timers.
   * 
   * <p>Can be restarted using the start method.
   */
  @Override
  public synchronized void stop() {
    LOGGER.info("Stopping the C2MON alive timer mechanism.");
    timer.cancel();   
    running = false;
  }

  /**
   * Run method of the AliveTimerManager thread.
   */
  public void run() {
    clusterCache.acquireWriteLockOnKey(LAST_ALIVE_TIMER_CHECK_LONG);
    try {
      Long lastCheck = (Long) clusterCache.getCopy(LAST_ALIVE_TIMER_CHECK_LONG);
      if (System.currentTimeMillis() - lastCheck.longValue() < 9000) { //results in check on a single server
        LOGGER.debug("Skipping alive check as already performed.");
      } else {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("run() : checking alive timers ... ");
        }        
        short aliveDownCount = 0;
        try {
          for (Long currentId : aliveTimerCache.getKeys()) {
            AliveTimer aliveTimerCopy = null;
            boolean aliveExpiration = false;
            aliveTimerCache.acquireWriteLockOnKey(currentId);
            try {
              aliveTimerCopy = aliveTimerCache.getCopy(currentId);        

              if (aliveTimerCopy.isActive()) {            
                if (aliveTimerFacade.hasExpired(currentId)) {
                  aliveTimerFacade.stop(currentId);
                  aliveExpiration = true;
                  aliveDownCount++;
                }            
              } else {
                aliveDownCount++;
              }
            } finally {
              aliveTimerCache.releaseWriteLockOnKey(currentId);          
            }
            
            if (aliveExpiration) {
              // get fresh copy with all changes
              aliveTimerCopy = aliveTimerCache.getCopy(currentId);
              onAliveTimerExpiration(aliveTimerCopy);
            }
            
          }
          if (!alarmActive && aliveDownCount > WARNING_THRESHOLD) {
            alarmActive = true;
            SMS_LOGGER.warn("Over " + WARNING_THRESHOLD + " DAQ/Equipment are currently down.");
          } else if (alarmActive && warningSwitchOffCountDown.decrementAndGet() == 0) {
            SMS_LOGGER.warn("DAQ/Equipment status back to normal (" + aliveDownCount + " detected as down)");
            alarmActive = false;
            warningSwitchOffCountDown = new AtomicInteger(SWITCH_OFF_COUNTDOWN);
          }
        } catch (CacheElementNotFoundException cacheEx) {
          LOGGER.warn("Failed to locate alive timer in cache on expiration check (may happen exceptionally if just removed).", cacheEx);
        } catch (Exception e) {    
          LOGGER.error("Unexpected exception when checking the alive timers", e);
        }
        lastCheck = Long.valueOf(System.currentTimeMillis());
        clusterCache.put(LAST_ALIVE_TIMER_CHECK_LONG, lastCheck);
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("run() : finished checking alive timers ... ");
        }        
      } // end of else block
    } finally {
      clusterCache.releaseWriteLockOnKey(LAST_ALIVE_TIMER_CHECK_LONG);
    }       
  }
  
  /**
   * Notifies the supervision manager.
   * 
   * @param pAliveTimer the alive timer that has expired
   */
  private void onAliveTimerExpiration(final AliveTimer pAliveTimer) {
    supervisionManager.onAliveTimerExpiration(pAliveTimer);        
  }
  
  @Override
  public boolean isAutoStartup() {
    return false;
  }

  @Override
  public void stop(Runnable runnable) {
    stop();
    runnable.run();
  }

  @Override
  public synchronized boolean isRunning() {
   return running; 
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_START_LAST + 1;
  }
}

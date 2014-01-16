package cern.c2mon.server.supervision.heartbeat;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;

import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.common.C2monCacheLoader;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.supervision.HeartbeatListener;
import cern.c2mon.server.supervision.HeartbeatManager;
import cern.c2mon.shared.client.supervision.Heartbeat;
import cern.c2mon.shared.util.jms.JmsSender;
import cern.c2mon.shared.util.json.GsonFactory;

import com.google.gson.Gson;

/**
 * The HeartbeatManager bean generates regular server heartbeats, which
 * can then be used by clients for monitoring the alive status of the
 * server.
 * 
 * <p>The heartbeat is published directly on a JMS topic. Server modules can
 * also register as listeners for internal heatbeat notifications.
 * 
 * @author Mark Brightwell
 *
 */
public class HeartbeatManagerImpl implements HeartbeatManager, SmartLifecycle {
  
  /**
   * Log4j Logger for this class.
   */
  private static final Logger LOG = Logger.getLogger(HeartbeatManagerImpl.class);

  /**
   * Gson that is reused.
   */
  private Gson gson = GsonFactory.createGson();
  
  /**
   * Lifecycle flag.
   */
  private volatile boolean running = false;
  
  /**
   * Timer object used for generating heartbeat messages at fixed intervals.
   */
  private Timer timer;
  
  /**
   * Heart beat interval. Default is 30000.
   */
  private long heartbeatInterval = Heartbeat.getHeartbeatInterval();
  
  /**
   * Bean for sending JMS heartbeat messages.
   */
  private JmsSender heartbeatSender;
  
  /**
   * The list of listeners registered for heartbeats.
   * Synchronise on this field when accessing this list.
   */
  private List<HeartbeatListener> listeners;
  
  /**
   * Is only accessed to check Terracotta is working in case of distributed cache.
   */  
  private ClusterCache clusterCache;
  
  /**
   * Constructor.
   * @param heartbeatSender the JmsSender for sending heartbeats to the clients 
   */  
  @Autowired
  public HeartbeatManagerImpl(@Qualifier("heartbeatSender") final JmsSender heartbeatSender,
                              @Qualifier("clusterCache") final ClusterCache clusterCache) {
    super();   
    this.heartbeatSender = heartbeatSender;    
    this.timer = new Timer();
    this.listeners = new ArrayList<HeartbeatListener>();
    this.clusterCache = clusterCache;
  }
 
  @Override
  public void registerToHeartbeat(final HeartbeatListener heartbeatListener) {
    synchronized (listeners) {
      listeners.add(heartbeatListener);
    }    
  }
  
  /**
   * Notifies all the registered listeners of the heartbeat.
   * @param heartbeat the new heartbeat
   */
  private void notifyListeners(Heartbeat heartbeat) {
    synchronized (listeners) {
      for (HeartbeatListener listener : listeners) {
        listener.notifyHeartbeat(heartbeat);
      }
    }    
  }
  
  /**
   * Overrides the default heartbeat interval.
   * 
   * @param heartbeatInterval the heartbeatInterval to set
   */
  public void setHeartbeatInterval(long heartbeatInterval) {
    this.heartbeatInterval = heartbeatInterval;
  }

  
  /**
   * Bean is started up manually.
   */
  @Override
  public boolean isAutoStartup() {  
    return false;
  }

  /**
   * Stops the heart beat and calls
   * the callback.
   */
  @Override
  public void stop(Runnable callback) {
    stop();
    callback.run();    
  }

  /**
   * Is the heartbeat currently switched on?
   */
  @Override
  public synchronized boolean isRunning() {
    return running;
  }

  /**
   * Start sending the heartbeat.
   */
  @Override
  public synchronized void start() {
    LOG.info("Starting server heartbeat.");
    this.timer.scheduleAtFixedRate(new HeartbeatTask(), 0, heartbeatInterval);
    running = true;
  }

  /**
   * Stops heartbeat sending.
   */
  @Override
  public synchronized void stop() {
    LOG.debug("Stopping server heartbeat.");    
    this.timer.cancel();
    running = false;
  }

  /**
   * Started once everything else is started (client use this signal
   * and will expect rapid response).
   */
  @Override
  public int getPhase() {
    return ServerConstants.PHASE_START_LAST + 1;
  }
  
  /**
   * The task that is run at every heartbeat.
   * @author Mark Brightwell
   *
   */
  private class HeartbeatTask extends TimerTask {
    
    /**
     * Sends the server heart beat to all heartbeat listeners.
     */
    @Override
    public void run() {      
      if (LOG.isDebugEnabled()) {
        LOG.debug("Sending server heartbeat.");
      }
      try {
        final Heartbeat heartbeat = new Heartbeat(); 
        //access cache to check cache process is responding when using distributed cache
        clusterCache.acquireWriteLockOnKey(C2monCacheLoader.aliveStatusInitialized);
        clusterCache.releaseWriteLockOnKey(C2monCacheLoader.aliveStatusInitialized);
        heartbeatSender.send(gson.toJson(heartbeat));                
        notifyListeners(heartbeat);      
      } catch (Exception e) {
        LOG.error("run() : Error sending heartbeat message.", e);
      }
    }
    
  }
  
}

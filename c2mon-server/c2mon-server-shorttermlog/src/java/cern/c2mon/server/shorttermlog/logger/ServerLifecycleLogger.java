package cern.c2mon.server.shorttermlog.logger;

import java.sql.Timestamp;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.SmartLifecycle;

import cern.c2mon.shared.client.lifecycle.LifecycleEventType;
import cern.c2mon.shared.client.lifecycle.ServerLifecycleEvent;
import cern.c2mon.shared.client.lifecycle.ServerLifecycleMapper;
import cern.c2mon.server.common.config.ServerConstants;

/**
 * Bean listening for server stops/starts and logging
 * them in the database.
 * 
 * <p>Will always log start events if the DB is available at the time. If not available,
 * thread will retry until DB is available or server shutdown.
 * 
 * <p>Stop events will be missing if the server is kill -9'd, or if the DB is not available
 * at shutdown of the server.
 * 
 * @author Mark Brightwell
 *
 */
public class ServerLifecycleLogger implements SmartLifecycle {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(ServerLifecycleLogger.class);
  
  /**
   * Lifecycle flag.
   */
  private volatile boolean running = false;
  
  /**
   * Name of this server.
   */
  private String serverName;
  
  /**
   * Mapper bean for logging events.
   */  
  private ServerLifecycleMapper serverLifecycleMapper;
  
  /**
   * Timer for retrying the start log if unsuccessful.
   */
  private Timer relogTimer;
  
  /**
   * Time between re-log attempts of server start event.
   */
  private long timebetweenRelogs = 120000;
  
  /**
   * Constructor.
   * @param serverLifecycleMapper the mapper bean used for writing to the DB
   */
  @Autowired  
  public ServerLifecycleLogger(final ServerLifecycleMapper serverLifecycleMapper) {
    super();
    this.serverLifecycleMapper = serverLifecycleMapper;
  }

  private void logStartEvent() {
    final ServerLifecycleEvent startEvent = new ServerLifecycleEvent(new Timestamp(System.currentTimeMillis()), serverName, LifecycleEventType.START);
    logStartEvent(startEvent);
  }
  
  private synchronized void logStartEvent(final ServerLifecycleEvent event) {
    try {      
      serverLifecycleMapper.logEvent(event);
      if (relogTimer != null){
        relogTimer.cancel();
      }
    } catch (PersistenceException e) {
      LOGGER.error("Exception caught when logging start event: will try again in 2 minutes time", e);
      if (relogTimer == null){
        relogTimer = new Timer("Lifecycle-start-log-timer");
      }
      if (running) {
        relogTimer.schedule(new TimerTask() {                   
          @Override
          public void run() {
            logStartEvent(event);
          }
       }, timebetweenRelogs);
      }
    }
  }
  
  private void logStopEvent() {
    try {
      serverLifecycleMapper.logEvent(new ServerLifecycleEvent(new Timestamp(System.currentTimeMillis()), serverName, LifecycleEventType.STOP));
    } catch (PersistenceException e) {
      LOGGER.error("Exception caught when logging server stop event", e);
    }    
  }
  
  @Override
  public boolean isAutoStartup() {
    return false;
  }

  @Override
  public void stop(Runnable arg0) {
    stop();
    arg0.run();
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public void start() {
    running = true;
    logStartEvent();
  }

  @Override
  public synchronized void stop() {
    running = false;
    if (relogTimer != null){
      relogTimer.cancel();
    }
    logStopEvent();
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_START_LAST + 1;
  }

  /**
   * @param serverName the serverName to set
   */
  @Required
  public void setServerName(String serverName) {
    this.serverName = serverName;
  }

  /**
   * @param serverLifecycleMapper the serverLifecycleMapper to set
   */
  public void setServerLifecycleMapper(ServerLifecycleMapper serverLifecycleMapper) {
    this.serverLifecycleMapper = serverLifecycleMapper;
  }

  /**
   * @param timeBetweenRelogs the timebetweenRelogs to set
   */
  public void setTimebetweenRelogs(long timeBetweenRelogs) {
    this.timebetweenRelogs = timeBetweenRelogs;
  }

}

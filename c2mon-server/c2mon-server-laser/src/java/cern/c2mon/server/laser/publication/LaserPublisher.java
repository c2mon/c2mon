package cern.c2mon.server.laser.publication;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import cern.tim.server.cache.CacheRegistrationService;
import cern.tim.server.cache.TimCacheListener;
import cern.tim.server.common.alarm.Alarm;
import cern.tim.server.common.config.ServerConstants;

/**
 * Bean responsible for submitting C2MON alarms to LASER.
 */
//starts as singleton bean in Spring context
@Service
public class LaserPublisher implements TimCacheListener<Alarm>, SmartLifecycle {

  /**
   * Flag for lifecycle calls.
   */
  private volatile boolean running = false;
  
  /**
   * Service for registering as listener to C2MON caches.
   */
  private CacheRegistrationService cacheRegistrationService;
    
  /**
   * Autowired constructor.
   * @param cacheRegistrationService the C2MON cache registration service bean
   */
  @Autowired
  public LaserPublisher(final CacheRegistrationService cacheRegistrationService) {
    super();
    this.cacheRegistrationService = cacheRegistrationService;
  }
  
  /**
   * Called at server startup.
   */
  @PostConstruct
  public void init() {
    cacheRegistrationService.registerToAlarms(this);
  }
  
  @Override
  public void notifyElementUpdated(Alarm cacheable) {
    //TODO submit alarm to laser
  }
  
  
  //below server lifecycle methods: complete start/stop (no need to allow for stop/restart, just final shutdown)
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
  public void start() {    
    running = true;
    //TODO start LASER connection
  }

  @Override
  public void stop() {
    running = false;
    //TODO wait for any LASER publication to end and close resources
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_STOP_LAST;
  }



}

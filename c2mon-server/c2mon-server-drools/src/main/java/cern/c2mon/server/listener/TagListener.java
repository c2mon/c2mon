package cern.c2mon.server.listener;

import cern.c2mon.server.cache.C2monBufferedCacheListener;
import cern.c2mon.server.cache.CacheRegistrationService;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.drools.DroolsEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.Collection;
@Component
@Slf4j
public class TagListener implements C2monBufferedCacheListener<Tag>, SmartLifecycle {

  private DroolsEngine droolsEngine;

  private final Lifecycle lifecycle;

  private volatile boolean running = false;

  public TagListener(@Autowired final CacheRegistrationService cacheRegistrationService, final DroolsEngine droolsEngine) {
    this.lifecycle = cacheRegistrationService.registerBufferedListenerToTags(this);
    this.droolsEngine = droolsEngine;
  }

  @Override
  public void notifyElementUpdated(Collection<Tag> collection) {
    System.out.println(collection);
  }

  @Override
  public void confirmStatus(Collection<Tag> eventCollection) {

  }

  @Override
  public String getThreadName() {
    return "DroolsListener";
  }

  @Override
  public boolean isAutoStartup() {
    return true;
  }

  @Override
  public void stop(Runnable runnable) {
    stop();
    runnable.run();
  }

  @Override
  public void start() {
    this.lifecycle.start();
    this.running = true;
  }

  @Override
  public void stop() {
    this.lifecycle.stop();
    this.running = false;
  }

  @Override
  public boolean isRunning() {
    return this.running;
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_START_LAST - 1;
  }
}

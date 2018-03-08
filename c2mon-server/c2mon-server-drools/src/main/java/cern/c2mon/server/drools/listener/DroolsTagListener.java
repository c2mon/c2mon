package cern.c2mon.server.drools.listener;

import cern.c2mon.server.cache.C2monBufferedCacheListener;
import cern.c2mon.server.cache.CacheRegistrationService;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.common.tag.Tag;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.KieSession;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.Collection;
@Component
@Slf4j
public class DroolsTagListener implements C2monBufferedCacheListener<Tag>, SmartLifecycle {

  private final KieSession kieSession;

  private final Lifecycle lifecycle;

  private volatile boolean running = false;

  public DroolsTagListener(final CacheRegistrationService cacheRegistrationService, final KieSession kieSession) {
    this.kieSession = kieSession;
    this.lifecycle = cacheRegistrationService.registerBufferedListenerToTags(this);
  }

  @Override
  public void notifyElementUpdated(Collection<Tag> collection) {
    for (Tag t : collection) {
      this.kieSession.insert(t);
    }
    this.kieSession.fireAllRules();
    for (Object o : this.kieSession.getObjects()) {
      System.out.println(o);
    }
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

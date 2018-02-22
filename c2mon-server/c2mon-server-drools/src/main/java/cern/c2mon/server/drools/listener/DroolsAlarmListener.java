/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.drools.listener;

import cern.c2mon.server.cache.C2monCacheListener;
import cern.c2mon.server.cache.CacheRegistrationService;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.server.common.config.ServerConstants;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.KieSession;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DroolsAlarmListener implements C2monCacheListener<Alarm>, SmartLifecycle {

  private Lifecycle listenerContainer;

  private volatile boolean running = false;

  private final KieSession kieSession;

  public DroolsAlarmListener(final CacheRegistrationService cacheRegistrationService, final KieSession kieSession) {
    this.kieSession = kieSession;
    listenerContainer = cacheRegistrationService.registerToAlarms(this);
  }

  @Override
  public void notifyElementUpdated(final Alarm alarm) {
    System.out.println(alarm);
    if (alarm == null) {
      log.warn("Received a null alarm");
      return;
    }
    this.kieSession.insert(alarm);
    this.kieSession.fireAllRules();
  }

  @Override
  public void confirmStatus(Alarm alarm) {}

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
  public boolean isRunning() {
    return running;
  }

  @Override
  public void start() {
    running = true;
    listenerContainer.start();
  }

  @Override
  public void stop() {
    listenerContainer.stop();
    running = false;
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_STOP_LAST - 1;
  }
}

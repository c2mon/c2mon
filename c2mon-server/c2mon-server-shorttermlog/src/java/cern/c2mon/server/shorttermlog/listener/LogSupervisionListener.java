/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.shorttermlog.listener;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.shorttermlog.mapper.SupervisionMapper;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.supervision.SupervisionListener;
import cern.c2mon.server.supervision.SupervisionNotifier;

/**
 * Listens for supervision notifications from the core and logs
 * these into the DB STL account.
 * 
 * @author Mark Brightwell
 *
 */
@Service
public class LogSupervisionListener implements SupervisionListener, SmartLifecycle {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(LogSupervisionListener.class);
  
  /**
   * Notifier of supervision module.
   */
  private SupervisionNotifier supervisionNotifier;
  
  /**
   * Supervision Mapper
   */
  private SupervisionMapper supervisionMapper;
  
  /**
   * Listener container lifecycle hook.
   */
  private Lifecycle listenerContainer;
  
  /**
   * Lifecycle flag.
   */
  private volatile boolean running = false;
  
  /**
   * Autowired constructor.
   * 
   * @param supervisionNotifier the notifier to register to
   * @param supervisionMapper the mapper to write to the DB
   */
  @Autowired
  public LogSupervisionListener(final SupervisionNotifier supervisionNotifier, final SupervisionMapper supervisionMapper) {
    super();
    this.supervisionNotifier = supervisionNotifier;
    this.supervisionMapper = supervisionMapper;
  }

  /**
   * Called at bean initialisation. Registers for
   * notifications.
   */
  @PostConstruct
  public void init() {
    LOGGER.debug("Registering short-term-log module for supervision updates");
    listenerContainer = supervisionNotifier.registerAsListener(this);
  }

  @Transactional(propagation=Propagation.REQUIRED,isolation=Isolation.DEFAULT)
  @Override
  public void notifySupervisionEvent(final SupervisionEvent supervisionEvent) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Logging supervision status " + supervisionEvent.getStatus() 
            + " for " + supervisionEvent.getEntity() 
            + " " + supervisionEvent.getEntityId());
    }
    supervisionMapper.logSupervisionEvent(supervisionEvent);
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
  public boolean isRunning() {
    return running;
  }

  @Override
  public void start() {
    LOGGER.debug("Starting supervision event logger.");
    running = true;
    listenerContainer.start();
  }

  @Override
  public void stop() {
    LOGGER.debug("Stopping supervision event logger.");
    listenerContainer.stop();
    running = false;    
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_STOP_LAST - 1;    
  }

}

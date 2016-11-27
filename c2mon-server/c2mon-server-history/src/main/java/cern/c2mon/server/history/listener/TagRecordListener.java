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
package cern.c2mon.server.history.listener;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.PostConstruct;

import cern.c2mon.server.history.logger.ExpressionLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import cern.c2mon.server.history.logger.BatchLogger;
import cern.c2mon.server.cache.C2monBufferedCacheListener;
import cern.c2mon.server.cache.CacheRegistrationService;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.common.tag.Tag;

/**
 * Listens to updates in the Rule and DataTag caches and calls the DAO
 * for logging these to the history database.
 *
 * @author Mark Brightwell
 *
 */
@Service
public class TagRecordListener implements C2monBufferedCacheListener<Tag>, SmartLifecycle {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(TagRecordListener.class);

  /**
   * Reference to registration service.
   */
  private CacheRegistrationService cacheRegistrationService;

  /**
   * Bean that logs Tags into the history.
   */
  private BatchLogger<Tag> tagLogger;

  /**
   * Bean that logs expressions into the STL.
   */
  private ExpressionLogger expressionLogger;

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
   * @param cacheRegistrationService for registering cache listeners
   * @param tagLogger for logging cache objects to the STL
   */
  @Autowired
  public TagRecordListener(final CacheRegistrationService cacheRegistrationService,
                             @Qualifier("tagLogger") final BatchLogger<Tag> tagLogger,
                             ExpressionLogger expressionLogger) {
    super();
    this.cacheRegistrationService = cacheRegistrationService;
    this.tagLogger = tagLogger;
    this.expressionLogger = expressionLogger;
  }

  /**
   * Registers to be notified of all Tag updates (data, rule and control tags).
   */
  @PostConstruct
  public void init() {
    listenerContainer = cacheRegistrationService.registerBufferedListenerToTags(this);
  }

  @Override
  public void confirmStatus(Collection<Tag> tagCollection) {
    //do not log confirm callbacks (STL data not essential)
  }

  @Override
  public String getThreadName() {
    return "DbPersister";
  }

  @Override
  public void notifyElementUpdated(Collection<Tag> tagCollection) {
    ArrayList<Tag> tagsToLog = new ArrayList<>(tagCollection.size());
    for (Tag tag : tagCollection) {
      if (tag.isLogged())
        tagsToLog.add(tag);
    }
    tagLogger.log(tagsToLog);
    tagsToLog.stream()
        .forEach(tag -> expressionLogger.log(tag.getExpressions(), tag.getId(), tag.getTimestamp()));
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
    LOGGER.debug("Starting Tag logger (history)");
    running = true;
    listenerContainer.start();
  }

  @Override
  public void stop() {
    LOGGER.debug("Stopping Tag logger (history)");
    listenerContainer.stop();
    running = false;
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_STOP_LAST - 1;
  }


}

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
package cern.c2mon.server.eslog.listener;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.BufferedTimCacheListener;
import cern.c2mon.server.cache.CacheRegistrationService;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.eslog.logger.Connector;
import cern.c2mon.server.eslog.logger.TransportConnector;
import cern.c2mon.server.eslog.structure.DataTagESLogConverter;
import cern.c2mon.server.eslog.structure.types.TagES;
import lombok.extern.slf4j.Slf4j;

/**
 * Listens to updates in the Rule and DataTag caches and calls the DAO
 * for logging these to the database (STL account).
 *
 * @author Alban Marguet
 *
 */
@Service
@Slf4j
public class TagESLogCacheListener implements BufferedTimCacheListener<Tag>, SmartLifecycle {

  /**
   * Reference to registration service.
   */
  private final CacheRegistrationService cacheRegistrationService;

  private final DataTagESLogConverter dataTagESLogConverter;

  private final Connector connector;

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
   */
  @Autowired
  public TagESLogCacheListener(final CacheRegistrationService cacheRegistrationService, final DataTagESLogConverter dataTagESLogConverter, final TransportConnector connector) {
    super();
    this.cacheRegistrationService = cacheRegistrationService;
    this.dataTagESLogConverter = dataTagESLogConverter;
    this.connector = connector;
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

  /**
   * When receiving a cache update, get the metadata for tags and send them to the ElasticSearch node.
   * @param tagCollection batch of tags to be logged to ElasticSearch
   */
  @Override
  public void notifyElementUpdated(Collection<Tag> tagCollection) {
    log.trace("notifyElementUpdated() - Received a tagCollection of " + tagCollection.size() + " elements.");

    Collection<TagES> tagESCollection = new ArrayList<>();
    Collection<Tag> tagsToLog = new ArrayList<>(tagCollection.size());
    for (Tag tag : tagCollection) {
      if (tag.isLogged()) {
        tagsToLog.add(tag);
      }
    }

    for (Tag tag: tagsToLog) {
      try {
        TagES tagES = dataTagESLogConverter.convertToTagES(tag);

        if (tagES != null) {
          tagESCollection.add(tagES);
        }
        else {
          log.warn("notifyElementUpdated() - Unsupported data type " + tag.getDataType() + " for tag #" + tag.getId() + " (" + tag.getName() + ") ==> Not sent to elasticsearch");
        }

      } catch (Exception e) {
        log.error("notifyElementUpdated() - Catch unexpected exception while trying to instantiate data and send it to the ElasticSearch cluster.", e);
        log.error("notifyElementUpdate() - tag was not added to the ElasticSearch cluster (type " + tag.getDataType() + ", tag #" + tag.getId() + " (" + tag.getName() + ", tagValue=" + tag.getValue() + ")");
      }
    }

    log.trace("notifyElementUpdated() - Created a TagESCollection of " + tagESCollection.size() + " elements.");


    try {
      // Sending to elasticsearch
      connector.indexTags(tagESCollection);
    }
    catch(Exception e) {
      log.error("notifyElementUpdate() - Exception occurred while trying to index data to the ElasticSearch cluster.");
    }
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
    log.debug("Starting Tag logger (elastic-search-log)");
    running = true;
    listenerContainer.start();
  }

  @Override
  public void stop() {
    log.debug("Stopping Tag logger (elastic-search-log)");
    listenerContainer.stop();
    running = false;
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_STOP_LAST - 1;
  }
}
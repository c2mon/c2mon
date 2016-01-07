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

import cern.c2mon.server.eslog.logger.Indexer;
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
import org.yaml.snakeyaml.nodes.CollectionNode;

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

  /**
   * Allows t oconvert from Tag to TagES.
   */
  private final DataTagESLogConverter dataTagESLogConverter;

  /**
   * The Connector allows to connect to an ElasticSearch cluster and to communicate in order to write data.
   */
  private final Connector connector;

  /**
   * the Indexer allows to use the connection to the ElasticSearch cluster and to write data.
   */
  private final Indexer indexer;

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
  public TagESLogCacheListener(final CacheRegistrationService cacheRegistrationService, final DataTagESLogConverter dataTagESLogConverter, final TransportConnector connector, final Indexer indexer) {
    super();
    this.cacheRegistrationService = cacheRegistrationService;
    this.dataTagESLogConverter = dataTagESLogConverter;
    this.connector = connector;
    this.indexer = indexer;
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

    retrieveTagsToLog(tagCollection, tagsToLog);
    convertTagsToLogToTagES(tagsToLog, tagESCollection);
    sendCollectionTagESToElasticSearch(tagESCollection);
  }

  private void retrieveTagsToLog(Collection<Tag> tagCollection, Collection<Tag> tagsToLog) {
    for (Tag tag : tagCollection) {
      if (tag.isLogged()) {
        tagsToLog.add(tag);
      }
    }
    log.trace("retrieveTagsToLog() - With " + tagsToLog.size() + " tags to be logged.");
  }

  private void convertTagsToLogToTagES(Collection<Tag> tagsToLog, Collection<TagES> tagESCollection) {
    for (Tag tag: tagsToLog) {
      try {
        TagES tagES = dataTagESLogConverter.convertToTagES(tag);
        addTagToCollectionIfNotNull(tagES, tagESCollection);
      } catch (Exception e) {
        log.error("notifyElementUpdated() - Error occurred during tag parsing for ElasticSearch. Tag #" + tag.getId() + " is not added to bulk sending (name=" + tag.getName() + ", value=" + tag.getValue() + ", type=" + tag.getDataType() + ")", e);
      }
    }

    log.trace("notifyElementUpdated() - Created a TagESCollection of " + tagESCollection.size() + " elements.");
  }

  private void addTagToCollectionIfNotNull(TagES tagES, Collection<TagES> tagESCollection) {
    if (tagES != null) {
      tagESCollection.add(tagES);
    }
  }

  private void sendCollectionTagESToElasticSearch(Collection<TagES> tagESCollection) {
    try {
      indexer.indexTags(tagESCollection);
    }
    catch(Exception e) {
      log.error("notifyElementUpdated() - Exception occurred while trying to index data to the ElasticSearch cluster.", e);
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
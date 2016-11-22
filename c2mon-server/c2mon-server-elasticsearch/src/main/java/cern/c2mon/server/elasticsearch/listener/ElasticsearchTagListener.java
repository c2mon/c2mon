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
package cern.c2mon.server.elasticsearch.listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import cern.c2mon.server.elasticsearch.indexer.EsTagIndexer;
import cern.c2mon.server.elasticsearch.structure.converter.ElasticsearchTagConverter;
import cern.c2mon.server.elasticsearch.structure.types.tag.EsTag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import cern.c2mon.pmanager.persistence.IPersistenceManager;
import cern.c2mon.server.cache.C2monBufferedCacheListener;
import cern.c2mon.server.cache.CacheRegistrationService;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.common.tag.Tag;

/**
 * Listens to updates in the Rule and DataTag caches and calls the {@link EsTagIndexer} for logging these to Elasticsearch.
 *
 * @author Alban Marguet
 */
@Service
@Slf4j
public class ElasticsearchTagListener implements C2monBufferedCacheListener<Tag>, SmartLifecycle {

  /**
   * Reference to registration service.
   */
  private final CacheRegistrationService cacheRegistrationService;

  /**
   * Allows to convert from Tag to {@link EsTag}.
   */
  private final ElasticsearchTagConverter elasticsearchTagConverter;

  private final IPersistenceManager<EsTag> esTagPersistenceManager;

  /**
   * Listener container lifecycle hook.
   */
  private Lifecycle listenerContainer;

  /**
   * Lifecycle flag.
   */
  private volatile boolean running = false;

  @Autowired
  public ElasticsearchTagListener(final ElasticsearchTagConverter elasticsearchTagConverter,
                                  final CacheRegistrationService cacheRegistrationService,
                                  @Qualifier("esTagPersistenceManager") final IPersistenceManager<EsTag> esTagPersistenceManager) {
    this.elasticsearchTagConverter = elasticsearchTagConverter;
    this.cacheRegistrationService = cacheRegistrationService;
    this.esTagPersistenceManager = esTagPersistenceManager;
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
    return "ElasticsearchPersister";
  }

  /**
   * When receiving a cache update, get the metadata for tags and send them to the {@link EsTagIndexer} to log them.
   *
   * @param tagCollection batch of tags to be logged to Elasticsearch
   */
  @Override
  public void notifyElementUpdated(final Collection<Tag> tagCollection) {
    if(tagCollection == null) {
      log.warn("notifyElementUpdated() - Received a null collection of tags");
      return;
    }
    log.info("notifyElementUpdated() - Received a tagCollection of " + tagCollection.size() + " elements.");

    Collection<Tag> loggableTags = filterLoggableTags(tagCollection);
    log.debug("retrieveLoggableTags() - With " + loggableTags.size() + " tags to be logged.");

    esTagPersistenceManager.storeData(convertTagsToEsTags(loggableTags));
  }

  private Collection<Tag> filterLoggableTags(final Collection<Tag> tagCollection) {
    if(tagCollection == null) {
      return Collections.emptyList();
    }
    return tagCollection.stream()
        .filter(Tag::isLogged)
        .collect(Collectors.toList());
  }

  private List<EsTag> convertTagsToEsTags(final Collection<Tag> tagsToLog) {
    final List<EsTag> esTagList = new ArrayList<>();
    if (CollectionUtils.isEmpty(tagsToLog)) {
      return esTagList;
    }

    tagsToLog.forEach(tag -> esTagList.add(elasticsearchTagConverter.convert(tag)));

    return esTagList;
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
    log.debug("Starting Tag logger (elasticsearch)");
    running = true;
    listenerContainer.start();
  }

  @Override
  public void stop() {
    log.debug("Stopping Tag logger (elasticsearch)");
    listenerContainer.stop();
    running = false;
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_STOP_LAST - 1;
  }
}

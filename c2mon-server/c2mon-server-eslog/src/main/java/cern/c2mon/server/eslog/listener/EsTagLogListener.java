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
package cern.c2mon.server.eslog.listener;

import cern.c2mon.pmanager.persistence.IPersistenceManager;
import cern.c2mon.server.cache.BufferedTimCacheListener;
import cern.c2mon.server.cache.CacheRegistrationService;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.eslog.indexer.EsTagIndexer;
import cern.c2mon.server.eslog.structure.converter.EsTagLogConverter;
import cern.c2mon.server.eslog.structure.types.tag.AbstractEsTag;
import cern.c2mon.server.eslog.structure.types.tag.EsValueType;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Listens to updates in the Rule and DataTag caches and calls the {@link EsTagIndexer} for logging these to ElasticSearch.
 *
 * @author Alban Marguet
 */
@Service
@Slf4j
public class EsTagLogListener implements BufferedTimCacheListener<Tag>, SmartLifecycle {

  /**
   * Reference to registration service.
   */
  private final CacheRegistrationService cacheRegistrationService;

  /**
   * Allows to convert from Tag to {@link AbstractEsTag}.
   */
  private final EsTagLogConverter esTagLogConverter;

  private final IPersistenceManager tagNumericPersistenceManager;
  private final IPersistenceManager tagStringPersistenceManager;
  private final IPersistenceManager tagBooleanPersistenceManager;

  /**
   * Listener container lifecycle hook.
   */
  private Lifecycle listenerContainer;

  /**
   * Lifecycle flag.
   */
  private volatile boolean running = false;

  @Autowired
  public EsTagLogListener(final EsTagLogConverter esTagLogConverter,
                          final CacheRegistrationService cacheRegistrationService,
                          @Qualifier("esTagNumericPersistenceManager") final IPersistenceManager tagNumericPersistenceManager,
                          @Qualifier("esTagStringPersistenceManager") final IPersistenceManager tagStringPersistenceManager,
                          @Qualifier("esTagBooleanPersistenceManager") final IPersistenceManager tagBooleanPersistenceManager) {
    this.esTagLogConverter = esTagLogConverter;
    this.cacheRegistrationService = cacheRegistrationService;
    this.tagNumericPersistenceManager = tagNumericPersistenceManager;
    this.tagStringPersistenceManager = tagStringPersistenceManager;
    this.tagBooleanPersistenceManager = tagBooleanPersistenceManager;
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
   * When receiving a cache update, get the metadata for tags and send them to the {@link EsTagIndexer} to log them.
   *
   * @param tagCollection batch of tags to be logged to ElasticSearch
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

    sendEsTagsToElastic(convertTagsToEsTags(loggableTags));
  }

  private Collection<Tag> filterLoggableTags(final Collection<Tag> tagCollection) {
    if(tagCollection == null) {
      return Collections.emptyList();
    }
    return tagCollection.stream()
        .filter(Tag::isLogged)
        .collect(Collectors.toList());
  }

  private ListMultimap<EsValueType, AbstractEsTag> convertTagsToEsTags(final Collection<Tag> tagsToLog) {
    final ListMultimap<EsValueType, AbstractEsTag> tagMultimap = ArrayListMultimap.create();
    if (CollectionUtils.isEmpty(tagsToLog)) {
      return tagMultimap;
    }

    for (Tag tag : tagsToLog) {
      AbstractEsTag esTagImpl = esTagLogConverter.convert(tag);
      if (esTagImpl != null) {
        tagMultimap.put(esTagImpl.getType(), esTagImpl);
      }
    }

    return tagMultimap;
  }

  private void sendEsTagsToElastic(final ListMultimap<EsValueType, AbstractEsTag> tagMultimap) {
    if(tagMultimap == null) {
      log.warn("sendCollectionTagESToElasticSearch() - received an empty map of tags, will do nothing!");
      return;
    }

    log.info("sendCollectionTagESToElasticSearch() - send a collection of tags to indexer of size: "
        + tagMultimap.size());

    tagNumericPersistenceManager.storeData(tagMultimap.get(EsValueType.NUMERIC));
    tagBooleanPersistenceManager.storeData(tagMultimap.get(EsValueType.BOOLEAN));
    tagStringPersistenceManager.storeData(tagMultimap.get(EsValueType.STRING));

    //objects will be stored with the string persistence manager, as well
    tagStringPersistenceManager.storeData(tagMultimap.get(EsValueType.OBJECT));
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
    log.debug("Starting Tag logger (eslog)");
    running = true;
    listenerContainer.start();
  }

  @Override
  public void stop() {
    log.debug("Stopping Tag logger (eslog)");
    listenerContainer.stop();
    running = false;
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_STOP_LAST - 1;
  }
}
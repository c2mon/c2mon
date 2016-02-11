package cern.c2mon.server.eslog.listener;
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

import cern.c2mon.pmanager.persistence.IPersistenceManager;
import cern.c2mon.server.cache.BufferedTimCacheListener;
import cern.c2mon.server.cache.CacheRegistrationService;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.eslog.structure.converter.EsTagLogConverter;
import cern.c2mon.server.eslog.structure.types.*;
import cern.c2mon.server.eslog.structure.types.EsTagImpl;
import cern.c2mon.server.eslog.structure.types.EsTagString;
import cern.c2mon.server.eslog.indexer.EsTagIndexer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Listens to updates in the Rule and DataTag caches and calls the {@link EsTagIndexer} for logging these to ElasticSearch.
 * @author Alban Marguet
 */
@Service
@Slf4j
public class EsTagLogListener implements BufferedTimCacheListener<Tag>, SmartLifecycle {

  /** Reference to registration service. */
  private final CacheRegistrationService cacheRegistrationService;

  /** Allows to convert from Tag to {@link EsTagImpl}. */
  private final EsTagLogConverter esTagLogConverter;

  /** The {@link EsTagIndexer} allows to use the connection to the ElasticSearch cluster and to write data. */
  private final IPersistenceManager persistenceManagerTagNumeric;
  private final IPersistenceManager persistenceManagerTagString;
  private final IPersistenceManager persistenceManagerTagBoolean;

  /** Listener container lifecycle hook. */
  private Lifecycle listenerContainer;

  /** Lifecycle flag. */
  private volatile boolean running = false;

  /**
   * Autowired constructor.
   * @param cacheRegistrationService for registering cache listeners
   */
  @Autowired
  public EsTagLogListener(final CacheRegistrationService cacheRegistrationService,
                          @Qualifier("esTagNumericPersistenceManager") final IPersistenceManager persistenceManagerTagNumeric,
                          @Qualifier("esTagStringPersistenceManager") final IPersistenceManager persistenceManagerTagString,
                          @Qualifier("esTagBooleanPersistenceManager") final IPersistenceManager persistenceManagerTagBoolean,
                          final EsTagLogConverter esTagLogConverter) {
    super();
    this.cacheRegistrationService = cacheRegistrationService;
    this.esTagLogConverter = esTagLogConverter;
    this.persistenceManagerTagNumeric = persistenceManagerTagNumeric;
    this.persistenceManagerTagString = persistenceManagerTagString;
    this.persistenceManagerTagBoolean = persistenceManagerTagBoolean;
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
   * @param tagCollection batch of tags to be logged to ElasticSearch
   */
  @Override
  public void notifyElementUpdated(Collection<Tag> tagCollection) {
    log.debug("notifyElementUpdated() - Received a tagCollection of " + tagCollection.size() + " elements.");
    List<Tag> tagsToLog = new ArrayList<>(tagCollection.size());
    List<EsTagImpl> tagNumericCollection = new ArrayList<>();
    List<EsTagImpl> tagStringCollection = new ArrayList<>();
    List<EsTagImpl> tagBooleanCollection = new ArrayList<>();

    retrieveTagsToLog(tagCollection, tagsToLog);
    convertTagsToLogToTagES(tagsToLog, tagNumericCollection, tagStringCollection, tagBooleanCollection);
    sendCollectionsTagESToElasticSearch(tagNumericCollection, tagStringCollection, tagBooleanCollection);
  }

  private void retrieveTagsToLog(Collection<Tag> tagCollection, Collection<Tag> tagsToLog) {
    for (Tag tag : tagCollection) {
      if (tag.isLogged()) {
        tagsToLog.add(tag);
      }
    }
    log.debug("retrieveTagsToLog() - With " + tagsToLog.size() + " tags to be logged.");
  }

  private void convertTagsToLogToTagES(Collection<Tag> tagsToLog,
                                       List<EsTagImpl> tagNumericCollection,
                                       List<EsTagImpl> tagStringCollection,
                                       List<EsTagImpl> tagBooleanCollection) {
    for (Tag tag: tagsToLog) {
      try {
        EsTagImpl esTagImpl = esTagLogConverter.convertToTagES(tag);
        if (esTagImpl instanceof EsTagString) {
          addTagToCollectionIfNotNull(esTagImpl, tagStringCollection);
        }
        else if (esTagImpl instanceof EsTagBoolean) {
          addTagToCollectionIfNotNull(esTagImpl, tagBooleanCollection);
        }
        else if (esTagImpl instanceof EsTagNumeric) {
          addTagToCollectionIfNotNull(esTagImpl, tagNumericCollection);
        }
      } catch (Exception e) {
        log.error("convertTagsToLogToTagES() - Error occurred during tag parsing for ElasticSearch. Tag #" + tag.getId() + " is not added to bulk sending (name=" + tag.getName() + ", value=" + tag.getValue() + ", type=" + tag.getDataType() + ")", e);
      }
    }
  }

  private void addTagToCollectionIfNotNull(EsTagImpl esTagImpl, Collection<EsTagImpl> esTagImplCollection) {
    if (esTagImpl != null) {
      esTagImplCollection.add(esTagImpl);
    }
  }

  private void sendCollectionsTagESToElasticSearch(List<EsTagImpl> tagNumericCollection,
                                                   List<EsTagImpl> tagStringCollection,
                                                   List<EsTagImpl> tagBooleanCollection) {
    try {
      log.debug("sendCollectionTagESToElasticSearch() - send a collection of tagNumeric to indexer of size: " + tagNumericCollection.size());
      persistenceManagerTagNumeric.storeData(tagNumericCollection);

      log.debug("sendCollectionTagESToElasticSearch() - send a collection of tagString to indexer of size: " + tagStringCollection.size());
      persistenceManagerTagString.storeData(tagStringCollection);

      log.debug("sendCollectionTagESToElasticSearch() - send a collection of tagBoolean to indexer of size: " + tagBooleanCollection.size());
      persistenceManagerTagBoolean.storeData(tagBooleanCollection);
    }
    catch(Exception e) {
      log.error("sendCollectionTagESToElasticSearch() - Exception occurred while trying to index data to the ElasticSearch cluster.", e);
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
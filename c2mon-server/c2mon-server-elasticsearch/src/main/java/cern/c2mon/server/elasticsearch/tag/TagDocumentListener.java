/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.elasticsearch.tag;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import cern.c2mon.pmanager.persistence.IPersistenceManager;
import cern.c2mon.server.cache.C2monBufferedCacheListener;
import cern.c2mon.server.cache.CacheRegistrationService;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.elasticsearch.client.ElasticsearchClient;

/**
 * Listens for {@link Tag} updates and converts them to {@link TagDocument}
 * instances before sending them to the {@link IPersistenceManager} responsible
 * for indexing them.
 *
 * @author Alban Marguet
 */
@Component
@Slf4j
public class TagDocumentListener implements C2monBufferedCacheListener<Tag>, SmartLifecycle {

  @Autowired
  private ElasticsearchClient elasticsearchClient;

  @Autowired
  private CacheRegistrationService cacheRegistrationService;

  @Autowired
  @Qualifier("tagDocumentPersistenceManager")
  private IPersistenceManager<TagDocument> persistenceManager;

  @Autowired
  private TagDocumentConverter converter;

  private Lifecycle listenerContainer;

  private volatile boolean running = false;

  @PostConstruct
  public void init() {
    // Register to be notified of all tag updates (data, rule and control tags)
    if (this.elasticsearchClient.getProperties().isEnabled()) {
      listenerContainer = cacheRegistrationService.registerBufferedListenerToTags(this);
    }
  }

  @Override
  public void notifyElementUpdated(final Collection<Tag> tags) {
    if (tags == null) {
      log.warn("Received a null collection of tags");
      return;
    }

    Collection<Tag> loggables = tags.stream().filter(Tag::isLogged).collect(Collectors.toList());
    log.debug("About to log {} tags", loggables.size());

    List<TagDocument> tagDocuments = loggables.stream()
        .map(tag -> converter.convert(tag))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());

    persistenceManager.storeData(tagDocuments);
  }

  @Override
  public void confirmStatus(Collection<Tag> tagCollection) {
    // logic not required
  }

  @Override
  public String getThreadName() {
    return "ElasticsearchPersister";
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
  public boolean isRunning() {
    return running;
  }

  @Override
  public void start() {
    if (this.elasticsearchClient.getProperties().isEnabled()) {
      running = true;
      listenerContainer.start();
    }
  }

  @Override
  public void stop() {
    if (this.elasticsearchClient.getProperties().isEnabled()) {
      listenerContainer.stop();
      running = false;
    }

  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_START_LAST - 1;
  }
}

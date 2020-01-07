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

import cern.c2mon.cache.config.tag.UnifiedTagCacheFacade;
import cern.c2mon.pmanager.persistence.IPersistenceManager;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.CacheEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;

import javax.annotation.PostConstruct;

/**
 * Listens for {@link Tag} updates and converts them to {@link TagDocument}
 * instances before sending them to the {@link IPersistenceManager} responsible
 * for indexing them.
 *
 * @author Alban Marguet
 */
@Component
@Slf4j
public class TagDocumentListener {

  private final ElasticsearchProperties properties;

  private final UnifiedTagCacheFacade unifiedTagCacheFacade;

  @Qualifier("tagDocumentPersistenceManager")
  private final IPersistenceManager<TagDocument> persistenceManager;

  private final TagDocumentConverter converter;

  /**
   * @param properties Elasticsearch properties
   * @param unifiedTagCacheFacade to register respective listener
   * @param persistenceManager to store respective data
   * @param converter to convert the tags
   */
  @Autowired
  public TagDocumentListener(ElasticsearchProperties properties, UnifiedTagCacheFacade unifiedTagCacheFacade, IPersistenceManager<TagDocument> persistenceManager, TagDocumentConverter converter) {
    this.properties = properties;
    this.persistenceManager = persistenceManager;
    this.converter = converter;
    this.unifiedTagCacheFacade = unifiedTagCacheFacade;
  }

  @PostConstruct
  public void init() {
    // Register to be notified of all tag updates (data, rule and control tags)
    if (properties.isEnabled()) {
      unifiedTagCacheFacade.registerBufferedListener(this::notifyElementUpdated, CacheEvent.UPDATE_ACCEPTED);
    }
  }

  public void notifyElementUpdated(final Collection<Tag> tags) {
    if (tags == null) {
      log.warn("Received a null collection of tags");
      return;
    }

    Collection<Tag> loggables = tags.stream()
        .filter(Tag::isLogged)
        .collect(Collectors.toList());
    log.debug("About to log {} tags", loggables.size());

    List<TagDocument> tagDocuments = loggables.stream()
        .map(tag -> converter.convert(tag))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());

    persistenceManager.storeData(tagDocuments);
  }

}

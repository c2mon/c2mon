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
package cern.c2mon.server.elasticsearch.tag.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cern.c2mon.server.elasticsearch.IndexManager;
import cern.c2mon.server.elasticsearch.MappingFactory;
import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import cern.c2mon.server.elasticsearch.domain.IndexMetadata;

/**
 * This class manages the indexing of {@link TagConfigDocument} instances to
 * the Elasticsearch cluster.
 *
 * @author Szymon Halastra
 * @author Justin Lewis Salmon
 * @author Serhiy Boychenko
 */
@Slf4j
@Component
public class TagConfigDocumentIndexer {

  private final String configIndex;
  private final IndexManager indexManager;

  /**
   * @param properties   of Elasticsearch server the application is communicating with.
   * @param indexManager to perform index-related operations.
   */
  @Autowired
  public TagConfigDocumentIndexer(ElasticsearchProperties properties, IndexManager indexManager) {
    this.indexManager = indexManager;
    this.configIndex = properties.getTagConfigIndex();
  }

  /**
   * Stores {@link TagConfigDocument} into related index.
   *
   * @param tag to be indexed.
   */
  public void indexTagConfig(final TagConfigDocument tag) {
    IndexMetadata indexMetadata = IndexMetadata.builder().name(configIndex).id(tag.getId()).routing(tag.getId()).build();

    if (!indexManager.exists(indexMetadata)) {
      indexManager.create(indexMetadata, MappingFactory.createTagConfigMapping());
    }

    if (!indexManager.index(indexMetadata, tag.toString())) {
      log.error("Could not index '#{}' to index '{}'.", tag.getId(), configIndex);
    }
  }

  /**
   * Updates {@link TagConfigDocument} (index and/or document is created in case it does not exist).
   *
   * @param tag to be updated.
   */
  public void updateTagConfig(TagConfigDocument tag) {
    IndexMetadata indexMetadata = IndexMetadata.builder().name(configIndex).id(tag.getId()).build();

    if (!indexManager.exists(indexMetadata)) {
      indexManager.create(indexMetadata, MappingFactory.createTagConfigMapping());
    }

    indexManager.update(indexMetadata, tag.toString());
  }

  /**
   * Deletes {@link TagConfigDocument}.
   *
   * @param tagId of the document to be deleted.
   */
  public void removeTagConfigById(final Long tagId) {
    IndexMetadata indexMetadata =
        IndexMetadata.builder().name(configIndex).id(String.valueOf(tagId)).routing(String.valueOf(tagId)).build();

    if (!indexManager.exists(indexMetadata)) {
      return;
    }

    indexManager.delete(indexMetadata);
  }
}

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

import java.util.Collections;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cern.c2mon.pmanager.IDBPersistenceHandler;
import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
import cern.c2mon.server.elasticsearch.IndexManager;
import cern.c2mon.server.elasticsearch.IndexNameManager;
import cern.c2mon.server.elasticsearch.MappingFactory;
import cern.c2mon.server.elasticsearch.bulk.BulkProcessorProxy;
import cern.c2mon.server.elasticsearch.client.ElasticsearchClient;
import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import cern.c2mon.server.elasticsearch.domain.IndexMetadata;

/**
 * This class manages the fallback-aware indexing of {@link TagDocument}
 * instances to the Elasticsearch cluster.
 *
 * @author Alban Marguet
 * @author Justin Lewis Salmon
 * @author Serhiy Boychenko
 */
@Slf4j
@Component
public class TagDocumentIndexer implements IDBPersistenceHandler<TagDocument> {

  private final IndexManager indexManager;
  private final IndexNameManager indexNameManager;

  private BulkProcessorProxy bulkProcessor;

  /**
   * @param client Elasticsearch client instance
   * @param properties Elasticsearch properties
   * @param indexNameManager to determine index names.
   * @param indexManager to execute index-related operations.
   */
  @Autowired
  public TagDocumentIndexer(ElasticsearchClient client, ElasticsearchProperties properties, IndexNameManager indexNameManager, IndexManager indexManager) {
    this.indexNameManager = indexNameManager;
    this.indexManager = indexManager;
    if (properties.isEnabled()) {
      bulkProcessor = new BulkProcessorProxy(client);
    }
  }

  @Override
  public void storeData(TagDocument tag) throws IDBPersistenceException {
    storeData(Collections.singletonList(tag));
  }

  @Override
  public void storeData(List<TagDocument> tags) throws IDBPersistenceException {
    try {
      log.debug("Trying to send a batch of size {}", tags.size());
      tags.forEach(this::indexTag);

      bulkProcessor.flush();
    } catch (Exception e) {
      log.warn("Error indexing batch", e);
      throw new IDBPersistenceException(e);
    }
  }

  private void indexTag(TagDocument tag) {
    String index = getOrCreateIndex(tag);

    log.trace("Indexing tag (#{}, index={}, type={})", tag.getId(), index, ElasticsearchProperties.TYPE);

    IndexRequest indexNewTag = new IndexRequest(index, ElasticsearchProperties.TYPE)
        .source(tag.toString(), XContentType.JSON)
        .routing(tag.getId());

    bulkProcessor.add(indexNewTag);
  }

  private String getOrCreateIndex(TagDocument tag) {
    IndexMetadata indexMetadata = IndexMetadata.builder().name(indexNameManager.indexFor(tag)).build();

    if (!indexManager.exists(indexMetadata)) {
      indexManager.create(indexMetadata, MappingFactory.createTagMapping());
    }

    return indexMetadata.getName();

  }

  @Override
  public String getDBInfo() {
    return "elasticsearch/tag";
  }
}

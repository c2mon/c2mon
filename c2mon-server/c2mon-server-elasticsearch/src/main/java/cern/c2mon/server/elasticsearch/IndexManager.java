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
package cern.c2mon.server.elasticsearch;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cern.c2mon.server.elasticsearch.client.ElasticsearchClient;
import cern.c2mon.server.elasticsearch.domain.IndexMetadata;

/**
 * Rest-based (check
 * <a href="https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/index.html>
 * Elasticsearch Documentation</a> for more details) supported index-related operations manager.
 *
 * @author James Hamilton
 * @author Serhiy Boychenko
 */
@Slf4j
@Component
public class IndexManager {

  private final List<String> indexCache = new CopyOnWriteArrayList<>();

  private final ElasticsearchClient client;

  /**
   * @param client {@link ElasticsearchClient} client instance.
   */
  @Autowired
  public IndexManager(ElasticsearchClient client) {
    this.client = client;
  }

  /**
   * Create a new index with an initial mapping.
   *
   * @param indexMetadata with details of index to be created
   * @param mapping the mapping source.
   * @return true if the index was successfully created, false otherwise.
   */
  public boolean create(IndexMetadata indexMetadata, String mapping) {
    synchronized (IndexManager.class) {
      if (exists(indexMetadata)) {
        return true;
      }

      boolean created = client.createIndex(indexMetadata, mapping);

      client.waitForYellowStatus();

      if (created) {
        indexCache.add(indexMetadata.getName());
      }

      return created;
    }
  }

  /**
   * Store document with relation to specific index.
   *
   * @param indexMetadata with details of index to write the data
   * @param data data to be written into the index
   * @return true if the document was successfully indexed, false otherwise.
   */
  public boolean index(IndexMetadata indexMetadata, String data) {
    synchronized (IndexManager.class) {
      boolean indexed = client.indexData(indexMetadata, data);

      client.waitForYellowStatus();

      return indexed;
    }
  }

  /**
   * Check if a given index exists.
   * <p>
   * The node-local index cache will be searched first before querying
   * Elasticsearch directly.
   *
   * @param indexMetadata index metadata to check if it exists
   * @return true if the index exists, false otherwise.
   */
  public boolean exists(IndexMetadata indexMetadata) {
    synchronized (IndexManager.class) {
      if (indexCache.contains(indexMetadata.getName())) {
        return true;
      }

      IndexMetadata.builder().name(indexMetadata.getName()).routing(indexMetadata.getRouting()).build();

      if (client.isIndexExisting(indexMetadata)) {
        indexCache.add(indexMetadata.getName());
        return true;
      }

      return false;
    }
  }

  /**
   * Update indexed document (document will be created if not existing).
   *
   * @param indexMetadata to be updated
   * @param data of the new document to be written
   * @return true if index was successfully updated, false otherwise.
   */
  public boolean update(IndexMetadata indexMetadata, String data) {
    synchronized (IndexManager.class) {
      boolean updated = client.updateIndex(indexMetadata, data);

      client.waitForYellowStatus();

      return updated;
    }
  }

  /**
   * Delete an index in Elasticsearch.
   *
   * @param indexMetadata to be deleted
   * @return true if index was successfully deleted, false otherwise.
   */
  public boolean delete(IndexMetadata indexMetadata) {
    synchronized (IndexManager.class) {
      indexCache.remove(indexMetadata.getName());
      boolean deleted = client.deleteIndex(indexMetadata);
      client.waitForYellowStatus();
      return deleted;
    }
  }

  /**
   * Removes all cached components from index cache.
   */
  void purgeIndexCache() {
    synchronized (IndexManager.class) {
      indexCache.clear();
    }
  }
}

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

/**
 * Defines all supported Elasticsearch index-related operations.
 *
 * @author Serhiy Boychenko
 */
public interface IndexManager {

  /**
   * Type is being removed in Elasticsearch 6.x (check
   * <a href="https://www.elastic.co/guide/en/elasticsearch/reference/master/removal-of-types.html">Elasticsearch
   * documentation</a> for more details).
   */
  String TYPE = "doc";

  /**
   * Create a new index with an initial mapping.
   *
   * @param indexName the name of the index to create.
   * @param mapping   the mapping source.
   * @return true if the index was successfully created, false otherwise.
   */
  boolean create(String indexName, String mapping);

  /**
   * Store document with relation to specific index.
   *
   * @param indexName to relate the document with.
   * @param source    of the document.
   * @param routing   representing particular shard.
   * @return true if the document was successfully indexed, false otherwise.
   */
  boolean index(String indexName, String source, String routing);

  /**
   * Store document with relation to specific index.
   *
   * @param indexName to relate the document with.
   * @param source    of the document.
   * @param id        of the document.
   * @param routing   representing particular shard.
   * @return true if the document was successfully indexed, false otherwise.
   */
  boolean index(String indexName, String source, String id, String routing);

  /**
   * Check if a given index exists.
   * <p>
   * The node-local index cache will be searched first before querying
   * Elasticsearch directly.
   *
   * @param indexName the name of the index
   * @return true if the index exists, false otherwise.
   */
  boolean exists(String indexName);

  /**
   * Check if a given index exists.
   * <p>
   * The node-local index cache will be searched first before querying
   * Elasticsearch directly.
   *
   * @param indexName to check if it exists.
   * @param routing   representing particular shard.
   * @return true if the index exists, false otherwise.
   */
  boolean exists(String indexName, String routing);

  /**
   * Update indexed document (document will be created if not existing).
   *
   * @param indexName to update its document.
   * @param source    of the new document.
   * @param id        of the old (existing) document.
   * @return true if index was successfully updated, false otherwise.
   */
  boolean update(String indexName, String source, String id);

  /**
   * Delete an index in Elasticsearch.
   *
   * @param indexName to be deleted.
   * @param id        of existing document.
   * @param routing   representing particular shard.
   * @return true if index was successfully deleted, false otherwise.
   */
  boolean delete(String indexName, String id, String routing);

  /**
   * Removes all cached components from index cache.
   */
  void purgeIndexCache();
}

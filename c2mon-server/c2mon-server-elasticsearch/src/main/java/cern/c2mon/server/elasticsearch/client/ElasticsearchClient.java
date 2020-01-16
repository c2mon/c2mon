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
package cern.c2mon.server.elasticsearch.client;

import org.elasticsearch.action.bulk.BulkProcessor;

import cern.c2mon.server.elasticsearch.domain.IndexMetadata;

/**
 * Defines an interface for Elasticsearch client-cluster communication.
 *
 * @author Serhiy Boychenko
 */
public interface ElasticsearchClient {
  /**
   * Set up the {@link BulkProcessor} for provided {@link BulkProcessor.Listener}.
   *
   * @param listener to be associated with {@link BulkProcessor}
   * @return BulkProcessor instance
   */
  BulkProcessor getBulkProcessor(BulkProcessor.Listener listener);

  /**
   * Creates an index
   *
   * @param indexMetadata with details for index to be created
   * @param mapping to be associated with index
   * @return true if index was created, false otherwise
   */
  boolean createIndex(IndexMetadata indexMetadata, String mapping);

  /**
   * Write data to the provided index
   *
   * @param indexMetadata with details for index to write the data
   * @param data to be written to the index
   * @return true if index was written, false otherwise
   */
  boolean indexData(IndexMetadata indexMetadata, String data);

  /**
   * Check if index exists
   *
   * @param indexMetadata with details for index to check if it exists
   * @return true if index exists, false otherwise
   */
  boolean isIndexExisting(IndexMetadata indexMetadata);

  /**
   * Update an index
   *
   * @param indexMetadata with details for index to be updated
   * @param data to be written to the index
   * @return true if index was updated, false otherwise
   */
  boolean updateIndex(IndexMetadata indexMetadata, String data);

  /**
   * Delete an index
   *
   * @param indexMetadata with details for index to be deleted
   * @return true if index was deleted, false otherwise
   */
  boolean deleteIndex(IndexMetadata indexMetadata);

  /**
   * Block and wait for the cluster to become yellow.
   */
  void waitForYellowStatus();

  /**
   * @return true if Elasticsearch cluster is healthy.
   */
  boolean isClusterYellow();

  /**
   * Closes client connection.
   */
  void close();
}

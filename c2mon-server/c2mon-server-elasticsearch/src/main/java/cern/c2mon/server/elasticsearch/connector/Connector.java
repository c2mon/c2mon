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
package cern.c2mon.server.elasticsearch.connector;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;

import cern.c2mon.server.elasticsearch.structure.types.EsAlarm;
import cern.c2mon.server.elasticsearch.structure.types.EsSupervisionEvent;

/**
 * Handle the connection/querying data to Elasticsearch cluster.
 *
 * @author Alban Marguet
 */
public interface Connector {

  /**
   * Close the communication with an Elasticsearch cluster.
   *
   * @param client of the Elasticsearch cluster.
   */
  void close(Client client);

  /**
   * Wait for the good status to be able to fetch data after ingestion.
   */
  void refreshClusterStats();

  /**
   * The client connected to Elasticsearch.
   */
  Client getClient();

  /**
   * The name of the Elasticsearch cluster.
   */
  String getCluster();

  /**
   * @return the {@link BulkProcessor} instance
   */
  BulkProcessor getBulkProcessor();

  /**
   * @return true if C2MON is connected to an Elasticsearch cluster. (client can communicate with it)
   * It means that initTestPass() return true.
   */
  boolean isConnected();

  /**
   * Used to insert a new index
   *
   * @param indexName the Elasticsearch index name that shall be created
   * @return true, if index creation was successful or index already exists
   */
  boolean createIndex(String indexName);

  /**
   * Create a new QueryIndexBuilder and execute it to create a new index with name {@param indexName} and
   *
   * @param index the Elasticsearch index under which the mapping shall be created.
   * @param type the mapping type
   * @param mapping JSON string
   * @return true, if acknowledged by the cluster.
   */
  boolean createIndexTypeMapping(String index, String type, String mapping);

  /**
   * Allows to add a new SupervisionEvent to Elasticsearch.
   *
   * @param indexName          to which add the data.
   * @param mapping            as JSON.
   * @param esSupervisionEvent the data.
   * @return true if acknowledged by the cluster.
   */
  boolean logSupervisionEvent(String indexName, String mapping, EsSupervisionEvent esSupervisionEvent);

  /**
   * Allows to add a new EsAlarm to Elasticsearch.
   *
   * @param indexName to which add the data.
   * @param mapping   as JSON.
   * @param esAlarm   the data.
   * @return true if acknowledged by the cluster.
   */
  boolean logAlarmEvent(String indexName, String mapping, EsAlarm esAlarm);

  /**
   * Allows to add data by batches to the Elasticsearch cluster thanks to a BulkProcessor.
   *
   * @return response of the BulkProcessor.
   */
  boolean bulkAdd(IndexRequest indexNewTag);

  /**
   * Wait for all operations to be taken into account.
   */
  boolean waitForYellowStatus();
}

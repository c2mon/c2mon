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

import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;

/**
 * Defines an interface for Elasticsearch client-cluster communication.
 *
 * @param <T> type of the client to communicate with Elasticsearch cluster.
 * @author Serhiy Boychenko
 */
public interface ElasticsearchClient<T> {

  /**
   * Block and wait for the cluster to become yellow.
   */
  void waitForYellowStatus();

  /**
   * Closes client connection.
   */
  void close();

  /**
   * @return properties used by client to communicate with Elasticsearch cluster.
   */
  ElasticsearchProperties getProperties();

  /**
   * @return client used to communicate with Elasticsearch cluster.
   */
  T getClient();

  /**
   * @return true if Elasticsearch cluster is healthy.
   */
  boolean isClusterYellow();
}

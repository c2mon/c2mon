/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
 * <p/>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p/>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.elasticsearch.client;

import java.io.IOException;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.node.NodeValidationException;

public interface ElasticsearchClient {
  void waitForYellowStatus();

  ClusterHealthResponse getClusterHealth();

  //@TODO "using Node directly within an application is not officially supported"
  //https://www.elastic.co/guide/en/elasticsearch/reference/5.5/breaking_50_java_api_changes.html
  //@TODO Embedded ES is no longer supported
  void startEmbeddedNode() throws NodeValidationException;

  void close();

  void closeEmbeddedNode() throws IOException;

  cern.c2mon.server.elasticsearch.config.ElasticsearchProperties getProperties();

  org.elasticsearch.client.Client getClient();

  boolean isClusterYellow();
}

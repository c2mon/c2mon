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
import org.elasticsearch.client.Client;
import org.elasticsearch.node.NodeValidationException;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;

public class ElasticsearchClientDummyImpl implements ElasticsearchClient {

  @Autowired
  private ElasticsearchProperties properties;

  @Override
  public void waitForYellowStatus() {
    throw new ElasticsearchClientNotAvailable();
  }

  @Override
  public ClusterHealthResponse getClusterHealth() {
    throw new ElasticsearchClientNotAvailable();
  }

  @Override
  public void startEmbeddedNode() throws NodeValidationException {
    throw new ElasticsearchClientNotAvailable();
  }

  @Override
  public void close() {
    throw new ElasticsearchClientNotAvailable();
  }

  @Override
  public void closeEmbeddedNode() throws IOException {
    throw new ElasticsearchClientNotAvailable();
  }

  @Override
  public ElasticsearchProperties getProperties() {
    return this.properties;
  }

  @Override
  public Client getClient() {
    throw new ElasticsearchClientNotAvailable();
  }

  @Override
  public boolean isClusterYellow() {
    throw new ElasticsearchClientNotAvailable();
  }
}

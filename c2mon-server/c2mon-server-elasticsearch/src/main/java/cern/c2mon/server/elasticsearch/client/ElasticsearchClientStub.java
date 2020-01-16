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
 * An {@link ElasticsearchClient} implementation to be used to ensure that no communication is happening with the server.
 *
 * @author Serhiy Boychenko
 */
public class ElasticsearchClientStub implements ElasticsearchClient {

  public static final String STUB_CLIENT_ERROR_MESSAGE = "Elasticsearch client stub does not support any operations.";

  @Override
  public BulkProcessor getBulkProcessor(BulkProcessor.Listener listener) {
    throw new UnsupportedOperationException(STUB_CLIENT_ERROR_MESSAGE);
  }

  @Override
  public boolean createIndex(IndexMetadata indexMetadata, String mapping) {
    throw new UnsupportedOperationException(STUB_CLIENT_ERROR_MESSAGE);
  }

  @Override
  public boolean indexData(IndexMetadata indexMetadata, String data) {
    throw new UnsupportedOperationException(STUB_CLIENT_ERROR_MESSAGE);
  }

  @Override
  public boolean isIndexExisting(IndexMetadata indexMetadata) {
    throw new UnsupportedOperationException(STUB_CLIENT_ERROR_MESSAGE);
  }

  @Override
  public boolean updateIndex(IndexMetadata indexMetadata, String data) {
    throw new UnsupportedOperationException(STUB_CLIENT_ERROR_MESSAGE);
  }

  @Override
  public boolean deleteIndex(IndexMetadata indexMetadata) {
    throw new UnsupportedOperationException(STUB_CLIENT_ERROR_MESSAGE);
  }

  @Override
  public void waitForYellowStatus() {
    throw new UnsupportedOperationException(STUB_CLIENT_ERROR_MESSAGE);
  }

  @Override
  public boolean isClusterYellow() {
    throw new UnsupportedOperationException(STUB_CLIENT_ERROR_MESSAGE);
  }

  @Override
  public void close() {
    throw new UnsupportedOperationException(STUB_CLIENT_ERROR_MESSAGE);
  }
}

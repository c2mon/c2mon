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
package cern.c2mon.server.elasticsearch.bulk;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.springframework.util.Assert;

import cern.c2mon.server.elasticsearch.client.ElasticsearchClient;

/**
 * Wrapper around {@link BulkProcessor}. If a bulk operation fails, this class
 * will throw a {@link RuntimeException}.
 *
 * @author Serhiy Boychenko
 */
@Slf4j
public class BulkProcessorProxy implements BulkProcessor.Listener {

  private final BulkProcessor bulkProcessor;

  /**
   * @param client to be used to communicate with Elasticsearch cluster.
   */
  public BulkProcessorProxy(ElasticsearchClient client) {
    this.bulkProcessor = client.getBulkProcessor(this);
  }

  /**
   * Allows to perform bulk {@link IndexRequest}s.
   *
   * @param request to be executed in bulk action.
   */
  public void add(IndexRequest request) {
    Assert.notNull(request, "IndexRequest must not be null!");
    bulkProcessor.add(request);
  }

  /**
   * flushes the data to server
   */
  public void flush() {
    bulkProcessor.flush();
  }

  @Override
  public void beforeBulk(long executionId, BulkRequest request) {
    log.debug("Going to execute new bulk operation composed of {} actions", request.numberOfActions());
  }

  @Override
  public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
    log.debug("Executed bulk operation composed of {} actions", request.numberOfActions());
  }

  @Override
  public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
    log.warn("Error executing bulk operation", failure);
    throw new IllegalStateException(failure);
  }
}

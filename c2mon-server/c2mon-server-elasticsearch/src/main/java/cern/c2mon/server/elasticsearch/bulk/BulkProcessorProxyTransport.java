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
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import cern.c2mon.server.elasticsearch.client.ElasticsearchClientTransport;

/**
 * Wrapper around {@link BulkProcessor}. If a bulk operation fails, this class
 * will throw a {@link RuntimeException}.
 *
 * @author Justin Lewis Salmon
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "c2mon.server.elasticsearch.rest", havingValue = "false")
public class BulkProcessorProxyTransport implements BulkProcessor.Listener, BulkProcessorProxy {

  private final BulkProcessor bulkProcessor;

  /**
   * @param client to be used to communicate with Elasticsearch cluster.
   */
  @Autowired
  public BulkProcessorProxyTransport(final ElasticsearchClientTransport client) {
    this.bulkProcessor = BulkProcessor.builder(client.getClient(), this)
        .setBulkActions(client.getProperties().getBulkActions())
        .setBulkSize(new ByteSizeValue(client.getProperties().getBulkSize(), ByteSizeUnit.MB))
        .setFlushInterval(TimeValue.timeValueSeconds(client.getProperties().getBulkFlushInterval()))
        .setConcurrentRequests(client.getProperties().getConcurrentRequests())
        .build();
  }

  @Override
  public void add(IndexRequest request) {
    Assert.notNull(request, "IndexRequest must not be null!");
    bulkProcessor.add(request);
  }

  @Override
  public boolean flush() {
    bulkProcessor.flush();
    return true;
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

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
package cern.c2mon.server.eslog.structure.queries;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;

/**
 * Allows to create a new Index inside the ElasticSearch cluster.
 * @author Alban Marguet.
 */
@Slf4j
public class QueryIndexBuilder extends Query {

  public QueryIndexBuilder(Client client) throws ClusterNotAvailableException {
    super(client);
  }

  public boolean indexNew(String index, String type, String mapping) throws ClusterNotAvailableException {
    if (type == null && mapping == null) {
      return handleAddingIndex(index);
    }
    else {
      return handleAddingMapping(index, type, mapping);
    }
  }

  private boolean handleAddingIndex(String index) throws ClusterNotAvailableException {
    CreateIndexRequestBuilder createIndexRequestBuilder = prepareCreateIndexRequestBuilder(index);
    CreateIndexResponse response = createIndexRequestBuilder.execute().actionGet();
    return response.isAcknowledged();
  }

  private boolean handleAddingMapping(String index, String type, String mapping) throws ClusterNotAvailableException {
    try {
      PutMappingResponse response = client.admin().indices().preparePutMapping(index).setType(type).setSource(mapping).execute().actionGet();
      return response.isAcknowledged();
    }
    catch (Exception e) {
      log.debug("Query - ClusterNotAvailableException");
      throw new ClusterNotAvailableException();
    }
  }
}
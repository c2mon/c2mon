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

import cern.c2mon.server.eslog.logger.Indexer;
import cern.c2mon.server.eslog.logger.TransportConnector;
import com.carrotsearch.hppc.ObjectContainer;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Allows to query the ElasticSearch cluster. Especially to create an index and adding a mapping to it or to add aliases.
 * @author Alban Marguet.
 */
@Slf4j
public abstract class Query {
  @Getter @Setter
  protected Client client;
  protected List<Long> tagIds;
  protected List<String> types;
  protected List<String> indices;

  public Query(Client client) {
    this.client = client;
    indices = new ArrayList<>();
    types = new ArrayList<>();
    tagIds = new ArrayList<>();
  }

  protected String[] getIndicesFromCluster() {
    return client.admin().indices().prepareGetIndex().get().indices();
  }

  protected CreateIndexRequestBuilder prepareCreateIndexRequestBuilder(String index) {
    return client.admin().indices().prepareCreate(index);
  }

  protected IndicesAliasesRequestBuilder prepareAliases() {
    return client.admin().indices().prepareAliases();
  }

  protected Iterator<ObjectCursor<IndexMetaData>> getIndicesWithMetadata() {
    return client.admin().cluster().prepareState().execute().actionGet().getState().getMetaData().indices().values().iterator();
  }

  protected ImmutableOpenMap<String, MappingMetaData> getIndexWithMetadata(String index) {
    return client.admin().cluster().prepareState().execute().actionGet().getState().getMetaData().index(index).getMappings();
  }

  protected ObjectContainer<AliasMetaData> getAliases(String index) {
    return client.admin().cluster().prepareState().execute().actionGet().getState().getMetaData().index(index).getAliases().values();
  }
}

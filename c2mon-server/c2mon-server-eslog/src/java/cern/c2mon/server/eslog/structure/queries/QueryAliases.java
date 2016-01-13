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
import com.carrotsearch.hppc.cursors.ObjectCursor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequestBuilder;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.AliasMetaData;

import java.util.*;

/**
 * @author Alban Marguet.
 */
@Slf4j
public class QueryAliases extends Query {

  public QueryAliases(Client client) {
    super(client);
  }

  public boolean addAlias(String indexMonth, String aliasName) {
    if (client != null) {
      String tagId = aliasName.substring(Indexer.TAG_PREFIX.length());
      String json = "{\"term\" : { \"id\" : " + tagId + " } }";
      IndicesAliasesRequestBuilder preparedAliases = prepareAliases();
      IndicesAliasesResponse response = preparedAliases.addAlias(indexMonth, aliasName, json).execute().actionGet();
      return response.isAcknowledged();
    }
    else {
      log.info("addAlias() - client has null value.");
    }

    return false;
  }

  public List<String> getListOfAnswer(String index) {
    List<String> result = new ArrayList<>();

    if (index != null) {
      Iterator<ObjectCursor<AliasMetaData>> aliasesIt = getAliases(index).iterator();
      addAliasesToResult(aliasesIt, result);
      log.info("QueryAliases - got a list of aliases, size=" + result.size());
    }
    return result;
  }

  private void addAliasesToResult(Iterator<ObjectCursor<AliasMetaData>> aliases, List<String> result) {
    while(aliases.hasNext()) {
      result.add(aliases.next().value.getAlias());
    }
  }
}

/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.eslog.structure.queries;

import cern.c2mon.server.eslog.structure.mappings.Mapping;
import cern.c2mon.server.eslog.structure.types.SupervisionES;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alban Marguet
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class SupervisionQuery extends Query {
  private String routing;
  private String jsonSource;

  public SupervisionQuery(Client client, SupervisionES supervisionES) throws ClusterNotAvailableException {
    super(client);
    routing = String.valueOf(supervisionES.getEntityId());
    jsonSource = supervisionES.toString();
  }

  public boolean logSupervisionEvent(String indexName, String mapping) throws ClusterNotAvailableException {
    if (!indexExists(indexName) && mapping != null) {
      client.admin().indices().prepareCreate(indexName).setSource(mapping).execute().actionGet();
      log.debug("logSupervisionEvent() - Source query is: " + jsonSource + ".");
    }

    if (indexExists(indexName)) {
      log.debug("logSupervisionEvent() - Add new Supervision event to index " + indexName + ".");
      IndexResponse response = client.prepareIndex().setIndex(indexName).setType(Mapping.ValueType.supervisionType.toString()).setSource(jsonSource).setRouting(routing).execute().actionGet();
      return response.isCreated();
    }
    return false;
  }
}
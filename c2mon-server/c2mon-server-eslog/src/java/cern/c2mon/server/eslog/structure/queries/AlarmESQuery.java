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
import cern.c2mon.server.eslog.structure.types.AlarmES;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Alban Marguet
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class AlarmESQuery extends Query {
  private String routing;
  private String jsonSource;

  /**
   * Creates an ElasticSearch query for an Alarm event, create the needed JSON and create the appropriate query.
   */
  public AlarmESQuery(Client client, AlarmES alarmES) throws ClusterNotAvailableException {
    super(client);
    jsonSource = alarmES.toString();
    routing = String.valueOf(alarmES.getAlarmId());
  }

  public boolean logAlarmES(String indexName, String mapping) throws ClusterNotAvailableException {
    log.debug("logAlarmES() - Try to create a writing query for Alarm.");
    if (!indexExists(indexName) && mapping != null) {
      client.admin().indices().prepareCreate(indexName).setSource(mapping).execute().actionGet();
      log.debug("logAlarmES() - Source query is: " + jsonSource + ".");
    }

    if (indexExists(indexName)) {
      log.debug("logAlarmES() - Add new Alarm event to index " + indexName + ".");
      IndexResponse response = client.prepareIndex().setIndex(indexName).setType(Mapping.ValueType.alarmType.toString()).setSource(jsonSource).setRouting(routing).execute().actionGet();
      return response.isCreated();
    }
    return false;
  }
}
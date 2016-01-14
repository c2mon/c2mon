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

import cern.c2mon.shared.client.supervision.SupervisionEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Alban Marguet
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SupervisionQuery extends Query {
  private long id;
  private String entity;
  private long timestamp;
  private String message;
  private String status;
  private Map<String, Object> json;

  public SupervisionQuery(Client client, SupervisionEvent supervisionEvent) {
    super(client);
    json = new HashMap<>();
    getElements(supervisionEvent);
    toJson();
  }

  public boolean logSupervisionEvent(String indexName, String mapping) {
    boolean indexExists = client.admin().indices().prepareExists(indexName).execute().actionGet().isExists();
    if (!indexExists) {
      CreateIndexResponse createIndex = client.admin().indices().prepareCreate(indexName).setSource(mapping).execute().actionGet();
    }

    IndexResponse response = client.prepareIndex().setIndex(indexName).setSource(json).execute().actionGet();
    return response.isCreated();
  }

  public void getElements(SupervisionEvent supervisionEvent) {
    id = supervisionEvent.getEntityId();
    entity = supervisionEvent.getEntity().name();
    timestamp = supervisionEvent.getEventTime().getTime();
    message = supervisionEvent.getMessage();
    status = supervisionEvent.getStatus().name();
  }

  private void toJson() {
    json.put("id", id);
    json.put("entity", entity);
    json.put("timestamp", timestamp);
    json.put("message", message);
    json.put("status", status);
  }
}
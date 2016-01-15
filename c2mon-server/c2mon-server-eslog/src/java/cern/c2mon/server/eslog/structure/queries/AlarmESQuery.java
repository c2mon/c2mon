package cern.c2mon.server.eslog.structure.queries;
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

import cern.c2mon.server.eslog.structure.types.AlarmES;
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
public class AlarmESQuery extends Query {
  private long tagId;
  private long alarmId;
  private String faultFamily;
  private String faultMember;
  private int faultCode;
  private boolean active;
  private int priority;
  private String info;
  private long serverTimestamp;
  private String timeZone;
  private Map<String, Object> json;

  public AlarmESQuery(Client client, AlarmES alarmES) {
    super(client);
    json = new HashMap<>();
    getElements(alarmES);
    toJson();
  }

  public boolean logAlarmES(String indexName, String mapping) {
    boolean indexExists = client.admin().indices().prepareExists(indexName).execute().actionGet().isExists();
    if (!indexExists) {
      client.admin().indices().prepareCreate(indexName).setSource(mapping).execute().actionGet();
    }

    IndexResponse response = client.prepareIndex().setIndex(indexName).setSource(json).execute().actionGet();
    return response.isCreated();
  }

  public void getElements(AlarmES alarmES) {
    tagId = alarmES.getTagId();
    alarmId = alarmES.getAlarmId();
    faultFamily = alarmES.getFaultFamily();
    faultMember = alarmES.getFaultMember();
    faultCode = alarmES.getFaultCode();
    active = alarmES.isActive();
    priority = alarmES.getPriority();
    info = alarmES.getInfo();
    serverTimestamp = alarmES.getServerTimestamp().getTime();
    timeZone = alarmES.getTimezone();
  }

  private void toJson() {
    json.put("tagId", tagId);
    json.put("alarmId", alarmId);
    json.put("faultFamily", faultFamily);
    json.put("faultMember", faultMember);
    json.put("faultCode", faultCode);
    json.put("active", active);
    json.put("priority", priority);
    json.put("info", info);
    json.put("serverTimeStamp", serverTimestamp);
    json.put("timeZone", timeZone);
  }
}

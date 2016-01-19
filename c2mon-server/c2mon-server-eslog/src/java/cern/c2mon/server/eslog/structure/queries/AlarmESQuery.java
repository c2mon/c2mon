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

import cern.c2mon.server.eslog.structure.types.AlarmES;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
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
  private Map<String, Object> jsonSource;

  /**
   * Creates an ElasticSearch query for an Alarm event, create the needed JSON and create the appropriate query.
   */
  public AlarmESQuery(Client client, AlarmES alarmES) {
    super(client);
    jsonSource = new HashMap<>();
    getElements(alarmES);
    toJson();
  }

  public boolean logAlarmES(String indexName, String mapping) {
    log.debug("logAlarmES() - Try to create a writing query for Alarm.");
    if (!indexExists(indexName) && mapping != null) {
      client.admin().indices().prepareCreate(indexName).setSource(mapping).execute().actionGet();
      IndexResponse response = client.prepareIndex().setIndex(indexName).setSource(jsonSource).execute().actionGet();
      log.debug("logAlarmES() - Source query is: " + jsonSource + ".");
      return response.isCreated();
    }
    return false;
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
    jsonSource.put("tagId", tagId);
    jsonSource.put("alarmId", alarmId);
    jsonSource.put("faultFamily", faultFamily);
    jsonSource.put("faultMember", faultMember);
    jsonSource.put("faultCode", faultCode);
    jsonSource.put("active", active);
    jsonSource.put("priority", priority);
    jsonSource.put("info", info);
    jsonSource.put("serverTimeStamp", serverTimestamp);
    jsonSource.put("timeZone", timeZone);
  }
}
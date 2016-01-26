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
package cern.c2mon.server.eslog.structure.types;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an Alarm Event for ElasticSearch.
 * @author Alban Marguet
 */
@Slf4j
@Data
public class AlarmES {
  private long tagId;
  private long alarmId;

  private String faultFamily;
  private String faultMember;
  private int faultCode;

  private boolean active;
  private String activity;
  private double activeNumeric;
  private int priority;
  private String info;

  private long serverTimestamp;
  private String timezone;
  private transient Map<String, String> metadata = new HashMap<>();

  @Override
  public String toString() {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    JsonObject tagESAsTree = gson.toJsonTree(this).getAsJsonObject();
    addMetadata(tagESAsTree);
    String json = gson.toJson(tagESAsTree);
    log.debug(json);
    return json;
  }

  public void addMetadata(JsonObject tagESAsTree) {
    if (tagESAsTree != null && metadata != null) {
      for (String key : metadata.keySet()) {
        tagESAsTree.addProperty(key, metadata.get(key));
      }
    }
  }
}
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
package cern.c2mon.server.eslog.structure.types;

import cern.c2mon.pmanager.IFallback;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Class that represents a Tag for ElasticSearch.
 * Used as "type" in ElasticSearch.
 * @author Alban Marguet.
 */
@Slf4j
@Data
public abstract class TagES implements TagESInterface, IFallback {
  private transient Gson GSON = new GsonBuilder().create();
  private long id;
  private String name;
  private String dataType;
  private long sourceTimestamp;
  private long serverTimestamp;
  private long daqTimestamp;
  private int status;
  private String quality; //tagstatusdesc
  private Boolean valid; //if quality is OK or not
  protected transient Object value;
  protected Boolean valueBoolean;
  protected String valueString;
  protected Number valueNumeric;
  private String valueDescription;
  private transient Map<String, String> metadata;
  
  private String process;
  private String equipment;
  private String subEquipment;

  abstract public void setValue(Object tagValue);

  @Override
  public String toString() {
    JsonObject tagESAsTree = GSON.toJsonTree(this).getAsJsonObject();
    addMetadata(tagESAsTree);
    String json = GSON.toJson(tagESAsTree);
    log.debug(json);
    return json;
  }

  @Override
  public IFallback getObject(String line) {
    return GSON.fromJson(line, TagBoolean.class);
  }

  @Override
  public String getId() {
    return String.valueOf(id);
  }

  public long getIdAsLong() {
    return id;
  }

  public void addMetadata(JsonObject tagESAsTree) {
    if (tagESAsTree != null && metadata != null) {
      for (String key : metadata.keySet()) {
        tagESAsTree.addProperty(key, metadata.get(key));
      }
    }
  }
}
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;

/**
 * Class that represents a Tag for ElasticSearch.
 * Used as "type" in ElasticSearch.
 * @author Alban Marguet.
 */
@Slf4j
@Data
public abstract class TagES implements TagESInterface {
  private long id;
  private String name;
  private String dataType;
  private long sourceTimestamp;
  private long serverTimestamp;
  private long daqTimestamp;
  private int status;
  private String quality; //tagstatusdesc
  protected transient Object value;
  protected Boolean valueBoolean;
  protected String valueString;
  protected Number valueNumeric;
  private String valueDescription;

  //TODO: be able to discover what is inside and create a static structure for it.
  private transient Map<String, Object> metadata;
  
  private String process;
  private String equipment;
  private String subEquipment;

  abstract public void setValue(Object tagValue);

  public String build() {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    String json = gson.toJson(this);
    log.debug(json);
    return json;
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    str.append(getId());
    str.append('\t');
    str.append(getName());
    str.append('\t');
    str.append(getValue());
    str.append('\t');
    str.append(getValueDescription());
    str.append('\t');
    str.append(getDataType());
    str.append('\t');
    str.append(getSourceTimestamp());
    str.append('\t');
    str.append(getDaqTimestamp());
    str.append('\t');
    str.append(getServerTimestamp());
    str.append('\t');
    str.append(getStatus());
    str.append('\t');
    str.append(getQualityAppend(str));
    str.append('\t');
    str.append(getProcess());
    str.append('\t');
    str.append(getEquipment());
    str.append('\t');
    str.append(getSubEquipment());
    return str.toString();
  }

  private String getQualityAppend(StringBuilder str) {
    boolean qualityIsEmpty = (getQuality() != null) && (getQuality().equals(""));
    if (qualityIsEmpty) {
      return "nullQuality";
    } else {
      return getQuality();
    }
  }

  private String serializeToString(Object object) {
    try {
      ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(arrayOutputStream);
      objectOutputStream.writeObject(object);
      objectOutputStream.flush();
      return new String(arrayOutputStream.toByteArray());
    }
    catch (IOException e) {
      log.warn("serializeToString() - Could not get String output for TagES id " + id);
      return null;
    }
  }
}

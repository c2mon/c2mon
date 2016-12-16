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
package cern.c2mon.shared.client.metadata;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * @author Franz Ritter
 */
@Data
@Slf4j
public class Metadata implements Serializable, Cloneable {

  private Map<String, Object> metadata = new HashMap<>();
  private List<String> removeList = new ArrayList<>();
  private boolean update = false;

  private static transient ObjectMapper mapper = new ObjectMapper();

  public static String toJSON(Metadata metadata) {
    try {
      return mapper.writeValueAsString(metadata);
    } catch (IOException e) {
      log.error("Exception caught while serializing metatata to JSON", e);
    }

    return null;
  }

  public static Metadata fromJSON(String json) {
    try {
      TypeReference<Metadata> typeRef = new TypeReference<Metadata>() {};
      return mapper.readValue(json, typeRef);
    } catch (IOException e) {
      log.error("Exception caught while deserializing metatata from JSON", e);
    }

    return null;
  }

  public void addMetadata(String key, Object value) {
    metadata.put(key, value);
  }

  public void updateMetadata(String key, Object value) {
    update = true;
    metadata.put(key, value);
  }

  public void addToRemoveList(String key) {
    update = true;
    removeList.add(key);
  }
}

/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.elasticsearch.alarm;

import java.io.IOException;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cern.c2mon.pmanager.IFallback;
import cern.c2mon.pmanager.fallback.exception.DataFallbackException;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.elasticsearch.exception.ProcessingException;

/**
 * Intermediate object (created from {@link Alarm} instances) used for direct
 * serialization to JSON objects suitable for indexing as Elasticsearch
 * documents.
 *
 * @author Alban Marguet
 * @author Justin Lewis Salmon
 */
public class AlarmDocument extends HashMap<String, Object> implements IFallback {

  private static final ObjectMapper mapper = new ObjectMapper();

  @Override
  public Object put(String key, Object value) {
    if ("timestamp".equals(key) || "sourceTimestamp".equals(key)) {
      value = Long.valueOf(value.toString());
    }
    return super.put(key, value);
  }

  @Override
  public String getId() {
    return String.valueOf(this.get("id"));
  }

  @Override
  public IFallback getObject(String line) throws DataFallbackException {
    try {
      return mapper.readValue(line, AlarmDocument.class);
    } catch (IOException e) {
      throw new DataFallbackException("Error reading line from fallback", e);
    }
  }

  @Override
  public String toString() {
    try {
      return mapper.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new ProcessingException("Error serializing document", e);
    }
  }
}

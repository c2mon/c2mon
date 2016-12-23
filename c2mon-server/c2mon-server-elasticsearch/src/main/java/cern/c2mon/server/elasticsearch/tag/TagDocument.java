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
package cern.c2mon.server.elasticsearch.tag;

import cern.c2mon.pmanager.IFallback;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author Alban Marguet
 * @author Justin Lewis Salmon
 */
public class TagDocument extends HashMap<String, Object> implements IFallback {

  private static final ObjectMapper mapper = new ObjectMapper();

  @Override
  public String getId() {
    return String.valueOf(this.get("id"));
  }

  @Override
  public IFallback getObject(String line) {
    try {
      return mapper.readValue(line, TagDocument.class);
    } catch (IOException e) {
      throw new RuntimeException("Error reading line from fallback", e);
    }
  }

  @Override
  public String toString() {
    try {
      return mapper.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error serializing document", e);
    }
  }
}

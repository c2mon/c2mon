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
package cern.c2mon.server.eslog.structure.mappings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Mapping used for AlarmES in ElasticSearch.
 * @author Alban Marguet
 */
@Slf4j
@Data
public class AlarmMapping implements Mapping {
  private AlarmProperties mappings;

  /**
   * @return the mapping as JSON String for ElasticSearch.
   */
  public String getMapping() {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    String json = gson.toJson(this);
    log.trace("getMapping() - Created the supervision mapping: " + json);
    return json;
  }

  /**
   * Initialize the mapping according that the valueType is alarm type.
   */
  @Override
  public void setProperties(ValueType valueType) {
    if (ValueType.isAlarm(valueType)) {
      mappings = new AlarmProperties();
    }
    else {
      log.debug("setProperties() - Could not instantiate properties, type is null");
    }
  }
}
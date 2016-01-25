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
package cern.c2mon.server.eslog.structure.mappings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * Allows to create dynamic mappings for the different types that exist in ElasticSearch.
 * @author Alban Marguet.
 */
@Slf4j
public class TagESMapping implements Mapping {
  protected Routing _routing;
  protected Properties properties;

  public TagESMapping() {
    _routing = new Routing();
    log.trace("TagESMapping() - Initialized a mapping with routing.");
  }

  @Override
  public String getMapping() {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    String json = gson.toJson(this);
    log.trace("getMapping() - Created the mapping : " + json);
    return json;
  }

  @Override
  public void setProperties(ValueType valueType) {
    properties = new Properties(valueType);
  }
}
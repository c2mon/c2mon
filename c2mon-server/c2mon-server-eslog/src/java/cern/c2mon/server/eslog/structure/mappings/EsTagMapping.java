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
 * Take care of the basic structure requiring the routing for faster retrieval and the body of the properties.
 * @author Alban Marguet.
 */
@Slf4j
public class EsTagMapping implements EsMapping {
  protected Routing _routing;
  protected Properties properties;

  /** Instantiate a new EsTagMapping by putting a routing required. */
  public EsTagMapping() {
    _routing = new Routing();
    log.trace("EsTagMapping() - Initialized a mapping with routing.");
  }

  /** @return the Mapping as JSON. */
  @Override
  public String getMapping() {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    String json = gson.toJson(this);
    log.trace("getMapping() - Created the mapping : " + json);
    return json;
  }

  /** Set the Properties object according to the {@param valueType} */
  @Override
  public void setProperties(ValueType valueType) {
    properties = new Properties(valueType);
  }
}
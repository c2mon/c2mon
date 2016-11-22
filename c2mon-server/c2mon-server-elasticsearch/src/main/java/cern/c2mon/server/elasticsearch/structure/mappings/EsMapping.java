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
package cern.c2mon.server.elasticsearch.structure.mappings;

import lombok.RequiredArgsConstructor;

/**
 * Defines the Elasticsearch arguments for the types and the indices.
 * Permits to have mappings according to what we want to insert. (dataType)
 * Also contains the Mapping for routing, tags, alarms and supervisionevents.
 *
 * @author Alban Marguet.
 */
public interface EsMapping {

  @RequiredArgsConstructor
  enum ValueType {
    STRING("string"),
    LONG("long"),
    INTEGER("integer"),
    DOUBLE("double"),
    BOOLEAN("boolean"),
    DATE("date"),
    OBJECT("object"),
    NESTED("nested");

    private final String type;

    @Override
    public String toString() {
      return this.type;
    }
  }

  String indexNotAnalyzed = "not_analyzed";
  String indexAnalyzed = "analyzed";
  String epochMillisFormat = "epoch_millis";

  /**
   * @return A JSON representation of the mapping object
   */
  String getMapping();
}

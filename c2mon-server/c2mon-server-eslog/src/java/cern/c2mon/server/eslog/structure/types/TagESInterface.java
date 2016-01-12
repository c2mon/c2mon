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

import java.io.IOException;

import cern.c2mon.server.eslog.structure.mappings.Mapping.ValueType;

/**
 * Interface for the ES Tag format.
 * @author Alban Marguet.
 */
public interface TagESInterface {
  /**
   * Build the JSON format of the Java TagES object for ElasticSearch to add its data.
   * @return the JSON format.
   * @throws IOException
   */
  String build() throws IOException;

  /**
   * Override the toString() method to display the structure of a TagES.
   */
  String toString();

  /**
   * @return the value of a TagES.
   */
  Object getValue();

  /**
   * Set the Object value but also the valueNumeric, valueBoolean or valueString depending on the class sof the child.
   * @param tagValue to give to a TagES.
   */
  void setValue(Object tagValue);
}

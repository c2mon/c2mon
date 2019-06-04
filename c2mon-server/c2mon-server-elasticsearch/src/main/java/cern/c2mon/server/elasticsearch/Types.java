/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.elasticsearch;

/**
 * Static utility singleton for working with Elasticsearch types.
 *
 * @author Justin Lewis Salmon
 */
public class Types {

  /**
   * Retrieve the corresponding Elasticsearch type of the given Java type.
   *
   * @param dataType the Java type (fully-qualified or simple)
   *
   * @return the corresponding Elasticsearch type
   */
  public static String of(String dataType) {
    String type = dataType.toLowerCase();

    if (dataType.contains(".")) {
      type = type.substring(type.lastIndexOf('.') + 1);
    }

    return "type_" + type;
  }
}

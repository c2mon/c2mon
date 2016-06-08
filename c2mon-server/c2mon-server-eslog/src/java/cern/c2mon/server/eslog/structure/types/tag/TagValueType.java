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
package cern.c2mon.server.eslog.structure.types.tag;

import cern.c2mon.server.common.tag.Tag;
import lombok.Getter;

/**
 * An enumeration with all the supported data types
 * that a {@link Tag}'s value can be represented.
 */
public enum TagValueType {
  BOOLEAN("boolean"),
  NUMERIC("numeric"),
  STRING("string"),
  OBJECT("object");


  /**
   * The friendly (readable) value of a {@link Tag}'s value type
   */
  @Getter
  private final String friendlyName;

  TagValueType(String friendlyType) {
    this.friendlyName = friendlyType;
  }

  @Override
  public String toString() {
    return this.friendlyName;
  }


}

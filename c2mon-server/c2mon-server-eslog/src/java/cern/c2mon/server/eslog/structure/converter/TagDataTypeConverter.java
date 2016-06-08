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
package cern.c2mon.server.eslog.structure.converter;

import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.eslog.structure.types.tag.TagValueType;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a {@link Converter} for C2MON {@link Tag}'s value
 * dataType to {@link TagValueType}
 */
public class TagDataTypeConverter implements Converter<String, TagValueType> {

  /**
   * Contains the definitions for various {@link Tag}'s value dataTypes
   * to their corresponding {@link TagValueType}s
   */
  private final Map<String, TagValueType> mappingDefinitions = new HashMap<>();

  public TagDataTypeConverter addDefinition(final String dataType, final TagValueType valueType) {
    if (StringUtils.isBlank(dataType)) {
      return this;
    }

    if (valueType == null) {
      this.mappingDefinitions.put(dataType, TagValueType.OBJECT);
      return this;
    }

    this.mappingDefinitions.put(dataType, valueType);

    return this;
  }

  /**
   * Converts the {@link Tag}'s value dataType to a {@link TagValueType}
   *
   * @param dataType the {@link Tag}'s value type
   * @return {@link TagValueType} that corresponds to the
   * provided dataType, or {@link TagValueType#OBJECT} if there is no match.
   */
  @Override
  public TagValueType convert(final String dataType) {
    if (StringUtils.isBlank(dataType)) {
      return TagValueType.OBJECT;
    }
    return this.mappingDefinitions.getOrDefault(dataType, TagValueType.OBJECT);
  }

}

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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConverterConfiguration {

  /**
   * Registers a configured dataType converter with basic definitions
   *
   * @return A converter for the most common dataTypes of a {@link Tag}'s value.
   */
  @Bean
  public Converter<String, TagValueType> dataTypeConverter() {
    return new TagDataTypeConverter()
        .addDefinition(String.class.getName(), TagValueType.STRING)
        .addDefinition("String", TagValueType.STRING)

        .addDefinition(Boolean.class.getName(), TagValueType.BOOLEAN)
        .addDefinition("Boolean", TagValueType.BOOLEAN)

        .addDefinition(Integer.class.getName(), TagValueType.NUMERIC)
        .addDefinition("Integer", TagValueType.NUMERIC)

        .addDefinition(Long.class.getName(), TagValueType.NUMERIC)
        .addDefinition("Long", TagValueType.NUMERIC)

        .addDefinition(Float.class.getName(), TagValueType.NUMERIC)
        .addDefinition("Float", TagValueType.NUMERIC)

        .addDefinition(Double.class.getName(), TagValueType.NUMERIC)
        .addDefinition("Double", TagValueType.NUMERIC);
  }

}

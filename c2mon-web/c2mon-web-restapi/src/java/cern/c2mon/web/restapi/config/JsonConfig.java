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
package cern.c2mon.web.restapi.config;

import cern.c2mon.client.common.tag.CommandTag;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.ext.history.updates.HistoryTagValueUpdateImpl;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.tag.TagConfig;
import cern.c2mon.web.restapi.serialization.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
public class JsonConfig {

  @Bean
  public Jackson2ObjectMapperBuilder jacksonBuilder() {
    Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();

    // Add custom serializers here
    builder.serializerByType(Tag.class, new TagSerializer());
    builder.serializerByType(TagConfig.class, new TagConfigSerializer());
    builder.serializerByType(AlarmValue.class, new AlarmValueSerializer());
    builder.serializerByType(CommandTag.class, new CommandTagSerializer());
    builder.serializerByType(HistoryTagValueUpdateImpl.class, new HistoryTagValueUpdateSerializer());

    // Enable pretty printing
    builder.indentOutput(true);

    return builder;
  }
}

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

import cern.c2mon.client.core.tag.ClientCommandTagImpl;
import cern.c2mon.client.core.tag.ClientDataTagImpl;
import cern.c2mon.client.ext.history.updates.HistoryTagValueUpdateImpl;
import cern.c2mon.shared.client.alarm.AlarmValueImpl;
import cern.c2mon.web.restapi.serialization.AlarmValueSerializer;
import cern.c2mon.web.restapi.serialization.ClientCommandTagSerializer;
import cern.c2mon.web.restapi.serialization.ClientDataTagSerializer;
import cern.c2mon.web.restapi.serialization.HistoryTagValueUpdateSerializer;
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
    builder.serializerByType(AlarmValueImpl.class, new AlarmValueSerializer());
    builder.serializerByType(ClientDataTagImpl.class, new ClientDataTagSerializer());
    builder.serializerByType(ClientCommandTagImpl.class, new ClientCommandTagSerializer());
    builder.serializerByType(HistoryTagValueUpdateImpl.class, new HistoryTagValueUpdateSerializer());

    // Enable pretty printing
    builder.indentOutput(true);

    return builder;
  }
}

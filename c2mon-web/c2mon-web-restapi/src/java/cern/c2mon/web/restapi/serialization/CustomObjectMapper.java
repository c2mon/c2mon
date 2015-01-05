/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2014 CERN. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.web.restapi.serialization;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.codehaus.jackson.map.ser.CustomSerializerFactory;
import org.springframework.stereotype.Component;

import cern.c2mon.client.core.tag.ClientCommandTagImpl;
import cern.c2mon.client.core.tag.ClientDataTagImpl;
import cern.c2mon.client.ext.history.updates.HistoryTagValueUpdateImpl;
import cern.c2mon.shared.client.alarm.AlarmValueImpl;

/**
 * This class extends the default Jackson {@link ObjectMapper} in order to allow
 * custom serialisation of resources.
 *
 * @author Justin Lewis Salmon
 */
@Component
public class CustomObjectMapper extends ObjectMapper {

  /**
   * Constructor.
   */
  public CustomObjectMapper() {
    super();
    CustomSerializerFactory factory = new CustomSerializerFactory();

    // Add custom serializers here
    factory.addSpecificMapping(AlarmValueImpl.class, new AlarmValueSerializer());
    factory.addSpecificMapping(ClientDataTagImpl.class, new ClientDataTagSerializer());
    factory.addSpecificMapping(ClientCommandTagImpl.class, new ClientCommandTagSerializer());
    factory.addSpecificMapping(HistoryTagValueUpdateImpl.class, new HistoryTagValueUpdateSerializer());

    this.setSerializerFactory(factory);

    // Enable pretty printing
    enable(Feature.INDENT_OUTPUT);
  }
}

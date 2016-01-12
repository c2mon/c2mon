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
package cern.c2mon.web.restapi.serialization;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;


import cern.c2mon.shared.client.alarm.AlarmValueImpl;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Custom serialisation class for {@link AlarmValueImpl} objects.
 *
 * @author Justin Lewis Salmon
 */
public class AlarmValueSerializer extends JsonSerializer<AlarmValueImpl> {

  /*
   * (non-Javadoc)
   *
   * @see
   * org.codehaus.jackson.map.ser.std.SerializerBase#serialize(java.lang.Object, org.codehaus.jackson.JsonGenerator,
   * org.codehaus.jackson.map.SerializerProvider)
   */
  @Override
  public void serialize(AlarmValueImpl value, JsonGenerator generator, SerializerProvider provider) throws IOException, JsonProcessingException {
    generator.writeStartObject();
    generator.writeNumberField("id", value.getId());
    generator.writeNumberField("tagId", value.getTagId());
    generator.writeStringField("tagDescription", value.getTagDescription());
    generator.writeNumberField("faultCode", value.getFaultCode());
    generator.writeStringField("faultFamily", value.getFaultFamily());
    generator.writeStringField("faultMember", value.getFaultMember());
    generator.writeStringField("timestamp", value.getTimestamp().toString());
    generator.writeBooleanField("active", value.isActive());
    generator.writeEndObject();
  }
}

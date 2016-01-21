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

import cern.c2mon.client.common.tag.Tag;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Custom serialisation class for {@link Tag} objects.
 *
 * @author Justin Lewis Salmon
 */
public class TagSerializer extends JsonSerializer<Tag> {

  /*
   * (non-Javadoc)
   *
   * @see
   * org.codehaus.jackson.map.ser.std.SerializerBase#serialize(java.lang.Object, org.codehaus.jackson.JsonGenerator, org.codehaus.jackson.map.SerializerProvider)
   */
  @Override
  public void serialize(Tag value, JsonGenerator generator, SerializerProvider provider) throws IOException, JsonGenerationException {
    generator.writeStartObject();
    generator.writeNumberField("id", value.getId());
    generator.writeStringField("name", value.getName());
    generator.writeStringField("description", value.getDescription());
    generator.writeObjectField("value", value.getValue());
    generator.writeStringField("valueDescription", value.getValueDescription());
    generator.writeStringField("serverTimestamp", value.getServerTimestamp().toString());

    // Sometimes the DAQ timestamp is null
    if (value.getDaqTimestamp() != null) {
      generator.writeStringField("sourceTimestamp", value.getDaqTimestamp().toString());
    }

    generator.writeStringField("mode", value.getMode().toString());
    generator.writeBooleanField("simulated", value.isSimulated());

    // If the value is null, the type will also be null
    if (value.getType() != null) {
      generator.writeStringField("dataType", value.getType().getSimpleName());
    }

    generator.writeObjectField("quality", value.getDataTagQuality());
    generator.writeObjectField("alarms", value.getAlarms());
    generator.writeEndObject();
  }
}

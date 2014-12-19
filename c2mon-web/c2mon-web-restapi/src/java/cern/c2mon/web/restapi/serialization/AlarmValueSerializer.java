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

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.std.SerializerBase;

import cern.c2mon.shared.client.alarm.AlarmValueImpl;

/**
 * @author Justin Lewis Salmon
 */
class AlarmValueSerializer extends SerializerBase<AlarmValueImpl> {

  /**
   *
   */
  public AlarmValueSerializer() {
    super(AlarmValueImpl.class);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.codehaus.jackson.map.ser.std.SerializerBase#serialize(java.lang.Object
   * , org.codehaus.jackson.JsonGenerator,
   * org.codehaus.jackson.map.SerializerProvider)
   */
  @Override
  public void serialize(AlarmValueImpl value, JsonGenerator generator, SerializerProvider provider) throws IOException, JsonGenerationException {
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

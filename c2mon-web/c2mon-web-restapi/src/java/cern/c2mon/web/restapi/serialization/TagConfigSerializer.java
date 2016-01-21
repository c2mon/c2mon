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

import cern.c2mon.shared.client.tag.TagConfig;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Custom serialisation class for {@link TagConfig} objects.
 *
 * @author Justin Lewis Salmon
 */
public class TagConfigSerializer extends JsonSerializer<TagConfig> {

  /*
   * (non-Javadoc)
   *
   * @see
   * org.codehaus.jackson.map.ser.std.SerializerBase#serialize(java.lang.Object, org.codehaus.jackson.JsonGenerator, org.codehaus.jackson.map.SerializerProvider)
   */
  @Override
  public void serialize(TagConfig value, JsonGenerator generator, SerializerProvider provider) throws IOException, JsonGenerationException {
    generator.writeStartObject();
    generator.writeNumberField("id", value.getId());
    generator.writeNumberField("priority", value.getPriority());
    generator.writeNumberField("valueDeadband", value.getValueDeadband());
    generator.writeNumberField("valueDeadbandType", value.getValueDeadbandType());
    generator.writeNumberField("timeDeadband", value.getTimeDeadband());
    generator.writeStringField("minValue", value.getMinValue());
    generator.writeStringField("maxValue", value.getMaxValue());
    generator.writeBooleanField("logged", value.isLogged());
    generator.writeBooleanField("controlTag", value.isControlTag());
    generator.writeBooleanField("guaranteedDelivery", value.isGuaranteedDelivery());

    // TODO: serialise the hardware address as JSON properly, not just dumping the ugly XML representation
    generator.writeStringField("hardwareAddress", value.getHardwareAddress());

    generator.writeStringField("ruleExpression", value.getRuleExpression());
    generator.writeArrayFieldStart("ruleIds");
    for (Long ruleId : value.getRuleIds()) {
      generator.writeNumber(ruleId);
    }
    generator.writeEndArray();

    generator.writeArrayFieldStart("alarmIds");
    for (Long alarmId : value.getAlarmIds()) {
      generator.writeNumber(alarmId);
    }
    generator.writeEndArray();

    generator.writeArrayFieldStart("processNames");
    for (String processName : value.getProcessNames()) {
      generator.writeNumber(processName);
    }
    generator.writeEndArray();

    generator.writeStringField("dipPublication", value.getDipPublication());
    generator.writeStringField("japcPublication", value.getJapcPublication());

    // TODO: add metadata here

    generator.writeEndObject();
  }
}

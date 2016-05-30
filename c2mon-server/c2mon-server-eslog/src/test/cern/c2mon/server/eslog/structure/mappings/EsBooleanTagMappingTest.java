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
package cern.c2mon.server.eslog.structure.mappings;

import cern.c2mon.server.eslog.structure.mappings.EsMapping.ValueType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

/**
 * Verify the good behaviour of the EsBooleanTagMapping class.
 * @author Alban Marguet.
 */
@RunWith(MockitoJUnitRunner.class)
public class EsBooleanTagMappingTest {
  private final String expectedMapping = "{\n" +
          "  \"_routing\": {\n" +
          "    \"required\": \"true\"\n" +
          "  },\n" +
          "  \"properties\": {\n" +
          "    \"id\": {\n" +
          "      \"type\": \"long\"\n" +
          "    },\n" +
          "    \"name\": {\n" +
          "      \"type\": \"string\",\n" +
          "      \"index\": \"not_analyzed\"\n" +
          "    },\n" +
          "    \"dataType\": {\n" +
          "      \"type\": \"string\",\n" +
          "      \"index\": \"not_analyzed\"\n" +
          "    },\n" +
          "    \"sourceTimestamp\": {\n" +
          "      \"type\": \"date\",\n" +
          "      \"format\": \"epoch_millis\"\n" +
          "    },\n" +
          "    \"serverTimestamp\": {\n" +
          "      \"type\": \"date\",\n" +
          "      \"format\": \"epoch_millis\"\n" +
          "    },\n" +
          "    \"daqTimestamp\": {\n" +
          "      \"type\": \"date\",\n" +
          "      \"format\": \"epoch_millis\"\n" +
          "    },\n" +
          "    \"status\": {\n" +
          "      \"type\": \"integer\"\n" +
          "    },\n" +
          "    \"quality\": {\n" +
          "      \"type\": \"string\",\n" +
          "      \"index\": \"not_analyzed\"\n" +
          "    },\n" +
          "    \"valid\": {\n" +
          "      \"type\": \"boolean\"\n" +
          "    },\n" +
          "    \"valueDescription\": {\n" +
          "      \"type\": \"string\",\n" +
          "      \"index\": \"not_analyzed\"\n" +
          "    },\n" +
          "    \"valueBoolean\": {\n" +
          "      \"type\": \"boolean\"\n" +
          "    },\n" +
          "    \"valueNumeric\": {\n" +
          "      \"type\": \"double\"\n" +
          "    },\n" +
          "    \"process\": {\n" +
          "      \"type\": \"string\",\n" +
          "      \"index\": \"not_analyzed\"\n" +
          "    },\n" +
          "    \"equipment\": {\n" +
          "      \"type\": \"string\",\n" +
          "      \"index\": \"not_analyzed\"\n" +
          "    },\n" +
          "    \"subEquipment\": {\n" +
          "      \"type\": \"string\",\n" +
          "      \"index\": \"not_analyzed\"\n" +
          "    },\n" +
          "    \"metadata\": {\n" +
          "      \"dynamic\": \"true\",\n" +
          "      \"type\": \"nested\"\n" +
          "    }\n" +
          "  }\n" +
          "}";

  @Test
  public void testGetBooleanMapping() {
    EsBooleanTagMapping mapping = new EsBooleanTagMapping(ValueType.BOOLEAN);
    String valueType = mapping.properties.getValueType();
    assertEquals(ValueType.BOOLEAN.toString(), valueType);
  }

  @Test(expected = IllegalArgumentException.class)
  public void wrongGetBooleanMapping() {
    EsBooleanTagMapping mapping = new EsBooleanTagMapping(ValueType.STRING);
    mapping.properties.getValueType();
  }

  @Test
  public void testOutput() {
    EsBooleanTagMapping mapping = new EsBooleanTagMapping(ValueType.BOOLEAN);
    assertEquals(expectedMapping, mapping.getMapping());
  }
}
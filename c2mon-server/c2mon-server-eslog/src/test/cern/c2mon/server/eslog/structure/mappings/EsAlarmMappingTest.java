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

import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Verify the output of the EsAlarmMapping.
 * @author Alban Marguet
 */
public class EsAlarmMappingTest {
  private EsAlarmMapping mapping;
  private String expectedMappings = "{\n" +
      "  \"mappings\": {\n" +
      "    \"alarm\": {\n" +
      "      \"properties\": {\n" +
      "        \"tagId\": {\n" +
      "          \"type\": \"long\"\n" +
      "        },\n" +
      "        \"alarmId\": {\n" +
      "          \"type\": \"long\"\n" +
      "        },\n" +
      "        \"faultFamily\": {\n" +
      "          \"type\": \"string\",\n" +
      "          \"index\": \"not_analyzed\"\n" +
      "        },\n" +
      "        \"faultMember\": {\n" +
      "          \"type\": \"string\",\n" +
      "          \"index\": \"not_analyzed\"\n" +
      "        },\n" +
      "        \"faultCode\": {\n" +
      "          \"type\": \"integer\"\n" +
      "        },\n" +
      "        \"active\": {\n" +
      "          \"type\": \"boolean\"\n" +
      "        },\n" +
      "        \"activity\": {\n" +
      "          \"type\": \"string\",\n" +
      "          \"index\": \"not_analyzed\"\n" +
      "        },\n" +
      "        \"activeNumeric\": {\n" +
      "          \"type\": \"double\"\n" +
      "        },\n" +
      "        \"priority\": {\n" +
      "          \"type\": \"integer\"\n" +
      "        },\n" +
      "        \"info\": {\n" +
      "          \"type\": \"string\",\n" +
      "          \"index\": \"not_analyzed\"\n" +
      "        },\n" +
      "        \"serverTimestamp\": {\n" +
      "          \"type\": \"date\",\n" +
      "          \"format\": \"epoch_millis\"\n" +
      "        },\n" +
      "        \"timeZone\": {\n" +
      "          \"type\": \"string\"\n" +
      "        }\n" +
      "      }\n" +
      "    }\n" +
      "  }\n" +
      "}";

  @Before
  public void setup() {
    mapping = new EsAlarmMapping();
  }

  @Test
  public void testSetProperties() {
    mapping.setProperties(EsMapping.ValueType.alarmType);
    assertNotNull(mapping.getMappings());
    assertEquals(expectedMappings, mapping.getMapping());
  }
}
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

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * Verify the output of the EsSupervisionMapping.
 * @author Alban Marguet
 */
public class EsSupervisionEventMappingTest {
  private EsSupervisionMapping mapping;
  private String expectedMappings = "{\n" +
      "  \"mappings\": {\n" +
      "    \"supervision\": {\n" +
      "      \"properties\": {\n" +
      "        \"id\": {\n" +
      "          \"type\": \"long\"\n" +
      "        },\n" +
      "        \"timestamp\": {\n" +
      "          \"type\": \"date\",\n" +
      "          \"format\": \"epoch_millis\"\n" +
      "        },\n" +
      "        \"message\": {\n" +
      "          \"type\": \"string\",\n" +
      "          \"index\": \"not_analyzed\"\n" +
      "        },\n" +
      "        \"status\": {\n" +
      "          \"type\": \"string\",\n" +
      "          \"index\": \"not_analyzed\"\n" +
      "        }\n" +
      "      }\n" +
      "    }\n" +
      "  }\n" +
      "}";

  @Before
  public void setup() {
    mapping = new EsSupervisionMapping();
  }

  @Test
  public void testSetProperties() {
    mapping.setProperties(EsMapping.ValueType.supervisionType);
    assertNotNull(mapping.getMappings());
    assertEquals(expectedMappings, mapping.getMapping());
  }
}
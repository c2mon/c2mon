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
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * Verify the output of the EsSupervisionMapping.
 * @author Alban Marguet
 */
@RunWith(JUnit4.class)
public class EsSupervisionEventMappingTest {
  private EsSupervisionMapping mapping;
  private String expectedMappings = "{\n" +
          "  \"mappings\": {\n" +
          "    \"supervision\": {\n" +
          "      \"properties\": {\n" +
          "        \"id\": {\n" +
          "          \"type\": \"long\"\n" +
          "        },\n" +
          "        \"name\": {\n" +
          "          \"type\": \"string\",\n" +
          "          \"index\": \"not_analyzed\"\n" +
          "        },\n" +
          "        \"entity\": {\n" +
          "          \"type\": \"string\",\n" +
          "          \"index\": \"not_analyzed\"\n" +
          "        },\n" +
          "        \"message\": {\n" +
          "          \"type\": \"string\",\n" +
          "          \"index\": \"not_analyzed\"\n" +
          "        },\n" +
          "        \"status\": {\n" +
          "          \"type\": \"string\",\n" +
          "          \"index\": \"not_analyzed\"\n" +
          "        },\n" +
          "        \"timestamp\": {\n" +
          "          \"type\": \"date\",\n" +
          "          \"format\": \"epoch_millis\"\n" +
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
    assertNotNull(mapping.getMappings());
    assertEquals(expectedMappings, mapping.getMapping());
  }
}
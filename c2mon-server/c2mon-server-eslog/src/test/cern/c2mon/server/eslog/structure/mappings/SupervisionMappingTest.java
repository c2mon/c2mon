/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * <p/>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p/>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p/>
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
 * @author Alban Marguet
 */
public class SupervisionMappingTest {
  private SupervisionMapping mapping;
  private int shards = 10;
  private int replica = 0;
  private String expectedMappings = "{\n" +
      "  \"mappings\": {\n" +
      "    \"supervision\": {\n" +
      "      \"id\": {\n" +
      "        \"type\": \"long\"\n" +
      "      },\n" +
      "      \"timestamp\": {\n" +
      "        \"type\": \"date\"\n" +
      "      },\n" +
      "      \"message\": {\n" +
      "        \"type\": \"string\"\n" +
      "      },\n" +
      "      \"status\": {\n" +
      "        \"type\": \"string\"\n" +
      "      }\n" +
      "    }\n" +
      "  }\n" +
      "}";
  private String expectedSettings = "{\n" +
      "  \"settings\": {\n" +
      "    \"number_of_shards\": 10,\n" +
      "    \"number_of_replicas\": 0\n" +
      "  }\n" +
      "}";

  @Before
  public void setup() {
    mapping = new SupervisionMapping();
  }

  @Test
  public void testConfigure() {
    mapping.configure(shards, replica);
    assertNotNull(mapping.getSettings());
    assertNull(mapping.getMappings());
    assertEquals(expectedSettings, mapping.getMapping());
  }

  @Test
  public void testSetProperties() {
    mapping.setProperties(Mapping.ValueType.boolType);
    assertNull(mapping.getMappings());

    mapping.setProperties(Mapping.ValueType.supervisionType);
    assertNotNull(mapping.getMappings());
    assertNull(mapping.getSettings());
    assertEquals(expectedMappings, mapping.getMapping());
  }
}

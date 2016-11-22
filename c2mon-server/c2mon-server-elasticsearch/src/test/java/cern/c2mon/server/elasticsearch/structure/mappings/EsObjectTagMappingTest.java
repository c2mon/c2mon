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
package cern.c2mon.server.elasticsearch.structure.mappings;

import cern.c2mon.server.elasticsearch.structure.types.tag.EsTag;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the good behaviour of the class EsStringTagMapping.
 * Needed to do a good indexing in Elasticsearch.
 * @author Alban Marguet.
 */
@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class EsObjectTagMappingTest {
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
      "    \"valueObject\": {\n" +
      "      \"type\": \"nested\",\n" +
      "      \"index\": \"analyzed\"\n" +
      "    },\n" +
      "    \"type\": {\n" +
      "      \"type\": \"string\",\n" +
      "      \"index\": \"not_analyzed\"\n" +
      "    },\n" +
      "    \"valueDescription\": {\n" +
      "      \"type\": \"string\",\n" +
      "      \"index\": \"not_analyzed\"\n" +
      "    },\n" +
      "    \"unit\": {\n" +
      "      \"type\": \"string\",\n" +
      "      \"index\": \"not_analyzed\"\n" +
      "    },\n" +
      "    \"quality\": {\n" +
      "      \"dynamic\": \"false\",\n" +
      "      \"type\": \"object\",\n" +
      "      \"properties\": {\n" +
      "        \"valid\": {\n" +
      "          \"type\": \"boolean\"\n" +
      "        },\n" +
      "        \"statusInfo\": {\n" +
      "          \"type\": \"string\",\n" +
      "          \"index\": \"not_analyzed\"\n" +
      "        },\n" +
      "        \"status\": {\n" +
      "          \"type\": \"integer\"\n" +
      "        }\n" +
      "      }\n" +
      "    },\n" +
      "    \"timestamp\": {\n" +
      "      \"type\": \"date\",\n" +
      "      \"format\": \"epoch_millis\"\n" +
      "    },\n" +
      "    \"c2mon\": {\n" +
      "      \"dynamic\": \"false\",\n" +
      "      \"type\": \"object\",\n" +
      "      \"properties\": {\n" +
      "        \"daqTimestamp\": {\n" +
      "          \"type\": \"date\",\n" +
      "          \"format\": \"epoch_millis\"\n" +
      "        },\n" +
      "        \"process\": {\n" +
      "          \"type\": \"string\",\n" +
      "          \"index\": \"not_analyzed\"\n" +
      "        },\n" +
      "        \"subEquipment\": {\n" +
      "          \"type\": \"string\",\n" +
      "          \"index\": \"not_analyzed\"\n" +
      "        },\n" +
      "        \"serverTimestamp\": {\n" +
      "          \"type\": \"date\",\n" +
      "          \"format\": \"epoch_millis\"\n" +
      "        },\n" +
      "        \"sourceTimestamp\": {\n" +
      "          \"type\": \"date\",\n" +
      "          \"format\": \"epoch_millis\"\n" +
      "        },\n" +
      "        \"dataType\": {\n" +
      "          \"type\": \"string\",\n" +
      "          \"index\": \"not_analyzed\"\n" +
      "        },\n" +
      "        \"equipment\": {\n" +
      "          \"type\": \"string\",\n" +
      "          \"index\": \"not_analyzed\"\n" +
      "        }\n" +
      "      }\n" +
      "    },\n" +
      "    \"metadata\": {\n" +
      "      \"dynamic\": \"true\",\n" +
      "      \"type\": \"nested\"\n" +
      "    }\n" +
      "  }\n" +
      "}";

  @Test
  public void testStringOutput() {
    EsTagMapping mapping = new EsTagMapping(EsTag.TYPE_OBJECT, EsTag.class.getName());
    assertNotNull(mapping.getMapping());
    assertEquals(expectedMapping, mapping.getMapping());
  }
}

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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import cern.c2mon.server.eslog.structure.mappings.EsMapping.ValueType;

/**
 * Tests the good behaviour of the class EsStringTagMapping.
 * Needed to do a good indexing in ElasticSearch.
 * @author Alban Marguet.
 */
@RunWith(MockitoJUnitRunner.class)
public class EsStringTagMappingTest {
  private String expectedMapping = "{\n" +
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
      "    \"valueString\": {\n" +
      "      \"type\": \"" + ValueType.stringType + "\"\n" +
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
      "    }\n" +
      "  }\n" +
      "}";

	@Test
	public void testGetStringMapping() {
		EsStringTagMapping mapping = new EsStringTagMapping(ValueType.stringType);
		String valueType = mapping.properties.getValueType();
		assertEquals(ValueType.stringType.toString(), valueType);
	}

	@Test(expected = IllegalArgumentException.class)
	public void wrongGetStringMapping() {
		EsStringTagMapping mapping = new EsStringTagMapping(ValueType.dateType);
		mapping.properties.getValueType();
	}

  @Test
  public void testOutput() {
    EsStringTagMapping mapping = new EsStringTagMapping(ValueType.stringType);
    assertEquals(expectedMapping, mapping.getMapping());
  }
}
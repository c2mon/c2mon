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

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import cern.c2mon.server.eslog.structure.mappings.EsMapping.ValueType;

/**
 * Test the good behaviour of the EsNumericTagMapping class.
 * We need a good mapping to index correctly the data in ElasticSearch.
 * @author Alban Marguet.
 */
@RunWith(MockitoJUnitRunner.class)
public class EsNumericTagMappingTest {
	private String expectedLongMapping = "{\n" +
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
      "    \"valueNumeric\": {\n" +
      "      \"type\": \"" + ValueType.longType + "\"\n" +
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
	public void testGetNumericMapping() {
		EsNumericTagMapping mapping = new EsNumericTagMapping(ValueType.intType);
		String valueType = mapping.properties.getValueType();
		assertTrue(ValueType.isNumeric(valueType));
	}

	@Test(expected = IllegalArgumentException.class)
	public void wrongGetNumericMapping() {
		EsNumericTagMapping mapping = new EsNumericTagMapping(ValueType.boolType);
		mapping.properties.getValueType();
	}

  @Test
  public void testOutput() {
    EsNumericTagMapping mapping = new EsNumericTagMapping(ValueType.longType);
    assertEquals(expectedLongMapping, mapping.getMapping());
  }
}
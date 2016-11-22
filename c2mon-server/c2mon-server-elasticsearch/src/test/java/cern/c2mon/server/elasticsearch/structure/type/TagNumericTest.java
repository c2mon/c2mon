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
package cern.c2mon.server.elasticsearch.structure.type;

import java.io.IOException;

import cern.c2mon.server.elasticsearch.structure.types.tag.EsTag;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import cern.c2mon.pmanager.IFallback;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the good behaviour of the EsTagNumeric class. verify that it builds
 * correctly in JSON and accept/reject good/bad types of value.
 *
 * @author Alban Marguet.
 */
@RunWith(MockitoJUnitRunner.class)
public class TagNumericTest {
  @Test
  public void testValue() {
    EsTag tagNumeric = new EsTag(1L, "Integer");
    tagNumeric.setRawValue(123);

    assertEquals(123, tagNumeric.getValue());
    assertTrue(tagNumeric.getValue() instanceof Integer);

    tagNumeric.setRawValue(1.23);

    assertEquals(1.23, tagNumeric.getValue());
    assertTrue(tagNumeric.getValue() instanceof Double);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadValue() {
    EsTag tagNumeric = new EsTag(1L, "Integer");
    tagNumeric.setRawValue("notNumeric");
  }


  @Test
  public void testBuild() throws IOException {
    EsTag tagNumeric = new EsTag(1L, Integer.class.getName());

    final String expectedTagJson = "{\"id\":1,\"type\":\"number\",\"timestamp\":0,\"c2mon\":{\"dataType\":\"java.lang.Integer\"," +
        "\"serverTimestamp\":0,\"sourceTimestamp\":0,\"daqTimestamp\":0},\"metadata\":{}}";

    assertEquals(expectedTagJson, tagNumeric.toString());
  }

  @Test
  public void testNullValue() {
    EsTag tagNumeric = new EsTag(1L, Integer.class.getName());
    tagNumeric.setRawValue(null);
    assertNull(tagNumeric.getValue());
  }

  @Test
  public void testGetObject() {

    final String expectedTagJson = "{\"id\":1,\"name\":\"CLIC:CFC-CCR-ALLGPSPS:SYS.MEM.FREEPCT\"," +
        "\"value\":73.9237,\"unit\":\"n/a\",\"type\":\"number\",\"quality\":{\"status\":0,\"valid\":true," +
        "\"statusInfo\":[\"OK\"]},\"timestamp\":1454342362957," +
        "\"c2mon\":{\"process\":\"P_CLIC_SPS\",\"equipment\":\"CLIC:CFC-CCR-ALLGPSPS\",\"dataType\":\"float\"," +
        "\"serverTimestamp\":1454342362981,\"sourceTimestamp\":1454342362981,\"daqTimestamp\":1454342362957}," +
        "\"metadata\":{\"metaFieldName\":\"metaValue\"}}";

    EsTag tagNumeric = new EsTag(1L, Integer.class.getName());
    IFallback result = tagNumeric.getObject(expectedTagJson);
    assertTrue(result instanceof EsTag);
    assertEquals(expectedTagJson, result.toString());
  }
}

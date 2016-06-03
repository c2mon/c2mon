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
package cern.c2mon.server.eslog.structure.type;

import cern.c2mon.pmanager.IFallback;
import cern.c2mon.server.eslog.structure.types.tag.EsTagNumeric;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the good behaviour of the EsTagNumeric class. verify that it builds
 * correctly in JSON and accept/reject good/bad types of value.
 *
 * @author Alban Marguet.
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class TagNumericTest {
  @InjectMocks
  private EsTagNumeric tagNumeric;

  @Test
  public void testValue() {
    tagNumeric.setRawValue(123);

    assertEquals(123, tagNumeric.getRawValue());
    assertTrue(tagNumeric.getRawValue() instanceof Integer);

    tagNumeric.setRawValue(1.23);

    assertEquals(1.23, tagNumeric.getRawValue());
    assertTrue(tagNumeric.getRawValue() instanceof Double);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadValue() {
    tagNumeric.setRawValue("notNumeric");
  }


  @Test
  public void testBuild() throws IOException {
    tagNumeric.setDataType("numeric");

    final String expectedTagJson = "{\"id\":0,\"dataType\":\"numeric\",\"timestamp\":0,\"serverTimestamp\":0," +
        "\"daqTimestamp\":0,\"unit\":\"n/a\",\"metadata\":{}}";

    assertEquals(expectedTagJson, tagNumeric.toString());
  }

  @Test
  public void testNullValue() {
    tagNumeric.setRawValue(null);
    assertNull(tagNumeric.getRawValue());
  }

  @Test
  public void testGetObject() {

    final String expectedTagJson = "{\"id\":0,\"name\":\"CLIC:CFC-CCR-ALLGPSPS:SYS.MEM.FREEPCT\"," +
        "\"dataType\":\"float\",\"timestamp\":1454342362957,\"serverTimestamp\":1454342362981,\"daqTimestamp\":1454342362957," +
        "\"quality\":{\"status\":0,\"valid\":true,\"statusInfo\":[\"OK\"]}," +
        "\"value\":73.9237,\"unit\":\"n/a\"," +
        "\"metadata\":{\"process\":\"P_CLIC_SPS\",\"equipment\":\"CLIC:CFC-CCR-ALLGPSPS\"}}";

    IFallback result = tagNumeric.getObject(expectedTagJson);
    assertTrue(result instanceof EsTagNumeric);
    assertEquals(expectedTagJson, result.toString());
  }
}
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

import cern.c2mon.server.eslog.structure.types.tag.EsTagBoolean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the good behaviour of the EsTagBoolean class. verify that it builds
 * correctly in JSON and accept/reject good/bad types of value.
 *
 * @author Alban Marguet.
 */
@RunWith(MockitoJUnitRunner.class)
public class TagBooleanTest {
  @InjectMocks
  private EsTagBoolean tagBoolean;

  @Test
  public void testValue() {
    tagBoolean.setRawValue(true);

    assertEquals(true, tagBoolean.getRawValue());
    assertEquals(1, tagBoolean.getValue());
    assertTrue(tagBoolean.getValueBoolean());
    assertTrue(tagBoolean.getRawValue() instanceof Boolean);

    tagBoolean.setRawValue(false);

    assertEquals(false, tagBoolean.getRawValue());
    assertTrue(tagBoolean.getRawValue() instanceof Boolean);
    assertFalse(tagBoolean.getValueBoolean());
    assertEquals(0, tagBoolean.getValue());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadValue() {
    tagBoolean.setRawValue("NotBoolean");
  }

  @Test
  public void testBuild() throws IOException {
    tagBoolean.getC2mon().setDataType(Boolean.class.getName());

    final String expectedTagJson = "{\"id\":0,\"timestamp\":0," +
        "\"c2mon\":{\"dataType\":\"java.lang.Boolean\",\"serverTimestamp\":0,\"sourceTimestamp\":0,\"daqTimestamp\":0}," +
        "\"metadata\":{}}";

    assertEquals(expectedTagJson, tagBoolean.toString());
  }

  @Test
  public void testNullValue() {
    tagBoolean.setRawValue(null);
    assertNull(tagBoolean.getRawValue());
  }
}
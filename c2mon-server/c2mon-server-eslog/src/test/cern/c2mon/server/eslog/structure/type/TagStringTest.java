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
import cern.c2mon.server.eslog.structure.types.tag.AbstractEsTag;
import cern.c2mon.server.eslog.structure.types.tag.EsTagString;
import cern.c2mon.server.eslog.structure.types.tag.TagQualityAnalysis;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Collections;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the good behaviour of the EsTagString class.
 * verify that it builds correctly in JSON and accept/reject good/bad types of value.
 * @author Alban Marguet.
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class TagStringTest {
  @InjectMocks
  private EsTagString tagString;

  @Test
  public void testValue() {
    tagString.setRawValue("test");

    assertEquals("test", tagString.getRawValue());
    assertTrue(tagString.getRawValue() instanceof String);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadValue() {
    tagString.setRawValue(123456789);
  }

  @Test
  public void testBuild() throws IOException {
    TagQualityAnalysis qualityAnalysis = new TagQualityAnalysis();
    qualityAnalysis.setStatusInfo(Collections.singleton(TagQualityAnalysis.OK));
    qualityAnalysis.setStatus(0);
    qualityAnalysis.setValid(true);
    tagString.setQuality(qualityAnalysis);

    String expectedTagJson = "{\"id\":0,\"timestamp\":0,\"serverTimestamp\":0,\"daqTimestamp\":0," +
        "\"quality\":{\"status\":0,\"valid\":true,\"statusInfo\":[\"OK\"]},\"unit\":\"n/a\",\"metadata\":{}}";

    assertEquals(expectedTagJson, tagString.toString());
  }

  @Test
  public void testNullValue() {
    tagString.setRawValue(null);
    assertNull(tagString.getRawValue());
  }

  @Test
  public void testGetObject() {
    AbstractEsTag tag = new EsTagString();

    tag.setId(192506);
    tag.setName("CM.MEY.VGTCTESTCM11:STATUS");
    tag.setDataType("string");

    tag.setTimestamp(1454342362957L);
    tag.setServerTimestamp(1451915554970L);
    tag.setDaqTimestamp(1454342362957L);

    TagQualityAnalysis qualityAnalysis = new TagQualityAnalysis();
    qualityAnalysis.setValid(true);
    qualityAnalysis.setStatus(0);
    qualityAnalysis.setStatusInfo(Collections.singleton(TagQualityAnalysis.OK));
    tag.setQuality(qualityAnalysis);

    tag.setValueString("DOWN");
    tag.setValueDescription("Communication fault tag indicates that equipment E_OPC_GTCTESTCM11 is down. Reason: Problems connecting to VGTCTESTCM11: Problems wih the DCOM connection occured");
    tag.getMetadata().put("process","P_GTCTESTCM11");
    tag.getMetadata().put("equipment", "E_OPC_GTCTESTCM11");

    System.out.println(tag.toString());

    final String expectedTagJson = "{\"id\":192506,\"name\":\"CM.MEY.VGTCTESTCM11:STATUS\",\"dataType\":\"string\",\"timestamp\":1454342362957,\"serverTimestamp\":1451915554970,\"daqTimestamp\":1454342362957,\"quality\":{\"status\":0,\"valid\":true,\"statusInfo\":[\"OK\"]},\"valueString\":\"DOWN\",\"unit\":\"n/a\",\"valueDescription\":\"Communication fault tag indicates that equipment E_OPC_GTCTESTCM11 is down. Reason: Problems connecting to VGTCTESTCM11: Problems wih the DCOM connection occured\",\"metadata\":{\"process\":\"P_GTCTESTCM11\",\"equipment\":\"E_OPC_GTCTESTCM11\"}}";
    IFallback result = tagString.getObject(expectedTagJson);
    assertTrue(result instanceof EsTagString);
    assertEquals(expectedTagJson, result.toString());
  }
}
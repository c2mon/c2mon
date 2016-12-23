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
import java.util.Collections;

import cern.c2mon.server.elasticsearch.tag.EsTag;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import cern.c2mon.pmanager.IFallback;
import cern.c2mon.server.elasticsearch.tag.TagQualityAnalysis;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the good behaviour of the EsTagString class.
 * verify that it builds correctly in JSON and accept/reject good/bad types of value.
 * @author Alban Marguet
 */
@RunWith(MockitoJUnitRunner.class)
public class TagStringTest {

  @Test
  public void testValue() {
    EsTag tagString = new EsTag(1L, String.class.getName());
    tagString.setRawValue("test");

    assertEquals("test", tagString.getValueString());
    assertTrue(tagString.getValueString() instanceof String);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadValue() {
    EsTag tagString = new EsTag(1L, String.class.getName());
    tagString.setRawValue(123456789);
  }

  @Test
  public void testBuild() throws IOException {
    EsTag tagString = new EsTag(1L, String.class.getName());
    TagQualityAnalysis qualityAnalysis = new TagQualityAnalysis();
    qualityAnalysis.setStatusInfo(Collections.singleton(TagQualityAnalysis.OK));
    qualityAnalysis.setStatus(0);
    qualityAnalysis.setValid(true);
    tagString.setQuality(qualityAnalysis);

    String expectedTagJson = "{\"id\":1,\"type\":\"string\",\"quality\":{\"status\":0,\"valid\":true," +
        "\"statusInfo\":[\"OK\"]},\"timestamp\":0," +
        "\"c2mon\":{\"dataType\":\"java.lang.String\",\"serverTimestamp\":0,\"sourceTimestamp\":0,\"daqTimestamp\":0},\"metadata\":{}}";
    assertEquals(expectedTagJson, tagString.toString());
  }

  @Test
  public void testNullValue() {
    EsTag tagString = new EsTag(1L, String.class.getName());
    tagString.setRawValue(null);
    assertNull(tagString.getValueString());
  }

  @Test
  public void testGetObject() {
    EsTag tag = new EsTag(192506L, String.class.getName());

    tag.setName("CM.MEY.VGTCTESTCM11:STATUS");
    tag.setTimestamp(1454342362957L);

    tag.getC2mon().setServerTimestamp(1451915554970L);
    tag.getC2mon().setDaqTimestamp(1454342362957L);
    tag.getC2mon().setSourceTimestamp(1451915554970L);

    TagQualityAnalysis qualityAnalysis = new TagQualityAnalysis();
    qualityAnalysis.setValid(true);
    qualityAnalysis.setStatus(0);
    qualityAnalysis.setStatusInfo(Collections.singleton(TagQualityAnalysis.OK));
    tag.setQuality(qualityAnalysis);

    tag.setValueString("DOWN");
    tag.setValueDescription("Communication fault tag indicates that equipment E_OPC_GTCTESTCM11 is down. Reason: Problems connecting to VGTCTESTCM11: Problems wih the DCOM connection occured");
    tag.getMetadata().put("process","P_GTCTESTCM11");
    tag.getMetadata().put("equipment", "E_OPC_GTCTESTCM11");

    final String expectedTagJson = "{\"id\":192506,\"name\":\"CM.MEY.VGTCTESTCM11:STATUS\",\"valueString\":\"DOWN\",\"valueDescription\":\"Communication fault tag indicates that equipment E_OPC_GTCTESTCM11 is down. Reason: Problems connecting to VGTCTESTCM11: Problems wih the DCOM connection occured\",\"unit\":\"n/a\",\"quality\":{\"status\":0,\"valid\":true,\"statusInfo\":[\"OK\"]},\"timestamp\":1454342362957,\"c2mon\":{\"process\":\"P_GTCTESTCM11\",\"equipment\":\"E_OPC_GTCTESTCM11\",\"dataType\":\"string\",\"serverTimestamp\":1451915554970,\"sourceTimestamp\":1451915554970,\"daqTimestamp\":1454342362957},\"metadata\":{}}";
    IFallback result = new EsTag().getObject(expectedTagJson);
    assertTrue(result instanceof EsTag);
    assertEquals(expectedTagJson, result.toString());
  }
}

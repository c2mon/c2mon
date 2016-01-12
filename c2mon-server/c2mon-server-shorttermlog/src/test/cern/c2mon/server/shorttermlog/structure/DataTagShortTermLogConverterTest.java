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
package cern.c2mon.server.shorttermlog.structure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.shared.common.datatag.TagQualityStatus;

/**
 * Unit test of DataTagShortTermLogConverter.
 *
 * @author Mark Brightwell
 *
 */
public class DataTagShortTermLogConverterTest {

  /**
   * Class to test.
   */
  private DataTagShortTermLogConverter converter = new DataTagShortTermLogConverter();

  /**
   * Tests the quality code is correctly generated.
   */
  @Test
  public void testCodeGeneration() {
    DataTagCacheObject tag = new DataTagCacheObject(10L);
    tag.getDataTagQuality().addInvalidStatus(TagQualityStatus.INACCESSIBLE, "desc1");
    tag.getDataTagQuality().addInvalidStatus(TagQualityStatus.UNINITIALISED, "desc1");
    tag.getDataTagQuality().addInvalidStatus(TagQualityStatus.VALUE_OUT_OF_BOUNDS, "desc1");
    assertNotNull(tag.getDataTagQuality());
    Loggable loggable = converter.convertToLogged(tag);
    TagShortTermLog logTag = (TagShortTermLog) loggable;
    assertEquals(11, logTag.getTagQualityCode());
  }

  /**
   * Tests conversion for some other fields.
   */
  @Test
  public void testConversion() {
    DataTagCacheObject tag = new DataTagCacheObject(10L);
    tag.setValueDescription("value desc");
    TagShortTermLog logTag = (TagShortTermLog) converter.convertToLogged(tag);
    assertEquals("value desc",logTag.getTagValueDesc());
  }

}

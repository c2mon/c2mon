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
package cern.c2mon.client.ext.history.updates;

import java.sql.Timestamp;

import junit.framework.Assert;

import org.junit.Test;

import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.TagQualityStatus;

public class HistoryTagValueUpdateTest {

  /**
   * Tests HistoryTagValueUpdate xml serialization.
   * @throws Exception
   */
  @Test
  public void testTagQualityIsIncludedInXml() throws Exception {
    
    java.util.Date date = new java.util.Date();
    Timestamp now = new Timestamp(date.getTime());
    
    DataTagQualityImpl q = new DataTagQualityImpl(TagQualityStatus.EQUIPMENT_DOWN, "It is down!!");
    
    String value = "Value";

    HistoryTagValueUpdateImpl h = new HistoryTagValueUpdateImpl(100L, 
        q, value, 
        now, now, now,
        now, "it looks ok", 
        null, TagMode.MAINTENANCE);
    
    Assert.assertTrue (h.getXml().contains("it looks ok"));
    Assert.assertTrue (h.getXml().contains("Value"));
    Assert.assertTrue (h.getXml().contains("MAINTENANCE"));
    Assert.assertTrue (h.getXml().contains("It is down"));
  }  
}

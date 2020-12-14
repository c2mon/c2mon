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
package cern.c2mon.shared.daq.datatag;

import java.sql.Timestamp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.shared.common.datatag.*;
import cern.c2mon.shared.common.datatag.util.SourceDataTagQualityCode;

/**
 * Unit test of SourceDataTag class.
 *
 * @author Mark Brightwell
 */
public class SourceDataTagTest {

  /**
   * Class to test.
   */
  private SourceDataTag tag;

  @Before
  public void init() {
    tag = new SourceDataTag(1L, "test tag", false, DataTagConstants.MODE_OPERATIONAL, "Object", new DataTagAddress());
  }

  /**
   * Test the timestamp and DAQ timestamp are correctly set in update method.
   */
  @Test
  public void testUpdateTimestamps() {
    Object value = new Object();

    Timestamp timestamp = new Timestamp(System.currentTimeMillis() - 1);
    tag.update(new ValueUpdate(value, "test value description", timestamp.getTime()));

    Assert.assertEquals(value, tag.getCurrentValue().getValue());
    Assert.assertEquals(timestamp, tag.getCurrentValue().getTimestamp());
    //DAQ timestamp should be later
    Assert.assertNotNull(tag.getCurrentValue().getDaqTimestamp());
    Assert.assertTrue(tag.getCurrentValue().getDaqTimestamp().after(timestamp));


    //recheck with current value set
    Object newValue = new Object();

    Timestamp newTimestamp = new Timestamp(System.currentTimeMillis() - 1);
    tag.update(new ValueUpdate(newValue, "test value description", newTimestamp.getTime()));

    Assert.assertEquals(newValue, tag.getCurrentValue().getValue());
    Assert.assertEquals(newTimestamp, tag.getCurrentValue().getTimestamp());
    //DAQ timestamp should be later
    Assert.assertNotNull(tag.getCurrentValue().getDaqTimestamp());
    Assert.assertTrue(tag.getCurrentValue().getDaqTimestamp().after(newTimestamp));
    Assert.assertTrue(tag.getCurrentValue().getDaqTimestamp().after(newTimestamp));
  }

  /**
   * Test the timestamp and DAQ timestamp are correctly set in invalidate method.
   */
  @Test
  public void testInvalidateTimestamps() {

    Timestamp timestamp = new Timestamp(System.currentTimeMillis() - 1);
    SourceDataTagQuality quality = new SourceDataTagQuality(SourceDataTagQualityCode.DATA_UNAVAILABLE);
    tag.update(quality, timestamp.getTime());

    Assert.assertEquals(timestamp, tag.getCurrentValue().getTimestamp());
    //DAQ timestamp should be later
    Assert.assertNotNull(tag.getCurrentValue().getDaqTimestamp());
    Assert.assertTrue(tag.getCurrentValue().getDaqTimestamp().after(timestamp));


    //for null timestamp, src and DAQ timestamp should be the same
    tag.update(quality, 0);

    Assert.assertEquals(new Timestamp(0), tag.getCurrentValue().getTimestamp());
  }

}

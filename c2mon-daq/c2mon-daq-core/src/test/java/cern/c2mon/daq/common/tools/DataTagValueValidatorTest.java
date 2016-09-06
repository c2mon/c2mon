/*******************************************************************************
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
 ******************************************************************************/

package cern.c2mon.daq.common.tools;

import org.junit.BeforeClass;
import org.junit.Test;

import cern.c2mon.daq.tools.DataTagValueValidator;
import cern.c2mon.shared.common.datatag.SourceDataTag;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Franz Ritter
 */
public class DataTagValueValidatorTest {

  private static DataTagValueValidator valueValidator;

  @BeforeClass
  public static void ini() {
    valueValidator = new DataTagValueValidator();
  }

  @Test
  public void isInRangeLong() {
    SourceDataTag sourceDataTag = new SourceDataTag(100L, "testTag", false);
    sourceDataTag.setMaxValue(Long.MAX_VALUE - 1L);
    sourceDataTag.setMinValue(0L);
    sourceDataTag.setDataType(Long.class.getName());

    assertTrue(valueValidator.isInRange(sourceDataTag, Long.MAX_VALUE - 2L));
    assertFalse(valueValidator.isInRange(sourceDataTag, Long.MAX_VALUE));
    assertTrue(valueValidator.isInRange(sourceDataTag, 0L));
    assertFalse(valueValidator.isInRange(sourceDataTag, -1L));
  }

  @Test
  public void isInRangeInteger() {
    SourceDataTag sourceDataTag = new SourceDataTag(100L, "testTag", false);
    sourceDataTag.setMaxValue(Integer.MAX_VALUE - 1);
    sourceDataTag.setMinValue(0);
    sourceDataTag.setDataType(Integer.class.getName());

    assertTrue(valueValidator.isInRange(sourceDataTag, Integer.MAX_VALUE - 2));
    assertFalse(valueValidator.isInRange(sourceDataTag, Integer.MAX_VALUE));
    assertTrue(valueValidator.isInRange(sourceDataTag, 0));
    assertFalse(valueValidator.isInRange(sourceDataTag, -1));
  }

  @Test
  public void isInRangeDouble() {
    SourceDataTag sourceDataTag = new SourceDataTag(100L, "testTag", false);
    sourceDataTag.setMaxValue(100.0);
    sourceDataTag.setMinValue(0.0);
    sourceDataTag.setDataType(Double.class.getName());

    assertTrue(valueValidator.isInRange(sourceDataTag, 99.0));
    assertFalse(valueValidator.isInRange(sourceDataTag, 101.0));
    assertTrue(valueValidator.isInRange(sourceDataTag, 0));
    assertFalse(valueValidator.isInRange(sourceDataTag, -1));
  }

  @Test
  public void isInRangeFloat() {
    SourceDataTag sourceDataTag = new SourceDataTag(100L, "testTag", false);
    sourceDataTag.setMaxValue(100.0f);
    sourceDataTag.setMinValue(0.0f);
    sourceDataTag.setDataType(Float.class.getName());

    assertTrue(valueValidator.isInRange(sourceDataTag, 99.0));
    assertFalse(valueValidator.isInRange(sourceDataTag, 101.0));
    assertTrue(valueValidator.isInRange(sourceDataTag, 0));
    assertFalse(valueValidator.isInRange(sourceDataTag, -1));
  }
}

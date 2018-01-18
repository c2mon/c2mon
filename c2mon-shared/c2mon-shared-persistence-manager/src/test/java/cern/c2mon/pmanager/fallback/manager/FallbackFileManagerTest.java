/******************************************************************************
 * Copyright (C) 2010-2018 CERN. All rights not expressly granted are reserved.
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

package cern.c2mon.pmanager.fallback.manager;

import cern.c2mon.pmanager.fallback.FallbackProperties;
import cern.c2mon.pmanager.fallback.exception.DataFallbackException;
import cern.c2mon.pmanager.fallback.exception.SystemDiskSpaceException;
import cern.c2mon.pmanager.mock.FallbackImpl;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JUnit test for the class
 * DataTagFallbackHelper. It tests that all
 * DataTagFallbackHelper methods are properly working.
 *
 * @author mruizgar
 */
public class FallbackFileManagerTest {

  /**
   * List of DataTagCacheObjects
   */
  private static final List<FallbackImpl> data = new ArrayList<>();

  /**
   * Instance to the FallbackFileManager class that we want to test
   */
  private static FallbackFileManager fFileManager;

  /**
   * Prepares the dataTags list that will be used for the tests
   */
  @BeforeClass
  public static void setUp() throws IOException {
    final File dataTagFallback = File.createTempFile("DataTagFallback", ".log");
    dataTagFallback.deleteOnExit();
    // create a FallbackImpl object
    FallbackImpl fImpl = new FallbackImpl();
    fImpl.setObjectData("100142 CP.MEY.TEST13:TEST_STATE    false   Boolean 2009-01-15 10:06:34.562 0   null    0   I   2009-01-15 10:08:10.8");
    data.add(fImpl);

    fImpl = new FallbackImpl();
    fImpl.setObjectData("100143  CP.MEY.TEST14:TEST_STATE    false   Boolean 2009-01-15 10:06:34.562 0   null    0   I   2009-01-15 10:08:11.235");
    data.add(fImpl);
    fFileManager = new FallbackFileManager(dataTagFallback.getAbsolutePath(), new FallbackImpl());
  }

  /**
   * Tests the fallback([Collection]) method. The test will be successful when
   * something is written in the fallback log file
   */
  @Test
  public final void testFallBackList() {
    try {
      fFileManager.fallback(data);
    } catch (DataFallbackException e) {
      Assert.fail("Data could not be written to the log file " + e.getMessage());
    }
    boolean empty = fFileManager.isFallbackFileEmpty();
    Assert.assertFalse("Fallback should not be empty", empty);
  }

  /**
   * Test the fallBack() method. The test will be successful when
   * something is written in the fallback log file
   */
  @Test
  public final void testFallBack() {
    boolean empty = true;

    try {
      fFileManager.fallback(data.get(0));
      empty = fFileManager.isFallbackFileEmpty();
    } catch (DataFallbackException e) {
      Assert.fail("Data could not be written to the log file " + e.getMessage());
    }
    Assert.assertFalse("Fallback should not be empty", empty);
  }

  /**
   * Tests the CheckDataTagsFileDiscSpace method.
   */
  @Test
  public final void testCheckDataTagsFileDiscSpace() {
    int nCheck = 100;
    try {
      fFileManager.setNextCheckFallbackData(nCheck);
      fFileManager.isDiskSpaceCheckDone(FallbackProperties.getInstance().getMinimunDiscFreeSpace());
    } catch (SystemDiskSpaceException e) {
      Assert.fail(e.getMessage());
    }
    Assert.assertEquals("Should be 100", nCheck, fFileManager.getNextCheckFallbackData());
  }
}

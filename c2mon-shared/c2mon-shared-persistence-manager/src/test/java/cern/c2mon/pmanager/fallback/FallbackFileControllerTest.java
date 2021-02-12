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

package cern.c2mon.pmanager.fallback;

import cern.c2mon.pmanager.fallback.exception.DataFallbackException;
import cern.c2mon.pmanager.mock.FallbackImpl;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * JUnit test for the FallbackFileController class
 * <p>
 * To execute this test you have to provide the following runtime arguments
 * -Dtim.log.fallback.file=XXX.log
 *
 * @author mruizgar
 */
public class FallbackFileControllerTest {

  private static final String FALLBACK_FILE_NAME = "DataTagFallback.log";
  private static final String COUNTER_FILE_NAME = ".Counter" + FALLBACK_FILE_NAME;

  /**
   * The fallback file path
   */
  private static File fallbackFile;

  /**
   * The counter fallback file path
   */
  private static File counterFallbackFile;

  /**
   * The Fallback file controller instance
   */
  private static FallbackFileController fFileController;

  /**
   * It sets up the class for the test
   */
  @BeforeClass
  public static void setUp() throws IOException {
    ClassLoader classLoader = FallbackFileControllerTest.class.getClassLoader();

    fallbackFile = new File(classLoader.getResource(FALLBACK_FILE_NAME).getFile());
    counterFallbackFile = new File(classLoader.getResource(COUNTER_FILE_NAME).getFile());

    //Initialize the counter file with a random number
    BufferedWriter counterFileWriter = new BufferedWriter(new FileWriter(counterFallbackFile));
    counterFileWriter.write("3000");
    counterFileWriter.close();

    fFileController = new FallbackFileController(fallbackFile.getAbsolutePath());
  }

  /**
   * Tests the counter reset when the current counter is bigger than the number of lines in the fallback file
   */
  @Test
  public final void testCounterReset(){
    int counter = 0;
    try {
      counter = fFileController.readCounter(false);
    } catch (DataFallbackException e) {
      fail("Exception should not be thrown");
    }
    assertEquals(0, counter);
  }

  /**
   * Tests the OpenOutputStream method, a new stream should be created
   */
  @Test
  public final void testOpenOutputStream() {
    try {

      fFileController.openFallbackOutputStream();
    } catch (DataFallbackException e) {
      fail("The OutputStream could not be opened");
    }
    assertNotNull(fFileController.getOutput());
  }

  /**
   * Tests the OpenInputStream method, the test is successful when the stream
   * is correctly opened
   */
  @Test
  public final void testOpenInputStream() {
    try {
      fFileController.openFallbackInputStream();
    } catch (DataFallbackException e) {
      fail("The InputStream could not be opened");
    }
    assertNotNull(fFileController.getInput());
  }

  /**
   * Tests that a new tag is correctly logged in the fallback log file
   */
  @Test
  public final void testWriteLine() {

    FallbackImpl fallbackImpl = new FallbackImpl();
    try {
      fFileController.openFallbackOutputStream();
      fFileController.writeLine(fallbackImpl);
      fFileController.closeFallbackOutputStream();
    } catch (DataFallbackException e) {
      fail("An error has ocurred while trying to write in the fallback log file");
    }

    assertFalse(fFileController.isFallBackFileEmpty());
  }

  /**
   * Tests the CloseInputStream method, the test is successful when the stream
   * is correclty closed
   */
  @Test
  public final void testCloseInputStream() {
    try {
      fFileController.closeFallbackInputStream();
    } catch (DataFallbackException e) {
      fail("The InputStream could not be closed");
    }
    assertNull(fFileController.getInput());
  }

  /**
   * Tests the CloseOutputStream method, the test is successful when the
   * stream is correctly closed
   */
  @Test
  public final void testCloseOutputStream() {
    try {
      fFileController.closeFallbackOutputStream();
    } catch (DataFallbackException e) {
      fail("The OutputStream could not be closed");
    }
    assertNull(fFileController.getOutput());
  }
}

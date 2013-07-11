/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.client.ext.history.testUtil;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * This class is used by tests to set up exception handlers on other threads
 * than the main thread.<br/>
 * Call {@link #setUpUncaughtException()} in when starting the test, then call
 * {@link #tearDownUncaughtException()} when finishing the test.
 * 
 * 
 * @author vdeila
 * 
 */
public final class UncaughtExceptionSetup {

  /** Exception handler for multi threaded tests */
  private static Exception uncaughtException = null;

  /** The handler handeling uncaught exceptions */
  private static UncaughtExceptionHandler uncaughtExceptionHandler = null;

  /**
   * Sets up the uncaught exception. Call the
   * {@link #tearDownUncaughtException()} when finished with the test
   */
  public static void setUpUncaughtException() {
    if (uncaughtExceptionHandler == null) {
      uncaughtExceptionHandler = new UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(final Thread t, final Throwable e) {
          if (uncaughtException == null) {
            uncaughtException = new Exception(e);
          }
        }
      };
    }
    uncaughtException = null;
    Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
  }

  /**
   * Is called when finishing the test. Any exceptions thrown during test will
   * be thrown here.
   * 
   * @throws Exception
   *           if any exception were thrown from any other thread.
   */
  public static void tearDownUncaughtException() throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(null);
    if (uncaughtException != null) {
      throw uncaughtException;
    }
  }

  /** Hidden constructor */
  private UncaughtExceptionSetup() {

  }
}

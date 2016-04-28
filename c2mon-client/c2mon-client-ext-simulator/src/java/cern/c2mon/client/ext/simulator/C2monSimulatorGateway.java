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
package cern.c2mon.client.ext.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.databind.Module;

import cern.c2mon.client.core.C2monServiceGateway;

public class C2monSimulatorGateway {
  /** Class logger */
  private static final Logger LOG = LoggerFactory.getLogger(C2monSimulatorGateway.class);

  /** The path to the core Spring XML */
  private static final String APPLICATION_SPRING_XML_PATH = "classpath://cern/c2mon/client/ext/simulator/config/c2mon-client-ext-simulator.xml";

  /** The extended SPRING application context for this gateway */
  private static ApplicationContext context;

  /** Static reference to the <code>C2monSessionManager</code> singleton instance */
  private static TagSimulator tagSimulator = null;

  /**
   * Hidden default constructor
   */
  private C2monSimulatorGateway() {
    // Do nothing
  }


  /**
   * Initializes the C2MON Simulator. Must be called before using for the
   * first time {@link #getTagSimulator()}.
   * @see #startC2monClientSynchronous(Module...)
   */
  public static synchronized void initialize() {
    if (C2monServiceGateway.getApplicationContext() == null) {
      C2monServiceGateway.startC2monClientSynchronous();
    }

    if (context == null) {
      context = C2monServiceGateway.getApplicationContext();
      tagSimulator = context.getBean(TagSimulator.class);
    }
  }

  /**
   * The C2MON Tag simulator allows changing locally the values
   * of the registered tags. This can be useful for testing the final
   * application.
   * @return The C2MON Tag simulator which allows changing locally the values
   *         of the registered tags
   */
  public static TagSimulator getTagSimulator() {
    if (context == null) {
      initialize();
    }
    return tagSimulator;
  }
}

/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2014 CERN. This program is free software; you can
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
 ******************************************************************************/

package cern.c2mon.client.ext.device;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.C2monServiceGateway.Module;

/**
 * Gateway to the C2monDeviceManager singleton.
 *
 * @author Justin Lewis Salmon
 */
public class C2monDeviceGateway {

  /** Class logger */
  private static final Logger LOG = Logger.getLogger(C2monDeviceGateway.class);

  /** The path to the core Spring XML */
  private static final String APPLICATION_SPRING_XML_PATH = "cern/c2mon/client/ext/device/config/c2mon-client-ext-device.xml";

  /** The extended Spring application context for this gateway */
  private static ApplicationContext context;

  /** Static reference to the <code>C2monDeviceManager</code> singleton instance */
  private static C2monDeviceManager deviceManager = null;

  /**
   * Hidden default constructor
   */
  private C2monDeviceGateway() {
  }

  /**
   * Initializes the C2monDeviceManager. Must be called before first use.
   *
   * @see #startC2monClientSynchronous(Module...)
   */
  public static synchronized void initialize() {
    if (C2monServiceGateway.getApplicationContext() == null) {
      C2monServiceGateway.startC2monClientSynchronous();
    }

    if (context == null) {
      context = new ClassPathXmlApplicationContext(new String[] { APPLICATION_SPRING_XML_PATH }, C2monServiceGateway.getApplicationContext());
      deviceManager = context.getBean(C2monDeviceManager.class);
    } else {
      LOG.warn("C2monDeviceManager is already initialized.");
    }
  }

  /**
   * @return the C2monDevieManager
   */
  public static synchronized C2monDeviceManager getDeviceManager() {
    if (deviceManager == null) {
      initialize();
    }

    return deviceManager;
  }
}

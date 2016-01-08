/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2013 CERN. This program is free software; you can
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
package cern.c2mon.client.ext.rbac;

import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.common.service.SessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@Slf4j
public class C2monSessionGateway {
  /** The path to the core Spring XML */
  private static final String APPLICATION_SPRING_XML_PATH = "cern/c2mon/client/ext/rbac/config/c2mon-client-ext-rbac.xml";

  /** The extended SPRING application context for this gateway */
  private static ApplicationContext context;

  /** Static reference to the {@link SessionService} singleton instance */
  private static SessionService sessionService = null;

  /**
   * Hidden default constructor
   */
  private C2monSessionGateway() {
    // Do nothing
  }
  
  
  /**
   * Initializes the C2MON ServiceSession. Must be called before using for the
   * first time {@link #getSessionService()}.
   * @see #startC2monClientSynchronous(Module...)
   */
  public static synchronized void initialize() {
    if (C2monServiceGateway.getApplicationContext() == null) {
      C2monServiceGateway.startC2monClientSynchronous();
    }
    initiateSessionService();
  }

  /**
   * Private method which initiates the static field(s) of this gateway by retrieving
   * it from the extended gateway {@link #context}.
   */
  private static void initiateSessionService() {
    if (context == null) {
      context = new ClassPathXmlApplicationContext(new String[]{APPLICATION_SPRING_XML_PATH}, C2monServiceGateway.getApplicationContext());
      sessionService = context.getBean(SessionService.class);
    }
    else {
      log.debug("C2monSessionService is already initialized.");
    }
  }

  /**
   * The SessionService allows a module to get authentication access.
   *
   * @return The SessionService which allows to secure access to modules based on authentication.
   */
  public static SessionService getSessionService() {
    return sessionService;
  }
}

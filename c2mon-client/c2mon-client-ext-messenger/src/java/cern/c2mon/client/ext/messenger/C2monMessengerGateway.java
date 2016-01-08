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
package cern.c2mon.client.ext.messenger;

import cern.c2mon.client.core.C2monServiceGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@Slf4j
public class C2monMessengerGateway {
  /** The path to the core Spring XML */
  private static final String APPLICATION_SPRING_XML_PATH = "cern/c2mon/client/ext/messenger/config/c2mon-client-ext-messenger.xml";

  /** The extended SPRING application context for this gateway */
  private static ApplicationContext context;

  /** Static reference to the {@link BroadcastMessageService} singleton instance */
  private static BroadcastMessageService broadcastMessageService = null;

  /**
   * Hidden default constructor
   */
  private C2monMessengerGateway() {
    // Do nothing
  }
  
  
  /**
   * Initializes the C2MON BroadcastMessageService. Must be called before using for the
   * first time {@link #getBroadcastMessageService()}.
   * @see #startC2monClientSynchronous(Module...)
   */
  public static synchronized void initialize() {
    if (C2monServiceGateway.getApplicationContext() == null) {
      C2monServiceGateway.startC2monClientSynchronous();
    }
    initiatesessionService();
  }

  /**
   * Private method which initiates the static field(s) of this gateway by retrieving
   * it from the extended gateway {@link #context}.
   */
  private static void initiatesessionService() {
    if (context == null) {
      context = new ClassPathXmlApplicationContext(new String[]{APPLICATION_SPRING_XML_PATH}, C2monServiceGateway.getApplicationContext());
      broadcastMessageService = context.getBean(BroadcastMessageService.class);
    }
    else {
      log.debug("BroadcastMessageService is already initialized.");
    }
  }

  public static BroadcastMessageService getBroadcastMessageService() {
    return broadcastMessageService;
  }
}

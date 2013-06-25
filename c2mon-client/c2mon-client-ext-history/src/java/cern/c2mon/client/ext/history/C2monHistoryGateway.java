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
package cern.c2mon.client.ext.history;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cern.c2mon.client.core.C2monServiceGateway;

public class C2monHistoryGateway {
  
  /** Class logger */
  private static final Logger LOG = Logger.getLogger(C2monHistoryGateway.class);
  
  /** The path to the core Spring XML */
  private static final String APPLICATION_SPRING_XML_PATH 
    = "classpath:cern/c2mon/client/ext/history/springConfig/spring-history.xml";
  
  /** The extended SPRING application context for this gateway */
  public static ApplicationContext context;
  
  /** Static reference to the <code>C2monHistoryManager</code> singleton instance */
  private static C2monHistoryManager historyManager = null;
  
  /**
   * Hidden default constructor
   */
  private C2monHistoryGateway() {
    // Do nothing
  }
  
  /**
   * Initializes the C2monHistoryManager. Must be called before using for the
   * first time {@link #getTagSimulator()}.
   * @see #startC2monClientSynchronous(Module...)
   */
  public static synchronized void initialize() {
    if (C2monServiceGateway.getApplicationContext() == null) {
      C2monServiceGateway.startC2monClientSynchronous();
    }
    initiateHistoryManager();
  }

  /**
   * Private method which initiates the static field(s) of this gateway by retrieving
   * it from the extended gateway {@link #context}.
   */
  private static void initiateHistoryManager() {
    if (context == null) {
      context = new ClassPathXmlApplicationContext
          (new String[]{APPLICATION_SPRING_XML_PATH}, C2monServiceGateway.getApplicationContext());
      historyManager = context.getBean(C2monHistoryManager.class);
    }
    else {
      LOG.warn("C2monHistoryManager is already initialized.");
    }
  }

 /**
  * @return The C2MON history manager which allows 
  *         switching data into history mode.
  */
  public static synchronized C2monHistoryManager getHistoryManager() {
    
    if (historyManager == null) {
      initialize();
    }
    return historyManager;
  }
}

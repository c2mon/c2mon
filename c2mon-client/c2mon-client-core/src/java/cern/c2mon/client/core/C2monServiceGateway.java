/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN. This program is free software; you can
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
package cern.c2mon.client.core;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

/**
 * This class is the main facade for all applications using the
 * C2MON client API.
 * <p>
 * The C2MON service gateway provides access to the different
 * C2MON manager singleton instances. A client application should
 * only use functionality which are provided by these classes.
 *
 * @author Matthias Braeger
 */
public final class C2monServiceGateway {
  
  /** Class logger */
  private static final Logger LOGGER = Logger.getLogger(C2monServiceGateway.class);
  
  /** Static reference to the <code>C2monShortTermLogManager</code> singleton instance */
  private static C2monHistoryManager stlManager = null;
  
  /** Static reference to the <code>C2monTagManager</code> singleton instance */
  private static C2monTagManager tagManager = null;
  
  /** Static reference to the <code>C2monSessionManager</code> singleton instance */
  private static C2monSessionManager sessionManager = null;
  
  /** Static reference to the <code>C2monSupervisionManager</code> singleton instance */
  private static C2monSupervisionManager supervisionManager = null;
  
  /**
   * Hidden constructor
   */
  private C2monServiceGateway() {
    // Do nothing
  }

  
  /**
   * @return The C2MON tag manager, which is managing
   *         the tag subscribtion and unsubscription.
   */
  public static C2monTagManager getTagManager() {
    return tagManager;
  }
  
  
  /**
   * @return The C2MON tag manager, which is managing
   *         the tag subscribtion and unsubscription.
   */
  public static C2monHistoryManager getShortTermLogManager() {
    return stlManager;
  }

  
  /**
   * @return the sessionManager
   */
  public static C2monSessionManager getSessionManager() {
    return sessionManager;
  }

  
  /**
   * @return the heartbeatManager
   */
  public static C2monSupervisionManager getSupervisionManager() {
    return supervisionManager;
  }

  /**
   * Starts the C2MON core. Must be called at application start-up.
   * 
   * <p>This method needs to be called before the C2monServiceGateway 
   * can be used. It should be called synchronously by the main application
   * thread, and will return once the core is ready for use. Notice
   * that the method will return even if the core cannot connect to
   * JMS (reconnection attempts will be made until successful).
   */
  public static void startC2monClient() {
    LOGGER.info("Starting C2MON client core.");
    final ClassPathXmlApplicationContext xmlContext = 
                    new ClassPathXmlApplicationContext("cern/c2mon/client/core/config/c2mon-client.xml");
    xmlContext.start();
    xmlContext.registerShutdownHook();
  }
  
  /**
   * The lifecycle of this inner class is managed by the Spring
   * context. It's purpose is to set the static fields of the
   * gateway.
   *
   * @author Matthias Braeger
   */
  @Service
  private class SpringGatewayInitializer {
    
    /**
     * Default Constructor used by the Spring container
     * @param pTagManager The tag manager singleton 
     * @param pSessionManager The session manager singleton
     * @param pHeartbeatManager The heartbeat singleton
     * @param pShortTermLogManager The short term log manager
     */
    @Autowired
    private SpringGatewayInitializer(
        final C2monTagManager pTagManager,
        final C2monSessionManager pSessionManager,
        final C2monSupervisionManager pHeartbeatManager,
        final C2monHistoryManager pShortTermLogManager) {
      
      tagManager = pTagManager;
      sessionManager = pSessionManager;
      supervisionManager = pHeartbeatManager;
      stlManager = pShortTermLogManager;
    }
  }
}

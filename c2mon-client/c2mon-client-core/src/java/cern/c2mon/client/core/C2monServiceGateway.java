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

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cern.c2mon.client.module.C2monAdminMessageManager;

/**
 * This class is the main facade for all applications using the
 * C2MON client API.
 * <p>
 * The C2MON service gateway provides access to the different
 * C2MON manager singleton instances. A client application should
 * only use functionality which are provided by these classes.
 * <p>
 * 
 *
 * @author Matthias Braeger
 */
public final class C2monServiceGateway {
  
  /** Class logger */
  private static final Logger LOG = Logger.getLogger(C2monServiceGateway.class);
  
  /** The path to the core Spring XML */
  private static final String APPLICATION_SPRING_XML_PATH = "cern/c2mon/client/core/config/c2mon-client.xml";
  
  /**
   * The maximum amount of time in milliseconds which the C2MON ServiceGateway shall
   * wait before aborting waiting that the connection to the C2MON server is established. 
   */
  private static final Long MAX_INITIALIZATION_TIME = 60000L;
  
  /** Static reference to the <code>C2monSessionManager</code> singleton instance */
  private static C2monSessionManager sessionManager = null;
  
  /** Static reference to the <code>C2monCommandManager</code> singleton instance */
  private static C2monCommandManager commandManager = null;
  
  /** Static reference to the <code>C2monHistoryManager</code> singleton instance */
  private static C2monHistoryManager historyManager = null;
  
  /** Static reference to the <code>C2monTagManager</code> singleton instance */
  private static C2monTagManager tagManager = null;
  
  /** Static reference to the <code>C2monSupervisionManager</code> singleton instance */
  private static C2monSupervisionManager supervisionManager = null;
  
  /** Static reference to the {@link C2monAdminMessageManager} singleton instance */
  private static C2monAdminMessageManager adminMessageManager = null;
  
  /**
   * Hidden constructor
   */
  private C2monServiceGateway() {
    // Do nothing
  }

  /**
   * @return The C2MON tag manager, which is managing
   *         the command tags
   */
  public static C2monSessionManager getSessionManager() {
    return sessionManager;
  }
  
  /**
   * @return The C2MON tag manager, which is managing
   *         the command tags
   */
  public static C2monCommandManager getCommandManager() {
    return commandManager;
  }
  
  
  /**
   * @return The C2MON tag manager, which is managing
   *         the tag subscribtion and unsubscription.
   */
  public static C2monTagManager getTagManager() {
    return tagManager;
  }
  
  
  /**
   * @return The C2MON history manager which allows 
   *         switching data into history mode.
   */
  public static C2monHistoryManager getHistoryManager() {
    return historyManager;
  }
  
  /**
   * @return the supervision manager
   */
  public static C2monSupervisionManager getSupervisionManager() {
    return supervisionManager;
  }
  
  /**
   * @return the admin message manager
   */
  public static C2monAdminMessageManager getAdminMessageManager() {
    if (adminMessageManager == null) {
      throw new RuntimeException("The admin message module is not enabled. When starting the C2mon client, please specify in the parameters to enable the admin message module.");
    }
    return adminMessageManager;
  }

  /**
   * Starts the C2MON core. Must be called at application start-up.
   * <p>
   * This method needs to be called before the C2monServiceGateway 
   * can be used. It should be called synchronously by the main application
   * thread, and will return once the core is ready for use.
   * <p>
   * <b>Notice</b> that the method will return even if the core cannot connect to
   * JMS (reconnection attempts will be made until successful). You can check
   * the successful connection status with
   * {@link C2monSupervisionManager#isServerConnectionWorking()}. The advantage of
   * this behavior is that you can use the time in between to initialize your
   * application. However, if you don't want to check yourself that the connection
   * to the C2MON server is established you should maybe use
   * {@link #startC2monClientSynchronous(Module...)} instead.
   * 
   * @param modules the modules that should be supported by the service gateway
   * @see C2monSupervisionManager#isServerConnectionWorking()
   * @see #startC2monClientSynchronous(Module...)
   */
  public static void startC2monClient(final Module ... modules) {
    LOG.info("Starting C2MON client core.");
    
    final Set<String> springXmlFiles = getSpringXmlPathsOfModules(modules);
    springXmlFiles.add(APPLICATION_SPRING_XML_PATH);
    
    final ClassPathXmlApplicationContext xmlContext = 
                    new ClassPathXmlApplicationContext(springXmlFiles.toArray(new String[0]));
    
    initiateGatewayFields(xmlContext);
    registerModules(xmlContext, modules);
    
    xmlContext.registerShutdownHook();
  }
  
  /**
   * Starts the C2MON core. Must be called at application start-up.
   * <p>
   * This method needs to be called before the C2monServiceGateway 
   * can be used. It should be called synchronously by the main application
   * thread, and will return once the core is ready for use. 
   * <p>
   * <b>Notice</b> that the method won't return before the core has succesfully 
   * established the connection to the C2MON server. However, if the C2MON
   * Client API hasn't managed to connect after 60 seconds this method will
   * return with a {@link RuntimeException}.
   * 
   * @param modules the modules that should be supported by the service gateway
   * @exception RuntimeException In case the connection to the C2MON server could not
   *            be established within 60 seconds. However, the C2MON Client API will
   *            continue trying to establish the connection, but by throwing this
   *            exception we want to avoid that the application is blocking too long
   *            on this call.
   * @see C2monSupervisionManager#isServerConnectionWorking()
   * @see #startC2monClient(Module...)
   */
  public static void startC2monClientSynchronous(final Module ... modules) throws RuntimeException {
    startC2monClient(modules);
    
    LOG.info("Waiting for C2MON server connection (max " + MAX_INITIALIZATION_TIME / 1000  + " sec)...");
    
    Long startTime = System.currentTimeMillis();
    while (!supervisionManager.isServerConnectionWorking()) {
      try { Thread.sleep(200); } catch (InterruptedException ie) { /* Do nothing */ }
      if (System.currentTimeMillis() - startTime >= MAX_INITIALIZATION_TIME) {
        throw new RuntimeException(
            "Waited " 
            + MAX_INITIALIZATION_TIME / 1000
            + " seconds and the connection to C2MON server could still not be established.");
      }
    }
    LOG.info("C2MON server connection established!");
  }
  
  /**
   * Initiate the static fields, retrieving it from the <code>xmlContext</code>
   * 
   * @param xmlContext the application context
   */
  private static void initiateGatewayFields(final ClassPathXmlApplicationContext xmlContext) {
    sessionManager = xmlContext.getBean(C2monSessionManager.class);
    tagManager = xmlContext.getBean(C2monTagManager.class);
    supervisionManager = xmlContext.getBean(C2monSupervisionManager.class);
    historyManager = xmlContext.getBean(C2monHistoryManager.class);
    commandManager = xmlContext.getBean(C2monCommandManager.class);
  }
  
  /**
   * Gets beans from the XML context 
   * 
   * @param xmlContext the xml context to get the beans from
   * @param modules the modules to load
   */
  private static void registerModules(final ClassPathXmlApplicationContext xmlContext, final Module ... modules) {
    for (Module module : modules) {
      switch (module) {
      case ADMIN_MESSAGE:
        adminMessageManager = xmlContext.getBean(C2monAdminMessageManager.class);
        break;
      default:
        throw new RuntimeException(String.format("The Spring module '%s' is unknown.", module.toString()));
      }
    }
  }
  
  /**
   * Gives the spring XML paths for the given <code>modules</code>
   * 
   * @param modules the modules to get the context paths for
   * @return the spring context paths of the given modules
   */
  private static Set<String> getSpringXmlPathsOfModules(final Module ... modules) {
    final Set<String> contexts = new HashSet<String>();
    for (Module module : modules) {
      contexts.add(module.getXmlPath());
    }
    return contexts;
  }
  
  /**
   * Optional modules that can be included in the service gateway
   */
  public enum Module {
    /** The {@link C2monServiceGateway#getAdminMessageManager()} is supported */
    ADMIN_MESSAGE("cern/c2mon/client/module/adminmessage/config/c2mon-client-adminmessage.xml");
    
    /** The path to the spring xml file for the module */
    private final String xmlPath;
    
    /**
     * @param xmlPath The path to the spring xml file for the module
     */
    private Module(final String xmlPath) {
      this.xmlPath = xmlPath;
    }

    /** @return The path to the spring xml file for the module */
    protected String getXmlPath() {
      return xmlPath;
    }
  }
  
}

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
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
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
    xmlContext.registerShutdownHook();
  }
  
  /**
   * Start the C2MON core, importing properties from the specified location.
   * 
   * @param propertyFileLocation properties to load into context (eg. file:/user/smith/properties.txt or classpath:properties.txt)
   */
  public static void startC2monClient(final String propertyFileLocation) {
    LOGGER.info("Starting C2MON client core, loading properties from " + propertyFileLocation);    

    GenericBeanDefinition propertiesFactoryBean = new GenericBeanDefinition();
    propertiesFactoryBean.setBeanClass(PropertiesFactoryBean.class);
    MutablePropertyValues propertyValues = new MutablePropertyValues();
    propertyValues.addPropertyValue("location", propertyFileLocation);
    propertiesFactoryBean.setPropertyValues(propertyValues);    

    //start an initial Spring application context and register properties bean
    GenericApplicationContext ctx = new GenericApplicationContext();    
    ctx.registerBeanDefinition("clientProperties", propertiesFactoryBean);        
    ctx.refresh();
    
    String[] springXmlFiles = {"cern/c2mon/client/core/config/c2mon-client.xml",                                         
                               "cern/c2mon/client/core/config/c2mon-client-properties.xml"}; 
    
    final ClassPathXmlApplicationContext xmlContext = 
                    new ClassPathXmlApplicationContext(springXmlFiles, ctx);    
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
  private static class SpringGatewayInitializer {
    
    /**
     * Default Constructor used by the Spring container
     * @param pSessionManager The C2MON session manager
     * @param pTagManager The tag manager singleton 
     * @param psupervisionManager The supervision singleton
     * @param pHistoryManager The history manager
     * @param pCommandManager The command manager
     */
    @Autowired
    private SpringGatewayInitializer(
        final C2monSessionManager pSessionManager,
        final C2monTagManager pTagManager,
        final C2monSupervisionManager psupervisionManager,
        final C2monHistoryManager pHistoryManager,
        final C2monCommandManager pCommandManager) {
      
      tagManager = pTagManager;
      supervisionManager = psupervisionManager;
      historyManager = pHistoryManager;
      commandManager = pCommandManager;
    }
  }
}

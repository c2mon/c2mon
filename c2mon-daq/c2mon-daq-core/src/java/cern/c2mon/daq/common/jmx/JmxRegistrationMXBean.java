/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2013 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.daq.common.jmx;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

/**
 * Helper class for delegating all Jmx MBean Registration
 * 
 * @author Nacho Vilches
 *
 */
public class JmxRegistrationMXBean {
  /**
   * The logger
   */
  private static final Logger LOGGER = Logger.getLogger(JmxRegistrationMXBean.class);
  
  /**
   * MBean Type enum (for the JConsole path)
   */
  public enum MBeanType {
    JMS("JMSSessionManager");
    
    /**
     * The MBean type name
     */
    private String name;
    
    /**
     * The MBean type name
     * 
     * @param name The MBean type name
     */
    MBeanType(final String name) {
      this.name = name;
    }
    
    /**
     * @return The MBean object name
     */
    public final String getName() {
      return this.name;
    }
  }

  /**
   * The MBean object name
   */
  private ObjectName objectName;
  
  /**
   * The MBean object name (for the JConsole path)
   */
  private String name;
  
  /**
   * The MBean type (for the JConsole path)
   */
  private MBeanType mbeanType;
  
  /**
   * The current MBean Server
   */
  private final MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
  
  /**
   * Constructor
   * 
   * @param mbeanType The MBean type
   * @param name The MBean object name
   */
  public JmxRegistrationMXBean(final MBeanType mbeanType, final String name) {
    this.mbeanType = mbeanType;
    this.name = name;
  }
  /**
   * Set the path name to show in JConsole
   * 
   */
  private void setObjectName() {
    try {
      this.objectName = new ObjectName("cern.c2mon:type=" + this.mbeanType.getName() + ", name=" + this.name);
    }
    catch (MalformedObjectNameException e) {
      LOGGER.error("The MXBean of " + this.mbeanType.getName() + " type called " + this.name + " has a malformed object name. ", e);
    }
    catch (NullPointerException e) {
      LOGGER.error("The MXBean of " + this.mbeanType.getName() + " type called " + this.name + " has null pointer. ", e);
    }
  }
  
  /**
   * Registered the current instance as a 
   * JMX MBean
   * 
   * @param object The object to register in JMX
   */
  public final void registerMBean(final Object object) {
    try {
      if (this.mbeanServer != null) {
        // Set the Object Name
        this.setObjectName();
        // Register the MBean
        this.mbeanServer.registerMBean(object, this.objectName);
        
        LOGGER.info("registerMBean() - MXBean registered properly: " + this.name);
      }
      else {
        LOGGER.error("registerMBean() -  No MBeanServer found. MXBean registration failed: " + this.name);
      } 
    }
    catch (InstanceAlreadyExistsException e) {
      LOGGER.error("The MXBean instance for " + object.getClass() + " called " + this.name + " already exists. ");
    }
    catch (MBeanRegistrationException e) {
      LOGGER.error("The MXBean " + object.getClass() + " called " + this.name + " is already registered. ", e);
    }
    catch (NotCompliantMBeanException e) {
      LOGGER.error("The MXBean " + object.getClass() + " called " + this.name + " is a Not Compliant Bean. ", e);
    }
  }

}
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
package cern.c2mon.daq.common.jmx;


/**
 * Interface to handle the JConsole exposed methods for 
 * the JmsSender
 * 
 * @author Nacho Vilches
 *
 */
public interface JmsSenderMXBean {

  /**
   * Operation
   * 
   * Set Enable/Disable the current ActiveJmsSender
   * related with the MBean. 
   * 
   * @param value
   *    - Enable (true): start sending data to the JMS broker
   *    - Disable (false): stop sending data to the JMS broker
   */
  void jmsBrokerDataConnectionEnable(boolean value);
  
  
  /**
   * Read-only attribute 'name'
   * 
   * @return String Gets the name to show in the JConsole
   */
  String getBeanName();
  
  /**
   * Read-only attribute 'enabled'
   * 
   * @return boolean Gets the current sending status of the ActiveJmsSender
   */
  boolean getEnabled();
  
}

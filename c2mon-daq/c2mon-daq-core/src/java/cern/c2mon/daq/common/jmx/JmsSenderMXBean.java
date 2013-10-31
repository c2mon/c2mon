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

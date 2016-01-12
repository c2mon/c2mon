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
package cern.c2mon.shared.common.datatag.address;


/**
 * The LASERHardwareAddress interface is used by the LASERMessageHandler. 
 * It contains all information necessary to extract a data element from any 
 * LASER publication. 
 * @see cern.c2mon.daq.laser.LASERMessageHandler
 * @author W. Buczak
 * @version $Revision: 1.0
 */
 public interface LASERHardwareAddress extends HardwareAddress {
 
  // ---------------------------------------------------------------------------
  // Public accessor methods
  // ---------------------------------------------------------------------------
  
  /** 
   * Get the name of the LASER alarm category
   * The category me can never be null.
   * @return the name of the alarm category
   */
  public String getAlarmCategory();
  
 /** 
   * Get the name of the alarm family
   * The alarm family can never be null.  
   * @return the name of the alarm category
   */
  public String getFaultFamily();
  
 /** 
   * Get the name of the alarm fault member
   * The fault member me can never be null.
   * @return the name of the alarm category
   */  
  public String getFaultMember();
  
 /** 
   * Get the name of the alarm fault code
   * The code me can never be null.
   * @return the name of the alarm category
   */  
  public int getFalutCode();

}

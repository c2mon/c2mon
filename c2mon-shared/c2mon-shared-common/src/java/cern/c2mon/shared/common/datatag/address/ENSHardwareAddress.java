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
 * HardwareAddress implementation for the ENSMessageHandler.
 * @author J. Stowisek
 * @version $Revision: 1.2 $ ($Date: 2005/03/22 16:14:22 $ - $State: Exp $) 
 */

public interface ENSHardwareAddress extends HardwareAddress  {
  /**
   * Data type for analogue entities (measures) in the ENS.
   */
  public static final String TYPE_ANALOG = "ANL";

  /**
   * Data type for digital entities in the ENS.
   */
  public static final String TYPE_DIGITAL = "DIG";

  /**
   * Data type for "counters" in the ENS.
   */
  public static final String TYPE_COUNTER = "CNT";

  /**
   * Data type for simple controls (commands) in the ENS.
   */
  public static final String TYPE_CTRL_SIMPLE = "SIMPLE_CTRL";

  /**
   * Data type for set-point controls (commands) in the ENS.
   */
  public static final String TYPE_CTRL_SETPOINT = "SETP_CTRL";

  /**
   * Get the ENS address of the tag
   */
  public String getAddress();

  /**
   * Get the ENS data type of the tag.
   * @see #TYPE_ANALOG
   * @see #TYPE_DIGITAL
   * @return a String representation of the ENS data type for this tag.
   */
  public String getDataType();
  
}

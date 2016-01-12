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
 * A simple example of a TIM HardwareAddress.
 * This hardware address is not used by any DAQ module but merely serves as an
 * example for implementing real hardware address interfaces and classes.
 * 
 * @author J. Stowisek
 * @version $Revision: 1.5 $ ($Date: 2005/02/01 17:07:16 $ - $State: Exp $)
 */

public interface SimpleHardwareAddress extends HardwareAddress {
  // ---------------------------------------------------------------------------
  // Public constant definitions
  // ---------------------------------------------------------------------------
  /**
   * Get the internal address.
   */
  public String getAddress();
}

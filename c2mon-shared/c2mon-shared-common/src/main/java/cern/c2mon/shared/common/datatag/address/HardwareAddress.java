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

import cern.c2mon.shared.common.ConfigurationException;

import java.io.Serializable;

/**
 * Parent interface for all hardware addresses used by DAQ modules.
 * All TIM message handlers (e.g. DBMessagHandler, DIPMessageHandler) define
 * their own address format. Therefore, each of them will require a different
 * HardwareAddress sub-interface for accessing address information.
 * @author J. Stowisek
 * @version $Revision: 1.5 $ ($Date: 2007/07/04 12:39:13 $ - $State: Exp $)
 */
public interface HardwareAddress extends Serializable, Cloneable {
  /**
   * Get an XML representation of the HardwareAddress object.
   * @return an XML representation of the HardwareAddress object.
   */
  String toConfigXML();

  void validate() throws ConfigurationException;

  HardwareAddress clone();
}

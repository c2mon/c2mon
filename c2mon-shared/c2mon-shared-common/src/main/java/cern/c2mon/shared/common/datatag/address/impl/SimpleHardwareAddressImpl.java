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
package cern.c2mon.shared.common.datatag.address.impl;

import org.simpleframework.xml.Element;

import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.address.SimpleHardwareAddress;

/**
 * Implementation of the SimpleHardwareAddress interface.
 * 
 * This subclass of HardwareAddress can be used for representing very simple
 * hardware addresses, which can be packed into a single String, e.g. a URL.
 * If the hardware address is composed of several parameters, 
 * possibly also including numeric and boolean parameters, it is recommended to 
 * create a new HardwareAddress subclass instead of packing all parameters into
 * a String.
 *
 * @author Jan Stowisek
 * @version $Revision: 1.1 $ ($Date: 2005/02/01 17:07:16 $ - $State: Exp $)
 */
 
public class SimpleHardwareAddressImpl extends HardwareAddressImpl implements SimpleHardwareAddress {
  // ---------------------------------------------------------------------------
  // Private members
  // ---------------------------------------------------------------------------
  /** 
   * The actual hardware address in String format 
   */
  @Element
  protected String address;
  
  // ----------------------------------------------------------------------------
  // CONSTRUCTORS
  // ----------------------------------------------------------------------------
  /**
   * Default constructor for internal use.
   */
  protected SimpleHardwareAddressImpl() {
    /* Nothing to do */
  }

  /**
   * Constructor.
   * Initialises the address of the new SimpleHardwareAddress object with the
   * specified String.
   * @param pAddress the new address String
   */
  public SimpleHardwareAddressImpl(final String pAddress) throws ConfigurationException {
    setAddress(pAddress);
  }
  
  // ----------------------------------------------------------------------------
  // PUBLIC MEMBER ACCESSORS
  // ----------------------------------------------------------------------------
  /**
   * Get the internal address.
   * @return the address
   */
  public final String getAddress() {
    return this.address;
  }
  
  /**
   * Set a new internal address.
   * The internal address must not be null.
   * @param newAddress new value for the address String
   */
  protected final void setAddress(final String pAddress) throws ConfigurationException {
    if (pAddress == null) {
      throw new ConfigurationException(
          ConfigurationException.INVALID_PARAMETER_VALUE,
          "Parameter \"address\" cannot be null"
      );
    }
    this.address = pAddress;
    return;
  }
}

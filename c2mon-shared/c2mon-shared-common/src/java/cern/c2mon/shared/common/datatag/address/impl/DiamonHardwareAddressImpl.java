/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2009 CERN This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.shared.common.datatag.address.impl;

import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.address.DiamonHardwareAddress;

/**
 * Implementation of the <code>DiamonHardwareAddress</code> interface
 * 
 * @see cern.c2mon.shared.common.datatag.address.DiamonHardwareAddress
 * 
 * @author Matthias Braeger
 */
public class DiamonHardwareAddressImpl extends HardwareAddressImpl implements DiamonHardwareAddress {

  /** Auto generated serial version UID */
  private static final long serialVersionUID = -8737243065738648240L;

  /** The host name that shall be set through the XML configuration via reflection */
  protected String host = ""; 
  
  /**
   * @return The host name
   */
  public final String getHost() {
    return this.host;
  }
  
  /**
   * Set a new host name.
   * @param pHost new value for the host name
   * @exception ConfigurationException Thrown, if the host name is equals null.
   */
  protected final void setHost(final String pHost) throws ConfigurationException {
    if (pHost == null) {
      throw new ConfigurationException(
          ConfigurationException.INVALID_PARAMETER_VALUE,
          "Parameter \"host\" cannot be null"
      );
    }
    this.host = pHost;
  }
}

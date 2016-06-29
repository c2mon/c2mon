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
import cern.c2mon.shared.common.datatag.address.AlarmSourceHardwareAddress;
import lombok.Getter;



/**
 * Implementation of the AlarmSourceHardwareAddress interface, used by c2mon-daq-alarmsource module.
 *
 * @author Wojtek Buczak. Matthias Braeger
 */
@Getter
public final class AlarmSourceHardwareAddressImpl extends HardwareAddressImpl implements AlarmSourceHardwareAddress {

  /** Serial version UID */
  private static final long serialVersionUID = 1L;

  /** the name of the alarm family */
  @Element(name = "fault-family")
  protected String faultFamily;

  /** the name of the alarm fault member */
  @Element(name = "fault-member")
  protected String faultMember;


  /** the alarm fault code */
  @Element(name = "fault-code")
  protected int faultCode = -1;

  /**
   * Create a LASERHardwareAddress object with specified category, fault family, member and code.
   */
  public AlarmSourceHardwareAddressImpl(String faultFamily, String faultMember, int faultCode) throws ConfigurationException {
    setFaultFamily(faultFamily);
    setFaultMember(faultMember);
    setFaultCode(faultCode);
  }

  /**
   * Internal constructor required by the fromConfigXML method of the super class.
   */
  protected AlarmSourceHardwareAddressImpl() {
    /* Nothing to do */
  }

  protected final void setFaultFamily(final String faultFamily) throws ConfigurationException {
    validateFaultFamily(faultFamily);
    this.faultFamily = faultFamily;
  }



  private void validateFaultFamily(final String faultFamily) {
    if (faultFamily == null || faultFamily.isEmpty()) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"fault family\" cannot be null or empty.");
    }
  }


  protected final void setFaultMember(final String faultMember) throws ConfigurationException {
    validateFaultMember(faultMember);
    this.faultMember = faultMember;
  }



  private void validateFaultMember(final String faultMember) {
    if (faultMember == null || faultMember.isEmpty()) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"fault member\" cannot be null or empty.");
    }
  }

  protected final void setFaultCode(int faultCode) throws ConfigurationException {
    validateFaultCode(faultCode);
    this.faultCode = faultCode;
  }

  private void validateFaultCode(int faultCode) {
    if (faultCode <= 0) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"fault code\" cannot be <= 0.");
    }
  }

  @Override
  public void validate() throws ConfigurationException {
    validateFaultFamily(faultFamily);
    validateFaultMember(faultMember);
    validateFaultCode(faultCode);
  }

  @Override
  public final String getAlarmId() {
    return faultFamily + ":" + faultMember + ":" + faultCode;
  }

}

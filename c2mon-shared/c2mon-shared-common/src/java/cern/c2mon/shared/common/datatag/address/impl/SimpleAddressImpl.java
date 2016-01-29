package cern.c2mon.shared.common.datatag.address.impl;
/******************************************************************************
 * Copyright (C) 2010- CERN. All rights not expressly granted are reserved.
 * <p/>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p/>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/

import cern.c2mon.shared.common.datatag.address.SimpleAddress;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author Franz Ritter
 *
 */
@Getter
@Setter
public class SimpleAddressImpl extends HardwareAddressImpl implements SimpleAddress {

  protected Map<String, String> properties;

  SimpleAddressImpl(Map<String,String> properties){
    this.properties = properties;
  }

  SimpleAddressImpl(){

  }


}

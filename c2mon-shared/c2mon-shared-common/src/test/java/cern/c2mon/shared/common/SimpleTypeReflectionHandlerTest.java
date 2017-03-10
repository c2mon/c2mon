/******************************************************************************
 * Copyright (C) 2010-2017 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.shared.common;

import org.junit.Test;

import cern.c2mon.shared.common.datatag.address.impl.PLCHardwareAddressImpl;

public class SimpleTypeReflectionHandlerTest {

  private SimpleTypeReflectionHandler handler = new SimpleTypeReflectionHandler();

  @Test
  public void testSetSimpleField() throws NoSuchFieldException, IllegalAccessException {

    PLCHardwareAddressImpl address = new PLCHardwareAddressImpl(1, 1, 3, 1, 34.5f, 37.8f, "TEST");

    handler.setSimpleField(address, "physicalMinVal", Float.valueOf(40.234f));
    handler.setSimpleField(address, "physicalMaxVal", Double.valueOf(40.234d));
    handler.setSimpleField(address, "physicalMaxVal", Long.valueOf(40L));
  }
}

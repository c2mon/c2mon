/**
 * /******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * <p>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.daq.common.conf;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.daq.common.conf.equipment.TagChangerHelper;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class TagChangerHelperTest {

  TagChangerHelper dummyTagChangerHelper;
  HardwareAddress hardwareAddress;
  HardwareAddress oldHardwareAddress;

  @Before
  public void setUp() {
    dummyTagChangerHelper = new DummyTagChangerHelper();
    hardwareAddress = new HardwareAddress() {
      @Override
      public String toConfigXML() {
        return null;
      }

      @Override
      public void validate() throws ConfigurationException {

      }

      @Override
      public HardwareAddress clone() throws CloneNotSupportedException {
        return null;
      }
    };

    oldHardwareAddress = new HardwareAddress() {
      @Override
      public String toConfigXML() {
        return null;
      }

      @Override
      public void validate() throws ConfigurationException {

      }

      @Override
      public HardwareAddress clone() throws CloneNotSupportedException {
        return null;
      }
    };
  }

  @Test
  public void hasHardwareAddressChangedOldIsNull() {
    assertTrue("Hardware address change expected.", dummyTagChangerHelper.hasHardwareAddressChanged(hardwareAddress, null));
  }

  @Test
  public void hasHardwareAddressChangedNewIsNull() {
    assertTrue("Hardware address change expected.", dummyTagChangerHelper.hasHardwareAddressChanged(null, oldHardwareAddress));
  }

  @Test
  public void hasHardwareAddressChangedEqualAdresses() {
    assertFalse("Hardware address has changed, but it shouln't.", dummyTagChangerHelper.hasHardwareAddressChanged(hardwareAddress, oldHardwareAddress));
  }
}

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
package cern.c2mon.daq.common.conf;

import cern.c2mon.daq.common.conf.core.DefaultDataTagChanger;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class TagChangerHelperTest {

  private DefaultDataTagChanger dataTagChanger;
  private HardwareAddress hardwareAddress;
  private HardwareAddress oldHardwareAddress;

  @Before
  public void setUp() {
    dataTagChanger = new DefaultDataTagChanger();
    hardwareAddress = new HardwareAddress() {
      @Override
      public String toConfigXML() {
        return null;
      }

      @Override
      public void validate() throws ConfigurationException {

      }

      @Override
      public HardwareAddress clone(){
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
      public HardwareAddress clone() {
        return null;
      }
    };
  }

  @Test
  public void hasHardwareAddressChangedOldIsNull() {
    assertTrue("Hardware address change expected.", dataTagChanger.hasHardwareAddressChanged(hardwareAddress, null));
  }

  @Test
  public void hasHardwareAddressChangedNewIsNull() {
    assertTrue("Hardware address change expected.", dataTagChanger.hasHardwareAddressChanged(null, oldHardwareAddress));
  }

  @Test
  public void hasHardwareAddressChangedEqualAdresses() {
    assertFalse("Hardware address has changed, but it shouln't.", dataTagChanger.hasHardwareAddressChanged(hardwareAddress, oldHardwareAddress));
  }
}

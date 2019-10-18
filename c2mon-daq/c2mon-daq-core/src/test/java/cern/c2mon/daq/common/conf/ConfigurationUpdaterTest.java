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

import cern.c2mon.daq.common.conf.core.ConfigurationUpdater;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.command.SourceCommandTag;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.SourceDataTag;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.process.EquipmentConfiguration;
import cern.c2mon.shared.daq.config.*;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConfigurationUpdaterTest {
    private ConfigurationUpdater configurationUpdater = new ConfigurationUpdater();

    private class TestHardwareAddress implements HardwareAddress {

        private String testField;

        public TestHardwareAddress(String testField) {
            this.setTestField(testField);
        }

        @Override
        public String toConfigXML() {
            return null;
        }

        @Override
        public void validate() throws ConfigurationException {

        }

        @Override
        public HardwareAddress clone() {
            try {
                return (HardwareAddress) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * @param testField the testField to set
         */
        public void setTestField(String testField) {
            this.testField = testField;
        }

        /**
         * @return the testField
         */
        public String getTestField() {
            return testField;
        }

    }
    @Test
    public void testUpdateCommandTag() throws IllegalAccessException, NoSuchFieldException {
        SourceCommandTag sourceCommandTag = new SourceCommandTag(1L, "asd");
        sourceCommandTag.setSourceRetries(0);
        sourceCommandTag.setSourceTimeout(1000);
        TestHardwareAddress oldHwAddress = new TestHardwareAddress("start");
        sourceCommandTag.setHardwareAddress(oldHwAddress);
        CommandTagUpdate update = new CommandTagUpdate();
        update.setSourceRetries(1);
        update.addFieldToRemove("sourceTimeout");
        HardwareAddressUpdate hwUpdate = new HardwareAddressUpdate();
        hwUpdate.getChangedValues().put("testField", "new");
        update.setHardwareAddressUpdate(hwUpdate);
        assertEquals(1000, sourceCommandTag.getSourceTimeout());
        assertNotSame(update.getSourceRetries(), sourceCommandTag.getSourceRetries());
        configurationUpdater.updateCommandTag(update, sourceCommandTag);
        assertEquals(update.getSourceRetries().intValue(), sourceCommandTag.getSourceRetries());
        assertEquals("asd", sourceCommandTag.getName());
        assertEquals(0, sourceCommandTag.getSourceTimeout());
        assertEquals("new", ((TestHardwareAddress)sourceCommandTag.getHardwareAddress()).getTestField());
    }

    @Test
    public void testUpdateDataTag() throws IllegalAccessException, NoSuchFieldException {
        SourceDataTag sourceDataTag = new SourceDataTag(1L, "asd", false);
        sourceDataTag.setMaxValue(100L);
        sourceDataTag.setMode((short)1);
        TestHardwareAddress oldHwAddress = new TestHardwareAddress("start");
        DataTagAddress address = new DataTagAddress();
        address.setTimeDeadband(100);
        sourceDataTag.setAddress(address);
        address.setHardwareAddress(oldHwAddress);

        DataTagUpdate update = new DataTagUpdate();
        update.setMaxValue(1000L);
        update.addFieldToRemove("mode");
        DataTagAddressUpdate dataTagAddressUpdate = new DataTagAddressUpdate();
        dataTagAddressUpdate.setTimeDeadband(10);
        dataTagAddressUpdate.setFreshnessInterval(100);
        update.setDataTagAddressUpdate(dataTagAddressUpdate);
        HardwareAddressUpdate hwUpdate = new HardwareAddressUpdate();
        hwUpdate.getChangedValues().put("testField", "new");
        dataTagAddressUpdate.setHardwareAddressUpdate(hwUpdate);

        assertEquals(1,sourceDataTag.getMode());
        assertNotSame(update.getMaxValue(), sourceDataTag.getMaxValue());
        configurationUpdater.updateDataTag(update, sourceDataTag);
        assertEquals(update.getMaxValue(), sourceDataTag.getMaxValue());
        assertEquals("asd", sourceDataTag.getName());
        assertEquals(0,sourceDataTag.getMode());
        assertEquals(10, address.getTimeDeadband());
        assertEquals("new", ((TestHardwareAddress)sourceDataTag.getHardwareAddress()).getTestField());
        assertTrue(address.getFreshnessInterval() == 100);
    }

    @Test
    public void testEquipmentConfigurationUpdate() throws IllegalAccessException, NoSuchFieldException {
        EquipmentConfiguration equipmentConfiguration = new EquipmentConfiguration();
        equipmentConfiguration.setName("asd");
        equipmentConfiguration.setAliveTagId(5);
        equipmentConfiguration.setAliveTagInterval(100L);
        EquipmentConfigurationUpdate update = new EquipmentConfigurationUpdate();
        update.setAliveTagId(23L);
        update.addFieldToRemove("aliveInterval");
        assertEquals(100L, equipmentConfiguration.getAliveTagInterval());
        assertNotSame(update.getAliveTagId(), equipmentConfiguration.getAliveTagId());
        configurationUpdater.updateEquipmentConfiguration(update, equipmentConfiguration);
        assertEquals(update.getAliveTagId().longValue(), equipmentConfiguration.getAliveTagId());
        assertEquals("asd", equipmentConfiguration.getName());
        assertEquals(0L, equipmentConfiguration.getAliveTagInterval());
    }
}

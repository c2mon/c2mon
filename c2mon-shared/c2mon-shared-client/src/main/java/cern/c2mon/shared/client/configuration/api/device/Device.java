/*******************************************************************************
 * Copyright (C) 2010-2020 CERN. All rights not expressly granted are reserved.
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
 ******************************************************************************/
package cern.c2mon.shared.client.configuration.api.device;

import cern.c2mon.shared.client.configuration.api.util.ConfigurationEntity;
import cern.c2mon.shared.client.configuration.api.util.IgnoreProperty;
import cern.c2mon.shared.client.device.*;
import lombok.Data;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuration object for a Device.
 * Holds the information to create a {@link cern.c2mon.shared.client.configuration.ConfigurationElement}
 * related to a Device.
 * <p/>
 * For further information how to use instances of this for server configurations read <a
 * href="http://c2mon.web.cern.ch/c2mon/docs/#_offline_configuration_via_c2mon_database_test_purpose_only">this</a> documentation.
 * <p/>
 */
@Data
public class Device implements ConfigurationEntity {

    @IgnoreProperty
    private boolean updated;

    @IgnoreProperty
    private boolean created;

    @IgnoreProperty
    private boolean deleted;

    /**
     * Internal identifier of the device. Created dynamically by the
     * server if null.
     */
    @IgnoreProperty
    private Long id;

    /**
     * Name of the device
     */
    private String name;

    /**
     * Name of the device class that this device belongs to. Either deviceClassName or deviceClassId have to be specified.
     */
    private String className;

    /**
     * Unique identifier of the device class that this device belongs to. Either deviceClassName or deviceClassId have to be specified.
     */
    private Long classId;


    /**
     * Mapper bean representing a list of device properties.
     */
    private DevicePropertyList deviceProperties;

    /**
     * Mapper bean representing a list of device commands.
     */
    private DeviceCommandList deviceCommands;

    /**
     * Use this method to obtain a builder for a new device configuration object.
     *
     * @param name            the name of the device
     * @param deviceClassName the name of the device class that this device belongs to.
     * @return a new Device.CreateBuilder with the specified name and device class name
     */
    public static CreateBuilder create(String name, String deviceClassName) {
        Assert.hasText(name, "Device name is required!");
        Assert.hasText(deviceClassName, "Device Class name is required!");
        return new CreateBuilder(name, deviceClassName);
    }

    /**
     * Use this method to obtain a Builder for a new Device configuration object.
     *
     * @param name          the name of the device
     * @param deviceClassId the identifier of the device class that this Device belongs to.
     * @return a new Device.CreateBuilder with the specified name and device class ID
     */
    public static CreateBuilder create(String name, Long deviceClassId) {
        Assert.hasText(name, "Device name is required!");
        Assert.notNull(deviceClassId, "Device Class ID is required!");
        return new CreateBuilder(name, deviceClassId);
    }

    /**
     * Builder class for device configuration objects which should be newly created on the server
     */
    public static class CreateBuilder {
        private final Device deviceToBuild = new Device();
        private final Map<String, DeviceProperty> deviceProperties = new ConcurrentHashMap<>();
        private final Map<String, DeviceCommand> deviceCommands = new ConcurrentHashMap<>();

        /**
         * Create a new device with the given device class
         *
         * @param name            the name of the new device, must be unique within the device class
         * @param deviceClassName the name uniquely identifying the device class on the server
         */
        CreateBuilder(String name, String deviceClassName) {
            deviceToBuild.setName(name);
            deviceToBuild.setClassName(deviceClassName);
            deviceToBuild.setCreated(true);
        }

        /**
         * Create a new device with the given device class
         *
         * @param name          the name of the new device, must be unique within the device class
         * @param deviceClassId the unique identifier of the device class on the server
         */
        CreateBuilder(String name, long deviceClassId) {
            deviceToBuild.setName(name);
            deviceToBuild.setClassId(deviceClassId);
            deviceToBuild.setCreated(true);
        }

        /**
         * Explicitly set the ID of the device. If no ID is given, it will be created dynamically by the server. An
         * exception will be thrown when applying the configuration if the ID already exists on the server.
         *
         * @param id the unique identifier of the device
         * @return the Device.CreateBuilder with the specified device id
         */
        public Device.CreateBuilder id(Long id) {
            this.deviceToBuild.setId(id);
            return this;
        }

        /**
         * Creates a {@link DeviceProperty} adds it to the device. A device can implement only those properties
         * contained in the parent class, but is not required to do so.
         *
         * @param name       the name of the device property. It must be unique within the device and correspond to a
         *                   {@link Property} defined in the parent device class
         * @param value      the concrete value of the property
         * @param category   the category of this property (e.g. "tagId", "clientRule", "constantValue")
         * @param resultType the result type of this property (for rules and constant values, defaults to String)
         * @return the Device.CreateBuilder with the specified device property
         */
        public Device.CreateBuilder addDeviceProperty(String name, String value, String category, String resultType) {
            Assert.isNull(deviceProperties.get(name),
                    "A device property with name " + name + " was already added to the Device.");
            this.deviceProperties.put(name, new DeviceProperty(name, value, category, resultType));
            return this;
        }

        /**
         * Creates a {@link DeviceProperty} from the given name and description, and adds it as a property field to the
         * device property with the given propertyName. Property fields are concrete instances of fields, which are
         * optional single-depth nested properties.
         *
         * @param propertyName the name of the parent property, must be unique within the device class. A device
         *                     property with this name must already have been added to the device
         * @param name         the name of the property field, must be unique within the property and device class
         * @param value        the concrete value of the property
         * @param category     the category of this property (e.g. "tagId", "clientRule", "constantValue")
         * @param resultType   the result type of this property (for rules and constant values, defaults to String)
         * @return the Device.CreateBuilder with the specified property field added to the parent device property
         */
        public Device.CreateBuilder addPropertyField(String propertyName, String name, String value, String category, String resultType) {
            DeviceProperty parentProperty = deviceProperties.get(propertyName);
            Assert.notNull(parentProperty,
                    "No device property with the name {} has been added to the Device.");
            Assert.isNull(parentProperty.getFields().get(name),
                    "A property field with name " + name + " was already added to the device property " + propertyName);
            parentProperty.setFields(new DeviceProperty(name, value, category, resultType));
            return this;
        }


        /**
         * Creates a {@link DeviceCommand} and adds it to the device. A device can implement only those commands
         * contained in the parent class, but is not required to do so.
         *
         * @param name       the name of the device command. It must be unique within the device and correspond to a
         *                   {@link Command} defined in the parent device class
         * @param value      the concrete value of the command
         * @param category   category of this command (usually just "commandTagId")
         * @param resultType the result type of this command (defaults to String)
         * @return the Device.CreateBuilder with the specified device command
         */
        public Device.CreateBuilder addDeviceCommand(String name, String value, String category, String resultType) {
            Assert.isNull(deviceCommands.get(name),
                    "A device command with name " + name + " was already added to the Device.");
            this.deviceCommands.put(name, new DeviceCommand(name, value, category, resultType));
            return this;
        }

        /**
         * Creates a concrete device object from the builder information
         *
         * @return the device configuration object
         */
        public Device build() {
            this.deviceToBuild.setDeviceProperties(new DevicePropertyList(deviceProperties.values()));
            this.deviceToBuild.setDeviceCommands(new DeviceCommandList(deviceCommands.values()));
            return this.deviceToBuild;
        }
    }
}

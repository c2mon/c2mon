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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.util.Assert;

import java.util.ArrayList;
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
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class CreateBuilder {
        private Device deviceToBuild = new Device();
        private Map<String, DeviceProperty> deviceProperties = new ConcurrentHashMap<>();
        private Map<String, DeviceCommand> deviceCommands = new ConcurrentHashMap<>();


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
         * Creates a {@link DeviceCommand} with the category "commandTagId"  adds it to the device. A device can
         * implement only those commands contained in the parent class, but is not required to do so.
         *
         * @param name       the name of the device command. It must be unique within the device and correspond to a
         *                   {@link Command} defined in the parent device class
         * @param value      the concrete value of the command
         * @return the Device.CreateBuilder with the specified device command
         */
        public Device.CreateBuilder addCommand(String name, Long value) {
            assertUniqueName(deviceCommands, name);
            this.deviceCommands.put(name, DeviceCommand.forCommandTagId(null, name, value));
            return this;
        }

        /**
         * Creates a {@link DeviceProperty} with the category "tagId" and adds it to the device. The corresponding
         * {@link Property} with the same name must be defined in the parent class already.
         *
         * @param name       the name of the device property. It must be unique within the device and correspond to a
         *                   {@link Property} defined in the parent device class
         * @param value      the concrete tag ID which this DeviceProperty references.
         * @return the Device.CreateBuilder with the specified device property
         */
        public Device.CreateBuilder addPropertyForTagId(String name, Long value) {
            assertUniqueName(deviceProperties, name);
            this.deviceProperties.put(name, DeviceProperty.forTagId(null, name, value));
            return this;
        }

        /**
         * Creates a {@link DeviceProperty} with the category "clientRule" and adds it to the device. The corresponding
         * {@link Property} with the same name must be defined in the parent class already.
         *
         * @param name       the name of the device property. It must be unique within the device and correspond to a
         *                   {@link Property} defined in the parent device class
         * @param value      the concrete client rule
         * @param resultType the resulting value type
         * @return the Device.CreateBuilder with the specified device property
         */
        public Device.CreateBuilder addPropertyForClientRule(String name, String value, ResultType resultType) {
            assertUniqueName(deviceProperties, name);
            this.deviceProperties.put(name, DeviceProperty.forClientRule(null, name, value, resultType));
            return this;
        }

        /**
         * Creates a {@link DeviceProperty} with the category "constantValue" and adds it to the device. The
         * corresponding {@link Property} with the same name must be defined in the parent class already.
         *
         * @param name       the name of the device property. It must be unique within the device and correspond to a
         *                   {@link Property} defined in the parent device class
         * @param value      the concrete constant value
         * @param resultType the resulting value type
         * @return the Device.CreateBuilder with the specified device property
         */
        public <T> Device.CreateBuilder addPropertyForConstantValue(String name, T value, ResultType resultType) {
            assertUniqueName(deviceProperties, name);
            this.deviceProperties.put(name, DeviceProperty.forConstantValue(null, name, value, resultType));
            return this;
        }

        /**
         * Creates a {@link DeviceProperty} with the category "mappedProperty" and adds it to the device. A
         * corresponding {@link Property} with the same name must be defined in the parent class already. Mapped
         * properties allow to add property fields. These are optional single-depth collections of nested properties.
         * They can be added to this mapped property subsequently to the returned builder.
         *
         * @param name       the name of the device property. It must be unique within the device and correspond to a
         *                   {@link Property} defined in the parent device class
         * @return a Device.MappedPropertyBuilder which extends the Device.CreateBuilder by the methods to add fields
         * to this particular mapped property.
         */
        public Device.MappedPropertyBuilder createMappedProperty(String name) {
            assertUniqueName(deviceProperties, name);
            return new MappedPropertyBuilder(deviceToBuild, deviceProperties, deviceCommands, name);
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


    /**
     * Builder class for device configuration objects with mapped properties which should be newly created on the server.
     * An instance of this class allows to add property fields to the most recently added mapped property.
     */
    public static class MappedPropertyBuilder extends CreateBuilder {
        private final DeviceProperty propertyToBuild;

        protected MappedPropertyBuilder(Device device, Map<String, DeviceProperty> properties, Map<String, DeviceCommand> commands, String name) {
            super(device, properties, commands);
            propertyToBuild = DeviceProperty.forMappedProperty(null, name, new ArrayList<>());
            properties.put(name, propertyToBuild);
        }

        /**
         * Creates a {@link DeviceProperty} with the category "tagId" and adds it as a field to the mapped property.
         * The corresponding {@link Property} with the same name must be defined in the parent class already as a field
         * to the mapped property.
         *
         * @param name       the name of the property field. It must be unique within the mapped property and correspond
         *                   to a {@link Property} defined as a field to the mapped property.
         * @param value      the concrete tag ID which this property field references.
         * @return the Device.MappedPropertyBuilder with the specified property field.
         */
        public Device.MappedPropertyBuilder addFieldForTagId(String name, Long value) {
            assertUniqueName(propertyToBuild.getFields(), name);
            this.propertyToBuild.setFields(DeviceProperty.forTagId(null, name, value));
            return this;
        }

        /**
         * Creates a {@link DeviceProperty} with the category "clientRule" and adds it as a field to the mapped
         * property. The corresponding {@link Property} with the same name must be defined in the parent class already
         * as a field to the mapped property.
         *
         * @param name       the name of the property field. It must be unique within the mapped property and correspond
         *                   to a {@link Property} defined as a field to the mapped property.
         * @param value      the concrete client rule
         * @param resultType the resulting value type
         * @return the Device.MappedPropertyBuilder with the specified property field.
         */
        public Device.MappedPropertyBuilder addFieldForClientRule(String name, String value, ResultType resultType) {
            assertUniqueName(propertyToBuild.getFields(), name);
            this.propertyToBuild.setFields(DeviceProperty.forClientRule(null, name, value, resultType));
            return this;
        }

        /**
         * Creates a {@link DeviceProperty} with the category "constantValue" and adds it as a field to the mapped
         * property. The corresponding {@link Property} with the same name must be defined in the parent class already
         * as a field to the mapped property.
         *
         * @param name       the name of the property field. It must be unique within the mapped property and correspond
         *                   to a {@link Property} defined as a field to the mapped property.
         * @param value      the concrete constant value
         * @param resultType the resulting value type
         * @return the Device.MappedPropertyBuilder with the specified property field.
         */
        public <T> Device.MappedPropertyBuilder addFieldForConstantValue(String name, T value, ResultType resultType) {
            assertUniqueName(propertyToBuild.getFields(), name);
            this.propertyToBuild.setFields(DeviceProperty.forConstantValue(null, name, value, resultType));
            return this;
        }
    }

    private static <T extends DeviceElement> void assertUniqueName(Map<String, T> elementMap, String name) {
        Assert.isNull(elementMap.get(name), "A device element with name " + name + " was already added.");
    }
}
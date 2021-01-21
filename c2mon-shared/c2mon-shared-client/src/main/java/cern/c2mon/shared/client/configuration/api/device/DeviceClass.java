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
import cern.c2mon.shared.client.configuration.api.util.DefaultValue;
import cern.c2mon.shared.client.configuration.api.util.IgnoreProperty;
import cern.c2mon.shared.client.device.*;
import lombok.Data;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuration object for a DeviceClass.
 * Holds the information to create a {@link cern.c2mon.shared.client.configuration.ConfigurationElement}
 * related to a DeviceClass.
 * <p/>
 * For further information how to use instances of this for server configurations read <a
 * href="http://c2mon.web.cern.ch/c2mon/docs/#_offline_configuration_via_c2mon_database_test_purpose_only">this</a> documentation.
 * <p/>
 *
 */
@Data
public class DeviceClass implements ConfigurationEntity {

    @IgnoreProperty
    private boolean updated;

    @IgnoreProperty
    private boolean created;

    @IgnoreProperty
    private boolean deleted;

    /**
     * Unique identifier of the device class.
     */
    @IgnoreProperty
    private Long id;

    /**
     * Name of the device class.
     */
    private String name;

    /**
     * Free-text description of the device class.
     */
    @DefaultValue("<no description provided>")
    private String description;

    /**
     * Mapper bean representing a list of properties.
     */
    private PropertyList properties;

    /**
     * Mapper bean representing a list of commands.
     */
    private CommandList commands;

    /**
     * Use this method to obtain a builder for a new device class configuration object.
     * @param name the name of the device class
     * @return a new DeviceClass.CreateBuilder with the specified name.
     */
    public static CreateBuilder create(String name) {
        Assert.hasText(name, "Device Class name is required!");
        return new CreateBuilder(name);
    }

    /**
     * Builder class for device class configuration objects which should be newly created on the server
     */
    public static class CreateBuilder {
        private final DeviceClass deviceClassToBuild = new DeviceClass();
        private final Map<String, Property> properties = new ConcurrentHashMap<>();
        private final Map<String, Command> commands = new ConcurrentHashMap<>();

        /**
         * Create a new device class
         * @param name the name of the new device class, must be unique on the server
         */
        CreateBuilder(String name) {
            deviceClassToBuild.setName(name);
            deviceClassToBuild.setCreated(true);
        }


        /**
         * Explicitly set the ID of the device class. If no ID is given, it will be created dynamically by the server.
         * An exception will be thrown when applying the configuration if the ID already exists on the server.
         * @param id the unique identifier of the device class
         * @return the DeviceClass.CreateBuilder with the specified device id
         */
        public DeviceClass.CreateBuilder id(Long id) {
            this.deviceClassToBuild.setId(id);
            return this;
        }

        /**
         * Adds a description to the new device class
         * @param description a description of the new device class
         * @return  the DeviceClass.CreateBuilder with the specified description
         */
        public DeviceClass.CreateBuilder description(String description) {
            this.deviceClassToBuild.setDescription(description);
            return this;
        }

        /**
         * Creates a {@link Property} and adds it to the device class
         * @param name the name of the property, must be unique within the device class
         * @param description a description of the property
         * @return  the DeviceClass.CreateBuilder with the new property
         */
        public DeviceClass.CreateBuilder addProperty(String name, String description) {
            Assert.isNull(properties.get(name),
                    "A property with name " + name + " was already added to the Device Class.");
            this.properties.put(name, new Property(name, description));
            return this;
        }

        /**
         * Creates a {@link Property} from the given name and description, and adds it as a field to the property with
         * the given propertyName. Fields are optional single-depth nested properties.
         * @param propertyName the name of the parent property, must be unique within the device class. A property with
         *                     this name must already have been added to the device class
         * @param name the name of the field, must be unique within the property and device class
         * @param description a description of the field
         * @return the Device.CreateBuilder with the specified field added to the parent property
         */
        public DeviceClass.CreateBuilder addField(String propertyName, String name, String description) {
            Property parentProperty = properties.get(propertyName);
            Assert.notNull(parentProperty,
                    "No property with the name {} has been added to the Device Class.");
            Assert.isTrue(parentProperty.getFields().stream().map(Property::getName).noneMatch(s -> s.equals(name)),
                    "A field with name " + name + " was already added to the property " + propertyName);
            parentProperty.getFields().add(new Property(name, description));
            return this;
        }

        /**
         * Creates a {@link Command} and adds it to the device class.
         * @param name the name of the command, must be unique within the device class
         * @param description a description of the command
         * @return  the DeviceClass.CreateBuilder with the new command
         */
        public DeviceClass.CreateBuilder addCommand(String name, String description) {
            Assert.isNull(commands.get(name),
                    "A command with name " + name + " was already added to the Device Class.");
            this.commands.put(name, new Command(name, description));
            return this;
        }

        /**
         * Creates a concrete device class object from the builder information
         * @return the device class configuration object
         */
        public DeviceClass build() {
            this.deviceClassToBuild.setProperties(new PropertyList(properties.values()));
            this.deviceClassToBuild.setCommands(new CommandList(commands.values()));
            return this.deviceClassToBuild;
        }
    }
}

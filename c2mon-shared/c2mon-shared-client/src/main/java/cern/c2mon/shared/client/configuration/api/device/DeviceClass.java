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
import cern.c2mon.shared.client.device.Command;
import cern.c2mon.shared.client.device.CommandList;
import cern.c2mon.shared.client.device.Property;
import cern.c2mon.shared.client.device.PropertyList;
import lombok.Data;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Stream;

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
    private boolean updated = false;

    @IgnoreProperty
    private boolean created = false;

    @IgnoreProperty
    private boolean deleted = false;

    /**
     * Unique identifier of the {@link cern.c2mon.server.common.device.DeviceClassCacheObject}.
     */
    @IgnoreProperty
    private Long id;

    /**
     * Name of the {@link cern.c2mon.server.common.device.DeviceClassCacheObject}
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
     * Mapper bean representing a list of device commands.
     */
    private CommandList commands;


    /**
     * Use this method to obtain a Builder for a new DeviceClass configuration object.
     * @param name the name of the deviceClass
     * @return a new DeviceClass.CreateBuilder with the specified name.
     */
    public static CreateBuilder create(String name) {
        Assert.hasText(name, "Device Class name is required!");
        return new CreateBuilder(name);
    }

    public static class CreateBuilder {
        private final DeviceClass deviceClassToBuild = new DeviceClass();
        private final Set<Property> properties = new HashSet<>();
        private final Set<Command> commands = new HashSet<>();

        public CreateBuilder(String name) {
            deviceClassToBuild.setName(name);
            deviceClassToBuild.setCreated(true);
        }

        public DeviceClass.CreateBuilder id(Long id) {
            this.deviceClassToBuild.setId(id);
            return this;
        }

        public DeviceClass.CreateBuilder description(String description) {
            this.deviceClassToBuild.setDescription(description);
            return this;
        }

        public DeviceClass.CreateBuilder addProperty(String name, String description) {
            Assert.isTrue(properties.stream().map(Property::getName).noneMatch(s -> s.equals(name)),
                    "A property with this name was already added configured for the Device Class.");
            this.properties.add(new Property(name, description));
            return this;
        }

        public DeviceClass.CreateBuilder addProperty(Property... properties) {
            long duplicateOld = Arrays.stream(properties)
                    .filter(this.properties::contains)
                    .count();
            long distinctNew = Arrays.stream(properties)
                    .map(Property::getName)
                    .distinct().count();
            Assert.isTrue(duplicateOld == 0 && distinctNew == properties.length,
                    "Attempting to add property with same name twice to the Device Class.");
            this.properties.addAll(Arrays.asList(properties));
            return this;
        }

        public DeviceClass.CreateBuilder addCommand(String name, String description) {
            Assert.isTrue(commands.stream().map(Command::getName).noneMatch(s -> s.equals(name)),
                    "A command with this name was already added configured for the Device Class.");
            this.commands.add(new Command(name, description));
            return this;
        }

        public DeviceClass.CreateBuilder addCommand(Command... commands) {
            long singleOccurrences = Stream.of(Arrays.stream(commands), this.commands.stream())
                    .flatMap(o -> o)
                    .map(Command::getName)
                    .distinct().count();
            Assert.isTrue(singleOccurrences == commands.length,
                    "Attempting to add property with same name twice to the Device Class.");
            this.commands.addAll(Arrays.asList(commands));
            return this;
        }

        public DeviceClass build() {
            this.deviceClassToBuild.setProperties(new PropertyList(properties));
            this.deviceClassToBuild.setCommands(new CommandList(commands));
            return this.deviceClassToBuild;
        }
    }
}

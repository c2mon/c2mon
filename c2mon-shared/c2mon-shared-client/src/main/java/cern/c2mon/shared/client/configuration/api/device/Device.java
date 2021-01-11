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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@Data
public class Device implements ConfigurationEntity {

    @IgnoreProperty
    private boolean updated = false;

    @IgnoreProperty
    private boolean created = false;

    @IgnoreProperty
    private boolean deleted = false;

    /**
     * Unique identifier of the device class that this Device belongs to. Either deviceClassName or deviceClassId have to be specified.
     */
    @IgnoreProperty
    private Long id;

    /**
     * Name of the device class that this Device belongs to. Either deviceClassName or deviceClassId have to be specified.
     */
    private String name;

    private String className;

    private Long classId;

    private DevicePropertyList deviceProperties;

    private DeviceCommandList deviceCommands;

    public static CreateBuilder create(String name, String deviceClassName) {
        Assert.hasText(name, "Device name is required!");
        Assert.hasText(deviceClassName, "Device Class name is required!");
        return new CreateBuilder(name, deviceClassName);
    }

    public static CreateBuilder create(String name, Long deviceClassId) {
        Assert.hasText(name, "Device name is required!");
        Assert.notNull(deviceClassId, "Device Class ID is required!");
        return new CreateBuilder(name, deviceClassId);
    }

    public static UpdateBuilder update(Long id) {
        return new UpdateBuilder(id);
    }

    public static Device.UpdateBuilder update(String name) {
        return new Device.UpdateBuilder(name);
    }

    public static class CreateBuilder {
        private final Device deviceToBuild = new Device();
        private final Set<DeviceProperty> deviceProperties = new HashSet<>();
        private final Set<DeviceCommand> deviceCommands = new HashSet<>();

        public CreateBuilder(String name, String deviceClassName) {
            deviceToBuild.setName(name);
            deviceToBuild.setClassName(deviceClassName);
            deviceToBuild.setCreated(true);
        }

        public CreateBuilder(String name, long deviceClassId) {
            deviceToBuild.setName(name);
            deviceToBuild.setClassId(deviceClassId);
            deviceToBuild.setCreated(true);
        }

        public Device.CreateBuilder id(Long id) {
            this.deviceToBuild.setId(id);
            return this;
        }

        public Device.CreateBuilder addDeviceProperty(String name, String value, String category, String resultType) {
            Assert.isTrue(deviceProperties.stream().map(DeviceProperty::getName).noneMatch(s -> s.equals(name)),
                    "A property with this name was already added configured for the Device Class.");
            this.deviceProperties.add(new DeviceProperty(name, value, category, resultType));
            return this;
        }

        public Device.CreateBuilder addDeviceProperty(DeviceProperty... properties) {
            long singleOccurrences = Stream.of(Arrays.stream(properties), this.deviceProperties.stream())
                    .flatMap(o -> o)
                    .map(DeviceProperty::getName)
                    .distinct().count();
            Assert.isTrue(singleOccurrences == properties.length,
                    "Attempting to add property with same name twice to the Device Class.");
            this.deviceProperties.addAll(Arrays.asList(properties));
            return this;
        }

        public Device.CreateBuilder addDeviceCommand(String name, String value, String category, String resultType) {
            Assert.isTrue(deviceCommands.stream().map(DeviceCommand::getName).noneMatch(s -> s.equals(name)),
                    "A property with this name was already added configured for the Device Class.");
            this.deviceCommands.add(new DeviceCommand(name, value, category, resultType));
            return this;
        }

        public Device.CreateBuilder addDeviceCommand(DeviceCommand... commands) {
            long singleOccurrences = Stream.of(Arrays.stream(commands), this.deviceCommands.stream())
                    .flatMap(o -> o)
                    .map(DeviceCommand::getName)
                    .distinct().count();
            Assert.isTrue(singleOccurrences == commands.length,
                    "Attempting to add property with same name twice to the Device Class.");
            this.deviceCommands.addAll(Arrays.asList(commands));
            return this;
        }

        public Device build() {
            this.deviceToBuild.setDeviceProperties(new DevicePropertyList(deviceProperties));
            this.deviceToBuild.setDeviceCommands(new DeviceCommandList(deviceCommands));
            return this.deviceToBuild;
        }
    }

    public static class UpdateBuilder {

        private final Device deviceToBuild = new Device();

        public UpdateBuilder(String name) {
            deviceToBuild.setName(name);
        }

        public UpdateBuilder(Long id) {
            deviceToBuild.setId(id);
        }

        public Device.UpdateBuilder id(Long id) {
            this.deviceToBuild.setId(id);
            return this;
        }


        public Device build() {
            deviceToBuild.setUpdated(true);
            return this.deviceToBuild;
        }
    }
}

package cern.c2mon.shared.client.configuration.api.device;

import cern.c2mon.shared.client.configuration.api.util.ConfigurationEntity;
import cern.c2mon.shared.client.configuration.api.util.DefaultValue;
import cern.c2mon.shared.client.configuration.api.util.IgnoreProperty;
import cern.c2mon.shared.client.device.Property;
import cern.c2mon.shared.client.device.PropertyList;
import lombok.Data;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class DeviceClass implements ConfigurationEntity {

    @IgnoreProperty
    private boolean updated = false;

    @IgnoreProperty
    private boolean created = false;

    @IgnoreProperty
    private boolean deleted = false;

    /**
     * Unique identifier of the device class.
     */
    @IgnoreProperty
    private Long id;

    private String name;

    /**
     * Free-text description of the device class.
     */
    @DefaultValue("<no description provided>")
    private String description;

    private PropertyList properties;

    public static CreateBuilder create(String name) {
        Assert.hasText(name, "Device Class name is required!");
        return new CreateBuilder(name);
    }

    public static UpdateBuilder update(Long id) {
        return new UpdateBuilder(id);
    }

    public static DeviceClass.UpdateBuilder update(String name) {
        return new DeviceClass.UpdateBuilder(name);
    }

    public static class CreateBuilder {
        private final DeviceClass deviceClassToBuild = new DeviceClass();
        private final Set<Property> properties = new HashSet<>();

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
            long singleOccurrences = Stream.of(Arrays.stream(properties), this.properties.stream())
                    .flatMap(o -> o)
                    .map(Property::getName)
                    .distinct().count();
            Assert.isTrue(singleOccurrences == properties.length,
                    "Attempting to add property with same name twice to the Device Class.");
            this.properties.addAll(Arrays.asList(properties));
            return this;
        }

        public DeviceClass build() {
            this.deviceClassToBuild.setProperties(new PropertyList(properties));
            return this.deviceClassToBuild;
        }
    }

    public static class UpdateBuilder {

        private final DeviceClass deviceClassToBuild = new DeviceClass();

        public UpdateBuilder(String name) {
            deviceClassToBuild.setName(name);
        }

        public UpdateBuilder(Long id) {
            deviceClassToBuild.setId(id);
        }

        public DeviceClass.UpdateBuilder id(Long id) {
            this.deviceClassToBuild.setId(id);
            return this;
        }

        public DeviceClass.UpdateBuilder description(String description) {
            this.deviceClassToBuild.setDescription(description);
            return this;
        }

        public DeviceClass build() {
            deviceClassToBuild.setUpdated(true);
            return this.deviceClassToBuild;
        }
    }
}

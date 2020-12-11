package cern.c2mon.shared.client.configuration.api.device;

import cern.c2mon.shared.client.configuration.api.util.ConfigurationEntity;
import cern.c2mon.shared.client.configuration.api.util.DefaultValue;
import cern.c2mon.shared.client.configuration.api.util.IgnoreProperty;
import lombok.Data;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

@Data
public class DeviceClass implements ConfigurationEntity {

    @IgnoreProperty
    private boolean updated = false;

    @IgnoreProperty
    private boolean created = false;

    @IgnoreProperty
    private boolean deleted = false;

    /**
     * Unique identifier of the equipment.
     */
    @IgnoreProperty
    private Long id;

    private String name;


    /**
     * Free-text description of the equipment.
     */
    @DefaultValue("<no description provided>")
    private String description;

    /**
     * names for properties that compose the Device Class
     */
    @IgnoreProperty
    private List<String> properties = new ArrayList<>();

    /**
     * names for commands that compose the Device Class
     */
    @IgnoreProperty
    private List<String> commands = new ArrayList<>();


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
        private DeviceClass deviceClassToBuild = new DeviceClass();

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

        public DeviceClass build() {
            return this.deviceClassToBuild;
        }
    }

    public static class UpdateBuilder {

        private DeviceClass deviceClassToBuild = new DeviceClass();

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

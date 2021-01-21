package cern.c2mon.shared.client.configuration.api.device;

import cern.c2mon.shared.client.device.DeviceElement;
import cern.c2mon.shared.client.device.DeviceProperty;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class DeviceBuilderTest {


    @Test
    public void addDevicePropertiesToBuilderShouldAddAllPropertiesToEntity() {
        Device entity = Device.create("device", "deviceClass")
                .addPropertyForTagId("name1", 2L)
                .addPropertyForTagId("name2", 3L)
                .build();
        Assert.assertTrue(deviceElementListContains(entity.getDeviceProperties().getDeviceProperties(), "name1", String.valueOf(2L), "tagId"));
        Assert.assertTrue(deviceElementListContains(entity.getDeviceProperties().getDeviceProperties(), "name2", String.valueOf(3L), "tagId"));
    }

    @Test
    public void entityShouldOnlyContainDevicePropertiesPreviouslyAddedToBuilder() {
        Device entity = Device.create("device", "deviceClass")
                .addPropertyForTagId("name1", 2L)
                .addPropertyForTagId("name2", 3L)
                .build();
        Assert.assertEquals(2, entity.getDeviceProperties().getDeviceProperties().size());
    }


    @Test(expected = IllegalArgumentException.class)
    public void addingTwoDevicePropertiesWithSameNameShouldThrowError() {
        Device.create("device", "deviceClass")
                .addPropertyForTagId("name", 2L)
                .addPropertyForTagId("name", 3L);
    }

    @Test
    public void addPropertyFieldToDevicePropertyShouldAddPropertyFieldsToEntity() {
        Device entity = Device.create("device", "deviceClass")
                .createMappedProperty("parentName")
                .addFieldForTagId("fieldName", 4L)
                .build();
        DeviceProperty parentProperty = getDevicePropertyWithName(entity, "parentName");
        Assert.assertTrue(parentProperty.getFields().containsKey("fieldName"));
    }

    @Test
    public void addPropertyFieldsToBuilderShouldAddAllPropertyFieldsToEntity() {
        Device entity = Device.create("device", "deviceClass")
                .createMappedProperty("parentName")
                .addFieldForTagId("fieldName1", 1L)
                .addFieldForTagId("fieldName2", 2L)
                .build();
        DeviceProperty parentProperty = getDevicePropertyWithName(entity, "parentName");
        Assert.assertTrue(parentProperty.getFields().containsKey("fieldName1") && parentProperty.getFields().containsKey("fieldName2"));
    }

    @Test
    public void addDeviceCommandToBuilderShouldAddCommandToEntity() {
        Device entity = Device.create("device", "deviceClass")
                .addCommand("name", 1L)
                .build();
        Assert.assertTrue(deviceElementListContains(entity.getDeviceCommands().getDeviceCommands(), "name", String.valueOf(1L), "commandTagId"));
    }

    @Test
    public void addCommandsToBuilderShouldAddAllCommandsToEntity() {
        Device entity = Device.create("device", "deviceClass")
                .addCommand("name1", 1L)
                .addCommand("name2", 2L)
                .build();
        Assert.assertTrue(deviceElementListContains(entity.getDeviceCommands().getDeviceCommands(), "name1", String.valueOf(1L), "commandTagId"));
        Assert.assertTrue(deviceElementListContains(entity.getDeviceCommands().getDeviceCommands(), "name2", String.valueOf(2L), "commandTagId"));
    }

    @Test
    public void entityShouldOnlyContainCommandsPreviouslyAddedToBuilder() {
        Device entity = Device.create("device", "deviceClass")
                .addCommand("name1", 1L)
                .addCommand("name2", 2L)
                .build();
        Assert.assertEquals(2, entity.getDeviceCommands().getDeviceCommands().size());
    }


    @Test(expected = IllegalArgumentException.class)
    public void addingTwoCommandsWithSameNameShouldThrowError() {
        Device.create("device", "deviceClass")
                .addCommand("name", 1L)
                .addCommand("name", 2L);
    }

    private <T extends DeviceElement> boolean deviceElementListContains(List<T> elements, final String name, final String value, final String category) {
        return elements
                .stream()
                .anyMatch(p -> p.getName().equals(name) &&
                        p.getValue().equals(value) &&
                        p.getCategory().equals(category));
    }

    private DeviceProperty getDevicePropertyWithName(Device entity, String name) {
        return entity.getDeviceProperties().getDeviceProperties()
                .stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}

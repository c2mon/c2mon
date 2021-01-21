package cern.c2mon.shared.client.configuration.api.device;

import cern.c2mon.shared.client.device.DeviceElement;
import cern.c2mon.shared.client.device.DeviceProperty;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class DeviceBuilderTest {

    @Test
    public void addDevicePropertyToBuilderShouldAddPropertyToEntity() {
        Device entity = Device.create("device", "deviceClass")
                .addDeviceProperty("name", "value", "category", null)
                .build();
        Assert.assertTrue(deviceElementListContains(entity.getDeviceProperties().getDeviceProperties(), "name", "value", "category"));
    }

    @Test
    public void addDevicePropertiesToBuilderShouldAddAllPropertiesToEntity() {
        Device entity = Device.create("device", "deviceClass")
                .addDeviceProperty("name1", "value", "category", null)
                .addDeviceProperty("name2", "value", "category", null)
                .build();
        Assert.assertTrue(deviceElementListContains(entity.getDeviceProperties().getDeviceProperties(), "name1", "value", "category"));
        Assert.assertTrue(deviceElementListContains(entity.getDeviceProperties().getDeviceProperties(), "name2", "value", "category"));
    }

    @Test
    public void entityShouldOnlyContainDevicePropertiesPreviouslyAddedToBuilder() {
        Device entity = Device.create("device", "deviceClass")
                .addDeviceProperty("name1", "value", "category", null)
                .addDeviceProperty("name2", "value", "category", null)
                .build();
        Assert.assertEquals(2, entity.getDeviceProperties().getDeviceProperties().size());
    }


    @Test(expected = IllegalArgumentException.class)
    public void addingTwoDevicePropertiesWithSameNameShouldThrowError() {
        Device.create("device", "deviceClass")
                .addDeviceProperty("name", "value", "category", null)
                .addDeviceProperty("name", "value", "category", null);
    }

    @Test
    public void addPropertyFieldToDevicePropertyShouldAddPrpertyFieldsToEntity() {
        Device entity = Device.create("device", "deviceClass")
                .addDeviceProperty("parentName", "value", "category", null)
                .addPropertyField("parentName", "fieldName", "fieldValue", "fieldCategory", "fieldResultType")
                .build();
        DeviceProperty parentProperty = getDevicePropertyWithName(entity, "parentName");
        Assert.assertTrue(parentProperty.getFields().containsKey("fieldName"));
    }

    @Test
    public void addPropertyFieldsToBuilderShouldAddAllPropertyFieldsToEntity() {
        Device entity = Device.create("device", "deviceClass")
                .addDeviceProperty("parentName", "value", "category", null)
                .addPropertyField("parentName", "fieldName1", "fieldValue", "fieldCategory", "fieldResultType")
                .addPropertyField("parentName", "fieldName2", "fieldValue", "fieldCategory", "fieldResultType")
                .build();
        DeviceProperty parentProperty = getDevicePropertyWithName(entity, "parentName");
        Assert.assertTrue(parentProperty.getFields().containsKey("fieldName1") && parentProperty.getFields().containsKey("fieldName2"));
    }

    @Test
    public void addPropertyFieldsToBuilderShouldAddOnlyAddedPropertyFieldsToEntity() {
        Device entity = Device.create("device", "deviceClass")
                .addDeviceProperty("parentName", "value", "category", null)
                .addPropertyField("parentName", "fieldName1", "fieldValue", "fieldCategory", "fieldResultType")
                .addPropertyField("parentName", "fieldName2", "fieldValue", "fieldCategory", "fieldResultType")
                .build();
        DeviceProperty parentProperty = getDevicePropertyWithName(entity, "parentName");
        Assert.assertEquals(2, parentProperty.getFields().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addPropertyFieldToNonExistentDevicePropertyShouldThrowError() {
        Device.create("device", "deviceClass")
                .addPropertyField("parentName", "fieldName", "fieldValue", "fieldCategory", "fieldResultType");
    }

    @Test
    public void addDeviceCommandToBuilderShouldAddCommandToEntity() {
        Device entity = Device.create("device", "deviceClass")
                .addDeviceCommand("name", "value", "category", null)
                .build();
        Assert.assertTrue(deviceElementListContains(entity.getDeviceCommands().getDeviceCommands(), "name", "value", "category"));
    }

    @Test
    public void addCommandsToBuilderShouldAddAllCommandsToEntity() {
        Device entity = Device.create("device", "deviceClass")
                .addDeviceCommand("name1", "value", "category", null)
                .addDeviceCommand("name2", "value", "category", null)
                .build();
        Assert.assertTrue(deviceElementListContains(entity.getDeviceCommands().getDeviceCommands(), "name1", "value", "category"));
        Assert.assertTrue(deviceElementListContains(entity.getDeviceCommands().getDeviceCommands(), "name2", "value", "category"));
    }

    @Test
    public void entityShouldOnlyContainCommandsPreviouslyAddedToBuilder() {
        Device entity = Device.create("device", "deviceClass")
                .addDeviceCommand("name1", "value", "category", null)
                .addDeviceCommand("name2", "value", "category", null)
                .build();
        Assert.assertEquals(2, entity.getDeviceCommands().getDeviceCommands().size());
    }


    @Test(expected = IllegalArgumentException.class)
    public void addingTwoCommandsWithSameNameShouldThrowError() {
        Device.create("device", "deviceClass")
                .addDeviceCommand("name", "value", "category", null)
                .addDeviceCommand("name", "value", "category", null);
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

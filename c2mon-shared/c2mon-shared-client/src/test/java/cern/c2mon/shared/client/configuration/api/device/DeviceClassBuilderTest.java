package cern.c2mon.shared.client.configuration.api.device;

import cern.c2mon.shared.client.device.DeviceClassElement;
import cern.c2mon.shared.client.device.Property;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class DeviceClassBuilderTest {

    @Test
    public void addPropertyToBuilderShouldAddPropertyToEntity() {
        DeviceClass entity = DeviceClass.create("test")
                .addProperty("name", "description")
                .build();
        Assert.assertTrue(deviceClassElementListContains(entity.getProperties().getProperties(), "name", "description"));
    }

    @Test
    public void addPropertiesToBuilderShouldAddAllPropertiesToEntity() {
        DeviceClass entity = DeviceClass.create("test")
                .addProperty("name1", "description")
                .addProperty("name2", "description")
                .build();
        Assert.assertTrue(deviceClassElementListContains(entity.getProperties().getProperties(), "name1", "description"));
        Assert.assertTrue(deviceClassElementListContains(entity.getProperties().getProperties(), "name2", "description"));
    }

    @Test
    public void entityShouldOnlyContainPropertiesPreviouslyAddedToBuilder() {
        DeviceClass entity = DeviceClass.create("test")
                .addProperty("name1", "description")
                .addProperty("name2", "description")
                .build();
        Assert.assertEquals(2, entity.getProperties().getProperties().size());
    }


    @Test(expected = IllegalArgumentException.class)
    public void addingTwoPropertiesWithSameNameShouldThrowError() {
        DeviceClass.create("test")
                .addProperty("name", "description1")
                .addProperty("name", "description2");
    }

    @Test
    public void addFieldToPropertyShouldAddFieldsToEntity() {
        DeviceClass entity = DeviceClass.create("test")
                .addProperty("parentName", "description")
                .addField("parentName", "fieldName", "fieldDescription")
                .build();
        Property parentProperty = getPropertyWithName(entity, "parentName");
        Assert.assertTrue(deviceClassElementListContains(parentProperty.getFields(), "fieldName", "fieldDescription"));
    }

    @Test
    public void addFieldsToBuilderShouldAddAllFieldsToEntity() {
        DeviceClass entity = DeviceClass.create("test")
                .addProperty("parentName", "description")
                .addField("parentName", "fieldName1", "fieldDescription")
                .addField("parentName", "fieldName2", "fieldDescription")
                .build();
        Property parentProperty = getPropertyWithName(entity, "parentName");
        Assert.assertTrue(deviceClassElementListContains(parentProperty.getFields(), "fieldName1", "fieldDescription"));
        Assert.assertTrue(deviceClassElementListContains(parentProperty.getFields(), "fieldName2", "fieldDescription"));
    }

    @Test
    public void addFieldsToBuilderShouldAddOnlyAddedFieldsToEntity() {
        DeviceClass entity = DeviceClass.create("test")
                .addProperty("parentName", "description")
                .addField("parentName", "fieldName1", "fieldDescription")
                .addField("parentName", "fieldName2", "fieldDescription")
                .build();
        Property parentProperty = getPropertyWithName(entity, "parentName");
        Assert.assertEquals(2, parentProperty.getFields().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addFieldToNonExistentPropertyShouldThrowError() {
        DeviceClass.create("test")
                .addField("parentName", "fieldName", "fieldDescription");
    }

    @Test
    public void addCommandToBuilderShouldAddCommandToEntity() {
        DeviceClass entity = DeviceClass.create("test")
                .addCommand("name", "description")
                .build();
        Assert.assertTrue(deviceClassElementListContains(entity.getCommands().getCommands(), "name", "description"));
    }

    @Test
    public void addCommandsToBuilderShouldAddAllCommandsToEntity() {
        DeviceClass entity = DeviceClass.create("test")
                .addCommand("name1", "description")
                .addCommand("name2", "description")
                .build();
        Assert.assertTrue(deviceClassElementListContains(entity.getCommands().getCommands(), "name1", "description"));
        Assert.assertTrue(deviceClassElementListContains(entity.getCommands().getCommands(), "name2", "description"));
    }

    @Test
    public void entityShouldOnlyContainCommandsPreviouslyAddedToBuilder() {
        DeviceClass entity = DeviceClass.create("test")
                .addCommand("name1", "description")
                .addCommand("name2", "description")
                .build();
        Assert.assertEquals(2, entity.getCommands().getCommands().size());
    }


    @Test(expected = IllegalArgumentException.class)
    public void addingTwoCommandsWithSameNameShouldThrowError() {
        DeviceClass.create("test")
                .addCommand("name", "description1")
                .addCommand("name", "description2");
    }

    private <T extends DeviceClassElement> boolean deviceClassElementListContains(List<T> elements, String name, String description) {
        return elements
                .stream()
                .anyMatch(p -> p.getName().equals(name) && p.getDescription().equals(description));
    }
    private Property getPropertyWithName(DeviceClass entity, String name) {
        return entity.getProperties().getProperties()
                .stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}

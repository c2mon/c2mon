package cern.c2mon.shared.client.configuration.api.device;

import cern.c2mon.shared.client.device.DeviceElement;
import cern.c2mon.shared.client.device.DeviceProperty;
import cern.c2mon.shared.client.device.ResultType;
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

    @Test(expected = IllegalArgumentException.class)
    public void addPropertyFieldsWithSameNameShouldThrowException() {
        Device.create("device", "deviceClass")
                .createMappedProperty("parentName")
                .addFieldForTagId("fieldName", 1L)
                .addFieldForClientRule("fieldName", "rule", ResultType.STRING);
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

    @Test
    public void addPropertyForTagIdShouldSetCategoryToTagID() {
        Device entity = Device.create("device", "deviceClass")
                .addPropertyForTagId("tagIdProp", 1L)
                .build();
        DeviceProperty tagIdProp = getDevicePropertyWithName(entity, "tagIdProp");
        Assert.assertEquals("tagId", tagIdProp.getCategory());
    }


    @Test
    public void addPropertyForTagIdShouldSetResultTypeToString() {
        Device entity = Device.create("device", "deviceClass")
                .addPropertyForTagId("tagIdProp", 1L)
                .build();
        DeviceProperty tagIdProp = getDevicePropertyWithName(entity, "tagIdProp");
        Assert.assertEquals(ResultType.STRING, tagIdProp.getResultType());
    }

    @Test
    public void addPropertyForClientRuleShouldSetCategoryToClientRule() {
        Device entity = Device.create("device", "deviceClass")
                .addPropertyForClientRule("clientRuleProp", "A RULE", ResultType.INTEGER)
                .build();
        DeviceProperty clientRuleProp = getDevicePropertyWithName(entity, "clientRuleProp");
        Assert.assertEquals("clientRule", clientRuleProp.getCategory());
    }

    @Test
    public void addPropertyForClientRuleShouldSetResultTypeToGivenType() {
        Device entity;
        for (ResultType type : ResultType.values()) {
            entity = Device.create("device", "deviceClass")
                    .addPropertyForClientRule("clientRuleProp", "A RULE", type)
                    .build();
            DeviceProperty clientRuleProp = getDevicePropertyWithName(entity, "clientRuleProp");
            Assert.assertEquals(type, clientRuleProp.getResultType());
        }
    }

    @Test
    public void addPropertyForConstantValueShouldSetCategoryToConstantValue() {
        Device entity = Device.create("device", "deviceClass")
                .addPropertyForConstantValue("constantValueProp", 1, ResultType.INTEGER)
                .build();
        DeviceProperty constantValueProp = getDevicePropertyWithName(entity, "constantValueProp");
        Assert.assertEquals("constantValue", constantValueProp.getCategory());
    }

    @Test
    public void addPropertyForConstantValueShouldSetResultTypeToGivenType() {
        Device entity;
        for (ResultType type : ResultType.values()) {
            entity = Device.create("device", "deviceClass")
                    .addPropertyForConstantValue("constantValueProp", 1, type)
                    .build();
            DeviceProperty constantValueProp = getDevicePropertyWithName(entity, "constantValueProp");
            Assert.assertEquals(type, constantValueProp.getResultType());
        }
    }

    @Test
    public void addMappedPropertyShouldSetCategoryToMappedProperty() {
        Device entity = Device.create("device", "deviceClass")
                .createMappedProperty("mappedProp")
                .build();
        DeviceProperty mappedProp = getDevicePropertyWithName(entity, "mappedProp");
        Assert.assertEquals("mappedProperty", mappedProp.getCategory());
    }

    @Test
    public void addMappedPropertyShouldSetResultTypeToString() {
        Device entity = Device.create("device", "deviceClass")
                .createMappedProperty("mappedProp")
                .build();
        DeviceProperty mappedProp = getDevicePropertyWithName(entity, "mappedProp");
        Assert.assertEquals(ResultType.STRING, mappedProp.getResultType());
    }

    @Test
    public void addMappedPropertyShouldSetValueToNull() {
        Device entity = Device.create("device", "deviceClass")
                .createMappedProperty("mappedProp")
                .build();
        DeviceProperty mappedProp = getDevicePropertyWithName(entity, "mappedProp");
        Assert.assertNull(mappedProp.getValue());
    }

    @Test
    public void addFieldsToMappedPropertyShouldOnlyBeInCorrespondingMappedProperty() {
        Device entity = Device.create("device", "deviceClass")
                .createMappedProperty("shouldHaveFields")
                .addFieldForTagId("fieldName1", 1L)
                .addFieldForTagId("fieldName2", 2L)
                .createMappedProperty("shouldBeEmpty")
                .build();
        DeviceProperty shouldBeEmpty = getDevicePropertyWithName(entity, "shouldBeEmpty");
        Assert.assertTrue(shouldBeEmpty.getFields().isEmpty());
    }
    @Test
    public void addFieldForTagIdShouldSetCategoryToTagID() {
        Device entity = Device.create("device", "deviceClass")
                .createMappedProperty("parentProp")
                .addFieldForTagId("tagIdField", 1L)
                .build();
        DeviceProperty tagIdProp = getFieldWithName(entity, "parentProp", "tagIdField");
        Assert.assertEquals("tagId", tagIdProp.getCategory());
    }

    @Test
    public void addFieldForClientRuleShouldSetCategoryToClientRule() {
        Device entity = Device.create("device", "deviceClass")
                .createMappedProperty("parentProp")
                .addFieldForClientRule("clientRuleField", "A RULE", ResultType.INTEGER)
                .build();
        DeviceProperty clientRuleProp = getFieldWithName(entity, "parentProp", "clientRuleField");
        Assert.assertEquals("clientRule", clientRuleProp.getCategory());
    }

    @Test
    public void addFieldForConstantValueShouldSetCategoryToConstantValue() {
        Device entity = Device.create("device", "deviceClass")
                .createMappedProperty("parentProp")
                .addFieldForConstantValue("constantValueField", 1, ResultType.INTEGER)
                .build();
        DeviceProperty constantValueProp = getFieldWithName(entity, "parentProp", "constantValueField");
        Assert.assertEquals("constantValue", constantValueProp.getCategory());
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
    private DeviceProperty getFieldWithName(Device entity, String propertyName, String name) {
        DeviceProperty parent = getDevicePropertyWithName(entity, propertyName);
        if (parent != null) {
            return parent.getFields().getOrDefault(name, null);
        }
        return null;
    }
}

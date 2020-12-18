package cern.c2mon.shared.client.configuration.api.device;

import cern.c2mon.shared.client.device.Property;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class DeviceClassBuilderTest {

    @Test
    public void addPropertyToBuilderShouldAddPropertyToEntity() {
        Property expected = new Property("name", "description");
        DeviceClass entity = DeviceClass.create("test").addProperty(expected).build();
        Assert.assertTrue(entity.getProperties().getProperties().contains(expected));
    }

    @Test
    public void addPropertiesToBuilderShouldAddAllPropertiesToEntity() {
        Property expected1 = new Property("name1", "description");
        Property expected2 = new Property("name2", "description");
        DeviceClass entity = DeviceClass.create("test").addProperty(expected1, expected2).build();
        Assert.assertTrue(entity.getProperties().getProperties().containsAll(Arrays.asList(expected1, expected2)));
    }

    @Test
    public void entityShouldOnlyContainPropertiesPreviouslyAddedToBuilder() {
        Property expected1 = new Property("name1", "description");
        Property expected2 = new Property("name2", "description");
        DeviceClass entity = DeviceClass.create("test").addProperty(expected1, expected2).build();
        Assert.assertEquals(2, entity.getProperties().getProperties().size());
    }

    @Test
    public void addPropertyFromNameAndStringToBuilderShouldAddPropertyToEntity() {
        DeviceClass entity = DeviceClass.create("test").addProperty("name", "description").build();
        List<Property> properties = entity.getProperties().getProperties();
        Assert.assertTrue(properties.stream()
                .anyMatch(p -> p.getName().equals("name") && p.getDescription().equals("description")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addingTwoPropertiesWithSameNameShouldThrowError() {
        DeviceClass.create("test")
                .addProperty("name", "description1")
                .addProperty("name", "description2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addingTwoPropertiesWithSameNameSimultaneouslyShouldThrowError() {
        DeviceClass.create("test")
                .addProperty( new Property("name", "description1"), new Property( "name", "description2"));
    }
}

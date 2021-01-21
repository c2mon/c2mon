package cern.c2mon.server.configuration.parser.factory;

import cern.c2mon.server.cache.DeviceCache;
import cern.c2mon.server.cache.DeviceClassCache;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.cache.loading.SequenceDAO;
import cern.c2mon.server.common.device.DeviceCacheObject;
import cern.c2mon.server.common.device.DeviceClassCacheObject;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.shared.client.configuration.api.device.Device;
import cern.c2mon.shared.client.device.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class DeviceFactoryTest {

    DeviceClassCache devClassCacheMock = createNiceMock(DeviceClassCache.class);
    DeviceCache devCacheMock = createNiceMock(DeviceCache.class);
    SequenceDAO sequenceDAOMock = createNiceMock(SequenceDAO.class);
    DeviceFactory factory = new DeviceFactory(devCacheMock, devClassCacheMock, sequenceDAOMock);

    Device deviceWithClassName;


    @Before
    public void setUp() {
        deviceWithClassName = Device.create("device", "class name").build();
        reset(devClassCacheMock, sequenceDAOMock);
        expect(devClassCacheMock.getDeviceClassIdByName(anyString()))
                .andReturn(100L)
                .once();
        expect(sequenceDAOMock.getNextDeviceId())
                .andReturn(1L)
                .once();
    }

    @Test
    public void getIdShouldReturnIdIfConfigured() {
        Device deviceWithClassId = Device.create("minimal Device", 100L)
                .id(20L)
                .build();
        assertEquals(20L, (long) factory.getId(deviceWithClassId));
    }

    @Test(expected = CacheElementNotFoundException.class)
    public void shouldEscalateExceptionIfNoDeviceClassCouldBeFound() {
        reset(devClassCacheMock);
        expect(devClassCacheMock.getDeviceClassIdByName(anyString()))
                .andThrow(new CacheElementNotFoundException())
                .once();
        replay(sequenceDAOMock, devClassCacheMock);
        assertEquals(100L, (long) factory.getId(deviceWithClassName));
    }

    @Test(expected = ConfigurationParseException.class)
    public void getIdShouldThrowExceptionifNoClassIdIsConfigured() {
        reset(devClassCacheMock);
        replay(sequenceDAOMock, devClassCacheMock);
        assertEquals(1L, (long) factory.getId(deviceWithClassName));
    }

    @Test
    public void getIdShouldSetClassIdIfInCache() {
        replay(sequenceDAOMock, devClassCacheMock);
        factory.getId(deviceWithClassName);
        assertEquals(100L, (long) deviceWithClassName.getClassId());
    }

    @Test
    public void getIdShouldReturnExistingIdIfApplicable() {
        cern.c2mon.server.common.device.Device device = new DeviceCacheObject(40L, "device", 100L);
        expect(devCacheMock.getByDeviceClassId(100L))
                .andReturn(Collections.singletonList(device))
                .once();
        replay(sequenceDAOMock, devClassCacheMock, devCacheMock);
        assertEquals(40L, (long) factory.getId(deviceWithClassName));
    }

    @Test
    public void createIdShouldReturnIdIfConfigured() {
        Device deviceWithClassId = Device.create("minimal Device", 100L)
                .id(20L)
                .build();
        assertEquals(20L, (long) factory.getId(deviceWithClassId));
    }

    @Test(expected = ConfigurationParseException.class)
    public void createIdShouldThrowExceptionIfAlreadyInCache() {
        cern.c2mon.server.common.device.Device device = new DeviceCacheObject(20L, "device", 100L);
        expect(devCacheMock.getByDeviceClassId(100L))
                .andReturn(Collections.singletonList(device))
                .once();
        replay(sequenceDAOMock, devClassCacheMock, devCacheMock);
        factory.createId(deviceWithClassName);
    }

    @Test
    public void createIdShouldGetFromDAOIfNew() {
        replay(sequenceDAOMock, devClassCacheMock, devCacheMock);
        assertEquals(1L, (long)factory.createId(deviceWithClassName));
    }


    @Test
    public void createIdShouldSetDevicePropertyIdsIfNotConfigured() {
        Property property = new Property(50L, "property", "desc");
        DeviceClassCacheObject classWithProperty = new DeviceClassCacheObject(100L);
        classWithProperty.setName("device class");
        classWithProperty.setProperties(Collections.singletonList(property));

        Device device = Device.create("device", "class name")
                .addDeviceProperty("property", "1001", "tagId", null)
                .build();

        expect(devClassCacheMock.get(anyLong()))
                .andReturn(classWithProperty)
                .once();
        replay(sequenceDAOMock, devClassCacheMock, devCacheMock);

        factory.createId(device);
        assertEquals(50L, (long)getElementWithName(device.getDeviceProperties().getDeviceProperties(), "property").getId());
    }

    @Test(expected = ConfigurationParseException.class)
    public void createIdShouldThrowExceptionIfNoAppropriatePropertyExistsForDeviceProperty() {
        DeviceClassCacheObject classWithoutProperty = new DeviceClassCacheObject(100L);
        classWithoutProperty.setName("device class");
        Device device = Device.create("device", "class name")
                .addDeviceProperty("property", "1001", "tagId", null)
                .build();

        expect(devClassCacheMock.get(anyLong()))
                .andReturn(classWithoutProperty)
                .once();
        replay(sequenceDAOMock, devClassCacheMock, devCacheMock);
        factory.createId(device);
    }



    @Test(expected = ConfigurationParseException.class)
    public void createIdShouldThrowExceptionIfNoAppropriatePropertyExistsForOneDeviceProperty() {
        Property property = new Property(50L, "property", "desc");
        DeviceClassCacheObject classWithProperty = new DeviceClassCacheObject(100L);
        classWithProperty.setName("device class");
        classWithProperty.setProperties(Collections.singletonList(property));

        Device device = Device.create("device", "class name")
                .addDeviceProperty("property", "1001", "tagId", null)
                .addDeviceProperty("no", "corresponding", "property", null)
                .build();

        expect(devClassCacheMock.get(anyLong()))
                .andReturn(classWithProperty)
                .once();
        replay(sequenceDAOMock, devClassCacheMock, devCacheMock);
        factory.createId(device);
    }


    @Test
    public void createIdShouldSetFieldIds() {
        Property field = new Property(40L, "field", "desc");
        Property property = new Property( 50L, "property", "desc", Collections.singletonList(field));

        DeviceClassCacheObject classWithProperty = new DeviceClassCacheObject(100L);
        classWithProperty.setName("device class");
        classWithProperty.setProperties(Collections.singletonList(property));

        Device device = Device.create("device", "class name")
                .addDeviceProperty("property", "parentValue", "parentCategory", "parentResultType")
                .addPropertyField("property", "field", "1001", "tagId", null)
                .build();

        expect(devClassCacheMock.get(anyLong()))
                .andReturn(classWithProperty)
                .once();
        replay(sequenceDAOMock, devClassCacheMock, devCacheMock);

        factory.createId(device);

        DeviceProperty parentProperty = getElementWithName(device.getDeviceProperties().getDeviceProperties(), "property");
        assertEquals(40L, (long) parentProperty.getFields().get("field").getId());
    }

    @Test
    public void createIdShouldSetFieldIdsByName() {
        Property field1 = new Property(40L, "field1", "desc");
        Property field2 = new Property(41L, "field2", "desc");
        Property property = new Property( 50L, "property", "desc", Arrays.asList(field1, field2));

        DeviceClassCacheObject classWithProperty = new DeviceClassCacheObject(100L);
        classWithProperty.setName("device class");
        classWithProperty.setProperties(Collections.singletonList(property));

        Device device = Device.create("device", "class name")
                .addDeviceProperty("property", "parentValue", "parentCategory", "parentResultType")
                .addPropertyField("property", "field1", "1001", "tagId", null)
                .addPropertyField("property", "field2", "1001", "tagId", null)
                .build();

        expect(devClassCacheMock.get(anyLong()))
                .andReturn(classWithProperty)
                .once();
        replay(sequenceDAOMock, devClassCacheMock, devCacheMock);

        factory.createId(device);

        DeviceProperty parentProperty = getElementWithName(device.getDeviceProperties().getDeviceProperties(), "property");
        assertEquals(40L, (long) parentProperty.getFields().get("field1").getId());
        assertEquals(41L, (long) parentProperty.getFields().get("field2").getId());
    }


    @Test(expected = ConfigurationParseException.class)
    public void createIdShouldThrowExceptionForDevicePropertyWithoutProperty() {

        DeviceClassCacheObject classWithProperty = new DeviceClassCacheObject(100L);
        classWithProperty.setName("device class");

        Device device = Device.create("device", "class name")
                .addDeviceProperty("property", "1001", "", "")
                .build();

        expect(devClassCacheMock.get(anyLong()))
                .andReturn(classWithProperty)
                .once();
        replay(sequenceDAOMock, devClassCacheMock, devCacheMock);

        factory.createId(device);
    }

    @Test(expected = ConfigurationParseException.class)
    public void createIdShouldThrowExceptionForDeviceFieldWithoutField() {
        Property property = new Property( 50L, "property", "desc");

        DeviceClassCacheObject classWithProperty = new DeviceClassCacheObject(100L);
        classWithProperty.setName("device class");
        classWithProperty.setProperties(Collections.singletonList(property));

        Device device = Device.create("device", "class name")
                .addDeviceProperty("property", "parentValue", "parentCategory", "parentResultType")
                .addPropertyField("property", "field", "1001", "tagId", null)
                .build();

        expect(devClassCacheMock.get(anyLong()))
                .andReturn(classWithProperty)
                .once();
        replay(sequenceDAOMock, devClassCacheMock, devCacheMock);

        factory.createId(device);
    }


    @Test(expected = ConfigurationParseException.class)
    public void createIdShouldThrowExceptionForDeviceCommandWithoutCommand() {

        DeviceClassCacheObject classWithProperty = new DeviceClassCacheObject(100L);
        classWithProperty.setName("device class");

        Device device = Device.create("device", "class name")
                .addDeviceCommand("command", "1001", "", "")
                .build();

        expect(devClassCacheMock.get(anyLong()))
                .andReturn(classWithProperty)
                .once();
        replay(sequenceDAOMock, devClassCacheMock, devCacheMock);

        factory.createId(device);
    }


    @Test(expected = ConfigurationParseException.class)
    public void createIdShouldThrowExceptionIfFieldDoesNotExist() {
        Property property = new Property( 40L,"property", "desc");

        DeviceClassCacheObject classWithProperty = new DeviceClassCacheObject(100L);
        classWithProperty.setName("device class");
        classWithProperty.setProperties(Collections.singletonList(property));

        Device device = Device.create("device", "class name")
                .addDeviceProperty("property", "parentValue", "parentCategory", "parentResultType")
                .addPropertyField("property", "field", "1001", "tagId", null)
                .build();

        expect(devClassCacheMock.get(anyLong()))
                .andReturn(classWithProperty)
                .once();
        replay(sequenceDAOMock, devClassCacheMock, devCacheMock);

        factory.createId(device);
    }

    @Test
    public void createIdShouldSetCommandIds() {
        Command command = new Command( 50L, "command", "desc");

        DeviceClassCacheObject classWithProperty = new DeviceClassCacheObject(100L);
        classWithProperty.setName("device class");
        classWithProperty.setCommands(Collections.singletonList(command));

        Device device = Device.create("device", "class name")
                .addDeviceCommand("command", "value", "category", null)
                .build();

        expect(devClassCacheMock.get(anyLong()))
                .andReturn(classWithProperty)
                .once();
        replay(sequenceDAOMock, devClassCacheMock, devCacheMock);

        factory.createId(device);
        assertEquals(50L, (long) getElementWithName(device.getDeviceCommands().getDeviceCommands(), "command").getId());
    }

    private <T extends DeviceElement> T getElementWithName(List<T> elements, String name) {
        return elements.stream()
                .filter(e -> e.getName().equals(name))
                .findFirst().orElse(null);
    }
}
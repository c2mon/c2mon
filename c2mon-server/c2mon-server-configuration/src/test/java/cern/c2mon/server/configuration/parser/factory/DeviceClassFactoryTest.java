package cern.c2mon.server.configuration.parser.factory;

import cern.c2mon.server.cache.DeviceClassCache;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.cache.loading.SequenceDAO;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.device.DeviceClass;
import cern.c2mon.shared.client.device.Command;
import cern.c2mon.shared.client.device.Property;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class DeviceClassFactoryTest {
    DeviceClassCache devClassCacheMock = createNiceMock(DeviceClassCache.class);
    SequenceDAO sequenceDAOMock = createNiceMock(SequenceDAO.class);
    DeviceClassFactory factory = new DeviceClassFactory(devClassCacheMock, sequenceDAOMock);

    DeviceClass minimalDeviceClass = DeviceClass.create("no_props_or_cmds")
            .build();
    DeviceClass deviceClassWithId = DeviceClass.create("device Class with ID")
            .id(10L)
            .build();
    @Before
    public void setUp() {
        reset(devClassCacheMock, sequenceDAOMock);
        expect(devClassCacheMock.getDeviceClassIdByName(anyString()))
                .andThrow(new CacheElementNotFoundException())
                .once();
        expect(sequenceDAOMock.getNextDeviceClassId())
                .andReturn(1L)
                .once();
    }

    @Test
    public void createDevClassShouldCheckCacheForName() {
        replay(devClassCacheMock);
        factory.createId(minimalDeviceClass);
        verify(devClassCacheMock);
    }

    @Test(expected = ConfigurationParseException.class)
    public void createDevClassShouldThrowExceptionIfElementExistsInCache() {
        reset(devClassCacheMock);
        expect(devClassCacheMock.getDeviceClassIdByName(anyString()))
                .andReturn(0L)
                .once();
        replay(devClassCacheMock);
        factory.createId(minimalDeviceClass);
    }

    @Test
    public void createDevClassShouldNotCreateIdIfElementExistsInCache() {
        reset(devClassCacheMock, sequenceDAOMock);
        expect(devClassCacheMock.getDeviceClassIdByName(anyString()))
                .andReturn(0L)
                .once();
        replay(devClassCacheMock,sequenceDAOMock);
        try {
            factory.createId(minimalDeviceClass);
        } catch (Exception ignored) {}
        verify(sequenceDAOMock);
    }

    @Test
    public void createDevClassShouldNotCreateIfDeviceClassHadIs() {
        reset(sequenceDAOMock);
        replay(sequenceDAOMock);
        factory.createId(deviceClassWithId);
    }

    @Test
    public void createReturnConfiguredIdIfApplicable() {
        reset(sequenceDAOMock);
        assertEquals(10L, (long) factory.createId(deviceClassWithId));
    }

    @Test
    public void createDevClassShouldCreateAndReturnId() {
        replay(devClassCacheMock,sequenceDAOMock);
        assertEquals(1L, (long) factory.createId(minimalDeviceClass));
    }

    @Test
    public void createDevClassShouldCreateAndSetPropertyIdIfNeeded() {
        Property property = new Property("property", "desc");
        DeviceClass classWithProperties = DeviceClass.create("device with properties")
                .addProperty(property)
                .build();
        expect(sequenceDAOMock.getNextPropertyId())
                .andReturn(2L)
                .once();
        replay(devClassCacheMock,sequenceDAOMock);
        factory.createId(classWithProperties);
        assertEquals(2L, (long) property.getId());
    }

    @Test
    public void createDevClassShouldCreatePropertyIdForAllProperties() {
        Property property1 = new Property("property1", "desc");
        Property property2 = new Property("property2", "desc");
        DeviceClass classWithProperties = DeviceClass.create("device with properties")
                .addProperty(property1)
                .addProperty(property2)
                .build();
        expect(sequenceDAOMock.getNextPropertyId())
                .andReturn(2L)
                .times(2);
        replay(devClassCacheMock,sequenceDAOMock);
        factory.createId(classWithProperties);
        assertEquals(2L, (long) property1.getId());
        assertEquals(2L, (long) property2.getId());
    }

    @Test
    public void createDevClassShouldCreateFieldIdIfNeeded() {
        Property field = new Property("field", "description");
        Property property = new Property("property1", "desc", Arrays.asList(field));
        DeviceClass classWithProperties = DeviceClass.create("device with properties")
                .addProperty(property)
                .build();
        expect(sequenceDAOMock.getNextPropertyId())
                .andReturn(2L)
                .times(1);
        expect(sequenceDAOMock.getNextFieldId())
                .andReturn(3L)
                .times(1);
        replay(devClassCacheMock,sequenceDAOMock);
        factory.createId(classWithProperties);
        assertEquals(2L, (long) property.getId());
        assertEquals(3L, (long) field.getId());
    }

    @Test
    public void createDevClassShouldCreateCommandIfNeeded() {
        Command command = new Command("command", "desc");
        DeviceClass classWithCommand = DeviceClass.create("device with properties")
                .addCommand(command)
                .build();
        expect(sequenceDAOMock.getNextCommandId())
                .andReturn(4L)
                .times(1);
        replay(devClassCacheMock,sequenceDAOMock);
        factory.createId(classWithCommand);
        assertEquals(4L, (long) command.getId());
    }

    @Test
    public void createDevClassShouldOnlyCreateNeededProperties() {
        Property property1 = new Property("property1", "desc");
        Property property2 = new Property("property2", "desc");
        DeviceClass classWithProperties = DeviceClass.create("device with properties")
                .addProperty(property1)
                .addProperty(property2)
                .build();
        expect(sequenceDAOMock.getNextPropertyId())
                .andReturn(2L)
                .times(2);
        replay(devClassCacheMock,sequenceDAOMock);
        factory.createId(classWithProperties);
        verify(sequenceDAOMock);
    }

    @Test
    public void getIdShouldReturnConfiguredIdIfApplicable() {
        assertEquals(10L, (long) factory.getId(deviceClassWithId));
    }

    @Test
    public void getIdShouldRequestIdFromCacheIfNotConfigured() {
        reset(devClassCacheMock);
        expect(devClassCacheMock.getDeviceClassIdByName(anyString()))
                .andReturn(12L)
                .once();
        replay(devClassCacheMock);
        assertEquals(12L, (long) factory.getId(minimalDeviceClass));
    }

    @Test
    public void getIdShouldReturnNullIfNeitherConfiguredNorInCache() {
        replay(devClassCacheMock);
        assertNull(factory.getId(minimalDeviceClass));
    }

    @Test
    public void createInstanceShouldContainOneElementOnly() {
        List<ConfigurationElement> elements = factory.createInstance(minimalDeviceClass);
        assertEquals(1, elements.size());
    }

    @Test
    public void createInstanceShould() {
        List<ConfigurationElement> elements = factory.createInstance(minimalDeviceClass);
        ConfigurationElement configurationElement = elements.get(0);
        assertEquals(null, configurationElement.getElementProperties());
    }

}

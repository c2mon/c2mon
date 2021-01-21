package cern.c2mon.server.configuration.parser.factory;

import cern.c2mon.server.cache.DeviceClassCache;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.cache.loading.SequenceDAO;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.device.DeviceClass;
import cern.c2mon.shared.client.device.DeviceClassElement;
import cern.c2mon.shared.client.device.Property;
import org.junit.Before;
import org.junit.Test;

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
        replay(devClassCacheMock, sequenceDAOMock);
        try {
            factory.createId(minimalDeviceClass);
        } catch (Exception ignored) {
        }
        verify(sequenceDAOMock);
    }

    @Test
    public void createDevClassShouldNotCreateIfDeviceClassHadIs() {
        reset(sequenceDAOMock);
        replay(sequenceDAOMock);
        factory.createId(deviceClassWithId);
        verify(sequenceDAOMock);
    }

    @Test
    public void createReturnConfiguredIdIfApplicable() {
        reset(sequenceDAOMock);
        assertEquals(10L, (long) factory.createId(deviceClassWithId));
    }

    @Test
    public void createDevClassShouldCreateAndReturnId() {
        replay(devClassCacheMock, sequenceDAOMock);
        assertEquals(1L, (long) factory.createId(minimalDeviceClass));
    }

    @Test
    public void createDevClassShouldCreateAndSetPropertyIdIfNeeded() {
        DeviceClass classWithProperties = DeviceClass.create("device with properties")
                .addProperty("property", "desc")
                .build();
        expect(sequenceDAOMock.getNextPropertyId())
                .andReturn(2L)
                .once();
        replay(devClassCacheMock, sequenceDAOMock);
        factory.createId(classWithProperties);
        assertEquals(2L, ( long) getElementWithName(classWithProperties.getProperties().getProperties(), "property").getId());
    }

    @Test
    public void createDevClassShouldCreatePropertyIdForAllProperties() {
        DeviceClass classWithProperties = DeviceClass.create("device with properties")
                .addProperty("property1", "desc")
                .addProperty("property2", "desc")
                .build();
        expect(sequenceDAOMock.getNextPropertyId())
                .andReturn(2L)
                .times(2);
        replay(devClassCacheMock, sequenceDAOMock);
        factory.createId(classWithProperties);
        assertEquals(2L, (long) getElementWithName(classWithProperties.getProperties().getProperties(), "property1").getId());
        assertEquals(2L, (long) getElementWithName(classWithProperties.getProperties().getProperties(), "property2").getId());
    }

    @Test
    public void createDevClassShouldCreateFieldIdIfNeeded() {
        DeviceClass classWithProperties = DeviceClass.create("device with properties")
                .addProperty("property1", "desc")
                .addField("property1", "field", "description")
                .build();
        expect(sequenceDAOMock.getNextPropertyId())
                .andReturn(2L)
                .times(1);
        expect(sequenceDAOMock.getNextFieldId())
                .andReturn(3L)
                .times(1);
        replay(devClassCacheMock, sequenceDAOMock);
        factory.createId(classWithProperties);
        Property parentProperty = getElementWithName(classWithProperties.getProperties().getProperties(), "property1");
        assertEquals(2L, (long) parentProperty.getId());
        assertEquals(3L, (long) getElementWithName(parentProperty.getFields(), "field").getId());

    }

    @Test
    public void createDevClassShouldCreateCommandIfNeeded() {
        DeviceClass classWithCommand = DeviceClass.create("device with properties")
                .addCommand("command", "desc")
                .build();
        expect(sequenceDAOMock.getNextCommandId())
                .andReturn(4L)
                .times(1);
        replay(devClassCacheMock, sequenceDAOMock);
        factory.createId(classWithCommand);
        assertEquals(4L, (long) getElementWithName(classWithCommand.getCommands().getCommands(), "command").getId());
    }

    @Test
    public void createDevClassShouldOnlyCreateNeededProperties() {
        DeviceClass classWithProperties = DeviceClass.create("device with properties")
                .addProperty("property1", "desc")
                .addProperty("property2", "desc")
                .build();
        expect(sequenceDAOMock.getNextPropertyId())
                .andReturn(2L)
                .times(2);
        replay(devClassCacheMock, sequenceDAOMock);
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

    private <T extends DeviceClassElement> T getElementWithName(List<T> elements, String name) {
        return elements.stream()
                .filter(e -> e.getName().equals(name))
                .findFirst().orElse(null);
    }

}

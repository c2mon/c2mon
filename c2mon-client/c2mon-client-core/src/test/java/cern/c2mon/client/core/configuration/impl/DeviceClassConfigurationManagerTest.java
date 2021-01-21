package cern.c2mon.client.core.configuration.impl;

import cern.c2mon.client.core.configuration.ConfigurationRequestSender;
import cern.c2mon.client.core.configuration.DeviceClassConfigurationManager;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.device.DeviceClass;
import cern.c2mon.shared.client.device.DeviceClassElement;
import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DeviceClassConfigurationManagerTest {

    private final ConfigurationRequestSender configurationRequestSenderMock = createNiceMock(ConfigurationRequestSender.class);
    private final DeviceClassConfigurationManager configurationService = new DeviceClassConfigurationManagerImpl(configurationRequestSenderMock);

    @Before
    public void setupServer() {
        reset(configurationRequestSenderMock);
    }

    @Test
    public void createDeviceClassByNameShouldSendCreateRequest() {
        Capture<Configuration> c = newCapture();
        expect(configurationRequestSenderMock.applyConfiguration(and(capture(c), isA(Configuration.class)), anyObject()))
                .andReturn(new ConfigurationReport())
                .once();
        replay(configurationRequestSenderMock);

        configurationService.createDeviceClass("devClassName");

        assertTrue(c.getValue().getEntities().get(0).isCreated());
    }

    @Test
    public void createDeviceClassByNameShouldCreateDeviceClassObject() {
        String deviceClassName = String.valueOf(System.currentTimeMillis());
        Capture<Configuration> c = newCapture();
        expect(configurationRequestSenderMock.applyConfiguration(and(capture(c), isA(Configuration.class)), anyObject()))
                .andReturn(new ConfigurationReport())
                .once();
        replay(configurationRequestSenderMock);

        configurationService.createDeviceClass(deviceClassName);

        assertTrue(c.getValue().getEntities().stream().anyMatch(e -> e.getName().equals(deviceClassName)));
    }

    @Test
    public void createDeviceClassByNameShouldCreateOnlyOneDeviceClassObject() {
        String deviceClassName = String.valueOf(System.currentTimeMillis());
        Capture<Configuration> c = newCapture();
        expect(configurationRequestSenderMock.applyConfiguration(and(capture(c), isA(Configuration.class)), anyObject()))
                .andReturn(new ConfigurationReport())
                .once();
        replay(configurationRequestSenderMock);

        configurationService.createDeviceClass(deviceClassName);

        assertEquals(1, c.getValue().getEntities().size());
    }

    @Test
    public void createDeviceClassByDeviceClassShouldReturnSameObject() {
        DeviceClass expected = DeviceClass.create(String.valueOf(System.currentTimeMillis())).build();
        Capture<Configuration> c = newCapture();
        expect(configurationRequestSenderMock.applyConfiguration(and(capture(c), isA(Configuration.class)), anyObject()))
                .andReturn(new ConfigurationReport())
                .once();
        replay(configurationRequestSenderMock);

        configurationService.createDeviceClass(expected);
        assertTrue(c.getValue().getEntities().contains(expected));
    }

    @Test
    public void createDeviceClassShouldCreateOnlyOneDeviceClassObject() {
        DeviceClass deviceClass = DeviceClass.create(String.valueOf(System.currentTimeMillis())).build();
        Capture<Configuration> c = newCapture();
        expect(configurationRequestSenderMock.applyConfiguration(and(capture(c), isA(Configuration.class)), anyObject()))
                .andReturn(new ConfigurationReport())
                .once();
        replay(configurationRequestSenderMock);

        configurationService.createDeviceClass(deviceClass);

        assertEquals(1, c.getValue().getEntities().size());
    }

    @Test
    public void createDeviceClassWithPropertyShouldReturnProperty() {
        DeviceClass expected = DeviceClass.create(String.valueOf(System.currentTimeMillis()))
                .addProperty("name", "description")
                .build();
        Capture<Configuration> c = newCapture();
        expect(configurationRequestSenderMock.applyConfiguration(and(capture(c), isA(Configuration.class)), anyObject()))
                .andReturn(new ConfigurationReport())
                .once();
        replay(configurationRequestSenderMock);

        configurationService.createDeviceClass(expected);
        DeviceClass entity = (DeviceClass) c.getValue().getEntities().get(0);
        assertTrue(containsElementWithName(entity.getProperties().getProperties(), "name"));
    }

    @Test
    public void createDeviceClassWithMultiplePropertiesShouldReturnProperties() {
        DeviceClass expected = DeviceClass.create(String.valueOf(System.currentTimeMillis()))
                .addProperty("name1", "description")
                .addProperty("name2", "description")
                .addProperty("name3", "description")
                .build();
        Capture<Configuration> c = newCapture();
        expect(configurationRequestSenderMock.applyConfiguration(and(capture(c), isA(Configuration.class)), anyObject()))
                .andReturn(new ConfigurationReport())
                .once();
        replay(configurationRequestSenderMock);

        configurationService.createDeviceClass(expected);
        DeviceClass entity = (DeviceClass) c.getValue().getEntities().get(0);
        assertTrue(containsElementWithName(entity.getProperties().getProperties(), "name1"));
        assertTrue(containsElementWithName(entity.getProperties().getProperties(), "name2"));
        assertTrue(containsElementWithName(entity.getProperties().getProperties(), "name3"));
    }

    @Test
    public void createDeviceClassWithCommandShouldReturnCommand() {
        DeviceClass expected = DeviceClass.create(String.valueOf(System.currentTimeMillis()))
                .addCommand("name", "description")
                .build();
        Capture<Configuration> c = newCapture();
        expect(configurationRequestSenderMock.applyConfiguration(and(capture(c), isA(Configuration.class)), anyObject()))
                .andReturn(new ConfigurationReport())
                .once();
        replay(configurationRequestSenderMock);

        configurationService.createDeviceClass(expected);
        DeviceClass entity = (DeviceClass) c.getValue().getEntities().get(0);
        assertTrue(containsElementWithName(entity.getCommands().getCommands(), "name"));
    }

    @Test
    public void createDeviceClassWithMultipleCommandsShouldReturnCommands() {
        DeviceClass expected = DeviceClass.create(String.valueOf(System.currentTimeMillis()))
                .addCommand("name1", "description")
                .addCommand("name2", "description")
                .addCommand("name3", "description")
                .build();
        Capture<Configuration> c = newCapture();
        expect(configurationRequestSenderMock.applyConfiguration(and(capture(c), isA(Configuration.class)), anyObject()))
                .andReturn(new ConfigurationReport())
                .once();
        replay(configurationRequestSenderMock);

        configurationService.createDeviceClass(expected);
        DeviceClass entity = (DeviceClass) c.getValue().getEntities().get(0);
        assertTrue(containsElementWithName(entity.getCommands().getCommands(), "name1"));
        assertTrue(containsElementWithName(entity.getCommands().getCommands(), "name2"));
        assertTrue(containsElementWithName(entity.getCommands().getCommands(), "name3"));
    }

    @Test
    public void deleteDeviceClassShouldSendDeleteRequest() {
        Capture<Configuration> c = newCapture();
        expect(configurationRequestSenderMock.applyConfiguration(and(capture(c), isA(Configuration.class)), anyObject()))
                .andReturn(new ConfigurationReport())
                .once();
        replay(configurationRequestSenderMock);
        configurationService.removeDeviceClass("class");
        DeviceClass entity = (DeviceClass) c.getValue().getEntities().get(0);
        assertTrue(entity.isDeleted());
    }

    @Test
    public void deleteDeviceClassByIdShouldSendDeleteRequest() {
        Capture<Configuration> c = newCapture();
        expect(configurationRequestSenderMock.applyConfiguration(and(capture(c), isA(Configuration.class)), anyObject()))
                .andReturn(new ConfigurationReport())
                .once();
        replay(configurationRequestSenderMock);

        configurationService.removeDeviceClassById(444L);
        DeviceClass entity = (DeviceClass) c.getValue().getEntities().get(0);
        assertTrue(entity.isDeleted());
    }


    private <T extends DeviceClassElement> boolean containsElementWithName(List<T> elements, String name) {
        return elements.stream()
                .anyMatch(e -> e.getName().equals(name));
    }
}

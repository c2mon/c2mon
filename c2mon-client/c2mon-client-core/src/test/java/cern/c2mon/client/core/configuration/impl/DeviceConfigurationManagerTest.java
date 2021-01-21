package cern.c2mon.client.core.configuration.impl;

import cern.c2mon.client.core.configuration.ConfigurationRequestSender;
import cern.c2mon.client.core.configuration.DeviceConfigurationManager;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.device.Device;
import cern.c2mon.shared.client.device.DeviceElement;
import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DeviceConfigurationManagerTest {

    private final ConfigurationRequestSender configurationRequestSenderMock = createNiceMock(ConfigurationRequestSender.class);
    private final DeviceConfigurationManager configurationService = new DeviceConfigurationManagerImpl(configurationRequestSenderMock);

    @Before
    public void setupServer() {
        reset(configurationRequestSenderMock);
    }

    @Test
    public void createDeviceByNameAndDevClassNameShouldSendCreateRequest() {
        String deviceName = String.valueOf(System.currentTimeMillis());
        Capture<Configuration> c = newCapture();
        expect(configurationRequestSenderMock.applyConfiguration(and(capture(c), isA(Configuration.class)), anyObject()))
                .andReturn(new ConfigurationReport())
                .once();
        replay(configurationRequestSenderMock);

        configurationService.createDevice(deviceName, "devClassName");

        assertTrue(c.getValue().getEntities().get(0).isCreated());
    }

    @Test
    public void createDeviceByNameAndDeviceClassNameShouldCreateDeviceObject() {
        String devName = String.valueOf(System.currentTimeMillis());
        Capture<Configuration> c = newCapture();
        expect(configurationRequestSenderMock.applyConfiguration(and(capture(c), isA(Configuration.class)), anyObject()))
                .andReturn(new ConfigurationReport())
                .once();
        replay(configurationRequestSenderMock);

        configurationService.createDevice(devName, "devClassName");

        assertTrue(c.getValue().getEntities().stream().anyMatch(e -> e.getName().equals(devName)));
    }

    @Test
    public void createDeviceByNameAndDeviceClassNameShouldCreateOnlyOneDeviceObject() {
        String deviceName = String.valueOf(System.currentTimeMillis());
        Capture<Configuration> c = newCapture();
        expect(configurationRequestSenderMock.applyConfiguration(and(capture(c), isA(Configuration.class)), anyObject()))
                .andReturn(new ConfigurationReport())
                .once();
        replay(configurationRequestSenderMock);

        configurationService.createDevice(deviceName, "devClassName");

        assertEquals(1, c.getValue().getEntities().size());
    }

    @Test
    public void createDeviceByDeviceShouldReturnSameObject() {
        Device expected = Device.create(String.valueOf(System.currentTimeMillis()), "devClassName").build();
        Capture<Configuration> c = newCapture();
        expect(configurationRequestSenderMock.applyConfiguration(and(capture(c), isA(Configuration.class)), anyObject()))
                .andReturn(new ConfigurationReport())
                .once();
        replay(configurationRequestSenderMock);

        configurationService.createDevice(expected);
        assertTrue(c.getValue().getEntities().contains(expected));
    }

    @Test
    public void createDeviceShouldCreateOnlyOneDeviceObject() {
        Device deviceClass = Device.create(String.valueOf(System.currentTimeMillis()), "devClassName").build();
        Capture<Configuration> c = newCapture();
        expect(configurationRequestSenderMock.applyConfiguration(and(capture(c), isA(Configuration.class)), anyObject()))
                .andReturn(new ConfigurationReport())
                .once();
        replay(configurationRequestSenderMock);

        configurationService.createDevice(deviceClass);

        assertEquals(1, c.getValue().getEntities().size());
    }

    @Test
    public void createDeviceWithDevicePropertyShouldReturnDeviceProperty() {
        Device expected = Device.create(String.valueOf(System.currentTimeMillis()), "devClassName")
                .addDeviceProperty("name", "value", "category", null)
                .build();
        Capture<Configuration> c = newCapture();
        expect(configurationRequestSenderMock.applyConfiguration(and(capture(c), isA(Configuration.class)), anyObject()))
                .andReturn(new ConfigurationReport())
                .once();
        replay(configurationRequestSenderMock);

        configurationService.createDevice(expected);
        Device entity = (Device) c.getValue().getEntities().get(0);
        assertTrue(containsElementWithName(entity.getDeviceProperties().getDeviceProperties(), "name"));
    }

    @Test
    public void createDeviceWithMultipleDevicePropertiesShouldReturnDeviceProperties() {
        Device expected = Device.create(String.valueOf(System.currentTimeMillis()), "devClassName")
                .addDeviceProperty("name1", "value1", "category1", null)
                .addDeviceProperty("name2", "value2", "category2", null)
                .addDeviceProperty("name3", "value3", "category3", null)
                .build();
        Capture<Configuration> c = newCapture();
        expect(configurationRequestSenderMock.applyConfiguration(and(capture(c), isA(Configuration.class)), anyObject()))
                .andReturn(new ConfigurationReport())
                .once();
        replay(configurationRequestSenderMock);

        configurationService.createDevice(expected);
        Device entity = (Device) c.getValue().getEntities().get(0);
        assertTrue(containsElementWithName(entity.getDeviceProperties().getDeviceProperties(), "name1"));
        assertTrue(containsElementWithName(entity.getDeviceProperties().getDeviceProperties(), "name2"));
        assertTrue(containsElementWithName(entity.getDeviceProperties().getDeviceProperties(), "name3"));
    }

    @Test
    public void createDeviceWithDeviceCommandShouldReturnDeviceCommand() {
        Device expected = Device.create(String.valueOf(System.currentTimeMillis()), "devClassName")
                .addDeviceCommand("name", "value", "category", null)
                .build();
        Capture<Configuration> c = newCapture();
        expect(configurationRequestSenderMock.applyConfiguration(and(capture(c), isA(Configuration.class)), anyObject()))
                .andReturn(new ConfigurationReport())
                .once();
        replay(configurationRequestSenderMock);

        configurationService.createDevice(expected);
        Device entity = (Device) c.getValue().getEntities().get(0);
        assertTrue(containsElementWithName(entity.getDeviceCommands().getDeviceCommands(), "name"));
    }

    @Test
    public void createDeviceWithMultipleDeviceCommandsShouldReturnDeviceCommands() {
        Device expected = Device.create(String.valueOf(System.currentTimeMillis()), "devClassName")
                .addDeviceCommand("name1", "value1", "category1", null)
                .addDeviceCommand("name2", "value2", "category2", null)
                .addDeviceCommand("name3", "value3", "category3", null)
                .build();
        Capture<Configuration> c = newCapture();
        expect(configurationRequestSenderMock.applyConfiguration(and(capture(c), isA(Configuration.class)), anyObject()))
                .andReturn(new ConfigurationReport())
                .once();
        replay(configurationRequestSenderMock);

        configurationService.createDevice(expected);
        Device entity = (Device) c.getValue().getEntities().get(0);
        assertTrue(containsElementWithName(entity.getDeviceCommands().getDeviceCommands(), "name1"));
        assertTrue(containsElementWithName(entity.getDeviceCommands().getDeviceCommands(), "name2"));
        assertTrue(containsElementWithName(entity.getDeviceCommands().getDeviceCommands(), "name3"));
    }

    @Test
    public void deleteDeviceClassShouldSendDeleteRequest() {
        Capture<Configuration> c = newCapture();
        expect(configurationRequestSenderMock.applyConfiguration(and(capture(c), isA(Configuration.class)), anyObject()))
                .andReturn(new ConfigurationReport())
                .once();
        replay(configurationRequestSenderMock);
        configurationService.removeDevice("class");
        Device entity = (Device) c.getValue().getEntities().get(0);
        assertTrue(entity.isDeleted());
    }

    @Test
    public void deleteDeviceClassByIdShouldSendDeleteRequest() {
        Capture<Configuration> c = newCapture();
        expect(configurationRequestSenderMock.applyConfiguration(and(capture(c), isA(Configuration.class)), anyObject()))
                .andReturn(new ConfigurationReport())
                .once();
        replay(configurationRequestSenderMock);

        configurationService.removeDeviceById(444L);
        Device entity = (Device) c.getValue().getEntities().get(0);
        assertTrue(entity.isDeleted());
    }

    private <T extends DeviceElement> boolean containsElementWithName(List<T> elements, String name) {
        return elements.stream()
                .anyMatch(e -> e.getName().equals(name));
    }
}

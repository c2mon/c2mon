package cern.c2mon.client.core.configuration.impl;

import cern.c2mon.client.core.configuration.ConfigurationRequestSender;
import cern.c2mon.client.core.configuration.DeviceConfigurationManager;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.device.Device;
import cern.c2mon.shared.client.device.DeviceCommand;
import cern.c2mon.shared.client.device.DeviceProperty;
import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

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
        Device expected = new Device.CreateBuilder(String.valueOf(System.currentTimeMillis()), "devClassName").build();
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
        Device deviceClass = new Device.CreateBuilder(String.valueOf(System.currentTimeMillis()), "devClassName").build();
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
        DeviceProperty prop = new DeviceProperty("name", "value", "category", null);
        Device expected = new Device.CreateBuilder(String.valueOf(System.currentTimeMillis()), "devClassName")
                .addDeviceProperty(prop)
                .build();
        Capture<Configuration> c = newCapture();
        expect(configurationRequestSenderMock.applyConfiguration(and(capture(c), isA(Configuration.class)), anyObject()))
                .andReturn(new ConfigurationReport())
                .once();
        replay(configurationRequestSenderMock);

        configurationService.createDevice(expected);
        Device entity = (Device) c.getValue().getEntities().get(0);
        assertTrue(entity.getDeviceProperties().getDeviceProperties().contains(prop));
    }

    @Test
    public void createDeviceWithMultipleDevicePropertiesShouldReturnDeviceProperties() {
        DeviceProperty p1 = new DeviceProperty("name1", "value1", "category1", null);
        DeviceProperty p2 = new DeviceProperty("name2", "value1", "category1", null);
        DeviceProperty p3 = new DeviceProperty("name3", "value1", "category1", null);
        Device expected = new Device.CreateBuilder(String.valueOf(System.currentTimeMillis()), "devClassName")
                .addDeviceProperty(p1, p2, p3)
                .build();
        Capture<Configuration> c = newCapture();
        expect(configurationRequestSenderMock.applyConfiguration(and(capture(c), isA(Configuration.class)), anyObject()))
                .andReturn(new ConfigurationReport())
                .once();
        replay(configurationRequestSenderMock);

        configurationService.createDevice(expected);
        Device entity = (Device) c.getValue().getEntities().get(0);
        assertTrue(entity.getDeviceProperties().getDeviceProperties().containsAll(Arrays.asList(p1, p2, p3)));
    }

    @Test
    public void createDeviceWithDeviceCommandShouldReturnDeviceCommand() {
        DeviceCommand cmd = new DeviceCommand("name", "value", "category", null);
        Device expected = new Device.CreateBuilder(String.valueOf(System.currentTimeMillis()), "devClassName")
                .addDeviceCommand(cmd)
                .build();
        Capture<Configuration> c = newCapture();
        expect(configurationRequestSenderMock.applyConfiguration(and(capture(c), isA(Configuration.class)), anyObject()))
                .andReturn(new ConfigurationReport())
                .once();
        replay(configurationRequestSenderMock);

        configurationService.createDevice(expected);
        Device entity = (Device) c.getValue().getEntities().get(0);
        assertTrue(entity.getDeviceCommands().getDeviceCommands().contains(cmd));
    }

    @Test
    public void createDeviceWithMultipleDeviceCommandsShouldReturnDeviceCommands() {
        DeviceCommand p1 = new DeviceCommand("name1", "value1", "category1", null);
        DeviceCommand p2 = new DeviceCommand("name2", "value1", "category1", null);
        DeviceCommand p3 = new DeviceCommand("name3", "value1", "category1", null);
        Device expected = new Device.CreateBuilder(String.valueOf(System.currentTimeMillis()), "devClassName")
                .addDeviceCommand(p1, p2, p3)
                .build();
        Capture<Configuration> c = newCapture();
        expect(configurationRequestSenderMock.applyConfiguration(and(capture(c), isA(Configuration.class)), anyObject()))
                .andReturn(new ConfigurationReport())
                .once();
        replay(configurationRequestSenderMock);

        configurationService.createDevice(expected);
        Device entity = (Device) c.getValue().getEntities().get(0);
        assertTrue(entity.getDeviceCommands().getDeviceCommands().containsAll(Arrays.asList(p1, p2, p3)));
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

}

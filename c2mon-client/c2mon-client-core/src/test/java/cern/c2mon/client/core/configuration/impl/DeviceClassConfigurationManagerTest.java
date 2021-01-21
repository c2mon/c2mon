package cern.c2mon.client.core.configuration.impl;

import cern.c2mon.client.core.configuration.ConfigurationRequestSender;
import cern.c2mon.client.core.configuration.DeviceClassConfigurationManager;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.device.DeviceClass;
import cern.c2mon.shared.client.device.Command;
import cern.c2mon.shared.client.device.Property;
import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

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
        Property p = new Property("name", "description");
        DeviceClass expected = DeviceClass.create(String.valueOf(System.currentTimeMillis()))
                .addProperty(p)
                .build();
        Capture<Configuration> c = newCapture();
        expect(configurationRequestSenderMock.applyConfiguration(and(capture(c), isA(Configuration.class)), anyObject()))
                .andReturn(new ConfigurationReport())
                .once();
        replay(configurationRequestSenderMock);

        configurationService.createDeviceClass(expected);
        DeviceClass entity = (DeviceClass) c.getValue().getEntities().get(0);
        assertTrue(entity.getProperties().getProperties().contains(p));
    }

    @Test
    public void createDeviceClassWithMultiplePropertiesShouldReturnProperties() {
        Property p1 = new Property("name1", "description");
        Property p2 = new Property("name2", "description");
        Property p3 = new Property("name3", "description");
        DeviceClass expected = DeviceClass.create(String.valueOf(System.currentTimeMillis()))
                .addProperty(p1, p2, p3)
                .build();
        Capture<Configuration> c = newCapture();
        expect(configurationRequestSenderMock.applyConfiguration(and(capture(c), isA(Configuration.class)), anyObject()))
                .andReturn(new ConfigurationReport())
                .once();
        replay(configurationRequestSenderMock);

        configurationService.createDeviceClass(expected);
        DeviceClass entity = (DeviceClass) c.getValue().getEntities().get(0);
        assertTrue(entity.getProperties().getProperties().containsAll(Arrays.asList(p1, p2, p3)));
    }

    @Test
    public void createDeviceClassWithCommandShouldReturnCommand() {
        Command cmd = new Command("name", "description");
        DeviceClass expected = DeviceClass.create(String.valueOf(System.currentTimeMillis()))
                .addCommand(cmd)
                .build();
        Capture<Configuration> c = newCapture();
        expect(configurationRequestSenderMock.applyConfiguration(and(capture(c), isA(Configuration.class)), anyObject()))
                .andReturn(new ConfigurationReport())
                .once();
        replay(configurationRequestSenderMock);

        configurationService.createDeviceClass(expected);
        DeviceClass entity = (DeviceClass) c.getValue().getEntities().get(0);
        assertTrue(entity.getCommands().getCommands().contains(cmd));
    }

    @Test
    public void createDeviceClassWithMultipleCommandsShouldReturnCommands() {
        Command p1 = new Command("name1", "description");
        Command p2 = new Command("name2", "description");
        Command p3 = new Command("name3", "description");
        DeviceClass expected = DeviceClass.create(String.valueOf(System.currentTimeMillis()))
                .addCommand(p1, p2, p3)
                .build();
        Capture<Configuration> c = newCapture();
        expect(configurationRequestSenderMock.applyConfiguration(and(capture(c), isA(Configuration.class)), anyObject()))
                .andReturn(new ConfigurationReport())
                .once();
        replay(configurationRequestSenderMock);

        configurationService.createDeviceClass(expected);
        DeviceClass entity = (DeviceClass) c.getValue().getEntities().get(0);
        assertTrue(entity.getCommands().getCommands().containsAll(Arrays.asList(p1, p2, p3)));
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

}

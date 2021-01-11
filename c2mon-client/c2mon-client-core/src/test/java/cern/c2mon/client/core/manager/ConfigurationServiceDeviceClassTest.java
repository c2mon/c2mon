/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.client.core.manager;

import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.config.C2monAutoConfiguration;
import cern.c2mon.client.core.config.mock.CoreSupervisionServiceMock;
import cern.c2mon.client.core.config.mock.JmsProxyMock;
import cern.c2mon.client.core.config.mock.RequestHandlerMock;
import cern.c2mon.client.core.service.ConfigurationService;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.device.Device;
import cern.c2mon.shared.client.configuration.api.device.DeviceClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        C2monAutoConfiguration.class,
        JmsProxyMock.class,
        RequestHandlerMock.class,
        CoreSupervisionServiceMock.class
})
public class ConfigurationServiceDeviceClassTest {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Component to test
     */
    @Autowired
    private ConfigurationService configurationService;


    @BeforeClass
    public static void setupServer() {
        System.setProperty("c2mon.client.jms.url", "tcp://127.0.0.1:61616");
        C2monServiceGateway.startC2monClientSynchronous();
    }


    @Test
    @Ignore
    @DirtiesContext
    public void testCreateDeviceClassByName() {
        String deviceClassName = LocalTime.now().format(formatter);
        ConfigurationReport report = configurationService.createDeviceClass(deviceClassName);
        Assert.assertEquals(ConfigConstants.Status.OK, report.getStatus());
    }

    @Test
    @Ignore
    @DirtiesContext
    public void testCreateDeviceClassByDeviceClass() {
        String deviceClassName = LocalTime.now().format(formatter);
        DeviceClass deviceClass = DeviceClass.create(deviceClassName).build();
        ConfigurationReport report = configurationService.createDeviceClass(deviceClass);
        Assert.assertEquals(ConfigConstants.Status.OK, report.getStatus());
    }

    @Test
    @Ignore
    @DirtiesContext
    public void testCreateDeviceClassByDeviceClassWithProperties() {
        String deviceClassName = LocalTime.now().format(formatter);
        DeviceClass deviceClass = DeviceClass.create(deviceClassName)
                .addProperty("testprop", "testpropdesc")
                .build();
        ConfigurationReport report = configurationService.createDeviceClass(deviceClass);
        Assert.assertEquals(ConfigConstants.Status.OK, report.getStatus());
    }

    @Test
    @Ignore
    @DirtiesContext
    public void testCreateDeviceClassByDeviceClassWithCommands() {
        String deviceClassName = LocalTime.now().format(formatter);
        DeviceClass deviceClass = DeviceClass.create(deviceClassName)
                .addCommand("testcmd", "testcmddesc")
                .build();
        ConfigurationReport report = configurationService.createDeviceClass(deviceClass);
        Assert.assertEquals(ConfigConstants.Status.OK, report.getStatus());
    }

    @Test
    @Ignore
    @DirtiesContext
    public void testCreateDevice() {
        String date = LocalTime.now().format(formatter);
        DeviceClass deviceClass = DeviceClass.create("devClass: " + date)
                .addCommand("testcmd", "testcmddesc")
                .build();
        configurationService.createDeviceClass(deviceClass);
        Device device = Device.create("device: " + date, "devClass: " + date).build();
        ConfigurationReport report = configurationService.createDevice(device);
        Assert.assertEquals(ConfigConstants.Status.OK, report.getStatus());
    }

    @Test
    @Ignore
    @DirtiesContext
    public void testRemoveDeviceClassByName() {
        String tmpName = "testRemoveDeviceClass";
        DeviceClass deviceClass = DeviceClass.create(tmpName)
                .addCommand("removeCommand", "description")
                .addProperty("removeProperty", "description")
                .build();
        configurationService.createDeviceClass(deviceClass);

        ConfigurationReport report = configurationService.removeDeviceClass(tmpName);
        Assert.assertEquals(ConfigConstants.Status.OK, report.getStatus());
    }


    @Test
    @Ignore
    @DirtiesContext
    public void testRemoveDeviceClassById() {
        DeviceClass deviceClass = DeviceClass.create("testRemoveDeviceClass")
                .id(444L)
                .addCommand("removeCommand", "description")
                .addProperty("removeProperty", "description")
                .build();
        configurationService.createDeviceClass(deviceClass);

        ConfigurationReport report = configurationService.removeDeviceClassById(444L);
        Assert.assertEquals(ConfigConstants.Status.OK, report.getStatus());
    }

}

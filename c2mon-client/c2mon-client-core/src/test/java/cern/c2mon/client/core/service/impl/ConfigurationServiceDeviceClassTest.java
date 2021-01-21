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
package cern.c2mon.client.core.service.impl;

import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.config.C2monAutoConfiguration;
import cern.c2mon.client.core.service.ConfigurationService;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.device.Device;
import cern.c2mon.shared.client.configuration.api.device.DeviceClass;
import cern.c2mon.shared.client.device.ResultType;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * These integrations tests require the current branch's C2MON server to be running in the background.
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = C2monAutoConfiguration.class)
public class ConfigurationServiceDeviceClassTest {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss - SSS");
/*
    @ClassRule
    public static GenericContainer c2mon = new GenericContainer("cern/c2mon")
            .waitingFor(Wait.forLogMessage(".*now initialized.*", 1))
            .withEnv("c2mon.server.elasticsearch.enabled", "false")
            .withEnv("c2mon.server.elasticsearch.embedded", "false")
            .withExposedPorts(JMS_PORT, 9001);*/
    /**
     * Component to test
     */
    @Autowired
    private ConfigurationService configurationService;


    @BeforeClass
    public static void setupServer() {
        String jmsUrl = "tcp://127.0.0.1:61616";
        log.info("JMS URL:{} ", jmsUrl);
        System.setProperty("c2mon.client.jms.url", jmsUrl);
        C2monServiceGateway.startC2monClientSynchronous();
    }


    @Test
    @Ignore
    public void testCreateDeviceClassByName() {
        String deviceClassName = LocalTime.now().format(formatter);
        ConfigurationReport report = configurationService.createDeviceClass(deviceClassName);
        Assert.assertEquals(ConfigConstants.Status.OK, report.getStatus());
    }

    @Test
    @Ignore
    public void testCreateDeviceClassByDeviceClass() {
        String deviceClassName = LocalTime.now().format(formatter);
        DeviceClass deviceClass = DeviceClass.create(deviceClassName).build();
        ConfigurationReport report = configurationService.createDeviceClass(deviceClass);
        Assert.assertEquals(ConfigConstants.Status.OK, report.getStatus());
    }

    @Test
    @Ignore
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
    public void testCreateDevice() {
        String date = LocalTime.now().format(formatter);
        DeviceClass deviceClass = DeviceClass.create("devClass: " + date)
                .build();
        configurationService.createDeviceClass(deviceClass);
        Device device = Device.create("device: " + date, "devClass: " + date)
                .build();
        ConfigurationReport report = configurationService.createDevice(device);
        log.info("Report: {}, {}", report.getStatus(), report.getStatusDescription());
        Assert.assertEquals(ConfigConstants.Status.OK, report.getStatus());
    }

    @Test
    @Ignore
    public void testCreateDeviceWithPropertyAndCommand() {
        String date = LocalTime.now().format(formatter);
        DeviceClass deviceClass = DeviceClass.create("devClass: " + date)
                .addCommand("command", "a command")
                .addProperty("constant_value_property", "a property for constant values")
                .addProperty("mapped_property", "a mapped property")
                .addField("mapped_property", "client_rule_field", "client rule field for mapped_property")
                .addField("mapped_property", "tag_id_field", "tag ID field for mapped_property")
                .addProperty("mapped_property_2", "another mapped property")
                .addField("mapped_property_2", "constant_value_field", "constant value field for mapped_property_2")
                .build();
        configurationService.createDeviceClass(deviceClass);
        Device device = Device.create("device: " + date, "devClass: " + date)
                .addCommand("command", 1L)
                .addPropertyForConstantValue("constant_value_property", "2L", ResultType.Long)
                .createMappedProperty("mapped_property")
                .addFieldForClientRule("client_rule_field", "A RULE", ResultType.Integer)
                .addFieldForTagId("tag_id_field", 3L)
                .createMappedProperty("mapped_property_2")
                .addFieldForConstantValue("constant_value_field", "A VALUE", ResultType.String)
                .build();
        ConfigurationReport report = configurationService.createDevice(device);
        log.info("Report: {}, {}", report.getStatus(), report.getStatusDescription());
        Assert.assertEquals(ConfigConstants.Status.OK, report.getStatus());
    }

    @Test
    @Ignore
    public void testCreateDeviceWithFields() {
        String date = LocalTime.now().format(formatter);
        DeviceClass deviceClass = DeviceClass.create("devClass: " + date)
                .addProperty("parent property", "property with field")
                .addField("parent property", "field1", "field")
                .build();
        ConfigurationReport report = configurationService.createDeviceClass(deviceClass);

        log.info("Report: {}, {}", report.getStatus(), report.getStatusDescription());

        Device device = Device.create("device: " + date, "devClass: " + date)
                .createMappedProperty("parent property")
                .addFieldForTagId("field1", 5L)
                .build();
        report = configurationService.createDevice(device);

        log.info("Report: {}, {}", report.getStatus(), report.getStatusDescription());
        Assert.assertEquals(ConfigConstants.Status.OK, report.getStatus());
    }

    @Test
    @Ignore
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
    public void testCascadingRemove() {
        String deviceClassName = "testRemoveDeviceClass";
        DeviceClass deviceClass = DeviceClass.create(deviceClassName)
                .addCommand("removeCommand", "description")
                .addProperty("removeProperty", "description")
                .build();
        configurationService.createDeviceClass(deviceClass);
        configurationService.createDevice("DEVICE_1", deviceClassName);
        configurationService.createDevice("DEVICE_2", deviceClassName);

        ConfigurationReport report = configurationService.removeDeviceClass(deviceClassName);
        Assert.assertEquals(ConfigConstants.Status.OK, report.getStatus());
    }


    @Test
    @Ignore
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
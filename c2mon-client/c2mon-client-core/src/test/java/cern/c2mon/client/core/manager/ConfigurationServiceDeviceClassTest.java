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
import cern.c2mon.shared.client.configuration.api.device.DeviceClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        C2monAutoConfiguration.class,
        JmsProxyMock.class,
        RequestHandlerMock.class,
        CoreSupervisionServiceMock.class
})
public class ConfigurationServiceDeviceClassTest {

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
    @DirtiesContext
    public void testCreateDeviceClassByName() {
        ConfigurationReport report = configurationService.createDeviceClass("deviceName1");
        Assert.assertEquals(ConfigConstants.Status.OK, report.getStatus());

    }

    @Test
    @DirtiesContext
    public void testCreateDeviceClassByDeviceClass() {
        DeviceClass deviceClass = new DeviceClass.CreateBuilder("deviceName2").build();
        ConfigurationReport report = configurationService.createDeviceClass(deviceClass);
        Assert.assertEquals(ConfigConstants.Status.OK, report.getStatus());
    }
}

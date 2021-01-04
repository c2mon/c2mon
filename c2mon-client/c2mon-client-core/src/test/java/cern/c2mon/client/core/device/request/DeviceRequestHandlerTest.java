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
package cern.c2mon.client.core.device.request;

import cern.c2mon.client.core.config.C2monClientProperties;
import cern.c2mon.client.core.config.mock.JmsProxyMock;
import cern.c2mon.client.core.jms.JmsProxy;
import cern.c2mon.shared.client.device.*;
import cern.c2mon.shared.client.request.ClientRequestResult;
import cern.c2mon.shared.client.request.JsonRequest;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.jms.JMSException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Justin Lewis Salmon
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        JmsProxyMock.class,
        C2monClientProperties.class
})
@TestPropertySource("classpath:device-request-handler-test.properties")
public class DeviceRequestHandlerTest {

  @Autowired
  C2monClientProperties properties;

  @Autowired
  JmsProxy jmsProxyMock;

  DeviceRequestHandler requestHandler;

  @Before
  public void setup() {
    requestHandler = new DeviceRequestHandler(jmsProxyMock, properties);
  }

  @Test
  public void testGetAllDeviceClassNames() throws JMSException {
    // Reset the mock
    EasyMock.reset(jmsProxyMock);

    Collection<ClientRequestResult> mockResponse = new ArrayList<>();
    mockResponse.add(new DeviceClassNameResponseImpl("test_device_class_name_1"));
    mockResponse.add(new DeviceClassNameResponseImpl("test_device_class_name_2"));

    // Expect the handler to send the request via the JmsProxy
    EasyMock.expect(jmsProxyMock.sendRequest(EasyMock.<JsonRequest<ClientRequestResult>> anyObject(), EasyMock.<String> anyObject(), EasyMock.anyInt()))
        .andReturn(mockResponse);

    // Setup is finished, need to activate the mock
    EasyMock.replay(jmsProxyMock);

    List<DeviceClassNameResponse> classNames = (List<DeviceClassNameResponse>) requestHandler.getAllDeviceClassNames();
    Assert.assertNotNull(classNames);
    Assert.assertTrue(classNames.size() == 2);
    Assert.assertTrue(classNames.get(0).getDeviceClassName().equals("test_device_class_name_1"));
    Assert.assertTrue(classNames.get(1).getDeviceClassName().equals("test_device_class_name_2"));

    // Verify that everything happened as expected
    EasyMock.verify(jmsProxyMock);
  }

  @Test
  public void testGetAllDevices() throws JMSException {
    // Reset the mock
    EasyMock.reset(jmsProxyMock);

    Collection<ClientRequestResult> mockResponse = new ArrayList<>();
    TransferDeviceImpl dti1 = new TransferDeviceImpl(1000L, "test_device_1", 1L, "test_class_name");
    TransferDeviceImpl dti2 = new TransferDeviceImpl(2000L, "test_device_2", 1L, "test_class_name");
    dti1.addDeviceProperty(new DeviceProperty(1L, "TEST_PROPERTY_1", "100430", "tagId", null));
    dti2.addDeviceProperty(new DeviceProperty(1L, "TEST_PROPERTY_2", "100430", "tagId", null));
    dti1.addDeviceCommand(new DeviceCommand(1L, "TEST_COMMAND_1", "4287", "commandTagId", null));
    dti2.addDeviceCommand(new DeviceCommand(1L, "TEST_COMMAND_1", "4287", "commandTagId", null));
    mockResponse.add(dti1);
    mockResponse.add(dti2);

    EasyMock.expect(jmsProxyMock.sendRequest(EasyMock.<JsonRequest<ClientRequestResult>> anyObject(), EasyMock.<String> anyObject(), EasyMock.anyInt()))
        .andReturn(mockResponse);

    // Setup is finished, need to activate the mock
    EasyMock.replay(jmsProxyMock);

    List<TransferDevice> devices = (List<TransferDevice>) requestHandler.getAllDevices("test_device_class_name_1");
    Assert.assertNotNull(devices);
    Assert.assertTrue(devices.get(0).getId().equals(dti1.getId()));
    Assert.assertTrue(devices.get(1).getId().equals(dti2.getId()));
    Assert.assertTrue(devices.get(0).getDeviceClassId().equals(dti1.getDeviceClassId()));
    Assert.assertTrue(devices.get(1).getDeviceClassId().equals(dti2.getDeviceClassId()));
    Assert.assertTrue(devices.get(0).getDeviceClassName().equals(dti1.getDeviceClassName()));
    Assert.assertTrue(devices.get(1).getDeviceClassName().equals(dti2.getDeviceClassName()));

    Assert.assertTrue(!devices.get(0).getDeviceProperties().isEmpty());
    Assert.assertTrue(devices.get(0).getDeviceProperties().get(0).getName().equals("TEST_PROPERTY_1"));
    Assert.assertTrue(devices.get(1).getDeviceProperties().get(0).getName().equals("TEST_PROPERTY_2"));

    // Verify that everything happened as expected
    EasyMock.verify(jmsProxyMock);
  }
}

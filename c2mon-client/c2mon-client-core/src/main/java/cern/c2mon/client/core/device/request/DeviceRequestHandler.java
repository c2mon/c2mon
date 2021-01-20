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

import java.util.Collection;
import java.util.Set;

import javax.jms.JMSException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.core.config.C2monClientProperties;
import cern.c2mon.client.core.jms.JmsProxy;
import cern.c2mon.client.core.jms.impl.RequestHandlerImpl;
import cern.c2mon.shared.client.device.DeviceClassNameResponse;
import cern.c2mon.shared.client.device.DeviceInfo;
import cern.c2mon.shared.client.device.TransferDevice;
import cern.c2mon.shared.client.request.ClientRequestImpl;

/**
 * This class extends the core <code>RequestHandler</code> implementation,
 * adding methods to interact with the C2MON server for retrieving information
 * about <code>Device</code>s.
 *
 * @author Justin Lewis Salmon
 */
@Service
public class DeviceRequestHandler extends RequestHandlerImpl {

  /**
   * Constructor for use during bean creation
   * @param jmsProxy the JMS proxy bean
   * @param properties the client property bean
   */
  @Autowired
  public DeviceRequestHandler(JmsProxy jmsProxy, C2monClientProperties properties) {
    super(jmsProxy, properties);
  }

  /**
   * Requests a list of the names of all existing device classes.
   *
   * @return a list of all device class names
   * @throws JMSException if JMS problem occurs or not connected at the moment
   */
  public Collection<DeviceClassNameResponse> getAllDeviceClassNames() throws JMSException {
    ClientRequestImpl<DeviceClassNameResponse> namesRequest = new ClientRequestImpl<>(DeviceClassNameResponse.class);
    return jmsProxy.sendRequest(namesRequest, defaultRequestQueue, namesRequest.getTimeout());
  }

  /**
   * Requests all devices of a particular class.
   *
   * @param deviceClassName the name of the device class
   * @return a list of all devices belonging to the specified class
   * @throws JMSException if JMS problem occurs or not connected at the moment
   */
  public Collection<TransferDevice> getAllDevices(String deviceClassName) throws JMSException {
    ClientRequestImpl<TransferDevice> devicesRequest = new ClientRequestImpl<>(TransferDevice.class);
    devicesRequest.setRequestParameter(deviceClassName);
    return jmsProxy.sendRequest(devicesRequest, defaultRequestQueue, devicesRequest.getTimeout());
  }

  /**
   * Retrieve a set of devices from the server based on descriptions contained
   * within {@link DeviceInfo} objects.
   *
   * If a device was not found, it will be omitted from the returned list.
   *
   * @param deviceInfoList the set of {@link DeviceInfo} objects describing the
   *          devices to be subscribed to
   * @return a list of devices that were found
   * @throws JMSException if JMS problem occurs or not connected at the moment
   */
  public Collection<TransferDevice> getDevices(Set<DeviceInfo> deviceInfoList) throws JMSException {
    ClientRequestImpl<TransferDevice> devicesRequest = new ClientRequestImpl<>(TransferDevice.class);
    devicesRequest.setObjectParameter(deviceInfoList);
    return jmsProxy.sendRequest(devicesRequest, defaultRequestQueue, devicesRequest.getTimeout());
  }
}

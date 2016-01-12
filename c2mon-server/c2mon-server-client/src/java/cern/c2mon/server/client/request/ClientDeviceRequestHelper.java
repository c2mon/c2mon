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
package cern.c2mon.server.client.request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.DeviceClassFacade;
import cern.c2mon.server.cache.DeviceFacade;
import cern.c2mon.server.client.util.TransferObjectFactory;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.shared.client.device.DeviceClassNameResponse;
import cern.c2mon.shared.client.device.DeviceInfo;
import cern.c2mon.shared.client.device.TransferDevice;
import cern.c2mon.shared.client.request.ClientRequest;
import cern.c2mon.shared.client.request.ClientRequestResult;

/**
 * Helper class for {@link ClientRequestDelegator} to handle
 * device requests.
 *
 * @author Matthias Braeger
 */
@Service
class ClientDeviceRequestHelper {
  
  /** Reference to the Device facade */
  private final DeviceFacade deviceFacade;

  /** Reference to the DeviceClass facade */
  private final DeviceClassFacade deviceClassFacade;
  
  @Autowired
  protected ClientDeviceRequestHelper(final DeviceClassFacade deviceClassFacade,
                                final DeviceFacade deviceFacade) {
    this.deviceClassFacade = deviceClassFacade;
    this.deviceFacade = deviceFacade;
  }
  
  /**
   * Inner method which handles the device class names request.
   *
   * @param deviceClassNamesRequest the request sent by the client
   * @return a collection of all the device class names
   */
  Collection<? extends ClientRequestResult> handleDeviceClassNamesRequest(final ClientRequest deviceClassNamesRequest) {
    Collection<DeviceClassNameResponse> classNames = new ArrayList<>();

    Collection<String> names = deviceClassFacade.getDeviceClassNames();
    for (String name : names) {
      classNames.add(TransferObjectFactory.createTransferDeviceName(name));
    }

    return classNames;
  }

  /**
   * Inner method which handles the device request.
   *
   * @param deviceRequest the request sent by the client
   * @return a collection of all devices of the requested class
   */
  @SuppressWarnings("unchecked")
  Collection<? extends ClientRequestResult> handleDeviceRequest(final ClientRequest deviceRequest) {
    Collection<TransferDevice> transferDevices = new ArrayList<>();
    List<Device> devices;

    if (deviceRequest.getObjectParameter() != null) {
      Set<DeviceInfo> deviceInfoList = (Set<DeviceInfo>) deviceRequest.getObjectParameter();
      devices = deviceFacade.getDevices(deviceInfoList);
    }
    else {
      String deviceClassName = deviceRequest.getRequestParameter();
      devices = deviceFacade.getDevices(deviceClassName);
    }

    for (Device device : devices) {
      transferDevices.add(TransferObjectFactory.createTransferDevice(device, deviceFacade.getClassNameForDevice(device.getId())));
    }

    return transferDevices;
  }
}

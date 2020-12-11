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

import cern.c2mon.server.cache.DeviceClassCache;
import cern.c2mon.server.common.device.DeviceClass;
import cern.c2mon.shared.client.device.DeviceClassNameResponseImpl;
import cern.c2mon.shared.client.request.ClientRequest;
import cern.c2mon.shared.client.request.ClientRequestResult;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Helper class for {@link ClientRequestDelegator} to handle device requests.
 * @author Elisabeth Stockinger
 */
@AllArgsConstructor
@Service
class ClientDeviceClassRequestHelper {

    /**
     * Reference to the DeviceClass cache that provides a list of all the deviceClass names
     */
    private final DeviceClassCache deviceClassCache;

    /**
     * Inner method which handles the device class names request.
     * @param clientRequest the device name sent by the client
     * @return a collection of all the device class names
     */
    Collection<? extends ClientRequestResult> handleDeviceClassNamesRequest(final ClientRequest clientRequest) {
        return deviceClassCache.getKeys().stream()
                .map(processId -> {
                    DeviceClass deviceClass = deviceClassCache.get(processId);
                    return new DeviceClassNameResponseImpl(deviceClass.getName());
                })
                .collect(Collectors.toList());
    }
}

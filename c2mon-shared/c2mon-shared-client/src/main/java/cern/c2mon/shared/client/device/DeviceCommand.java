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
package cern.c2mon.shared.client.device;

import lombok.NoArgsConstructor;

/**
 * Simple XML mapper bean representing a device command. Used when serializing and deserialising device commands during
 * configuration.
 */
@NoArgsConstructor
public class DeviceCommand extends DeviceElement {

    private static final long serialVersionUID = 2717346918745916452L;

    /**
     * Creates a device command with the category "commandTagId". The result type of device commands defaults to String.
     *
     * @param id    the unique ID of the parent command. Can be null during configuration requests, in which case the server
     *              will assign the appropriate ID by name.
     * @param name  the unique name of the parent command.
     * @param value the ID of the commandTag references by this device command.
     * @return the newly created device command.
     */
    public static DeviceCommand forCommandTagId(Long id, String name, Long value) {
        return new DeviceCommand(id, name, String.valueOf(value));
    }

    private DeviceCommand(final Long id, final String name, final String value) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.category = Category.COMMAND_TAG_ID.label;
    }

}

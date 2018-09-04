/******************************************************************************
 * Copyright (C) 2010-2018 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.shared.common.datatag.address.impl;

/**
 * Constants class containing structure types for data tags
 *
 * @author Szymon Halastra
 */
public final class PLCHardwareAddressType {
    /**
     * Structure type for boolean data tags (inputs)
     */
    public static final int STRUCT_BOOLEAN = 1;

    /**
     * Structure type for analog (numeric) data tags (inputs)
     */
    public static final int STRUCT_ANALOG = 2;

    /**
     * Structure type for boolean command tags (outputs)
     */
    public static final int STRUCT_BOOLEAN_COMMAND = 3;

    /**
     * Structure type for analog command tags (outputs)
     */
    public static final int STRUCT_ANALOG_COMMAND = 4;

    /**
     * Structure type for internal boolean states for diagnostic purposes
     */
    public static final int STRUCT_DIAG_BOOLEAN = 5;

    /**
     * Structure type for internal analogue states for diagnostic purposes
     */
    public static final int STRUCT_DIAG_ANALOG = 6;

    /**
     * Structure type for internal boolean commands for diagnostic purposes (e.g. PING)
     */
    public static final int STRUCT_DIAG_BOOLEAN_COMMAND = 7;

    /**
     * Structure type for String data tags (inputs)
     */
    public static final int STRUCT_STRING = 8;

    /**
     * Private constructor for hiding possibility of creating the object of this class
     */
    private PLCHardwareAddressType() {
    }
}

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
package cern.c2mon.shared.common.datatag.address;

/**
 * This interface represents hardware address of JMX DataTag
 * 
 * @author wbuczak
 */

/**
 * The <code>JMXHardwareAddress</code> interface is used by the <code>JMXMessageHandler</code>.
 * 
 * @see cern.c2mon.daq.jmx.JMXMessageHandler
 * @author
 */
public interface JMXHardwareAddress extends HardwareAddress {

    enum ReceiveMethod {
        poll, notification;
    };

    /**
     * @return JMX object name
     */
    String getObjectName();

    /**
     * @return returns JMX object's attribute
     */
    String getAttribute();

    /**
     * @return method name to be called (if calling JMX servic'e method)
     */
    String getCallMethod();

    /**
     * @return returns receiving method (polling or notification)
     */
    ReceiveMethod getReceiveMethod();

    Integer getIndex();

    /**
     * Used for composite JMX bean attributes
     * 
     * @return composite attribute name, or null if not set
     */
    String getCompositeField();

    /**
     * @return
     */
    String getMapField();

    /**
     * @return true if attribute field is not null
     */
    boolean hasAttribute();

    /**
     * @return true if receive method is not null
     */
    boolean hasCallMethod();

    /**
     * @return
     */
    boolean hasIndex();

    /**
     * @return
     */
    boolean hasMapField();

    /**
     * @return
     */
    boolean hasCompositeField();
}

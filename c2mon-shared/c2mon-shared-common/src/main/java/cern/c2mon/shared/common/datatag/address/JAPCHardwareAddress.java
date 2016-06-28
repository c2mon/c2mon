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
 * In order to subscribed to a data item via JAPC, you will need two types of information: (1) the name of the parameter
 * you want to subscribe to (device name and property name) (2) To get data from the control system implementation you
 * will need Parameter (s).
 * 
 * @author J. Stowisek, W. Buczak
 */

public interface JAPCHardwareAddress extends HardwareAddress {

    enum COMMAND_TYPE {
        SET, GET, UNKNOWN
    }

    /**
     * @return Optional adress specification, default is rda
     */
    @Deprecated
    String getProtocol();

    /**
     * @return Optional address specification, default is rda
     */
    @Deprecated
    String getService();

    /**
     * @return the name of the device (cannot be null)
     */
    String getDeviceName();

    /**
     * @return the name of the property within the device (cannot be null)
     */
    String getPropertyName();

    /**
     * @return the cycle descriptor describing how often the value shall be acquired by JAPC. If null, the value will be
     *         acquired on change. However, the onChange model is not supported by all devices.
     */
    String getCycleSelector();

    /**
     * Only used within the  WieJapcMessageHandler for retrieving the correct index position in a 2d array. Default is <code>null</code>
     * @return the index field name
     */
    String getIndexFieldName();

    /**
     * @return column index of the data point element inside the array or array2d or -1 if not used
     */
    int getColumnIndex();

    /**
     * @return row index of the data point element inside the array or array2d or -1 if not used
     */
    @Deprecated
    int getRowIndex();

    /**
     * Mandatory for MAP type data, not used for SIMPLE
     * 
     * @return The name that specifies the index position
     * @deprecated Please use {@link #dataFieldName} instead
     */
    @Deprecated
    String getIndexName();

    /**
     * Mandatory for MAP type data, not used for SIMPLE
     * 
     * @return
     */
    String getDataFieldName();

    /**
     * used by DMN2 JAPC commands ( can be either SET or GET )
     * 
     * @return
     */
    COMMAND_TYPE getCommandType();

    /**
     * used by DMN2 JAPC commands , returns context field (may not be set)
     * 
     * @return
     */
    String getContextField();

    
    /**
     * @return returns filter as key-value pair (e.g. key=value), or null if filter is not defined
     */
    String getFilter();

    /**
     * @return returns true if context field is set (not null)
     */
    boolean hasContextField();
    
    
    /**
     * 
     * @return returns true if filter is set
     */
    boolean hasFilter();
}

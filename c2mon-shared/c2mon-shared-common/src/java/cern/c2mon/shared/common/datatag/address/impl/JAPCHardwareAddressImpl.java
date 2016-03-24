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
package cern.c2mon.shared.common.datatag.address.impl;

import org.simpleframework.xml.Element;

import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.address.JAPCHardwareAddress;
import lombok.Getter;
import lombok.Setter;

/**
 * DataTag address for c2mon-daq-japc DAQ flavour
 *
 * @author Matthias Braeger
 */
@Getter @Setter
public class JAPCHardwareAddressImpl extends HardwareAddressImpl implements JAPCHardwareAddress {

    /** Serial version UID */
    private static final long serialVersionUID = 6770995924304633009L;

    /** 
     * Optional protocol specification, default is 'rda'
     * @deprecated Not used by the GenericJapcMessageHandler
     */
    @Deprecated
    @Element(required = false)
    protected String protocol = "rda";

    /** 
     * Optional service specification, default is 'rda'
     * @deprecated Not used by the GenericJapcMessageHandler
     */
    @Deprecated
    @Element(required = false)
    protected String service = "rda";

    /** The name of the JAPC device (cannot be null) */
    @Element(name = "device-name")
    protected String deviceName = null;

    /** The name of the property within the JAPC device (cannot be null) */
    @Element(name = "property-name")
    protected String propertyName = null;

    /** 
     * The cycle selector describes how often the value shall be acquired by JAPC. 
     * If null, the value will be acquired on change. However, the onChange model 
     * is not supported by all devices. Default is <code>null</code>
     */
    @Element(name = "cycle-selector", required = false)
    protected String cycleSelector = null;

    /** Only used within the  WieJapcMessageHandler for retrieving the correct index position in a 2d array. Default is <code>null</code> */ 
    @Element(name = "index-field-name", required = false)
    protected String indexFieldName = null;

    /** @deprecated Please use {@link #dataFieldName} instead */
    @Deprecated
    @Element(name = "index-name", required = false)
    protected String indexName = null;

    /** The data field name within a JAPC MAP. Mandatory for JAPC MAP type data, not used for SIMPLE */
    @Element(name = "data-field-name", required = false)
    protected String dataFieldName = null;

    /** 
     * The column index of the data point element inside the array or array2d.
     * By Default set to -1, if not used
     */
    @Element(name = "column-index")
    protected int columnIndex = -1;

    /** 
     * The row index of the data point element inside the array or array2d. 
     * By Default set to -1, if not used
     * @deprecated Not used by the GenericJapcMessageHandler
     */
    @Deprecated
    @Element(name = "row-index")
    protected int rowIndex = -1;

    /** used by JAPC commands and can be either SET or GET. Default value is UNKNOWN */
    @Element(name = "command-type", required = false)
    protected String commandType = null;

    /** Only used for commands (may not be set) */
    @Element(name = "context-field", required = false)
    protected String contextField = null;

    /** 
     * Used to retrieve a certain JAPC selector. 
     * The filter string has to be specified in the following format: 'key=value'
     */
    @Element(required = false)
    protected String filter = null;

    protected JAPCHardwareAddressImpl() {
        // nothing to do
    }
    
    public JAPCHardwareAddressImpl(final String pDeviceName, final String pPropertyName) throws ConfigurationException {
      setDeviceName(pDeviceName);
      setPropertyName(pPropertyName);
    }

    public JAPCHardwareAddressImpl(final String pDeviceName, final String pPropertyName, final String pCycleSelector)
            throws ConfigurationException {
        setDeviceName(pDeviceName);
        setPropertyName(pPropertyName);
        setCycleSelector(pCycleSelector);
    }

    public JAPCHardwareAddressImpl(final String pDeviceName, final String pPropertyName, final String pCycleSelector,
            final int pRowIndex, final int pColumnIndex) throws ConfigurationException {
        setDeviceName(pDeviceName);
        setPropertyName(pPropertyName);
        setCycleSelector(pCycleSelector);
        setRowIndex(pRowIndex);
        setColumnIndex(pColumnIndex);
    }

    public JAPCHardwareAddressImpl(final String pDeviceName, final String pPropertyName, final String pCycleSelector,
            final int pColumnIndex) throws ConfigurationException {
        setDeviceName(pDeviceName);
        setPropertyName(pPropertyName);
        setCycleSelector(pCycleSelector);
        setColumnIndex(pColumnIndex);
    }

    public JAPCHardwareAddressImpl(final String pDeviceName, final String pPropertyName, final String dataFieldName,
            final String commandType, String contextField) throws ConfigurationException {
        setDeviceName(pDeviceName);
        setPropertyName(pPropertyName);
        setDataFieldName(dataFieldName);
        setCommandType(commandType);
        setContextField(contextField);
    }


    public JAPCHardwareAddressImpl(final String pDeviceName, final String pPropertyName, final String dataFieldName,
            final String commandType, String contextField,  String filter) throws ConfigurationException {
        setDeviceName(pDeviceName);
        setPropertyName(pPropertyName);
        setDataFieldName(dataFieldName);
        setCommandType(commandType);
        setContextField(contextField);
        setFilter(filter);
    }

    @Override
    public boolean hasContextField() {
        return (this.contextField == null || this.contextField.length() == 0) ? false : true;
    }

    @Override
    public COMMAND_TYPE getCommandType() {
        COMMAND_TYPE result = COMMAND_TYPE.UNKNOWN;

        if (this.commandType != null) {
            if (this.commandType.equalsIgnoreCase("SET"))
                result = COMMAND_TYPE.SET;
            else if (this.commandType.equalsIgnoreCase("GET"))
                result = COMMAND_TYPE.GET;

        }

        return result;
    }

    public final boolean isMapType() {
        return this.indexFieldName != null;
    }

    @Override
    public boolean hasFilter() {
        return (filter == null || filter.length() == 0) ? false : true;
    }

    protected final void setDeviceName(final String pDeviceName) throws ConfigurationException {
        if (pDeviceName == null) {
            throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
                    "parameter \"deviceName\" must not be null.");
        }
        this.deviceName = pDeviceName;
    }

    protected final void setPropertyName(final String pPropertyName) throws ConfigurationException {
        if (pPropertyName == null) {
            throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
                    "parameter \"propertyName\" must not be null.");
        }
        this.propertyName = pPropertyName;
    }

    @Override
    public void validate() throws ConfigurationException {
      // TODO: Write address validation
    }
}

package cern.c2mon.shared.common.datatag.address.impl;

import org.simpleframework.xml.Element;

import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.address.JAPCHardwareAddress;

public class JAPCHardwareAddressImpl extends HardwareAddressImpl implements JAPCHardwareAddress {

    private static final long serialVersionUID = 6770995924304633009L;
    
    @Element(required = false)
    protected String protocol = null;

    @Element(required = false)
    protected String service = null;

    @Element
    protected String deviceName = null;

    @Element
    protected String propertyName = null;

    @Element(required = false)
    protected String cycleSelector = null;

    @Element(required = false)
    protected String indexFieldName = null;

    @Element(required = false)
    protected String indexName = null;

    @Element(required = false)
    protected String dataFieldName = null;

    @Element
    protected int columnIndex = -1;

    @Element
    protected int rowIndex = -1;

    @Element(required = false)
    protected String commandType = null;

    @Element(required = false)
    protected String contextField = null;
    
    @Element(required = false)
    protected String filter = null;

    protected JAPCHardwareAddressImpl() {
        // nothing to do
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
    

    /**
     * @return the protocol
     */
    public final String getProtocol() {
        return protocol;
    }

    /**
     * @return the service
     */
    public final String getService() {
        return service;
    }

    public final String getDeviceName() {
        return this.deviceName;
    }

    public final String getPropertyName() {
        return this.propertyName;
    }

    public final String getCycleSelector() {
        return this.cycleSelector;
    }

    public final String getIndexFieldName() {
        return this.indexFieldName;
    }

    /**
     * @return the parameterName
     */
    public final String getIndexName() {
        return this.indexName;
    }

    /**
     * @return the dataField
     */
    public final String getDataFieldName() {
        return this.dataFieldName;
    }

    /**
     * @return column index of the data point element inside the array or array2d or -1 if not used
     */
    public int getColumnIndex() {
        return this.columnIndex;
    }

    public String getContextField() {
        return this.contextField;
    }
    
    public boolean hasContextField() {
        return (this.contextField == null || this.contextField.length() == 0) ? false : true;
    }

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

       
    /**
     * @return row index of the data point element inside the array or array2d or -1 if not used
     */
    public int getRowIndex() {
        return this.rowIndex;
    }

    public final boolean isMapType() {
        return this.indexFieldName != null;
    }

    public final boolean isInsideArray2d() {
        return (rowIndex == -1 ? false : true);
    }

    
    @Override
    public String getFilter() {       
        return filter;
    }    
    
    @Override
    public boolean hasFilter() {
        return (filter == null || filter.length() == 0) ? false : true;
    }
    
    
    /**
     * @param protocol the protocol to set
     */
    protected final void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * @param service the service to set
     */
    protected final void setService(String service) {
        this.service = service;
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

    protected final void setCycleSelector(final String pCycleSelector) throws ConfigurationException {
        this.cycleSelector = pCycleSelector;
    }

    protected final void setIndexFieldName(final String indexFieldName) throws ConfigurationException {
        this.indexFieldName = indexFieldName;
    }

    /**
     * @param parameterName the parameterName to set
     */
    protected final void setIndexName(String indexName) throws ConfigurationException {
        this.indexName = indexName;
    }

    /**
     * @param dataField the dataField to set
     */
    protected final void setDataFieldName(String dataFieldName) throws ConfigurationException {
        this.dataFieldName = dataFieldName;
    }

    protected final void setRowIndex(final int pRowIndex) throws ConfigurationException {
        this.rowIndex = pRowIndex;
    }

    protected final void setColumnIndex(final int pColumnIndex) throws ConfigurationException {
        this.columnIndex = pColumnIndex;
    }

    protected final void setCommandType(final String commandType) throws ConfigurationException {
        this.commandType = commandType;
    }

    protected final void setContextField(final String contextField) throws ConfigurationException {
        this.contextField = contextField;
    }
    
    protected final void setFilter(final String filter) throws ConfigurationException {
        this.filter = filter;
    }

    public void validate() throws ConfigurationException {
    }   
}
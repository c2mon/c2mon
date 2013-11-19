/*
 * $Id $
 * 
 * $Date$ $Revision$ $Author$
 * 
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.shared.common.datatag.address.impl;

import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.address.JMXHardwareAddress;

/**
 * Implementation of the <code>JMXHardwareAddress</code> interface
 * 
 * @see cern.c2mon.shared.common.datatag.address.JMXHardwareAddress
 * @author Wojtek Buczak
 */
public class JMXHardwareAddressImpl extends HardwareAddressImpl implements JMXHardwareAddress {

    private static final long serialVersionUID = 3154428431583350667L;

    protected String objectName;

    protected String attribute;

    protected String callMethod;

    /**
     * for JMX attributes which are implement java.util.List, this attribute specifies an index of the element to look
     * for This parameter is not mandatory
     */
    protected Integer index;

    protected String compositeField;

    /**
     * for JMX attributes which are maps, this attribute holds a name of the field in the map to look for This parameter
     * is not mandatory.
     */
    protected String mapField;

    protected String receiveMethod;

    /**
     * Constructor for internal use (for reading the HardwareAddress back from XML)
     */
    protected JMXHardwareAddressImpl() {
        // default receive method is poll
        this.receiveMethod = ReceiveMethod.poll.toString();
    }

    public JMXHardwareAddressImpl(final String objectName, final String attribute, final String callMethod,
            final Integer index, final String compositeField, final String mapField, final String receiveMethod)
            throws ConfigurationException {

        setObjectName(objectName);
        setAttribute(attribute);
        setCallMethod(callMethod);
        setIndex(index);
        setCompositeField(compositeField);
        setMapField(mapField);
        setReceiveMethod(receiveMethod);
        
        if (this.attribute == null && this.callMethod == null)
            throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
                    "ether parameter \"attribute\" or \"callMethod\" must not be null.");        
    }

    public JMXHardwareAddressImpl(final String objectName, final String attribute, final String callMethod,
            final String receiveMethod) throws ConfigurationException {
        this(objectName, attribute, callMethod, null, null, null, receiveMethod);
    }

    public JMXHardwareAddressImpl(final String objectName, final String attribute, String receiveMethod)
            throws ConfigurationException {
        this(objectName, attribute, null, receiveMethod);
    }

    public JMXHardwareAddressImpl(final String objectName, final String attribute) throws ConfigurationException {
        this(objectName, attribute, null, null);
    }

    protected final void setObjectName(final String objectName) throws ConfigurationException {
        if (objectName == null || objectName.length() == 0) {
            throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
                    "parameter \"objectName\" must not be <= 0");
        }

        this.objectName = objectName;
    }

    protected final void setCallMethod(final String callMethod) {
        this.callMethod = callMethod;
    }

    protected final void setReceiveMethod(final String receiveMethod) throws ConfigurationException {
        if (receiveMethod == null)
            this.receiveMethod = ReceiveMethod.poll.toString();
        else {

            try {
                switch (ReceiveMethod.valueOf(receiveMethod)) {
                case poll:
                    this.receiveMethod = ReceiveMethod.poll.toString();
                    break;
                case notification:
                    this.receiveMethod = ReceiveMethod.notification.toString();
                    break;

                default:
                    throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
                            "parameter \"receiveMethod\" must be {poll | notification}");

                }
            } catch (IllegalArgumentException ex) {
                throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
                        "parameter \"receiveMethod\" must be {poll | notification}");
            }
        }
    }

    protected final void setAttribute(final String attribute) {
        // no restrictions so far..
        this.attribute = attribute;
    }

    protected final void setIndex(final Integer index) {
        this.index = index;
    }

    protected final void setCompositeField(final String compositeField) {
        this.compositeField = compositeField;
    }

    protected final void setMapField(final String mapField) {
        this.mapField = mapField;
    }

    @Override
    public String getObjectName() {
        return this.objectName;
    }

    @Override
    public String getAttribute() {
        return this.attribute;
    }

    @Override
    public boolean hasAttribute() {
        return !(this.attribute == null);
    }

    @Override
    public String getCallMethod() {
        return this.callMethod;
    }

    @Override
    public boolean hasCallMethod() {
        return !(this.callMethod == null);
    }

    @Override
    public ReceiveMethod getReceiveMethod() {
        return ReceiveMethod.valueOf(this.receiveMethod);
    }

    @Override
    public Integer getIndex() {
        return this.index;
    }

    @Override
    public String getCompositeField() {
        return this.compositeField;
    }

    @Override
    public String getMapField() {
        return this.mapField;
    }

    @Override
    public boolean hasIndex() {
        return !(this.index == null);
    }

    @Override
    public boolean hasMapField() {
        return !(this.mapField == null);
    }

    @Override
    public boolean hasCompositeField() {
        return !(this.compositeField == null);
    }

}

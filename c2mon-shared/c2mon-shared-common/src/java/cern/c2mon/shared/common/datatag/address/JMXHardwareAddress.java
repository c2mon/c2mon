/*
 * Copyright CERN 2012-2-13, All Rights Reserved.
 */
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

/**
 * SubscriptionCancel.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.opcfoundation.webservices.XMLDA._1_0;

public class SubscriptionCancel  implements java.io.Serializable {
    private java.lang.String serverSubHandle;  // attribute

    private java.lang.String clientRequestHandle;  // attribute

    public SubscriptionCancel() {
    }

    public SubscriptionCancel(
           java.lang.String serverSubHandle,
           java.lang.String clientRequestHandle) {
           this.serverSubHandle = serverSubHandle;
           this.clientRequestHandle = clientRequestHandle;
    }


    /**
     * Gets the serverSubHandle value for this SubscriptionCancel.
     * 
     * @return serverSubHandle
     */
    public java.lang.String getServerSubHandle() {
        return serverSubHandle;
    }


    /**
     * Sets the serverSubHandle value for this SubscriptionCancel.
     * 
     * @param serverSubHandle
     */
    public void setServerSubHandle(java.lang.String serverSubHandle) {
        this.serverSubHandle = serverSubHandle;
    }


    /**
     * Gets the clientRequestHandle value for this SubscriptionCancel.
     * 
     * @return clientRequestHandle
     */
    public java.lang.String getClientRequestHandle() {
        return clientRequestHandle;
    }


    /**
     * Sets the clientRequestHandle value for this SubscriptionCancel.
     * 
     * @param clientRequestHandle
     */
    public void setClientRequestHandle(java.lang.String clientRequestHandle) {
        this.clientRequestHandle = clientRequestHandle;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SubscriptionCancel)) return false;
        SubscriptionCancel other = (SubscriptionCancel) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.serverSubHandle==null && other.getServerSubHandle()==null) || 
             (this.serverSubHandle!=null &&
              this.serverSubHandle.equals(other.getServerSubHandle()))) &&
            ((this.clientRequestHandle==null && other.getClientRequestHandle()==null) || 
             (this.clientRequestHandle!=null &&
              this.clientRequestHandle.equals(other.getClientRequestHandle())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getServerSubHandle() != null) {
            _hashCode += getServerSubHandle().hashCode();
        }
        if (getClientRequestHandle() != null) {
            _hashCode += getClientRequestHandle().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SubscriptionCancel.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", ">SubscriptionCancel"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("serverSubHandle");
        attrField.setXmlName(new javax.xml.namespace.QName("", "ServerSubHandle"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("clientRequestHandle");
        attrField.setXmlName(new javax.xml.namespace.QName("", "ClientRequestHandle"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}

/**
 * SubscribeRequestItem.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.opcfoundation.webservices.XMLDA._1_0;

public class SubscribeRequestItem  implements java.io.Serializable {
    private java.lang.String itemPath;  // attribute

    private javax.xml.namespace.QName reqType;  // attribute

    private java.lang.String itemName;  // attribute

    private java.lang.String clientItemHandle;  // attribute

    private float deadband;  // attribute

    private int requestedSamplingRate;  // attribute

    private boolean enableBuffering;  // attribute

    public SubscribeRequestItem() {
    }

    public SubscribeRequestItem(
           java.lang.String itemPath,
           javax.xml.namespace.QName reqType,
           java.lang.String itemName,
           java.lang.String clientItemHandle,
           float deadband,
           int requestedSamplingRate,
           boolean enableBuffering) {
           this.itemPath = itemPath;
           this.reqType = reqType;
           this.itemName = itemName;
           this.clientItemHandle = clientItemHandle;
           this.deadband = deadband;
           this.requestedSamplingRate = requestedSamplingRate;
           this.enableBuffering = enableBuffering;
    }


    /**
     * Gets the itemPath value for this SubscribeRequestItem.
     * 
     * @return itemPath
     */
    public java.lang.String getItemPath() {
        return itemPath;
    }


    /**
     * Sets the itemPath value for this SubscribeRequestItem.
     * 
     * @param itemPath
     */
    public void setItemPath(java.lang.String itemPath) {
        this.itemPath = itemPath;
    }


    /**
     * Gets the reqType value for this SubscribeRequestItem.
     * 
     * @return reqType
     */
    public javax.xml.namespace.QName getReqType() {
        return reqType;
    }


    /**
     * Sets the reqType value for this SubscribeRequestItem.
     * 
     * @param reqType
     */
    public void setReqType(javax.xml.namespace.QName reqType) {
        this.reqType = reqType;
    }


    /**
     * Gets the itemName value for this SubscribeRequestItem.
     * 
     * @return itemName
     */
    public java.lang.String getItemName() {
        return itemName;
    }


    /**
     * Sets the itemName value for this SubscribeRequestItem.
     * 
     * @param itemName
     */
    public void setItemName(java.lang.String itemName) {
        this.itemName = itemName;
    }


    /**
     * Gets the clientItemHandle value for this SubscribeRequestItem.
     * 
     * @return clientItemHandle
     */
    public java.lang.String getClientItemHandle() {
        return clientItemHandle;
    }


    /**
     * Sets the clientItemHandle value for this SubscribeRequestItem.
     * 
     * @param clientItemHandle
     */
    public void setClientItemHandle(java.lang.String clientItemHandle) {
        this.clientItemHandle = clientItemHandle;
    }


    /**
     * Gets the deadband value for this SubscribeRequestItem.
     * 
     * @return deadband
     */
    public float getDeadband() {
        return deadband;
    }


    /**
     * Sets the deadband value for this SubscribeRequestItem.
     * 
     * @param deadband
     */
    public void setDeadband(float deadband) {
        this.deadband = deadband;
    }


    /**
     * Gets the requestedSamplingRate value for this SubscribeRequestItem.
     * 
     * @return requestedSamplingRate
     */
    public int getRequestedSamplingRate() {
        return requestedSamplingRate;
    }


    /**
     * Sets the requestedSamplingRate value for this SubscribeRequestItem.
     * 
     * @param requestedSamplingRate
     */
    public void setRequestedSamplingRate(int requestedSamplingRate) {
        this.requestedSamplingRate = requestedSamplingRate;
    }


    /**
     * Gets the enableBuffering value for this SubscribeRequestItem.
     * 
     * @return enableBuffering
     */
    public boolean isEnableBuffering() {
        return enableBuffering;
    }


    /**
     * Sets the enableBuffering value for this SubscribeRequestItem.
     * 
     * @param enableBuffering
     */
    public void setEnableBuffering(boolean enableBuffering) {
        this.enableBuffering = enableBuffering;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SubscribeRequestItem)) return false;
        SubscribeRequestItem other = (SubscribeRequestItem) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.itemPath==null && other.getItemPath()==null) || 
             (this.itemPath!=null &&
              this.itemPath.equals(other.getItemPath()))) &&
            ((this.reqType==null && other.getReqType()==null) || 
             (this.reqType!=null &&
              this.reqType.equals(other.getReqType()))) &&
            ((this.itemName==null && other.getItemName()==null) || 
             (this.itemName!=null &&
              this.itemName.equals(other.getItemName()))) &&
            ((this.clientItemHandle==null && other.getClientItemHandle()==null) || 
             (this.clientItemHandle!=null &&
              this.clientItemHandle.equals(other.getClientItemHandle()))) &&
            this.deadband == other.getDeadband() &&
            this.requestedSamplingRate == other.getRequestedSamplingRate() &&
            this.enableBuffering == other.isEnableBuffering();
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
        if (getItemPath() != null) {
            _hashCode += getItemPath().hashCode();
        }
        if (getReqType() != null) {
            _hashCode += getReqType().hashCode();
        }
        if (getItemName() != null) {
            _hashCode += getItemName().hashCode();
        }
        if (getClientItemHandle() != null) {
            _hashCode += getClientItemHandle().hashCode();
        }
        _hashCode += new Float(getDeadband()).hashCode();
        _hashCode += getRequestedSamplingRate();
        _hashCode += (isEnableBuffering() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SubscribeRequestItem.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "SubscribeRequestItem"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("itemPath");
        attrField.setXmlName(new javax.xml.namespace.QName("", "ItemPath"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("reqType");
        attrField.setXmlName(new javax.xml.namespace.QName("", "ReqType"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "QName"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("itemName");
        attrField.setXmlName(new javax.xml.namespace.QName("", "ItemName"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("clientItemHandle");
        attrField.setXmlName(new javax.xml.namespace.QName("", "ClientItemHandle"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("deadband");
        attrField.setXmlName(new javax.xml.namespace.QName("", "Deadband"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "float"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("requestedSamplingRate");
        attrField.setXmlName(new javax.xml.namespace.QName("", "RequestedSamplingRate"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("enableBuffering");
        attrField.setXmlName(new javax.xml.namespace.QName("", "EnableBuffering"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
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

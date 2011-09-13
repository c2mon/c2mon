/**
 * SubscriptionPolledRefresh.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.opcfoundation.webservices.XMLDA._1_0;

public class SubscriptionPolledRefresh  implements java.io.Serializable {
    private org.opcfoundation.webservices.XMLDA._1_0.RequestOptions options;

    private java.lang.String[] serverSubHandles;

    private java.util.Calendar holdTime;  // attribute

    private int waitTime;  // attribute

    private boolean returnAllItems;  // attribute

    public SubscriptionPolledRefresh() {
    }

    public SubscriptionPolledRefresh(
           org.opcfoundation.webservices.XMLDA._1_0.RequestOptions options,
           java.lang.String[] serverSubHandles,
           java.util.Calendar holdTime,
           int waitTime,
           boolean returnAllItems) {
           this.options = options;
           this.serverSubHandles = serverSubHandles;
           this.holdTime = holdTime;
           this.waitTime = waitTime;
           this.returnAllItems = returnAllItems;
    }


    /**
     * Gets the options value for this SubscriptionPolledRefresh.
     * 
     * @return options
     */
    public org.opcfoundation.webservices.XMLDA._1_0.RequestOptions getOptions() {
        return options;
    }


    /**
     * Sets the options value for this SubscriptionPolledRefresh.
     * 
     * @param options
     */
    public void setOptions(org.opcfoundation.webservices.XMLDA._1_0.RequestOptions options) {
        this.options = options;
    }


    /**
     * Gets the serverSubHandles value for this SubscriptionPolledRefresh.
     * 
     * @return serverSubHandles
     */
    public java.lang.String[] getServerSubHandles() {
        return serverSubHandles;
    }


    /**
     * Sets the serverSubHandles value for this SubscriptionPolledRefresh.
     * 
     * @param serverSubHandles
     */
    public void setServerSubHandles(java.lang.String[] serverSubHandles) {
        this.serverSubHandles = serverSubHandles;
    }

    public java.lang.String getServerSubHandles(int i) {
        return this.serverSubHandles[i];
    }

    public void setServerSubHandles(int i, java.lang.String _value) {
        this.serverSubHandles[i] = _value;
    }


    /**
     * Gets the holdTime value for this SubscriptionPolledRefresh.
     * 
     * @return holdTime
     */
    public java.util.Calendar getHoldTime() {
        return holdTime;
    }


    /**
     * Sets the holdTime value for this SubscriptionPolledRefresh.
     * 
     * @param holdTime
     */
    public void setHoldTime(java.util.Calendar holdTime) {
        this.holdTime = holdTime;
    }


    /**
     * Gets the waitTime value for this SubscriptionPolledRefresh.
     * 
     * @return waitTime
     */
    public int getWaitTime() {
        return waitTime;
    }


    /**
     * Sets the waitTime value for this SubscriptionPolledRefresh.
     * 
     * @param waitTime
     */
    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }


    /**
     * Gets the returnAllItems value for this SubscriptionPolledRefresh.
     * 
     * @return returnAllItems
     */
    public boolean isReturnAllItems() {
        return returnAllItems;
    }


    /**
     * Sets the returnAllItems value for this SubscriptionPolledRefresh.
     * 
     * @param returnAllItems
     */
    public void setReturnAllItems(boolean returnAllItems) {
        this.returnAllItems = returnAllItems;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SubscriptionPolledRefresh)) return false;
        SubscriptionPolledRefresh other = (SubscriptionPolledRefresh) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.options==null && other.getOptions()==null) || 
             (this.options!=null &&
              this.options.equals(other.getOptions()))) &&
            ((this.serverSubHandles==null && other.getServerSubHandles()==null) || 
             (this.serverSubHandles!=null &&
              java.util.Arrays.equals(this.serverSubHandles, other.getServerSubHandles()))) &&
            ((this.holdTime==null && other.getHoldTime()==null) || 
             (this.holdTime!=null &&
              this.holdTime.equals(other.getHoldTime()))) &&
            this.waitTime == other.getWaitTime() &&
            this.returnAllItems == other.isReturnAllItems();
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
        if (getOptions() != null) {
            _hashCode += getOptions().hashCode();
        }
        if (getServerSubHandles() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getServerSubHandles());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getServerSubHandles(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getHoldTime() != null) {
            _hashCode += getHoldTime().hashCode();
        }
        _hashCode += getWaitTime();
        _hashCode += (isReturnAllItems() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SubscriptionPolledRefresh.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", ">SubscriptionPolledRefresh"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("holdTime");
        attrField.setXmlName(new javax.xml.namespace.QName("", "HoldTime"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("waitTime");
        attrField.setXmlName(new javax.xml.namespace.QName("", "WaitTime"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("returnAllItems");
        attrField.setXmlName(new javax.xml.namespace.QName("", "ReturnAllItems"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("options");
        elemField.setXmlName(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "Options"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "RequestOptions"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("serverSubHandles");
        elemField.setXmlName(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "ServerSubHandles"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
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

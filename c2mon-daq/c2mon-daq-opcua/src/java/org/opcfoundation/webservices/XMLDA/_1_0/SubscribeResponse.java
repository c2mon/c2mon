/**
 * SubscribeResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.opcfoundation.webservices.XMLDA._1_0;

public class SubscribeResponse  implements java.io.Serializable {
    private org.opcfoundation.webservices.XMLDA._1_0.ReplyBase subscribeResult;

    private org.opcfoundation.webservices.XMLDA._1_0.SubscribeItemValue[] RItemList;

    private org.opcfoundation.webservices.XMLDA._1_0.OPCError[] errors;

    private java.lang.String serverSubHandle;  // attribute

    public SubscribeResponse() {
    }

    public SubscribeResponse(
           org.opcfoundation.webservices.XMLDA._1_0.ReplyBase subscribeResult,
           org.opcfoundation.webservices.XMLDA._1_0.SubscribeItemValue[] RItemList,
           org.opcfoundation.webservices.XMLDA._1_0.OPCError[] errors,
           java.lang.String serverSubHandle) {
           this.subscribeResult = subscribeResult;
           this.RItemList = RItemList;
           this.errors = errors;
           this.serverSubHandle = serverSubHandle;
    }


    /**
     * Gets the subscribeResult value for this SubscribeResponse.
     * 
     * @return subscribeResult
     */
    public org.opcfoundation.webservices.XMLDA._1_0.ReplyBase getSubscribeResult() {
        return subscribeResult;
    }


    /**
     * Sets the subscribeResult value for this SubscribeResponse.
     * 
     * @param subscribeResult
     */
    public void setSubscribeResult(org.opcfoundation.webservices.XMLDA._1_0.ReplyBase subscribeResult) {
        this.subscribeResult = subscribeResult;
    }


    /**
     * Gets the RItemList value for this SubscribeResponse.
     * 
     * @return RItemList
     */
    public org.opcfoundation.webservices.XMLDA._1_0.SubscribeItemValue[] getRItemList() {
        return RItemList;
    }


    /**
     * Sets the RItemList value for this SubscribeResponse.
     * 
     * @param RItemList
     */
    public void setRItemList(org.opcfoundation.webservices.XMLDA._1_0.SubscribeItemValue[] RItemList) {
        this.RItemList = RItemList;
    }


    /**
     * Gets the errors value for this SubscribeResponse.
     * 
     * @return errors
     */
    public org.opcfoundation.webservices.XMLDA._1_0.OPCError[] getErrors() {
        return errors;
    }


    /**
     * Sets the errors value for this SubscribeResponse.
     * 
     * @param errors
     */
    public void setErrors(org.opcfoundation.webservices.XMLDA._1_0.OPCError[] errors) {
        this.errors = errors;
    }

    public org.opcfoundation.webservices.XMLDA._1_0.OPCError getErrors(int i) {
        return this.errors[i];
    }

    public void setErrors(int i, org.opcfoundation.webservices.XMLDA._1_0.OPCError _value) {
        this.errors[i] = _value;
    }


    /**
     * Gets the serverSubHandle value for this SubscribeResponse.
     * 
     * @return serverSubHandle
     */
    public java.lang.String getServerSubHandle() {
        return serverSubHandle;
    }


    /**
     * Sets the serverSubHandle value for this SubscribeResponse.
     * 
     * @param serverSubHandle
     */
    public void setServerSubHandle(java.lang.String serverSubHandle) {
        this.serverSubHandle = serverSubHandle;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SubscribeResponse)) return false;
        SubscribeResponse other = (SubscribeResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.subscribeResult==null && other.getSubscribeResult()==null) || 
             (this.subscribeResult!=null &&
              this.subscribeResult.equals(other.getSubscribeResult()))) &&
            ((this.RItemList==null && other.getRItemList()==null) || 
             (this.RItemList!=null &&
              java.util.Arrays.equals(this.RItemList, other.getRItemList()))) &&
            ((this.errors==null && other.getErrors()==null) || 
             (this.errors!=null &&
              java.util.Arrays.equals(this.errors, other.getErrors()))) &&
            ((this.serverSubHandle==null && other.getServerSubHandle()==null) || 
             (this.serverSubHandle!=null &&
              this.serverSubHandle.equals(other.getServerSubHandle())));
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
        if (getSubscribeResult() != null) {
            _hashCode += getSubscribeResult().hashCode();
        }
        if (getRItemList() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getRItemList());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getRItemList(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getErrors() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getErrors());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getErrors(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getServerSubHandle() != null) {
            _hashCode += getServerSubHandle().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SubscribeResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", ">SubscribeResponse"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("serverSubHandle");
        attrField.setXmlName(new javax.xml.namespace.QName("", "ServerSubHandle"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("subscribeResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "SubscribeResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "ReplyBase"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("RItemList");
        elemField.setXmlName(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "RItemList"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "SubscribeItemValue"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "Items"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("errors");
        elemField.setXmlName(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "Errors"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "OPCError"));
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

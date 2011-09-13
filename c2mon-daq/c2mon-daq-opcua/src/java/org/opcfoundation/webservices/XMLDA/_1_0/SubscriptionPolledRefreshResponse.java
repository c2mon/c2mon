/**
 * SubscriptionPolledRefreshResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.opcfoundation.webservices.XMLDA._1_0;

public class SubscriptionPolledRefreshResponse  implements java.io.Serializable {
    private org.opcfoundation.webservices.XMLDA._1_0.ReplyBase subscriptionPolledRefreshResult;

    private java.lang.String[] invalidServerSubHandles;

    private org.opcfoundation.webservices.XMLDA._1_0.ItemValue[] RItemList;

    private org.opcfoundation.webservices.XMLDA._1_0.OPCError[] errors;

    private boolean dataBufferOverflow;  // attribute

    public SubscriptionPolledRefreshResponse() {
    }

    public SubscriptionPolledRefreshResponse(
           org.opcfoundation.webservices.XMLDA._1_0.ReplyBase subscriptionPolledRefreshResult,
           java.lang.String[] invalidServerSubHandles,
           org.opcfoundation.webservices.XMLDA._1_0.ItemValue[] RItemList,
           org.opcfoundation.webservices.XMLDA._1_0.OPCError[] errors,
           boolean dataBufferOverflow) {
           this.subscriptionPolledRefreshResult = subscriptionPolledRefreshResult;
           this.invalidServerSubHandles = invalidServerSubHandles;
           this.RItemList = RItemList;
           this.errors = errors;
           this.dataBufferOverflow = dataBufferOverflow;
    }


    /**
     * Gets the subscriptionPolledRefreshResult value for this SubscriptionPolledRefreshResponse.
     * 
     * @return subscriptionPolledRefreshResult
     */
    public org.opcfoundation.webservices.XMLDA._1_0.ReplyBase getSubscriptionPolledRefreshResult() {
        return subscriptionPolledRefreshResult;
    }


    /**
     * Sets the subscriptionPolledRefreshResult value for this SubscriptionPolledRefreshResponse.
     * 
     * @param subscriptionPolledRefreshResult
     */
    public void setSubscriptionPolledRefreshResult(org.opcfoundation.webservices.XMLDA._1_0.ReplyBase subscriptionPolledRefreshResult) {
        this.subscriptionPolledRefreshResult = subscriptionPolledRefreshResult;
    }


    /**
     * Gets the invalidServerSubHandles value for this SubscriptionPolledRefreshResponse.
     * 
     * @return invalidServerSubHandles
     */
    public java.lang.String[] getInvalidServerSubHandles() {
        return invalidServerSubHandles;
    }


    /**
     * Sets the invalidServerSubHandles value for this SubscriptionPolledRefreshResponse.
     * 
     * @param invalidServerSubHandles
     */
    public void setInvalidServerSubHandles(java.lang.String[] invalidServerSubHandles) {
        this.invalidServerSubHandles = invalidServerSubHandles;
    }

    public java.lang.String getInvalidServerSubHandles(int i) {
        return this.invalidServerSubHandles[i];
    }

    public void setInvalidServerSubHandles(int i, java.lang.String _value) {
        this.invalidServerSubHandles[i] = _value;
    }


    /**
     * Gets the RItemList value for this SubscriptionPolledRefreshResponse.
     * 
     * @return RItemList
     */
    public org.opcfoundation.webservices.XMLDA._1_0.ItemValue[] getRItemList() {
        return RItemList;
    }


    /**
     * Sets the RItemList value for this SubscriptionPolledRefreshResponse.
     * 
     * @param RItemList
     */
    public void setRItemList(org.opcfoundation.webservices.XMLDA._1_0.ItemValue[] RItemList) {
        this.RItemList = RItemList;
    }

//    public org.opcfoundation.webservices.XMLDA._1_0.ItemValue[] getRItemList(int i) {
//        return this.RItemList[i];
//    }
//
//    public void setRItemList(int i, org.opcfoundation.webservices.XMLDA._1_0.ItemValue[] _value) {
//        this.RItemList[i] = _value;
//    }


    /**
     * Gets the errors value for this SubscriptionPolledRefreshResponse.
     * 
     * @return errors
     */
    public org.opcfoundation.webservices.XMLDA._1_0.OPCError[] getErrors() {
        return errors;
    }


    /**
     * Sets the errors value for this SubscriptionPolledRefreshResponse.
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
     * Gets the dataBufferOverflow value for this SubscriptionPolledRefreshResponse.
     * 
     * @return dataBufferOverflow
     */
    public boolean isDataBufferOverflow() {
        return dataBufferOverflow;
    }


    /**
     * Sets the dataBufferOverflow value for this SubscriptionPolledRefreshResponse.
     * 
     * @param dataBufferOverflow
     */
    public void setDataBufferOverflow(boolean dataBufferOverflow) {
        this.dataBufferOverflow = dataBufferOverflow;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SubscriptionPolledRefreshResponse)) return false;
        SubscriptionPolledRefreshResponse other = (SubscriptionPolledRefreshResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.subscriptionPolledRefreshResult==null && other.getSubscriptionPolledRefreshResult()==null) || 
             (this.subscriptionPolledRefreshResult!=null &&
              this.subscriptionPolledRefreshResult.equals(other.getSubscriptionPolledRefreshResult()))) &&
            ((this.invalidServerSubHandles==null && other.getInvalidServerSubHandles()==null) || 
             (this.invalidServerSubHandles!=null &&
              java.util.Arrays.equals(this.invalidServerSubHandles, other.getInvalidServerSubHandles()))) &&
            ((this.RItemList==null && other.getRItemList()==null) || 
             (this.RItemList!=null &&
              java.util.Arrays.equals(this.RItemList, other.getRItemList()))) &&
            ((this.errors==null && other.getErrors()==null) || 
             (this.errors!=null &&
              java.util.Arrays.equals(this.errors, other.getErrors()))) &&
            this.dataBufferOverflow == other.isDataBufferOverflow();
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
        if (getSubscriptionPolledRefreshResult() != null) {
            _hashCode += getSubscriptionPolledRefreshResult().hashCode();
        }
        if (getInvalidServerSubHandles() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getInvalidServerSubHandles());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getInvalidServerSubHandles(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
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
        _hashCode += (isDataBufferOverflow() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SubscriptionPolledRefreshResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", ">SubscriptionPolledRefreshResponse"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("dataBufferOverflow");
        attrField.setXmlName(new javax.xml.namespace.QName("", "DataBufferOverflow"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("subscriptionPolledRefreshResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "SubscriptionPolledRefreshResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "ReplyBase"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("invalidServerSubHandles");
        elemField.setXmlName(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "InvalidServerSubHandles"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("RItemList");
        elemField.setXmlName(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "RItemList"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "SubscribePolledRefreshReplyItemList"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
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

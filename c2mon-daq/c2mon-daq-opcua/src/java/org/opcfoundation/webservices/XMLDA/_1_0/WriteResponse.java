/**
 * WriteResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.opcfoundation.webservices.XMLDA._1_0;

public class WriteResponse  implements java.io.Serializable {
    private org.opcfoundation.webservices.XMLDA._1_0.ReplyBase writeResult;

    private org.opcfoundation.webservices.XMLDA._1_0.ItemValue[] RItemList;

    private org.opcfoundation.webservices.XMLDA._1_0.OPCError[] errors;

    public WriteResponse() {
    }

    public WriteResponse(
           org.opcfoundation.webservices.XMLDA._1_0.ReplyBase writeResult,
           org.opcfoundation.webservices.XMLDA._1_0.ItemValue[] RItemList,
           org.opcfoundation.webservices.XMLDA._1_0.OPCError[] errors) {
           this.writeResult = writeResult;
           this.RItemList = RItemList;
           this.errors = errors;
    }


    /**
     * Gets the writeResult value for this WriteResponse.
     * 
     * @return writeResult
     */
    public org.opcfoundation.webservices.XMLDA._1_0.ReplyBase getWriteResult() {
        return writeResult;
    }


    /**
     * Sets the writeResult value for this WriteResponse.
     * 
     * @param writeResult
     */
    public void setWriteResult(org.opcfoundation.webservices.XMLDA._1_0.ReplyBase writeResult) {
        this.writeResult = writeResult;
    }


    /**
     * Gets the RItemList value for this WriteResponse.
     * 
     * @return RItemList
     */
    public org.opcfoundation.webservices.XMLDA._1_0.ItemValue[] getRItemList() {
        return RItemList;
    }


    /**
     * Sets the RItemList value for this WriteResponse.
     * 
     * @param RItemList
     */
    public void setRItemList(org.opcfoundation.webservices.XMLDA._1_0.ItemValue[] RItemList) {
        this.RItemList = RItemList;
    }


    /**
     * Gets the errors value for this WriteResponse.
     * 
     * @return errors
     */
    public org.opcfoundation.webservices.XMLDA._1_0.OPCError[] getErrors() {
        return errors;
    }


    /**
     * Sets the errors value for this WriteResponse.
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

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof WriteResponse)) return false;
        WriteResponse other = (WriteResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.writeResult==null && other.getWriteResult()==null) || 
             (this.writeResult!=null &&
              this.writeResult.equals(other.getWriteResult()))) &&
            ((this.RItemList==null && other.getRItemList()==null) || 
             (this.RItemList!=null &&
              java.util.Arrays.equals(this.RItemList, other.getRItemList()))) &&
            ((this.errors==null && other.getErrors()==null) || 
             (this.errors!=null &&
              java.util.Arrays.equals(this.errors, other.getErrors())));
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
        if (getWriteResult() != null) {
            _hashCode += getWriteResult().hashCode();
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
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(WriteResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", ">WriteResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("writeResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "WriteResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "ReplyBase"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("RItemList");
        elemField.setXmlName(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "RItemList"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "ItemValue"));
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

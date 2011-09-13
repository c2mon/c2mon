/**
 * GetPropertiesResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.opcfoundation.webservices.XMLDA._1_0;

public class GetPropertiesResponse  implements java.io.Serializable {
    private org.opcfoundation.webservices.XMLDA._1_0.ReplyBase getPropertiesResult;

    private org.opcfoundation.webservices.XMLDA._1_0.ItemProperty[][] propertyLists;

    private org.opcfoundation.webservices.XMLDA._1_0.OPCError[] errors;

    public GetPropertiesResponse() {
    }

    public GetPropertiesResponse(
           org.opcfoundation.webservices.XMLDA._1_0.ReplyBase getPropertiesResult,
           org.opcfoundation.webservices.XMLDA._1_0.ItemProperty[][] propertyLists,
           org.opcfoundation.webservices.XMLDA._1_0.OPCError[] errors) {
           this.getPropertiesResult = getPropertiesResult;
           this.propertyLists = propertyLists;
           this.errors = errors;
    }


    /**
     * Gets the getPropertiesResult value for this GetPropertiesResponse.
     * 
     * @return getPropertiesResult
     */
    public org.opcfoundation.webservices.XMLDA._1_0.ReplyBase getGetPropertiesResult() {
        return getPropertiesResult;
    }


    /**
     * Sets the getPropertiesResult value for this GetPropertiesResponse.
     * 
     * @param getPropertiesResult
     */
    public void setGetPropertiesResult(org.opcfoundation.webservices.XMLDA._1_0.ReplyBase getPropertiesResult) {
        this.getPropertiesResult = getPropertiesResult;
    }


    /**
     * Gets the propertyLists value for this GetPropertiesResponse.
     * 
     * @return propertyLists
     */
    public org.opcfoundation.webservices.XMLDA._1_0.ItemProperty[][] getPropertyLists() {
        return propertyLists;
    }


    /**
     * Sets the propertyLists value for this GetPropertiesResponse.
     * 
     * @param propertyLists
     */
    public void setPropertyLists(org.opcfoundation.webservices.XMLDA._1_0.ItemProperty[][] propertyLists) {
        this.propertyLists = propertyLists;
    }

    public org.opcfoundation.webservices.XMLDA._1_0.ItemProperty[] getPropertyLists(int i) {
        return this.propertyLists[i];
    }

    public void setPropertyLists(int i, org.opcfoundation.webservices.XMLDA._1_0.ItemProperty[] _value) {
        this.propertyLists[i] = _value;
    }


    /**
     * Gets the errors value for this GetPropertiesResponse.
     * 
     * @return errors
     */
    public org.opcfoundation.webservices.XMLDA._1_0.OPCError[] getErrors() {
        return errors;
    }


    /**
     * Sets the errors value for this GetPropertiesResponse.
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
        if (!(obj instanceof GetPropertiesResponse)) return false;
        GetPropertiesResponse other = (GetPropertiesResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.getPropertiesResult==null && other.getGetPropertiesResult()==null) || 
             (this.getPropertiesResult!=null &&
              this.getPropertiesResult.equals(other.getGetPropertiesResult()))) &&
            ((this.propertyLists==null && other.getPropertyLists()==null) || 
             (this.propertyLists!=null &&
              java.util.Arrays.equals(this.propertyLists, other.getPropertyLists()))) &&
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
        if (getGetPropertiesResult() != null) {
            _hashCode += getGetPropertiesResult().hashCode();
        }
        if (getPropertyLists() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getPropertyLists());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getPropertyLists(), i);
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
        new org.apache.axis.description.TypeDesc(GetPropertiesResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", ">GetPropertiesResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("getPropertiesResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "GetPropertiesResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "ReplyBase"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("propertyLists");
        elemField.setXmlName(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "PropertyLists"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "PropertyReplyList"));
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

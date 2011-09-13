/**
 * GetStatusResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.opcfoundation.webservices.XMLDA._1_0;

public class GetStatusResponse  implements java.io.Serializable {
    private org.opcfoundation.webservices.XMLDA._1_0.ReplyBase getStatusResult;

    private org.opcfoundation.webservices.XMLDA._1_0.ServerStatus status;

    public GetStatusResponse() {
    }

    public GetStatusResponse(
           org.opcfoundation.webservices.XMLDA._1_0.ReplyBase getStatusResult,
           org.opcfoundation.webservices.XMLDA._1_0.ServerStatus status) {
           this.getStatusResult = getStatusResult;
           this.status = status;
    }


    /**
     * Gets the getStatusResult value for this GetStatusResponse.
     * 
     * @return getStatusResult
     */
    public org.opcfoundation.webservices.XMLDA._1_0.ReplyBase getGetStatusResult() {
        return getStatusResult;
    }


    /**
     * Sets the getStatusResult value for this GetStatusResponse.
     * 
     * @param getStatusResult
     */
    public void setGetStatusResult(org.opcfoundation.webservices.XMLDA._1_0.ReplyBase getStatusResult) {
        this.getStatusResult = getStatusResult;
    }


    /**
     * Gets the status value for this GetStatusResponse.
     * 
     * @return status
     */
    public org.opcfoundation.webservices.XMLDA._1_0.ServerStatus getStatus() {
        return status;
    }


    /**
     * Sets the status value for this GetStatusResponse.
     * 
     * @param status
     */
    public void setStatus(org.opcfoundation.webservices.XMLDA._1_0.ServerStatus status) {
        this.status = status;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetStatusResponse)) return false;
        GetStatusResponse other = (GetStatusResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.getStatusResult==null && other.getGetStatusResult()==null) || 
             (this.getStatusResult!=null &&
              this.getStatusResult.equals(other.getGetStatusResult()))) &&
            ((this.status==null && other.getStatus()==null) || 
             (this.status!=null &&
              this.status.equals(other.getStatus())));
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
        if (getGetStatusResult() != null) {
            _hashCode += getGetStatusResult().hashCode();
        }
        if (getStatus() != null) {
            _hashCode += getStatus().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetStatusResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", ">GetStatusResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("getStatusResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "GetStatusResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "ReplyBase"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("status");
        elemField.setXmlName(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "Status"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "ServerStatus"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
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

/**
 * OPCQuality.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.opcfoundation.webservices.XMLDA._1_0;

public class OPCQuality  implements java.io.Serializable {
    private org.opcfoundation.webservices.XMLDA._1_0.QualityBits qualityField;  // attribute

    private org.opcfoundation.webservices.XMLDA._1_0.LimitBits limitField;  // attribute

    private org.apache.axis.types.UnsignedByte vendorField;  // attribute

    public OPCQuality() {
    }

    public OPCQuality(
           org.opcfoundation.webservices.XMLDA._1_0.QualityBits qualityField,
           org.opcfoundation.webservices.XMLDA._1_0.LimitBits limitField,
           org.apache.axis.types.UnsignedByte vendorField) {
           this.qualityField = qualityField;
           this.limitField = limitField;
           this.vendorField = vendorField;
    }


    /**
     * Gets the qualityField value for this OPCQuality.
     * 
     * @return qualityField
     */
    public org.opcfoundation.webservices.XMLDA._1_0.QualityBits getQualityField() {
        return qualityField;
    }


    /**
     * Sets the qualityField value for this OPCQuality.
     * 
     * @param qualityField
     */
    public void setQualityField(org.opcfoundation.webservices.XMLDA._1_0.QualityBits qualityField) {
        this.qualityField = qualityField;
    }


    /**
     * Gets the limitField value for this OPCQuality.
     * 
     * @return limitField
     */
    public org.opcfoundation.webservices.XMLDA._1_0.LimitBits getLimitField() {
        return limitField;
    }


    /**
     * Sets the limitField value for this OPCQuality.
     * 
     * @param limitField
     */
    public void setLimitField(org.opcfoundation.webservices.XMLDA._1_0.LimitBits limitField) {
        this.limitField = limitField;
    }


    /**
     * Gets the vendorField value for this OPCQuality.
     * 
     * @return vendorField
     */
    public org.apache.axis.types.UnsignedByte getVendorField() {
        return vendorField;
    }


    /**
     * Sets the vendorField value for this OPCQuality.
     * 
     * @param vendorField
     */
    public void setVendorField(org.apache.axis.types.UnsignedByte vendorField) {
        this.vendorField = vendorField;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof OPCQuality)) return false;
        OPCQuality other = (OPCQuality) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.qualityField==null && other.getQualityField()==null) || 
             (this.qualityField!=null &&
              this.qualityField.equals(other.getQualityField()))) &&
            ((this.limitField==null && other.getLimitField()==null) || 
             (this.limitField!=null &&
              this.limitField.equals(other.getLimitField()))) &&
            ((this.vendorField==null && other.getVendorField()==null) || 
             (this.vendorField!=null &&
              this.vendorField.equals(other.getVendorField())));
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
        if (getQualityField() != null) {
            _hashCode += getQualityField().hashCode();
        }
        if (getLimitField() != null) {
            _hashCode += getLimitField().hashCode();
        }
        if (getVendorField() != null) {
            _hashCode += getVendorField().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(OPCQuality.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "OPCQuality"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("qualityField");
        attrField.setXmlName(new javax.xml.namespace.QName("", "QualityField"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "qualityBits"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("limitField");
        attrField.setXmlName(new javax.xml.namespace.QName("", "LimitField"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "limitBits"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("vendorField");
        attrField.setXmlName(new javax.xml.namespace.QName("", "VendorField"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "unsignedByte"));
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

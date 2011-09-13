/**
 * SubscribeItemValue.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.opcfoundation.webservices.XMLDA._1_0;

public class SubscribeItemValue  implements java.io.Serializable {
    private org.opcfoundation.webservices.XMLDA._1_0.ItemValue itemValue;

    private int revisedSamplingRate;  // attribute

    public SubscribeItemValue() {
    }

    public SubscribeItemValue(
           org.opcfoundation.webservices.XMLDA._1_0.ItemValue itemValue,
           int revisedSamplingRate) {
           this.itemValue = itemValue;
           this.revisedSamplingRate = revisedSamplingRate;
    }


    /**
     * Gets the itemValue value for this SubscribeItemValue.
     * 
     * @return itemValue
     */
    public org.opcfoundation.webservices.XMLDA._1_0.ItemValue getItemValue() {
        return itemValue;
    }


    /**
     * Sets the itemValue value for this SubscribeItemValue.
     * 
     * @param itemValue
     */
    public void setItemValue(org.opcfoundation.webservices.XMLDA._1_0.ItemValue itemValue) {
        this.itemValue = itemValue;
    }


    /**
     * Gets the revisedSamplingRate value for this SubscribeItemValue.
     * 
     * @return revisedSamplingRate
     */
    public int getRevisedSamplingRate() {
        return revisedSamplingRate;
    }


    /**
     * Sets the revisedSamplingRate value for this SubscribeItemValue.
     * 
     * @param revisedSamplingRate
     */
    public void setRevisedSamplingRate(int revisedSamplingRate) {
        this.revisedSamplingRate = revisedSamplingRate;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SubscribeItemValue)) return false;
        SubscribeItemValue other = (SubscribeItemValue) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.itemValue==null && other.getItemValue()==null) || 
             (this.itemValue!=null &&
              this.itemValue.equals(other.getItemValue()))) &&
            this.revisedSamplingRate == other.getRevisedSamplingRate();
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
        if (getItemValue() != null) {
            _hashCode += getItemValue().hashCode();
        }
        _hashCode += getRevisedSamplingRate();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SubscribeItemValue.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "SubscribeItemValue"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("revisedSamplingRate");
        attrField.setXmlName(new javax.xml.namespace.QName("", "RevisedSamplingRate"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("itemValue");
        elemField.setXmlName(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "ItemValue"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "ItemValue"));
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

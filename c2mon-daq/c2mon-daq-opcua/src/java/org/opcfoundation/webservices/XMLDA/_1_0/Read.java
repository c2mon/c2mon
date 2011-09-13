/**
 * Read.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.opcfoundation.webservices.XMLDA._1_0;

public class Read  implements java.io.Serializable {
    private org.opcfoundation.webservices.XMLDA._1_0.RequestOptions options;

    private org.opcfoundation.webservices.XMLDA._1_0.ReadRequestItem[] itemList;

    public Read() {
    }

    public Read(
           org.opcfoundation.webservices.XMLDA._1_0.RequestOptions options,
           org.opcfoundation.webservices.XMLDA._1_0.ReadRequestItem[] itemList) {
           this.options = options;
           this.itemList = itemList;
    }


    /**
     * Gets the options value for this Read.
     * 
     * @return options
     */
    public org.opcfoundation.webservices.XMLDA._1_0.RequestOptions getOptions() {
        return options;
    }


    /**
     * Sets the options value for this Read.
     * 
     * @param options
     */
    public void setOptions(org.opcfoundation.webservices.XMLDA._1_0.RequestOptions options) {
        this.options = options;
    }


    /**
     * Gets the itemList value for this Read.
     * 
     * @return itemList
     */
    public org.opcfoundation.webservices.XMLDA._1_0.ReadRequestItem[] getItemList() {
        return itemList;
    }


    /**
     * Sets the itemList value for this Read.
     * 
     * @param itemList
     */
    public void setItemList(org.opcfoundation.webservices.XMLDA._1_0.ReadRequestItem[] itemList) {
        this.itemList = itemList;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Read)) return false;
        Read other = (Read) obj;
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
            ((this.itemList==null && other.getItemList()==null) || 
             (this.itemList!=null &&
              java.util.Arrays.equals(this.itemList, other.getItemList())));
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
        if (getItemList() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getItemList());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getItemList(), i);
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
        new org.apache.axis.description.TypeDesc(Read.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", ">Read"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("options");
        elemField.setXmlName(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "Options"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "RequestOptions"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("itemList");
        elemField.setXmlName(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "ItemList"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "ReadRequestItem"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "Items"));
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

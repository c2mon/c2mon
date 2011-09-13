/**
 * GetProperties.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.opcfoundation.webservices.XMLDA._1_0;

public class GetProperties  implements java.io.Serializable {
    private org.opcfoundation.webservices.XMLDA._1_0.ItemIdentifier[] itemIDs;

    private javax.xml.namespace.QName[] propertyNames;

    private java.lang.String localeID;  // attribute

    private java.lang.String clientRequestHandle;  // attribute

    private java.lang.String itemPath;  // attribute

    private boolean returnAllProperties;  // attribute

    private boolean returnPropertyValues;  // attribute

    private boolean returnErrorText;  // attribute

    public GetProperties() {
    }

    public GetProperties(
           org.opcfoundation.webservices.XMLDA._1_0.ItemIdentifier[] itemIDs,
           javax.xml.namespace.QName[] propertyNames,
           java.lang.String localeID,
           java.lang.String clientRequestHandle,
           java.lang.String itemPath,
           boolean returnAllProperties,
           boolean returnPropertyValues,
           boolean returnErrorText) {
           this.itemIDs = itemIDs;
           this.propertyNames = propertyNames;
           this.localeID = localeID;
           this.clientRequestHandle = clientRequestHandle;
           this.itemPath = itemPath;
           this.returnAllProperties = returnAllProperties;
           this.returnPropertyValues = returnPropertyValues;
           this.returnErrorText = returnErrorText;
    }


    /**
     * Gets the itemIDs value for this GetProperties.
     * 
     * @return itemIDs
     */
    public org.opcfoundation.webservices.XMLDA._1_0.ItemIdentifier[] getItemIDs() {
        return itemIDs;
    }


    /**
     * Sets the itemIDs value for this GetProperties.
     * 
     * @param itemIDs
     */
    public void setItemIDs(org.opcfoundation.webservices.XMLDA._1_0.ItemIdentifier[] itemIDs) {
        this.itemIDs = itemIDs;
    }

    public org.opcfoundation.webservices.XMLDA._1_0.ItemIdentifier getItemIDs(int i) {
        return this.itemIDs[i];
    }

    public void setItemIDs(int i, org.opcfoundation.webservices.XMLDA._1_0.ItemIdentifier _value) {
        this.itemIDs[i] = _value;
    }


    /**
     * Gets the propertyNames value for this GetProperties.
     * 
     * @return propertyNames
     */
    public javax.xml.namespace.QName[] getPropertyNames() {
        return propertyNames;
    }


    /**
     * Sets the propertyNames value for this GetProperties.
     * 
     * @param propertyNames
     */
    public void setPropertyNames(javax.xml.namespace.QName[] propertyNames) {
        this.propertyNames = propertyNames;
    }

    public javax.xml.namespace.QName getPropertyNames(int i) {
        return this.propertyNames[i];
    }

    public void setPropertyNames(int i, javax.xml.namespace.QName _value) {
        this.propertyNames[i] = _value;
    }


    /**
     * Gets the localeID value for this GetProperties.
     * 
     * @return localeID
     */
    public java.lang.String getLocaleID() {
        return localeID;
    }


    /**
     * Sets the localeID value for this GetProperties.
     * 
     * @param localeID
     */
    public void setLocaleID(java.lang.String localeID) {
        this.localeID = localeID;
    }


    /**
     * Gets the clientRequestHandle value for this GetProperties.
     * 
     * @return clientRequestHandle
     */
    public java.lang.String getClientRequestHandle() {
        return clientRequestHandle;
    }


    /**
     * Sets the clientRequestHandle value for this GetProperties.
     * 
     * @param clientRequestHandle
     */
    public void setClientRequestHandle(java.lang.String clientRequestHandle) {
        this.clientRequestHandle = clientRequestHandle;
    }


    /**
     * Gets the itemPath value for this GetProperties.
     * 
     * @return itemPath
     */
    public java.lang.String getItemPath() {
        return itemPath;
    }


    /**
     * Sets the itemPath value for this GetProperties.
     * 
     * @param itemPath
     */
    public void setItemPath(java.lang.String itemPath) {
        this.itemPath = itemPath;
    }


    /**
     * Gets the returnAllProperties value for this GetProperties.
     * 
     * @return returnAllProperties
     */
    public boolean isReturnAllProperties() {
        return returnAllProperties;
    }


    /**
     * Sets the returnAllProperties value for this GetProperties.
     * 
     * @param returnAllProperties
     */
    public void setReturnAllProperties(boolean returnAllProperties) {
        this.returnAllProperties = returnAllProperties;
    }


    /**
     * Gets the returnPropertyValues value for this GetProperties.
     * 
     * @return returnPropertyValues
     */
    public boolean isReturnPropertyValues() {
        return returnPropertyValues;
    }


    /**
     * Sets the returnPropertyValues value for this GetProperties.
     * 
     * @param returnPropertyValues
     */
    public void setReturnPropertyValues(boolean returnPropertyValues) {
        this.returnPropertyValues = returnPropertyValues;
    }


    /**
     * Gets the returnErrorText value for this GetProperties.
     * 
     * @return returnErrorText
     */
    public boolean isReturnErrorText() {
        return returnErrorText;
    }


    /**
     * Sets the returnErrorText value for this GetProperties.
     * 
     * @param returnErrorText
     */
    public void setReturnErrorText(boolean returnErrorText) {
        this.returnErrorText = returnErrorText;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetProperties)) return false;
        GetProperties other = (GetProperties) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.itemIDs==null && other.getItemIDs()==null) || 
             (this.itemIDs!=null &&
              java.util.Arrays.equals(this.itemIDs, other.getItemIDs()))) &&
            ((this.propertyNames==null && other.getPropertyNames()==null) || 
             (this.propertyNames!=null &&
              java.util.Arrays.equals(this.propertyNames, other.getPropertyNames()))) &&
            ((this.localeID==null && other.getLocaleID()==null) || 
             (this.localeID!=null &&
              this.localeID.equals(other.getLocaleID()))) &&
            ((this.clientRequestHandle==null && other.getClientRequestHandle()==null) || 
             (this.clientRequestHandle!=null &&
              this.clientRequestHandle.equals(other.getClientRequestHandle()))) &&
            ((this.itemPath==null && other.getItemPath()==null) || 
             (this.itemPath!=null &&
              this.itemPath.equals(other.getItemPath()))) &&
            this.returnAllProperties == other.isReturnAllProperties() &&
            this.returnPropertyValues == other.isReturnPropertyValues() &&
            this.returnErrorText == other.isReturnErrorText();
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
        if (getItemIDs() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getItemIDs());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getItemIDs(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getPropertyNames() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getPropertyNames());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getPropertyNames(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getLocaleID() != null) {
            _hashCode += getLocaleID().hashCode();
        }
        if (getClientRequestHandle() != null) {
            _hashCode += getClientRequestHandle().hashCode();
        }
        if (getItemPath() != null) {
            _hashCode += getItemPath().hashCode();
        }
        _hashCode += (isReturnAllProperties() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        _hashCode += (isReturnPropertyValues() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        _hashCode += (isReturnErrorText() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetProperties.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", ">GetProperties"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("localeID");
        attrField.setXmlName(new javax.xml.namespace.QName("", "LocaleID"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("clientRequestHandle");
        attrField.setXmlName(new javax.xml.namespace.QName("", "ClientRequestHandle"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("itemPath");
        attrField.setXmlName(new javax.xml.namespace.QName("", "ItemPath"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("returnAllProperties");
        attrField.setXmlName(new javax.xml.namespace.QName("", "ReturnAllProperties"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("returnPropertyValues");
        attrField.setXmlName(new javax.xml.namespace.QName("", "ReturnPropertyValues"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("returnErrorText");
        attrField.setXmlName(new javax.xml.namespace.QName("", "ReturnErrorText"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("itemIDs");
        elemField.setXmlName(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "ItemIDs"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "ItemIdentifier"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("propertyNames");
        elemField.setXmlName(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "PropertyNames"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "QName"));
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

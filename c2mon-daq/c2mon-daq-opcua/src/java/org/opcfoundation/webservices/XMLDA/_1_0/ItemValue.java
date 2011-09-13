/**
 * ItemValue.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.opcfoundation.webservices.XMLDA._1_0;

public class ItemValue  implements java.io.Serializable {
    private java.lang.String diagnosticInfo;

    private java.lang.Object value;

    private org.opcfoundation.webservices.XMLDA._1_0.OPCQuality quality;

    private java.lang.String itemPath;  // attribute

    private java.lang.String itemName;  // attribute

    private java.lang.String clientItemHandle;  // attribute

    private java.util.Calendar timestamp;  // attribute

    private javax.xml.namespace.QName resultID;  // attribute

    private javax.xml.namespace.QName valueTypeQualifier;  // attribute

    public ItemValue() {
    }

    public ItemValue(
           java.lang.String diagnosticInfo,
           java.lang.Object value,
           org.opcfoundation.webservices.XMLDA._1_0.OPCQuality quality,
           java.lang.String itemPath,
           java.lang.String itemName,
           java.lang.String clientItemHandle,
           java.util.Calendar timestamp,
           javax.xml.namespace.QName resultID,
           javax.xml.namespace.QName valueTypeQualifier) {
           this.diagnosticInfo = diagnosticInfo;
           this.value = value;
           this.quality = quality;
           this.itemPath = itemPath;
           this.itemName = itemName;
           this.clientItemHandle = clientItemHandle;
           this.timestamp = timestamp;
           this.resultID = resultID;
           this.valueTypeQualifier = valueTypeQualifier;
    }


    /**
     * Gets the diagnosticInfo value for this ItemValue.
     * 
     * @return diagnosticInfo
     */
    public java.lang.String getDiagnosticInfo() {
        return diagnosticInfo;
    }


    /**
     * Sets the diagnosticInfo value for this ItemValue.
     * 
     * @param diagnosticInfo
     */
    public void setDiagnosticInfo(java.lang.String diagnosticInfo) {
        this.diagnosticInfo = diagnosticInfo;
    }


    /**
     * Gets the value value for this ItemValue.
     * 
     * @return value
     */
    public java.lang.Object getValue() {
        return value;
    }


    /**
     * Sets the value value for this ItemValue.
     * 
     * @param value
     */
    public void setValue(java.lang.Object value) {
        this.value = value;
    }


    /**
     * Gets the quality value for this ItemValue.
     * 
     * @return quality
     */
    public org.opcfoundation.webservices.XMLDA._1_0.OPCQuality getQuality() {
        return quality;
    }


    /**
     * Sets the quality value for this ItemValue.
     * 
     * @param quality
     */
    public void setQuality(org.opcfoundation.webservices.XMLDA._1_0.OPCQuality quality) {
        this.quality = quality;
    }


    /**
     * Gets the itemPath value for this ItemValue.
     * 
     * @return itemPath
     */
    public java.lang.String getItemPath() {
        return itemPath;
    }


    /**
     * Sets the itemPath value for this ItemValue.
     * 
     * @param itemPath
     */
    public void setItemPath(java.lang.String itemPath) {
        this.itemPath = itemPath;
    }


    /**
     * Gets the itemName value for this ItemValue.
     * 
     * @return itemName
     */
    public java.lang.String getItemName() {
        return itemName;
    }


    /**
     * Sets the itemName value for this ItemValue.
     * 
     * @param itemName
     */
    public void setItemName(java.lang.String itemName) {
        this.itemName = itemName;
    }


    /**
     * Gets the clientItemHandle value for this ItemValue.
     * 
     * @return clientItemHandle
     */
    public java.lang.String getClientItemHandle() {
        return clientItemHandle;
    }


    /**
     * Sets the clientItemHandle value for this ItemValue.
     * 
     * @param clientItemHandle
     */
    public void setClientItemHandle(java.lang.String clientItemHandle) {
        this.clientItemHandle = clientItemHandle;
    }


    /**
     * Gets the timestamp value for this ItemValue.
     * 
     * @return timestamp
     */
    public java.util.Calendar getTimestamp() {
        return timestamp;
    }


    /**
     * Sets the timestamp value for this ItemValue.
     * 
     * @param timestamp
     */
    public void setTimestamp(java.util.Calendar timestamp) {
        this.timestamp = timestamp;
    }


    /**
     * Gets the resultID value for this ItemValue.
     * 
     * @return resultID
     */
    public javax.xml.namespace.QName getResultID() {
        return resultID;
    }


    /**
     * Sets the resultID value for this ItemValue.
     * 
     * @param resultID
     */
    public void setResultID(javax.xml.namespace.QName resultID) {
        this.resultID = resultID;
    }


    /**
     * Gets the valueTypeQualifier value for this ItemValue.
     * 
     * @return valueTypeQualifier
     */
    public javax.xml.namespace.QName getValueTypeQualifier() {
        return valueTypeQualifier;
    }


    /**
     * Sets the valueTypeQualifier value for this ItemValue.
     * 
     * @param valueTypeQualifier
     */
    public void setValueTypeQualifier(javax.xml.namespace.QName valueTypeQualifier) {
        this.valueTypeQualifier = valueTypeQualifier;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ItemValue)) return false;
        ItemValue other = (ItemValue) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.diagnosticInfo==null && other.getDiagnosticInfo()==null) || 
             (this.diagnosticInfo!=null &&
              this.diagnosticInfo.equals(other.getDiagnosticInfo()))) &&
            ((this.value==null && other.getValue()==null) || 
             (this.value!=null &&
              this.value.equals(other.getValue()))) &&
            ((this.quality==null && other.getQuality()==null) || 
             (this.quality!=null &&
              this.quality.equals(other.getQuality()))) &&
            ((this.itemPath==null && other.getItemPath()==null) || 
             (this.itemPath!=null &&
              this.itemPath.equals(other.getItemPath()))) &&
            ((this.itemName==null && other.getItemName()==null) || 
             (this.itemName!=null &&
              this.itemName.equals(other.getItemName()))) &&
            ((this.clientItemHandle==null && other.getClientItemHandle()==null) || 
             (this.clientItemHandle!=null &&
              this.clientItemHandle.equals(other.getClientItemHandle()))) &&
            ((this.timestamp==null && other.getTimestamp()==null) || 
             (this.timestamp!=null &&
              this.timestamp.equals(other.getTimestamp()))) &&
            ((this.resultID==null && other.getResultID()==null) || 
             (this.resultID!=null &&
              this.resultID.equals(other.getResultID()))) &&
            ((this.valueTypeQualifier==null && other.getValueTypeQualifier()==null) || 
             (this.valueTypeQualifier!=null &&
              this.valueTypeQualifier.equals(other.getValueTypeQualifier())));
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
        if (getDiagnosticInfo() != null) {
            _hashCode += getDiagnosticInfo().hashCode();
        }
        if (getValue() != null) {
            _hashCode += getValue().hashCode();
        }
        if (getQuality() != null) {
            _hashCode += getQuality().hashCode();
        }
        if (getItemPath() != null) {
            _hashCode += getItemPath().hashCode();
        }
        if (getItemName() != null) {
            _hashCode += getItemName().hashCode();
        }
        if (getClientItemHandle() != null) {
            _hashCode += getClientItemHandle().hashCode();
        }
        if (getTimestamp() != null) {
            _hashCode += getTimestamp().hashCode();
        }
        if (getResultID() != null) {
            _hashCode += getResultID().hashCode();
        }
        if (getValueTypeQualifier() != null) {
            _hashCode += getValueTypeQualifier().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ItemValue.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "ItemValue"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("itemPath");
        attrField.setXmlName(new javax.xml.namespace.QName("", "ItemPath"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("itemName");
        attrField.setXmlName(new javax.xml.namespace.QName("", "ItemName"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("clientItemHandle");
        attrField.setXmlName(new javax.xml.namespace.QName("", "ClientItemHandle"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("timestamp");
        attrField.setXmlName(new javax.xml.namespace.QName("", "Timestamp"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("resultID");
        attrField.setXmlName(new javax.xml.namespace.QName("", "ResultID"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "QName"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("valueTypeQualifier");
        attrField.setXmlName(new javax.xml.namespace.QName("", "ValueTypeQualifier"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "QName"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("diagnosticInfo");
        elemField.setXmlName(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "DiagnosticInfo"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("value");
        elemField.setXmlName(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "Value"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("quality");
        elemField.setXmlName(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "Quality"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "OPCQuality"));
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

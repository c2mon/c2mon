/**
 * ServerStatus.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.opcfoundation.webservices.XMLDA._1_0;

public class ServerStatus  implements java.io.Serializable {
    private java.lang.String statusInfo;

    private java.lang.String vendorInfo;

    private java.lang.String[] supportedLocaleIDs;

    private org.opcfoundation.webservices.XMLDA._1_0.InterfaceVersion[] supportedInterfaceVersions;

    private java.util.Calendar startTime;  // attribute

    private java.lang.String productVersion;  // attribute

    public ServerStatus() {
    }

    public ServerStatus(
           java.lang.String statusInfo,
           java.lang.String vendorInfo,
           java.lang.String[] supportedLocaleIDs,
           org.opcfoundation.webservices.XMLDA._1_0.InterfaceVersion[] supportedInterfaceVersions,
           java.util.Calendar startTime,
           java.lang.String productVersion) {
           this.statusInfo = statusInfo;
           this.vendorInfo = vendorInfo;
           this.supportedLocaleIDs = supportedLocaleIDs;
           this.supportedInterfaceVersions = supportedInterfaceVersions;
           this.startTime = startTime;
           this.productVersion = productVersion;
    }


    /**
     * Gets the statusInfo value for this ServerStatus.
     * 
     * @return statusInfo
     */
    public java.lang.String getStatusInfo() {
        return statusInfo;
    }


    /**
     * Sets the statusInfo value for this ServerStatus.
     * 
     * @param statusInfo
     */
    public void setStatusInfo(java.lang.String statusInfo) {
        this.statusInfo = statusInfo;
    }


    /**
     * Gets the vendorInfo value for this ServerStatus.
     * 
     * @return vendorInfo
     */
    public java.lang.String getVendorInfo() {
        return vendorInfo;
    }


    /**
     * Sets the vendorInfo value for this ServerStatus.
     * 
     * @param vendorInfo
     */
    public void setVendorInfo(java.lang.String vendorInfo) {
        this.vendorInfo = vendorInfo;
    }


    /**
     * Gets the supportedLocaleIDs value for this ServerStatus.
     * 
     * @return supportedLocaleIDs
     */
    public java.lang.String[] getSupportedLocaleIDs() {
        return supportedLocaleIDs;
    }


    /**
     * Sets the supportedLocaleIDs value for this ServerStatus.
     * 
     * @param supportedLocaleIDs
     */
    public void setSupportedLocaleIDs(java.lang.String[] supportedLocaleIDs) {
        this.supportedLocaleIDs = supportedLocaleIDs;
    }

    public java.lang.String getSupportedLocaleIDs(int i) {
        return this.supportedLocaleIDs[i];
    }

    public void setSupportedLocaleIDs(int i, java.lang.String _value) {
        this.supportedLocaleIDs[i] = _value;
    }


    /**
     * Gets the supportedInterfaceVersions value for this ServerStatus.
     * 
     * @return supportedInterfaceVersions
     */
    public org.opcfoundation.webservices.XMLDA._1_0.InterfaceVersion[] getSupportedInterfaceVersions() {
        return supportedInterfaceVersions;
    }


    /**
     * Sets the supportedInterfaceVersions value for this ServerStatus.
     * 
     * @param supportedInterfaceVersions
     */
    public void setSupportedInterfaceVersions(org.opcfoundation.webservices.XMLDA._1_0.InterfaceVersion[] supportedInterfaceVersions) {
        this.supportedInterfaceVersions = supportedInterfaceVersions;
    }

    public org.opcfoundation.webservices.XMLDA._1_0.InterfaceVersion getSupportedInterfaceVersions(int i) {
        return this.supportedInterfaceVersions[i];
    }

    public void setSupportedInterfaceVersions(int i, org.opcfoundation.webservices.XMLDA._1_0.InterfaceVersion _value) {
        this.supportedInterfaceVersions[i] = _value;
    }


    /**
     * Gets the startTime value for this ServerStatus.
     * 
     * @return startTime
     */
    public java.util.Calendar getStartTime() {
        return startTime;
    }


    /**
     * Sets the startTime value for this ServerStatus.
     * 
     * @param startTime
     */
    public void setStartTime(java.util.Calendar startTime) {
        this.startTime = startTime;
    }


    /**
     * Gets the productVersion value for this ServerStatus.
     * 
     * @return productVersion
     */
    public java.lang.String getProductVersion() {
        return productVersion;
    }


    /**
     * Sets the productVersion value for this ServerStatus.
     * 
     * @param productVersion
     */
    public void setProductVersion(java.lang.String productVersion) {
        this.productVersion = productVersion;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ServerStatus)) return false;
        ServerStatus other = (ServerStatus) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.statusInfo==null && other.getStatusInfo()==null) || 
             (this.statusInfo!=null &&
              this.statusInfo.equals(other.getStatusInfo()))) &&
            ((this.vendorInfo==null && other.getVendorInfo()==null) || 
             (this.vendorInfo!=null &&
              this.vendorInfo.equals(other.getVendorInfo()))) &&
            ((this.supportedLocaleIDs==null && other.getSupportedLocaleIDs()==null) || 
             (this.supportedLocaleIDs!=null &&
              java.util.Arrays.equals(this.supportedLocaleIDs, other.getSupportedLocaleIDs()))) &&
            ((this.supportedInterfaceVersions==null && other.getSupportedInterfaceVersions()==null) || 
             (this.supportedInterfaceVersions!=null &&
              java.util.Arrays.equals(this.supportedInterfaceVersions, other.getSupportedInterfaceVersions()))) &&
            ((this.startTime==null && other.getStartTime()==null) || 
             (this.startTime!=null &&
              this.startTime.equals(other.getStartTime()))) &&
            ((this.productVersion==null && other.getProductVersion()==null) || 
             (this.productVersion!=null &&
              this.productVersion.equals(other.getProductVersion())));
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
        if (getStatusInfo() != null) {
            _hashCode += getStatusInfo().hashCode();
        }
        if (getVendorInfo() != null) {
            _hashCode += getVendorInfo().hashCode();
        }
        if (getSupportedLocaleIDs() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getSupportedLocaleIDs());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getSupportedLocaleIDs(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getSupportedInterfaceVersions() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getSupportedInterfaceVersions());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getSupportedInterfaceVersions(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getStartTime() != null) {
            _hashCode += getStartTime().hashCode();
        }
        if (getProductVersion() != null) {
            _hashCode += getProductVersion().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ServerStatus.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "ServerStatus"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("startTime");
        attrField.setXmlName(new javax.xml.namespace.QName("", "StartTime"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("productVersion");
        attrField.setXmlName(new javax.xml.namespace.QName("", "ProductVersion"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("statusInfo");
        elemField.setXmlName(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "StatusInfo"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("vendorInfo");
        elemField.setXmlName(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "VendorInfo"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("supportedLocaleIDs");
        elemField.setXmlName(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "SupportedLocaleIDs"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("supportedInterfaceVersions");
        elemField.setXmlName(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "SupportedInterfaceVersions"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "interfaceVersion"));
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

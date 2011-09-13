/**
 * RequestOptions.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.opcfoundation.webservices.XMLDA._1_0;

public class RequestOptions  implements java.io.Serializable {
    private java.util.Calendar requestDeadline;  // attribute

    private boolean returnErrorText;  // attribute

    private boolean returnDiagnosticInfo;  // attribute

    private boolean returnItemTime;  // attribute

    private boolean returnItemPath;  // attribute

    private boolean returnItemName;  // attribute

    private java.lang.String clientRequestHandle;  // attribute

    private java.lang.String localeID;  // attribute

    public RequestOptions() {
    }

    public RequestOptions(
           java.util.Calendar requestDeadline,
           boolean returnErrorText,
           boolean returnDiagnosticInfo,
           boolean returnItemTime,
           boolean returnItemPath,
           boolean returnItemName,
           java.lang.String clientRequestHandle,
           java.lang.String localeID) {
           this.requestDeadline = requestDeadline;
           this.returnErrorText = returnErrorText;
           this.returnDiagnosticInfo = returnDiagnosticInfo;
           this.returnItemTime = returnItemTime;
           this.returnItemPath = returnItemPath;
           this.returnItemName = returnItemName;
           this.clientRequestHandle = clientRequestHandle;
           this.localeID = localeID;
    }


    /**
     * Gets the requestDeadline value for this RequestOptions.
     * 
     * @return requestDeadline
     */
    public java.util.Calendar getRequestDeadline() {
        return requestDeadline;
    }


    /**
     * Sets the requestDeadline value for this RequestOptions.
     * 
     * @param requestDeadline
     */
    public void setRequestDeadline(java.util.Calendar requestDeadline) {
        this.requestDeadline = requestDeadline;
    }


    /**
     * Gets the returnErrorText value for this RequestOptions.
     * 
     * @return returnErrorText
     */
    public boolean isReturnErrorText() {
        return returnErrorText;
    }


    /**
     * Sets the returnErrorText value for this RequestOptions.
     * 
     * @param returnErrorText
     */
    public void setReturnErrorText(boolean returnErrorText) {
        this.returnErrorText = returnErrorText;
    }


    /**
     * Gets the returnDiagnosticInfo value for this RequestOptions.
     * 
     * @return returnDiagnosticInfo
     */
    public boolean isReturnDiagnosticInfo() {
        return returnDiagnosticInfo;
    }


    /**
     * Sets the returnDiagnosticInfo value for this RequestOptions.
     * 
     * @param returnDiagnosticInfo
     */
    public void setReturnDiagnosticInfo(boolean returnDiagnosticInfo) {
        this.returnDiagnosticInfo = returnDiagnosticInfo;
    }


    /**
     * Gets the returnItemTime value for this RequestOptions.
     * 
     * @return returnItemTime
     */
    public boolean isReturnItemTime() {
        return returnItemTime;
    }


    /**
     * Sets the returnItemTime value for this RequestOptions.
     * 
     * @param returnItemTime
     */
    public void setReturnItemTime(boolean returnItemTime) {
        this.returnItemTime = returnItemTime;
    }


    /**
     * Gets the returnItemPath value for this RequestOptions.
     * 
     * @return returnItemPath
     */
    public boolean isReturnItemPath() {
        return returnItemPath;
    }


    /**
     * Sets the returnItemPath value for this RequestOptions.
     * 
     * @param returnItemPath
     */
    public void setReturnItemPath(boolean returnItemPath) {
        this.returnItemPath = returnItemPath;
    }


    /**
     * Gets the returnItemName value for this RequestOptions.
     * 
     * @return returnItemName
     */
    public boolean isReturnItemName() {
        return returnItemName;
    }


    /**
     * Sets the returnItemName value for this RequestOptions.
     * 
     * @param returnItemName
     */
    public void setReturnItemName(boolean returnItemName) {
        this.returnItemName = returnItemName;
    }


    /**
     * Gets the clientRequestHandle value for this RequestOptions.
     * 
     * @return clientRequestHandle
     */
    public java.lang.String getClientRequestHandle() {
        return clientRequestHandle;
    }


    /**
     * Sets the clientRequestHandle value for this RequestOptions.
     * 
     * @param clientRequestHandle
     */
    public void setClientRequestHandle(java.lang.String clientRequestHandle) {
        this.clientRequestHandle = clientRequestHandle;
    }


    /**
     * Gets the localeID value for this RequestOptions.
     * 
     * @return localeID
     */
    public java.lang.String getLocaleID() {
        return localeID;
    }


    /**
     * Sets the localeID value for this RequestOptions.
     * 
     * @param localeID
     */
    public void setLocaleID(java.lang.String localeID) {
        this.localeID = localeID;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RequestOptions)) return false;
        RequestOptions other = (RequestOptions) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.requestDeadline==null && other.getRequestDeadline()==null) || 
             (this.requestDeadline!=null &&
              this.requestDeadline.equals(other.getRequestDeadline()))) &&
            this.returnErrorText == other.isReturnErrorText() &&
            this.returnDiagnosticInfo == other.isReturnDiagnosticInfo() &&
            this.returnItemTime == other.isReturnItemTime() &&
            this.returnItemPath == other.isReturnItemPath() &&
            this.returnItemName == other.isReturnItemName() &&
            ((this.clientRequestHandle==null && other.getClientRequestHandle()==null) || 
             (this.clientRequestHandle!=null &&
              this.clientRequestHandle.equals(other.getClientRequestHandle()))) &&
            ((this.localeID==null && other.getLocaleID()==null) || 
             (this.localeID!=null &&
              this.localeID.equals(other.getLocaleID())));
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
        if (getRequestDeadline() != null) {
            _hashCode += getRequestDeadline().hashCode();
        }
        _hashCode += (isReturnErrorText() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        _hashCode += (isReturnDiagnosticInfo() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        _hashCode += (isReturnItemTime() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        _hashCode += (isReturnItemPath() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        _hashCode += (isReturnItemName() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getClientRequestHandle() != null) {
            _hashCode += getClientRequestHandle().hashCode();
        }
        if (getLocaleID() != null) {
            _hashCode += getLocaleID().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(RequestOptions.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "RequestOptions"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("requestDeadline");
        attrField.setXmlName(new javax.xml.namespace.QName("", "RequestDeadline"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("returnErrorText");
        attrField.setXmlName(new javax.xml.namespace.QName("", "ReturnErrorText"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("returnDiagnosticInfo");
        attrField.setXmlName(new javax.xml.namespace.QName("", "ReturnDiagnosticInfo"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("returnItemTime");
        attrField.setXmlName(new javax.xml.namespace.QName("", "ReturnItemTime"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("returnItemPath");
        attrField.setXmlName(new javax.xml.namespace.QName("", "ReturnItemPath"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("returnItemName");
        attrField.setXmlName(new javax.xml.namespace.QName("", "ReturnItemName"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("clientRequestHandle");
        attrField.setXmlName(new javax.xml.namespace.QName("", "ClientRequestHandle"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("localeID");
        attrField.setXmlName(new javax.xml.namespace.QName("", "LocaleID"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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

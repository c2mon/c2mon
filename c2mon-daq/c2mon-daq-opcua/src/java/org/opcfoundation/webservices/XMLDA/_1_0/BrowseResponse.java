/**
 * BrowseResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.opcfoundation.webservices.XMLDA._1_0;

public class BrowseResponse  implements java.io.Serializable {
    private org.opcfoundation.webservices.XMLDA._1_0.ReplyBase browseResult;

    private org.opcfoundation.webservices.XMLDA._1_0.ItemProperty[][] elements;

    private org.opcfoundation.webservices.XMLDA._1_0.OPCError[] errors;

    private java.lang.String continuationPoint;  // attribute

    private boolean moreElements;  // attribute

    public BrowseResponse() {
    }

    public BrowseResponse(
           org.opcfoundation.webservices.XMLDA._1_0.ReplyBase browseResult,
           org.opcfoundation.webservices.XMLDA._1_0.ItemProperty[][] elements,
           org.opcfoundation.webservices.XMLDA._1_0.OPCError[] errors,
           java.lang.String continuationPoint,
           boolean moreElements) {
           this.browseResult = browseResult;
           this.elements = elements;
           this.errors = errors;
           this.continuationPoint = continuationPoint;
           this.moreElements = moreElements;
    }


    /**
     * Gets the browseResult value for this BrowseResponse.
     * 
     * @return browseResult
     */
    public org.opcfoundation.webservices.XMLDA._1_0.ReplyBase getBrowseResult() {
        return browseResult;
    }


    /**
     * Sets the browseResult value for this BrowseResponse.
     * 
     * @param browseResult
     */
    public void setBrowseResult(org.opcfoundation.webservices.XMLDA._1_0.ReplyBase browseResult) {
        this.browseResult = browseResult;
    }


    /**
     * Gets the elements value for this BrowseResponse.
     * 
     * @return elements
     */
    public org.opcfoundation.webservices.XMLDA._1_0.ItemProperty[][] getElements() {
        return elements;
    }


    /**
     * Sets the elements value for this BrowseResponse.
     * 
     * @param elements
     */
    public void setElements(org.opcfoundation.webservices.XMLDA._1_0.ItemProperty[][] elements) {
        this.elements = elements;
    }

    public org.opcfoundation.webservices.XMLDA._1_0.ItemProperty[] getElements(int i) {
        return this.elements[i];
    }

    public void setElements(int i, org.opcfoundation.webservices.XMLDA._1_0.ItemProperty[] _value) {
        this.elements[i] = _value;
    }


    /**
     * Gets the errors value for this BrowseResponse.
     * 
     * @return errors
     */
    public org.opcfoundation.webservices.XMLDA._1_0.OPCError[] getErrors() {
        return errors;
    }


    /**
     * Sets the errors value for this BrowseResponse.
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


    /**
     * Gets the continuationPoint value for this BrowseResponse.
     * 
     * @return continuationPoint
     */
    public java.lang.String getContinuationPoint() {
        return continuationPoint;
    }


    /**
     * Sets the continuationPoint value for this BrowseResponse.
     * 
     * @param continuationPoint
     */
    public void setContinuationPoint(java.lang.String continuationPoint) {
        this.continuationPoint = continuationPoint;
    }


    /**
     * Gets the moreElements value for this BrowseResponse.
     * 
     * @return moreElements
     */
    public boolean isMoreElements() {
        return moreElements;
    }


    /**
     * Sets the moreElements value for this BrowseResponse.
     * 
     * @param moreElements
     */
    public void setMoreElements(boolean moreElements) {
        this.moreElements = moreElements;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof BrowseResponse)) return false;
        BrowseResponse other = (BrowseResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.browseResult==null && other.getBrowseResult()==null) || 
             (this.browseResult!=null &&
              this.browseResult.equals(other.getBrowseResult()))) &&
            ((this.elements==null && other.getElements()==null) || 
             (this.elements!=null &&
              java.util.Arrays.equals(this.elements, other.getElements()))) &&
            ((this.errors==null && other.getErrors()==null) || 
             (this.errors!=null &&
              java.util.Arrays.equals(this.errors, other.getErrors()))) &&
            ((this.continuationPoint==null && other.getContinuationPoint()==null) || 
             (this.continuationPoint!=null &&
              this.continuationPoint.equals(other.getContinuationPoint()))) &&
            this.moreElements == other.isMoreElements();
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
        if (getBrowseResult() != null) {
            _hashCode += getBrowseResult().hashCode();
        }
        if (getElements() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getElements());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getElements(), i);
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
        if (getContinuationPoint() != null) {
            _hashCode += getContinuationPoint().hashCode();
        }
        _hashCode += (isMoreElements() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(BrowseResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", ">BrowseResponse"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("continuationPoint");
        attrField.setXmlName(new javax.xml.namespace.QName("", "ContinuationPoint"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("moreElements");
        attrField.setXmlName(new javax.xml.namespace.QName("", "MoreElements"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("browseResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "BrowseResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "ReplyBase"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("elements");
        elemField.setXmlName(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "Elements"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "BrowseElement"));
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

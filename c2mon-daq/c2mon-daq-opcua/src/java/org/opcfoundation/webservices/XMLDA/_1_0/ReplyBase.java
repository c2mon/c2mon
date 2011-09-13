/**
 * ReplyBase.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.opcfoundation.webservices.XMLDA._1_0;

public class ReplyBase  implements java.io.Serializable {
    private java.util.Calendar rcvTime;  // attribute

    private java.util.Calendar replyTime;  // attribute

    private java.lang.String clientRequestHandle;  // attribute

    private java.lang.String revisedLocaleID;  // attribute

    private org.opcfoundation.webservices.XMLDA._1_0.ServerState serverState;  // attribute

    public ReplyBase() {
    }

    public ReplyBase(
           java.util.Calendar rcvTime,
           java.util.Calendar replyTime,
           java.lang.String clientRequestHandle,
           java.lang.String revisedLocaleID,
           org.opcfoundation.webservices.XMLDA._1_0.ServerState serverState) {
           this.rcvTime = rcvTime;
           this.replyTime = replyTime;
           this.clientRequestHandle = clientRequestHandle;
           this.revisedLocaleID = revisedLocaleID;
           this.serverState = serverState;
    }


    /**
     * Gets the rcvTime value for this ReplyBase.
     * 
     * @return rcvTime
     */
    public java.util.Calendar getRcvTime() {
        return rcvTime;
    }


    /**
     * Sets the rcvTime value for this ReplyBase.
     * 
     * @param rcvTime
     */
    public void setRcvTime(java.util.Calendar rcvTime) {
        this.rcvTime = rcvTime;
    }


    /**
     * Gets the replyTime value for this ReplyBase.
     * 
     * @return replyTime
     */
    public java.util.Calendar getReplyTime() {
        return replyTime;
    }


    /**
     * Sets the replyTime value for this ReplyBase.
     * 
     * @param replyTime
     */
    public void setReplyTime(java.util.Calendar replyTime) {
        this.replyTime = replyTime;
    }


    /**
     * Gets the clientRequestHandle value for this ReplyBase.
     * 
     * @return clientRequestHandle
     */
    public java.lang.String getClientRequestHandle() {
        return clientRequestHandle;
    }


    /**
     * Sets the clientRequestHandle value for this ReplyBase.
     * 
     * @param clientRequestHandle
     */
    public void setClientRequestHandle(java.lang.String clientRequestHandle) {
        this.clientRequestHandle = clientRequestHandle;
    }


    /**
     * Gets the revisedLocaleID value for this ReplyBase.
     * 
     * @return revisedLocaleID
     */
    public java.lang.String getRevisedLocaleID() {
        return revisedLocaleID;
    }


    /**
     * Sets the revisedLocaleID value for this ReplyBase.
     * 
     * @param revisedLocaleID
     */
    public void setRevisedLocaleID(java.lang.String revisedLocaleID) {
        this.revisedLocaleID = revisedLocaleID;
    }


    /**
     * Gets the serverState value for this ReplyBase.
     * 
     * @return serverState
     */
    public org.opcfoundation.webservices.XMLDA._1_0.ServerState getServerState() {
        return serverState;
    }


    /**
     * Sets the serverState value for this ReplyBase.
     * 
     * @param serverState
     */
    public void setServerState(org.opcfoundation.webservices.XMLDA._1_0.ServerState serverState) {
        this.serverState = serverState;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ReplyBase)) return false;
        ReplyBase other = (ReplyBase) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.rcvTime==null && other.getRcvTime()==null) || 
             (this.rcvTime!=null &&
              this.rcvTime.equals(other.getRcvTime()))) &&
            ((this.replyTime==null && other.getReplyTime()==null) || 
             (this.replyTime!=null &&
              this.replyTime.equals(other.getReplyTime()))) &&
            ((this.clientRequestHandle==null && other.getClientRequestHandle()==null) || 
             (this.clientRequestHandle!=null &&
              this.clientRequestHandle.equals(other.getClientRequestHandle()))) &&
            ((this.revisedLocaleID==null && other.getRevisedLocaleID()==null) || 
             (this.revisedLocaleID!=null &&
              this.revisedLocaleID.equals(other.getRevisedLocaleID()))) &&
            ((this.serverState==null && other.getServerState()==null) || 
             (this.serverState!=null &&
              this.serverState.equals(other.getServerState())));
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
        if (getRcvTime() != null) {
            _hashCode += getRcvTime().hashCode();
        }
        if (getReplyTime() != null) {
            _hashCode += getReplyTime().hashCode();
        }
        if (getClientRequestHandle() != null) {
            _hashCode += getClientRequestHandle().hashCode();
        }
        if (getRevisedLocaleID() != null) {
            _hashCode += getRevisedLocaleID().hashCode();
        }
        if (getServerState() != null) {
            _hashCode += getServerState().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ReplyBase.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "ReplyBase"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("rcvTime");
        attrField.setXmlName(new javax.xml.namespace.QName("", "RcvTime"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("replyTime");
        attrField.setXmlName(new javax.xml.namespace.QName("", "ReplyTime"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("clientRequestHandle");
        attrField.setXmlName(new javax.xml.namespace.QName("", "ClientRequestHandle"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("revisedLocaleID");
        attrField.setXmlName(new javax.xml.namespace.QName("", "RevisedLocaleID"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("serverState");
        attrField.setXmlName(new javax.xml.namespace.QName("", "ServerState"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "serverState"));
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

/**
 * QualityBits.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.opcfoundation.webservices.XMLDA._1_0;

public class QualityBits implements java.io.Serializable {
    private java.lang.String _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected QualityBits(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final java.lang.String _bad = "bad";
    public static final java.lang.String _badConfigurationError = "badConfigurationError";
    public static final java.lang.String _badNotConnected = "badNotConnected";
    public static final java.lang.String _badDeviceFailure = "badDeviceFailure";
    public static final java.lang.String _badSensorFailure = "badSensorFailure";
    public static final java.lang.String _badLastKnownValue = "badLastKnownValue";
    public static final java.lang.String _badCommFailure = "badCommFailure";
    public static final java.lang.String _badOutOfService = "badOutOfService";
    public static final java.lang.String _badWaitingForInitialData = "badWaitingForInitialData";
    public static final java.lang.String _uncertain = "uncertain";
    public static final java.lang.String _uncertainLastUsableValue = "uncertainLastUsableValue";
    public static final java.lang.String _uncertainSensorNotAccurate = "uncertainSensorNotAccurate";
    public static final java.lang.String _uncertainEUExceeded = "uncertainEUExceeded";
    public static final java.lang.String _uncertainSubNormal = "uncertainSubNormal";
    public static final java.lang.String _good = "good";
    public static final java.lang.String _goodLocalOverride = "goodLocalOverride";
    public static final QualityBits bad = new QualityBits(_bad);
    public static final QualityBits badConfigurationError = new QualityBits(_badConfigurationError);
    public static final QualityBits badNotConnected = new QualityBits(_badNotConnected);
    public static final QualityBits badDeviceFailure = new QualityBits(_badDeviceFailure);
    public static final QualityBits badSensorFailure = new QualityBits(_badSensorFailure);
    public static final QualityBits badLastKnownValue = new QualityBits(_badLastKnownValue);
    public static final QualityBits badCommFailure = new QualityBits(_badCommFailure);
    public static final QualityBits badOutOfService = new QualityBits(_badOutOfService);
    public static final QualityBits badWaitingForInitialData = new QualityBits(_badWaitingForInitialData);
    public static final QualityBits uncertain = new QualityBits(_uncertain);
    public static final QualityBits uncertainLastUsableValue = new QualityBits(_uncertainLastUsableValue);
    public static final QualityBits uncertainSensorNotAccurate = new QualityBits(_uncertainSensorNotAccurate);
    public static final QualityBits uncertainEUExceeded = new QualityBits(_uncertainEUExceeded);
    public static final QualityBits uncertainSubNormal = new QualityBits(_uncertainSubNormal);
    public static final QualityBits good = new QualityBits(_good);
    public static final QualityBits goodLocalOverride = new QualityBits(_goodLocalOverride);
    public java.lang.String getValue() { return _value_;}
    public static QualityBits fromValue(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        QualityBits enumeration = (QualityBits)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    public static QualityBits fromString(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        return fromValue(value);
    }
    public boolean equals(java.lang.Object obj) {return (obj == this);}
    public int hashCode() { return toString().hashCode();}
    public java.lang.String toString() { return _value_;}
    public java.lang.Object readResolve() throws java.io.ObjectStreamException { return fromValue(_value_);}
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new org.apache.axis.encoding.ser.EnumSerializer(
            _javaType, _xmlType);
    }
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new org.apache.axis.encoding.ser.EnumDeserializer(
            _javaType, _xmlType);
    }
    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(QualityBits.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "qualityBits"));
    }
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}

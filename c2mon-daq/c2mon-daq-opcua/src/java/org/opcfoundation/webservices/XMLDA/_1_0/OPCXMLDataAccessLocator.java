/**
 * OPCXML_DataAccessLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.opcfoundation.webservices.XMLDA._1_0;

public class OPCXMLDataAccessLocator extends org.apache.axis.client.Service implements org.opcfoundation.webservices.XMLDA._1_0.OPCXMLDataAccess {

    public OPCXMLDataAccessLocator() {
    }


    public OPCXMLDataAccessLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public OPCXMLDataAccessLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for OPCXML_DataAccessSoap
    private java.lang.String OPCXML_DataAccessSoap_address = "http://vcvrmtdev01/opc.simatic.net/sopcweb.asmx";

    public java.lang.String getOPCXML_DataAccessSoapAddress() {
        return OPCXML_DataAccessSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String OPCXML_DataAccessSoapWSDDServiceName = "OPCXML_DataAccessSoap";

    public java.lang.String getOPCXML_DataAccessSoapWSDDServiceName() {
        return OPCXML_DataAccessSoapWSDDServiceName;
    }

    public void setOPCXML_DataAccessSoapWSDDServiceName(java.lang.String name) {
        OPCXML_DataAccessSoapWSDDServiceName = name;
    }

    public org.opcfoundation.webservices.XMLDA._1_0.OPCXMLDataAccessSoap getOPCXML_DataAccessSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(OPCXML_DataAccessSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getOPCXML_DataAccessSoap(endpoint);
    }

    public org.opcfoundation.webservices.XMLDA._1_0.OPCXMLDataAccessSoap getOPCXML_DataAccessSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            org.opcfoundation.webservices.XMLDA._1_0.OPCXMLDataAccessSoapStub _stub = new org.opcfoundation.webservices.XMLDA._1_0.OPCXMLDataAccessSoapStub(portAddress, this);
            _stub.setPortName(getOPCXML_DataAccessSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setOPCXML_DataAccessSoapEndpointAddress(java.lang.String address) {
        OPCXML_DataAccessSoap_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (org.opcfoundation.webservices.XMLDA._1_0.OPCXMLDataAccessSoap.class.isAssignableFrom(serviceEndpointInterface)) {
                org.opcfoundation.webservices.XMLDA._1_0.OPCXMLDataAccessSoapStub _stub = new org.opcfoundation.webservices.XMLDA._1_0.OPCXMLDataAccessSoapStub(new java.net.URL(OPCXML_DataAccessSoap_address), this);
                _stub.setPortName(getOPCXML_DataAccessSoapWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("OPCXML_DataAccessSoap".equals(inputPortName)) {
            return getOPCXML_DataAccessSoap();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "OPCXML_DataAccess");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://opcfoundation.org/webservices/XMLDA/1.0/", "OPCXML_DataAccessSoap"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("OPCXML_DataAccessSoap".equals(portName)) {
            setOPCXML_DataAccessSoapEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}

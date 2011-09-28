package org.opcfoundation.webservices.XMLDA._1_0;

public class OPCXMLDataAccessSoapProxy implements org.opcfoundation.webservices.XMLDA._1_0.OPCXMLDataAccessSoap {
  private String _endpoint = null;
  private org.opcfoundation.webservices.XMLDA._1_0.OPCXMLDataAccessSoap oPCXMLDataAccessSoap = null;
  
  public OPCXMLDataAccessSoapProxy() {
    _initOPCXML_DataAccessSoapProxy();
  }
  
  public OPCXMLDataAccessSoapProxy(String endpoint) {
    _endpoint = endpoint;
    _initOPCXML_DataAccessSoapProxy();
  }
  
  private void _initOPCXML_DataAccessSoapProxy() {
    try {
      oPCXMLDataAccessSoap = (new org.opcfoundation.webservices.XMLDA._1_0.OPCXMLDataAccessLocator()).getOPCXML_DataAccessSoap();
      if (oPCXMLDataAccessSoap != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)oPCXMLDataAccessSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)oPCXMLDataAccessSoap)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (oPCXMLDataAccessSoap != null)
      ((javax.xml.rpc.Stub)oPCXMLDataAccessSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public org.opcfoundation.webservices.XMLDA._1_0.OPCXMLDataAccessSoap getOPCXML_DataAccessSoap() {
    if (oPCXMLDataAccessSoap == null)
      _initOPCXML_DataAccessSoapProxy();
    return oPCXMLDataAccessSoap;
  }
  
  public org.opcfoundation.webservices.XMLDA._1_0.GetStatusResponse getStatus(org.opcfoundation.webservices.XMLDA._1_0.GetStatus parameters) throws java.rmi.RemoteException{
    if (oPCXMLDataAccessSoap == null)
      _initOPCXML_DataAccessSoapProxy();
    return oPCXMLDataAccessSoap.getStatus(parameters);
  }
  
  public ReadResponse read(org.opcfoundation.webservices.XMLDA._1_0.Read parameters) throws java.rmi.RemoteException{
    if (oPCXMLDataAccessSoap == null)
      _initOPCXML_DataAccessSoapProxy();
    return oPCXMLDataAccessSoap.read(parameters);
  }
  
  public org.opcfoundation.webservices.XMLDA._1_0.WriteResponse write(org.opcfoundation.webservices.XMLDA._1_0.Write parameters) throws java.rmi.RemoteException{
    if (oPCXMLDataAccessSoap == null)
      _initOPCXML_DataAccessSoapProxy();
    return oPCXMLDataAccessSoap.write(parameters);
  }
  
  public org.opcfoundation.webservices.XMLDA._1_0.SubscribeResponse subscribe(org.opcfoundation.webservices.XMLDA._1_0.Subscribe parameters) throws java.rmi.RemoteException{
    if (oPCXMLDataAccessSoap == null)
      _initOPCXML_DataAccessSoapProxy();
    return oPCXMLDataAccessSoap.subscribe(parameters);
  }
  
  public org.opcfoundation.webservices.XMLDA._1_0.SubscriptionPolledRefreshResponse subscriptionPolledRefresh(org.opcfoundation.webservices.XMLDA._1_0.SubscriptionPolledRefresh parameters) throws java.rmi.RemoteException{
    if (oPCXMLDataAccessSoap == null)
      _initOPCXML_DataAccessSoapProxy();
    return oPCXMLDataAccessSoap.subscriptionPolledRefresh(parameters);
  }
  
  public org.opcfoundation.webservices.XMLDA._1_0.BrowseResponse browse(javax.xml.namespace.QName[] parameters) throws java.rmi.RemoteException{
    if (oPCXMLDataAccessSoap == null)
      _initOPCXML_DataAccessSoapProxy();
    return oPCXMLDataAccessSoap.browse(parameters);
  }
  
  public org.opcfoundation.webservices.XMLDA._1_0.GetPropertiesResponse getProperties(org.opcfoundation.webservices.XMLDA._1_0.GetProperties parameters) throws java.rmi.RemoteException{
    if (oPCXMLDataAccessSoap == null)
      _initOPCXML_DataAccessSoapProxy();
    return oPCXMLDataAccessSoap.getProperties(parameters);
  }
  
  public org.opcfoundation.webservices.XMLDA._1_0.SubscriptionCancelResponse subscriptionCancel(org.opcfoundation.webservices.XMLDA._1_0.SubscriptionCancel parameters) throws java.rmi.RemoteException{
    if (oPCXMLDataAccessSoap == null)
      _initOPCXML_DataAccessSoapProxy();
    return oPCXMLDataAccessSoap.subscriptionCancel(parameters);
  }
  
  
}
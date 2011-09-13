/**
 * OPCXML_DataAccessSoap.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.opcfoundation.webservices.XMLDA._1_0;

public interface OPCXMLDataAccessSoap extends java.rmi.Remote {
    public org.opcfoundation.webservices.XMLDA._1_0.GetStatusResponse getStatus(org.opcfoundation.webservices.XMLDA._1_0.GetStatus parameters) throws java.rmi.RemoteException;
    public void read(org.opcfoundation.webservices.XMLDA._1_0.RequestOptions options, org.opcfoundation.webservices.XMLDA._1_0.ReadRequestItem[] itemList, org.opcfoundation.webservices.XMLDA._1_0.holders.ReplyBaseHolder readResult, org.opcfoundation.webservices.XMLDA._1_0.holders.ReplyItemListHolder RItemList, org.opcfoundation.webservices.XMLDA._1_0.holders.OPCErrorArrayHolder errors) throws java.rmi.RemoteException;
    public org.opcfoundation.webservices.XMLDA._1_0.WriteResponse write(org.opcfoundation.webservices.XMLDA._1_0.Write parameters) throws java.rmi.RemoteException;
    public org.opcfoundation.webservices.XMLDA._1_0.SubscribeResponse subscribe(org.opcfoundation.webservices.XMLDA._1_0.Subscribe parameters) throws java.rmi.RemoteException;
    public org.opcfoundation.webservices.XMLDA._1_0.SubscriptionPolledRefreshResponse subscriptionPolledRefresh(org.opcfoundation.webservices.XMLDA._1_0.SubscriptionPolledRefresh parameters) throws java.rmi.RemoteException;
    public org.opcfoundation.webservices.XMLDA._1_0.BrowseResponse browse(javax.xml.namespace.QName[] parameters) throws java.rmi.RemoteException;
    public org.opcfoundation.webservices.XMLDA._1_0.GetPropertiesResponse getProperties(org.opcfoundation.webservices.XMLDA._1_0.GetProperties parameters) throws java.rmi.RemoteException;
    public org.opcfoundation.webservices.XMLDA._1_0.SubscriptionCancelResponse subscriptionCancel(org.opcfoundation.webservices.XMLDA._1_0.SubscriptionCancel parameters) throws java.rmi.RemoteException;
}

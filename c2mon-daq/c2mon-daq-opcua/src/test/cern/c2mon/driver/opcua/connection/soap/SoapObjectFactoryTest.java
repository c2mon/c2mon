package cern.c2mon.driver.opcua.connection.soap;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.rpc.Call;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Stub;

import org.junit.Test;
import org.opcfoundation.webservices.XMLDA._1_0.ItemValue;
import org.opcfoundation.webservices.XMLDA._1_0.OPCXMLDataAccessSoap;
import org.opcfoundation.webservices.XMLDA._1_0.ReadRequestItem;
import org.opcfoundation.webservices.XMLDA._1_0.Subscribe;
import org.opcfoundation.webservices.XMLDA._1_0.SubscribeRequestItem;
import org.opcfoundation.webservices.XMLDA._1_0.SubscriptionPolledRefresh;
import org.opcfoundation.webservices.XMLDA._1_0.Write;

import cern.c2mon.driver.opcua.connection.soap.SoapObjectFactory;

public class SoapObjectFactoryTest {

    @Test
    public void testCreateSubscribe() {
        String requestHandle = "asd";
        int subscripionPingRate = 1000;
        List<SubscribeRequestItem> subscribeRequestItems =
            new ArrayList<SubscribeRequestItem>();
        subscribeRequestItems.add(new SubscribeRequestItem());
        subscribeRequestItems.add(new SubscribeRequestItem());
        subscribeRequestItems.add(new SubscribeRequestItem());
        Subscribe subscribe = SoapObjectFactory.createSubscribe(
                requestHandle, subscribeRequestItems, subscripionPingRate);
        assertEquals(
                requestHandle, subscribe.getOptions().getClientRequestHandle());
        assertEquals(subscripionPingRate, subscribe.getSubscriptionPingRate());
        assertEquals(3, subscribe.getItemList().length);
    }
    
    @Test
    public void testCreateSubscribeRequestItem() {
        String clientItemHandle = "asd";
        String address = "asd2";
        float valueDeadband = 32984.0f;
        int timeDeadband = 100;
        boolean bufferEnabled = false;
        SubscribeRequestItem item = 
            SoapObjectFactory.createSubscribeRequestItem(
                    clientItemHandle, address, valueDeadband, timeDeadband,
                    bufferEnabled);
        assertEquals(clientItemHandle, item.getClientItemHandle());
        assertEquals(address, item.getItemName());
        assertEquals(
                Double.valueOf(valueDeadband), 
                Double.valueOf(item.getDeadband()));
        assertEquals(timeDeadband, item.getRequestedSamplingRate());
        assertEquals(bufferEnabled, item.isEnableBuffering());
    }
    
    @Test
    public void testCreateSubscriptionPolledRefresh() {
        String serverSubscriptionHandle = "asd";
        int waitTime = 234;
        SubscriptionPolledRefresh refresh = 
            SoapObjectFactory.createSubscriptionPolledRefresh(
                    serverSubscriptionHandle, waitTime);
        assertEquals(serverSubscriptionHandle, refresh.getServerSubHandles(0));
        assertEquals(waitTime, refresh.getWaitTime());
        assertNotNull(refresh.getHoldTime());
    }
    
    @Test
    public void testCreateOPCDataAccessSoapWithDomain() 
            throws MalformedURLException, ServiceException {
        URL serverURL = new URL("http://somehost/somepath");
        String domain = "asd";
        String user = "user";
        String password = "password";
        OPCXMLDataAccessSoap access = 
            SoapObjectFactory.createOPCDataAccessSoapInterface(
                serverURL, domain, user, password);
        assertEquals(
                domain + "\\" + user,
                ((Stub) access)._getProperty(Stub.USERNAME_PROPERTY));
        assertEquals(
                password,
                ((Stub) access)._getProperty(Stub.PASSWORD_PROPERTY));
        assertEquals(
                serverURL.toString(),
                ((Stub) access)._getProperty(Stub.ENDPOINT_ADDRESS_PROPERTY));
    }
    
    @Test
    public void testCreateOPCDataAccessSoapWithoutDomain() 
            throws MalformedURLException, ServiceException {
        URL serverURL = new URL("http://somehost/somepath");
        String domain = null;
        String user = "user";
        String password = "password";
        OPCXMLDataAccessSoap access = 
            SoapObjectFactory.createOPCDataAccessSoapInterface(
                serverURL, domain, user, password);
        assertEquals(
                user,
                ((Stub) access)._getProperty(Stub.USERNAME_PROPERTY));
        assertEquals(
                password,
                ((Stub) access)._getProperty(Stub.PASSWORD_PROPERTY));
        assertEquals(
                serverURL.toString(),
                ((Stub) access)._getProperty(Stub.ENDPOINT_ADDRESS_PROPERTY));
    }
    
    @Test
    public void testCreateOPCDataAccessSoapWithoutCredentials() 
            throws MalformedURLException, ServiceException {
        URL serverURL = new URL("http://somehost/somepath");
        String domain = null;
        String user = null;
        String password = null;
        OPCXMLDataAccessSoap access = 
            SoapObjectFactory.createOPCDataAccessSoapInterface(
                serverURL, domain, user, password);
        assertNull(((Stub) access)._getProperty(Stub.USERNAME_PROPERTY));
        assertNull(((Stub) access)._getProperty(Stub.PASSWORD_PROPERTY));
        assertEquals(
                serverURL.toString(),
                ((Stub) access)._getProperty(Stub.ENDPOINT_ADDRESS_PROPERTY));
    }
    
    @Test
    public void testCreateRequestItem() {
        String clientItemHandle = "asd";
        String address = "address";
        ReadRequestItem item = 
            SoapObjectFactory.createReadRequestItem(clientItemHandle, address);
        assertEquals(clientItemHandle, item.getClientItemHandle());
        assertEquals(address, item.getItemName());
    }
    
    @Test
    public void testCreateWrite() {
        String clientItemHandle = "asd";
        ItemValue value = new ItemValue();
        ItemValue[] values = { value, value};
        Write write = 
            SoapObjectFactory.createWrite(clientItemHandle, values);
        assertEquals(
                clientItemHandle, write.getOptions().getClientRequestHandle());
        assertEquals(2, write.getItemList().length);
        assertEquals(value, write.getItemList()[0]);
        assertEquals(value, write.getItemList()[1]);
    }
    
    @Test
    public void testCreateItemValue() {
        String clientItemHandle = "asd";
        String itemName = "asdasdasd";
        Object value = 3424L;
        ItemValue itemValue =
            SoapObjectFactory.createItemValue(clientItemHandle, itemName, value);
        assertEquals(clientItemHandle, itemValue.getClientItemHandle());
        assertEquals(itemName, itemValue.getItemName());
        assertEquals(value, itemValue.getValue());
    }
}

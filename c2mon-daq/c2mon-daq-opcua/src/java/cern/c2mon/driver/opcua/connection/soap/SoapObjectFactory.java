package cern.c2mon.driver.opcua.connection.soap;

import java.net.URL;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.rpc.Call;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Stub;

import org.apache.axis.transport.http.HTTPConstants;
import org.opcfoundation.webservices.XMLDA._1_0.ItemValue;
import org.opcfoundation.webservices.XMLDA._1_0.OPCXMLDataAccessLocator;
import org.opcfoundation.webservices.XMLDA._1_0.OPCXMLDataAccessSoap;
import org.opcfoundation.webservices.XMLDA._1_0.ReadRequestItem;
import org.opcfoundation.webservices.XMLDA._1_0.RequestOptions;
import org.opcfoundation.webservices.XMLDA._1_0.Subscribe;
import org.opcfoundation.webservices.XMLDA._1_0.SubscribeRequestItem;
import org.opcfoundation.webservices.XMLDA._1_0.SubscriptionPolledRefresh;
import org.opcfoundation.webservices.XMLDA._1_0.Write;

/**
 * Factory to create various Soap object and hide the sometimes ugly creation.
 * 
 * @author Andreas Lang
 *
 */
public final class SoapObjectFactory {
    
    /**
     * Private constructor. There should be no instances of this helper class.
     */
    private SoapObjectFactory() { }
    
    /**
     * Creates a new OPCDataAccess object.
     * 
     * @param serverURL The URL to the web service.
     * @param domain The domain (only used if NT authetification).
     * @param user The user to use (also NT auth).
     * @param password The password to use (also NT auth).
     * @return The new data access object.
     * @throws ServiceException May throw a service exception.
     */
    public static OPCXMLDataAccessSoap createOPCDataAccessSoapInterface(
        final URL serverURL, final String domain, final String user, 
        final String password)
            throws ServiceException {
        OPCXMLDataAccessLocator locator = new OPCXMLDataAccessLocator();
        locator.setMaintainSession(true);
        locator.setCacheWSDL(true);
        OPCXMLDataAccessSoap access = locator.getOPCXML_DataAccessSoap(serverURL);
        if (user != null && password != null) {
            if (domain != null)
                ((Stub) access)._setProperty(
                        Call.USERNAME_PROPERTY, domain + "\\" + user);
            else
                ((Stub) access)._setProperty(
                        Call.USERNAME_PROPERTY, user);
            ((Stub) access)._setProperty(Call.PASSWORD_PROPERTY, password);
        }
        ((Stub) access)._setProperty(HTTPConstants.MC_ACCEPT_GZIP, Boolean.TRUE);
        return access;
    }
    
    /**
     * Creates a new SubscribeRequestItem. They are used for Subscribe requests.
     * 
     * @param clientItemHandle The client item handle is used to identify
     * updates after they were returned from the server.
     * @param address The address of the item (server dependent).
     * @param valueDeadband The relative value deadband.
     * @param timeDeadband The time deadband to use.
     * @param bufferEnabled If true updates of this item will be even returned
     * if there were other updates afterwards.
     * @return The created SubscribeRequestItem.
     */
    public static SubscribeRequestItem createSubscribeRequestItem(
            final String clientItemHandle, final String address, 
            final float valueDeadband, final int timeDeadband,
            final boolean bufferEnabled) {
        SubscribeRequestItem requestItem = new SubscribeRequestItem();
        requestItem.setClientItemHandle(clientItemHandle);
        requestItem.setDeadband(valueDeadband);
        requestItem.setEnableBuffering(bufferEnabled);
        requestItem.setItemName(address);
        requestItem.setRequestedSamplingRate(timeDeadband);
        return requestItem;
    }
    
    /**
     * Creates a new ReadRequestItem. They are used for Read requests.
     * 
     * @param clientItemHandle The client item handle is used to identify
     * updates after they were returned from the server.
     * @param address The address of the item (server dependent).
     * if there were other updates afterwards.
     * @return The created SubscribeRequestItem.
     */
    public static ReadRequestItem createReadRequestItem(
            final String clientItemHandle, final String address) {
        ReadRequestItem requestItem = new ReadRequestItem();
        requestItem.setClientItemHandle(clientItemHandle);
        requestItem.setItemName(address);
        return requestItem;
    }
    
    /**
     * Creates a new Subscribe object. It is used to send a Subscribe message to
     * the server.
     * 
     * @param requestHandle The request handle - not realy important.
     * @param subscribeRequestItems The items to subscribe to.
     * @param subscripionPingRate The rate the subscription will be pinged at
     * least.
     * @return The created Subscribe object.
     */
    public static Subscribe createSubscribe(final String requestHandle,
            final List<SubscribeRequestItem> subscribeRequestItems,
            final int subscripionPingRate) {
        Subscribe subscribe = new Subscribe(
                createDefaultRequestOptions(requestHandle), 
                subscribeRequestItems.toArray(
                        new SubscribeRequestItem[subscribeRequestItems.size()]),
                false, subscripionPingRate);
        return subscribe;
    }

    /**
     * Creates a default request options object.
     * 
     * @param requestHandle The handle which identifies the request.
     * @return The request options object.
     */
    public static RequestOptions 
    createDefaultRequestOptions(final String requestHandle) {
        return new RequestOptions(null, true, false, false, false, false, 
                requestHandle, "en");
    }
    
    /**
     * Creates a new subscription polled refresh with the default settings.
     * 
     * @param serverSubscriptionHandle The handle which identifies the
     * subscription.
     * @param waitTime The time after which the refresh will return even if
     * there where no updates for the items in this subscription.
     * @return The refresh with the correct settings.
     */
    public static SubscriptionPolledRefresh createSubscriptionPolledRefresh(
            final String serverSubscriptionHandle, final int waitTime) {
        SubscriptionPolledRefresh subscriptionPolledRefresh =
            new SubscriptionPolledRefresh();
        String[] serverSubHandles =
            new String[] { serverSubscriptionHandle };
        RequestOptions options = new RequestOptions(
                null, true, false, true, false, false, "doesnotmatter", "en");
        subscriptionPolledRefresh.setHoldTime(new GregorianCalendar());
        subscriptionPolledRefresh.setReturnAllItems(false);
        subscriptionPolledRefresh.setServerSubHandles(serverSubHandles);
        subscriptionPolledRefresh.setWaitTime(waitTime);
        subscriptionPolledRefresh.setOptions(options);
        return subscriptionPolledRefresh;
    }

    /**
     * Creates a write request.
     * 
     * @param requestHandle The handle of the request.
     * @param itemList The item list to put into the write request.
     * @return The new Write request.
     */
    public static Write createWrite(
            final String requestHandle, final ItemValue[] itemList) {
        Write write = new Write(createDefaultRequestOptions(requestHandle),
                itemList, false);
        return write;
    }

    /**
     * Creates an ItemValue.
     * 
     * @param clientItemHandle The handle of the value.
     * @param itemName The name/address of the value.
     * @param value The current value of the ItemValue.
     * @return The new ItemValue.
     */
    public static ItemValue createItemValue(
            final String clientItemHandle, final String itemName,
            final Object value) {
        ItemValue itemValue = new ItemValue();
        itemValue.setClientItemHandle(clientItemHandle);
        itemValue.setItemName(itemName);
        itemValue.setValue(value);
        return itemValue;
    }

}

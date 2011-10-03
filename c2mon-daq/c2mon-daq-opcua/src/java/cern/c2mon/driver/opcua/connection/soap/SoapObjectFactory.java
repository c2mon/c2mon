package cern.c2mon.driver.opcua.connection.soap;

import java.net.URL;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.opcfoundation.xmlda.ItemValue;
import org.opcfoundation.xmlda.OPCXML_DataAccessStub;
import org.opcfoundation.xmlda.ReadRequestItem;
import org.opcfoundation.xmlda.RequestOptions;
import org.opcfoundation.xmlda.Subscribe;
import org.opcfoundation.xmlda.SubscribeRequestItem;
import org.opcfoundation.xmlda.SubscribeRequestItemList;
import org.opcfoundation.xmlda.SubscriptionPolledRefresh;
import org.opcfoundation.xmlda.Write;
import org.opcfoundation.xmlda.WriteRequestItemList;

/**
 * Factory to create various Soap object and hide the sometimes ugly creation.
 * 
 * @author Andreas Lang
 * 
 */
public final class SoapObjectFactory {
    private static final String AXIS2_CONFIG_FILE_LOCATION = "/axis2.xml";
    private static final org.apache.axiom.om.OMFactory FACTORY = OMAbstractFactory.getOMFactory();
    /**
     * Private constructor. There should be no instances of this helper class.
     */
    private SoapObjectFactory() {
    }

    /**
     * Creates a new OPCDataAccess object.
     * 
     * @param serverURL
     *            The URL to the web service.
     * @param domain
     *            The domain (only used if NT authetification).
     * @param user
     *            The user to use (also NT auth).
     * @param password
     *            The password to use (also NT auth).
     * @return The new data access object.
     * @throws AxisFault 
     */
    public static OPCXML_DataAccessStub createOPCDataAccessSoapInterface(final URL serverURL, final String domain, final String user, final String password) throws AxisFault {
        URL url = SoapObjectFactory.class.getResource(AXIS2_CONFIG_FILE_LOCATION);
        ConfigurationContext config =
            ConfigurationContextFactory.createConfigurationContextFromURIs(url, null);
        OPCXML_DataAccessStub stub = 
            new OPCXML_DataAccessStub(config, serverURL.toString());
        Options options = stub._getServiceClient().getOptions();
        options.setProperty(HTTPConstants.MC_ACCEPT_GZIP, Boolean.TRUE);
        HttpTransportProperties.Authenticator
           auth = new HttpTransportProperties.Authenticator();
        auth.setUsername(user);
        auth.setPassword(password);
        auth.setHost(serverURL.getHost());
        if (serverURL.getPort() > -1)
        	auth.setPort(serverURL.getPort());
        else
        	auth.setPort(serverURL.getDefaultPort());
        if (domain != null)
            auth.setDomain(domain);
        options.setProperty(HTTPConstants.AUTHENTICATE, auth);
        return stub;
    }

    /**
     * Creates a new SubscribeRequestItem. They are used for Subscribe requests.
     * 
     * @param clientItemHandle
     *            The client item handle is used to identify updates after they
     *            were returned from the server.
     * @param address
     *            The address of the item (server dependent).
     * @param valueDeadband
     *            The relative value deadband.
     * @param timeDeadband
     *            The time deadband to use.
     * @param bufferEnabled
     *            If true updates of this item will be even returned if there
     *            were other updates afterwards.
     * @return The created SubscribeRequestItem.
     */
    public static SubscribeRequestItem createSubscribeRequestItem(final String clientItemHandle, final String address, final float valueDeadband, final int timeDeadband, final boolean bufferEnabled) {
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
     * @param clientItemHandle
     *            The client item handle is used to identify updates after they
     *            were returned from the server.
     * @param address
     *            The address of the item (server dependent). if there were
     *            other updates afterwards.
     * @return The created SubscribeRequestItem.
     */
    public static ReadRequestItem createReadRequestItem(final String clientItemHandle, final String address) {
        ReadRequestItem requestItem = new ReadRequestItem();
        requestItem.setClientItemHandle(clientItemHandle);
        requestItem.setItemName(address);
        return requestItem;
    }

    /**
     * Creates a new Subscribe object. It is used to send a Subscribe message to
     * the server.
     * 
     * @param requestHandle
     *            The request handle - not realy important.
     * @param subscribeRequestItems
     *            The items to subscribe to.
     * @param subscripionPingRate
     *            The rate the subscription will be pinged at least.
     * @return The created Subscribe object.
     */
    public static Subscribe createSubscribe(final String requestHandle, final List<SubscribeRequestItem> subscribeRequestItems, final int subscripionPingRate) {
        Subscribe subscribe = new Subscribe();
        subscribe.setOptions(createDefaultRequestOptions(requestHandle));
        SubscribeRequestItemList list = new SubscribeRequestItemList();
        for (SubscribeRequestItem item : subscribeRequestItems) {
            list.addItems(item);
        }
        subscribe.setItemList(list);
        subscribe.setReturnValuesOnReply(false);
        subscribe.setSubscriptionPingRate(subscripionPingRate);
        return subscribe;
    }

    /**
     * Creates a default request options object.
     * 
     * @param requestHandle
     *            The handle which identifies the request.
     * @return The request options object.
     */
    public static RequestOptions createDefaultRequestOptions(final String requestHandle) {
        RequestOptions options = new RequestOptions();
        options.setClientRequestHandle(requestHandle);
        options.setLocaleID("en");
        options.setReturnDiagnosticInfo(false);
        options.setReturnErrorText(true);
        options.setReturnItemTime(true);
        return options;
    }

    /**
     * Creates a new subscription polled refresh with the default settings.
     * 
     * @param serverSubscriptionHandle
     *            The handle which identifies the subscription.
     * @param waitTime
     *            The time after which the refresh will return even if there
     *            where no updates for the items in this subscription.
     * @return The refresh with the correct settings.
     */
    public static SubscriptionPolledRefresh createSubscriptionPolledRefresh(final String serverSubscriptionHandle, final int waitTime) {
        SubscriptionPolledRefresh subscriptionPolledRefresh = new SubscriptionPolledRefresh();
        String[] serverSubHandles = new String[] { serverSubscriptionHandle };
        RequestOptions options = createDefaultRequestOptions("doesnotmatter");
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
     * @param requestHandle
     *            The handle of the request.
     * @param itemList
     *            The item list to put into the write request.
     * @return The new Write request.
     */
    public static Write createWrite(final String requestHandle, final ItemValue[] itemList) {
        Write write = new Write();
        WriteRequestItemList list = new WriteRequestItemList();
        for (ItemValue value : itemList) {
            list.addItems(value);
        }
        write.setOptions(createDefaultRequestOptions(requestHandle));
        write.setItemList(list);
        write.setReturnValuesOnReply(false);
        return write;
    }

    /**
     * Creates an ItemValue.
     * 
     * @param clientItemHandle
     *            The handle of the value.
     * @param itemName
     *            The name/address of the value.
     * @param value
     *            The current value of the ItemValue.
     * @return The new ItemValue.
     */
    public static ItemValue createItemValue(
    		final String clientItemHandle, final String itemName, final Object value) {
        ItemValue itemValue = new ItemValue();
        itemValue.setClientItemHandle(clientItemHandle);
        itemValue.setItemName(itemName);
        OMNamespace omNs = FACTORY.createOMNamespace("http://opcfoundation.org/webservices/XMLDA/1.0/", "");
        OMElement valueElement = FACTORY.createOMElement("Value", omNs);
        valueElement.declareNamespace(FACTORY.createOMNamespace("http://www.w3.org/2001/XMLSchema", "xsd"));
        String xmlType;
        if (value.getClass().equals(String.class))
        	xmlType = "xsd:string";
        else
        	xmlType = "xsd:double";
		valueElement.addAttribute(
    			FACTORY.createOMAttribute("type", FACTORY.createOMNamespace(
    							"http://www.w3.org/2001/XMLSchema-instance",  "xsi"), xmlType));
        valueElement.addChild(FACTORY.createOMText(valueElement, value.toString()));
        itemValue.setValue(valueElement);
        return itemValue;
    }

}

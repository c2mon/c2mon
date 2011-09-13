package cern.c2mon.driver.opcua.connection.soap;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import org.opcfoundation.webservices.XMLDA._1_0.ItemValue;
import org.opcfoundation.webservices.XMLDA._1_0.OPCError;
import org.opcfoundation.webservices.XMLDA._1_0.OPCXMLDataAccessSoap;
import org.opcfoundation.webservices.XMLDA._1_0.ReadRequestItem;
import org.opcfoundation.webservices.XMLDA._1_0.ReadResponse;
import org.opcfoundation.webservices.XMLDA._1_0.RequestOptions;
import org.opcfoundation.webservices.XMLDA._1_0.Subscribe;
import org.opcfoundation.webservices.XMLDA._1_0.SubscribeRequestItem;
import org.opcfoundation.webservices.XMLDA._1_0.SubscribeResponse;
import org.opcfoundation.webservices.XMLDA._1_0.WriteResponse;
import org.opcfoundation.webservices.XMLDA._1_0.holders.OPCErrorArrayHolder;
import org.opcfoundation.webservices.XMLDA._1_0.holders.ReplyBaseHolder;
import org.opcfoundation.webservices.XMLDA._1_0.holders.ReplyItemListHolder;

import cern.c2mon.driver.opcua.OPCAddress;
import cern.c2mon.driver.opcua.connection.common.IGroupProvider;
import cern.c2mon.driver.opcua.connection.common.IItemDefinitionFactory;
import cern.c2mon.driver.opcua.connection.common.impl.OPCCommunicationException;
import cern.c2mon.driver.opcua.connection.common.impl.OPCCriticalException;
import cern.c2mon.driver.opcua.connection.common.impl.OPCEndpoint;
import cern.c2mon.driver.opcua.connection.common.impl.SubscriptionGroup;

/**
 * OPC endpoint for OPC XML DA.
 * 
 * @author Andreas Lang
 *
 */
public class DASoapEndpoint extends OPCEndpoint<DASoapItemDefintion> {

    /**
     * Constant indicating if items should be buffered.
     */
    private static final boolean BUFFER_ENABLED = false;

    /**
     * Ping rate of the subscription. This is the maximum time in which 
     * a subscription needs to be polled or else it will expire.
     */
    private static final int SUBSCRIPTION_PING_RATE = 1000 * 30;

    /**
     * Default Hold time.
     * @see SoapLongPoll
     */
    private static final int HOLD_TIME = 1000;

    /**
     * Default Wait time.
     * @see SoapLongPoll
     */
    private static final int WAIT_TIME = 10000;

    /**
     * Soap stub object. This is only intended for short calls.
     */
    private OPCXMLDataAccessSoap dataAccess;

    /**
     * The exception handler to handle exceptions inside the polling mechanism.
     */
    private ISoapLongPollExceptionHandler exceptionHandler =
        new DefaultSoapLongPollExceptionHandler(this);

    /**
     * The OPC address of this endpoint.
     */
    private OPCAddress address;
    
    /**
     * Collection of Soap long polls.
     */
    private Map<SubscriptionGroup<DASoapItemDefintion>, SoapLongPoll> polls =
        new HashMap<SubscriptionGroup<DASoapItemDefintion>, SoapLongPoll>();

    /**
     * Creates a new DASoapEndpoint.
     * 
     * @param itemAddressFactory Factory to create item addresses from tags.
     * @param groupProvider Provider for groups matching to data tags.
     */
    public DASoapEndpoint(
            final IItemDefinitionFactory<DASoapItemDefintion> itemAddressFactory,
            final IGroupProvider<DASoapItemDefintion> groupProvider) {
        super(itemAddressFactory, groupProvider);
    }
    
    /**
     * Initializes this endpoint with the correct properties.
     * 
     * @param opcAddress The address for this endpoint.
     */
    @Override
    protected synchronized void onInit(final OPCAddress opcAddress) {
        this.address = opcAddress;
        String domain = opcAddress.getDomain();
        String user = opcAddress.getUser();
        String password = opcAddress.getPassword();
        try {
            URL serverURL = opcAddress.getUri().toURL();
            dataAccess = 
                SoapObjectFactory.createOPCDataAccessSoapInterface(serverURL,
                    domain, user, password);
        } catch (MalformedURLException e) {
            throw new OPCCriticalException(e);
        } catch (ServiceException e) {
            throw new OPCCommunicationException("OPC XML server threw an excetpion.", e);
        }
    }

    /**
     * Subscribes for all the groups.
     * 
     * @param subscriptionGroups The groups to subscribe to.
     */
    @Override
    protected synchronized void onSubscribe(
            final Collection< SubscriptionGroup<DASoapItemDefintion> >
            subscriptionGroups) {
        for (SubscriptionGroup<DASoapItemDefintion> subscriptionGroup
                : subscriptionGroups) {
            stopPollForSubscription(subscriptionGroup);
            subscribe(subscriptionGroup);
        }
    }

    /**
     * Subscribes for the provided group.
     * 
     * @param subscriptionGroup The group to subscribe to.
     */
    private void subscribe(
            final SubscriptionGroup<DASoapItemDefintion> subscriptionGroup) {
        Collection<DASoapItemDefintion> itemDefinitions = 
            subscriptionGroup.getDefintions();
        List<SubscribeRequestItem> subscribeRequestItems = 
            new ArrayList<SubscribeRequestItem>(itemDefinitions.size());
        for (DASoapItemDefintion itemDefinition : itemDefinitions) {
            float valueDeadband = subscriptionGroup.getValueDeadband();
            int timeDeadband = subscriptionGroup.getTimeDeadband();
            Long definitionId = itemDefinition.getId();
            subscribeRequestItems.add(
                    SoapObjectFactory.createSubscribeRequestItem(
                        getClientHandle(definitionId),
                        itemDefinition.getAddress(), valueDeadband,
                        timeDeadband, BUFFER_ENABLED)
                    );
            if (itemDefinition.hasRedundantAddress()) {
                subscribeRequestItems.add(
                        SoapObjectFactory.createSubscribeRequestItem(
                            getAlternativeClientHandle(definitionId),
                            itemDefinition.getRedundantAddress(), valueDeadband,
                            timeDeadband, BUFFER_ENABLED)
                        );
            }
        }
        Subscribe subscribe = SoapObjectFactory.createSubscribe(
                subscriptionGroup.toString(), subscribeRequestItems,
                SUBSCRIPTION_PING_RATE);
        try {
            SubscribeResponse subscribeResponse = 
                dataAccess.subscribe(subscribe);
            SoapLongPoll poll = 
                startPoll(subscribeResponse.getServerSubHandle());
            polls.put(subscriptionGroup, poll);
            checkErrors(subscribeResponse.getErrors());
        } catch (RemoteException e) {
            throw new OPCCommunicationException(e);
        } catch (ServiceException e) {
            throw new OPCCommunicationException(e);
        }
    }

    /**
     * Starts a poll for this server subscription handle.
     * 
     * @param serverSubHandle The subscription handle to poll for updates.
     * @return The poll for this subscription.
     * @throws ServiceException Throws a service exception if the service is
     * unreachable.
     */
    private SoapLongPoll startPoll(final String serverSubHandle) 
            throws ServiceException {
        SoapLongPoll soapLongPoll = new SoapLongPoll(
                address, serverSubHandle, HOLD_TIME, WAIT_TIME);
        soapLongPoll.addListener(new ISoapLongPollListener() {
            @Override
            public void valueChanged(final String clientHandle,
                    final long timestamp, final Object value) {
                notifyEndpointListenersValueChange(getDefinitionId(clientHandle), 
                        timestamp, value);
            }
        });
        soapLongPoll.setExceptionHandler(exceptionHandler);
        soapLongPoll.startPolling();
        return soapLongPoll;
    }
    
    /**
     * Sets the data access object.
     * 
     * @param dataAccess the dataAccess to set
     */
    public void setDataAccess(final OPCXMLDataAccessSoap dataAccess) {
        this.dataAccess = dataAccess;
    }

    /**
     * Refreshes the values of a collection of item definitions.
     * 
     * @param itemDefintions The item definitions to refresh.
     */
    @Override
    protected synchronized void onRefresh(
            final Collection<DASoapItemDefintion> itemDefintions) {
        RequestOptions options =
            SoapObjectFactory.createDefaultRequestOptions("request");
        Collection<ReadRequestItem> itemList = new ArrayList<ReadRequestItem>();
        for (DASoapItemDefintion definition : itemDefintions) {
            String clientItemHandle = Long.valueOf(definition.getId()).toString();
            String address = definition.getAddress();
            itemList.add(
                    SoapObjectFactory.createReadRequestItem(
                            clientItemHandle, address));
            if (definition.hasRedundantAddress()) {
                String redClientItemHandle =
                    Long.valueOf(definition.getId()).toString();
                String redAddress = definition.getAddress();
                itemList.add(
                        SoapObjectFactory.createReadRequestItem(
                                redClientItemHandle, redAddress));
            }
        }
        ReadRequestItem[] itemListArray = 
            itemList.toArray(new ReadRequestItem[itemList.size()]);
        ReadResponse readResponse = new ReadResponse();
        ReplyBaseHolder readResult = 
            new ReplyBaseHolder(readResponse.getReadResult());
        OPCErrorArrayHolder errors = new OPCErrorArrayHolder();
        ReplyItemListHolder rItemList = new ReplyItemListHolder(
                readResponse.getRItemList());
        try {
            dataAccess.read(options, itemListArray, readResult, rItemList,
                    errors);
            checkErrors(errors.value);
            for (ItemValue value : readResponse.getRItemList()) {
                String clientItemHandle = value.getClientItemHandle();
                notifyEndpointListenersValueChange(
                        getDefinitionId(clientItemHandle), 
                        value.getTimestamp().getTimeInMillis(), 
                        value.getValue());
            }
            
        } catch (RemoteException e) {
            throw new OPCCommunicationException(e);
        }
    }
    
    /**
     * Gets the definition id to the provided client item handle.
     * 
     * @param clientItemHandle The item handle to get the definition id for.
     * @return The definition id.
     */
    private long getDefinitionId(final String clientItemHandle) {
        return Long.valueOf(clientItemHandle);
    }

    /**
     * Gets an alternative client handle.
     * 
     * @param definitionId The definition id to get the handle for.
     * @return The modified item handle.
     */
    private String getAlternativeClientHandle(final long definitionId) {
        return getClientHandle(definitionId) + ".0";
    }
    
    /**
     * Gets an client handle. At the moment this is just the long as String.
     * 
     * @param definitionId The definition id to get the handle for.
     * @return The modified item handle.
     */
    private String getClientHandle(final long definitionId) {
        return Long.valueOf(definitionId).toString();
    }

    /**
     * Method calls are not supported from the DASoapEndpoint. It will throw
     * an {@link OPCCriticalException}.
     * 
     * @param itemDefintion ignored.
     * @param values ingored.
     */
    @Override
    protected void onCallMethod(final DASoapItemDefintion itemDefintion,
            final Object... values) {
        throw new OPCCriticalException("Method calls not supported for OPC DA Soap.");
        
    }

    /**
     * Writes to a value in the OPC server.
     * 
     * @param itemDefintion The item definition to write to.
     * @param value The value to write.
     */
    @Override
    protected synchronized void onWrite(final DASoapItemDefintion itemDefintion,
            final Object value) {
        long id = itemDefintion.getId();
        ItemValue[] values = {SoapObjectFactory.createItemValue(
                getClientHandle(id),
                itemDefintion.getAddress(), value)};
        try {
            WriteResponse response = dataAccess.write(
                    SoapObjectFactory.createWrite(getClientHandle(id), values));
            OPCError[] errors = response.getErrors();
            checkErrors(errors);
        } catch (RemoteException e) {
            throw new OPCCommunicationException(e);
        }
        
    }

    /**
     * The error array to check.
     * 
     * @param errors The errors which happened.
     */
    private void checkErrors(final OPCError[] errors) {
        if (errors != null) {
            StringBuffer errorString = new StringBuffer();
            errorString.append("Error(s) writing: ");
            for (OPCError error : errors) {
                errorString.append(error.getText());
                errorString.append(", ");
            }
            String message = 
                errorString.toString().substring(
                        0, errorString.length() - 2);
            throw new OPCCommunicationException(message);
        }
    }

    /**
     * Stops the polling and resets all configured fields.
     */
    @Override
    protected synchronized void onStop() {
        for (SoapLongPoll poll : polls.values()) {
            poll.release();
        }
        address = null;
    }

    /**
     * Called on removal of a subscription group.
     * 
     * @param subscriptionGroup The group to remove.
     * @param removedDefinition The definition to remove.
     */
    @Override
    protected synchronized void onRemove(
            final SubscriptionGroup<DASoapItemDefintion> subscriptionGroup,
            final DASoapItemDefintion removedDefinition) {
        stopPollForSubscription(subscriptionGroup);
        if (subscriptionGroup.size() > 0)
            subscribe(subscriptionGroup);
    }

    /**
     * Called on subscribe.
     * 
     * @param subscriptionGroup The subscription group to subscribe for.
     */
    @Override
    protected synchronized void onSubscribe(
            final SubscriptionGroup<DASoapItemDefintion> subscriptionGroup) {
        stopPollForSubscription(subscriptionGroup);
        subscribe(subscriptionGroup);
    }

    /**
     * Stops the poll for a subscription if there is one. If not nothing will
     * happen.
     * 
     * @param subscriptionGroup The subscription group for which the polling
     * should be stopped.
     */
    private void stopPollForSubscription(
            final SubscriptionGroup<DASoapItemDefintion> subscriptionGroup) {
        if (polls.containsKey(subscriptionGroup))
            polls.get(subscriptionGroup).release();
    }

}

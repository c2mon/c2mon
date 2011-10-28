package cern.c2mon.driver.opcua.connection.ua;

import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.DateTime;
import org.opcfoundation.ua.builtintypes.LocalizedText;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.builtintypes.Variant;
import org.opcfoundation.ua.core.ApplicationDescription;
import org.opcfoundation.ua.core.ApplicationType;
import org.opcfoundation.ua.core.CallMethodRequest;
import org.opcfoundation.ua.core.MonitoredItemNotification;
import org.opcfoundation.ua.core.ServerState;
import org.opcfoundation.ua.core.TimestampsToReturn;
import org.opcfoundation.ua.transport.security.Cert;
import org.opcfoundation.ua.transport.security.SecurityMode;

import com.prosysopc.ua.ApplicationIdentity;
import com.prosysopc.ua.CertificateValidationListener;
import com.prosysopc.ua.MonitoredItemBase;
import com.prosysopc.ua.PkiFileBasedCertificateValidator;
import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.SubscriptionBase;
import com.prosysopc.ua.UserIdentity;
import com.prosysopc.ua.PkiFileBasedCertificateValidator.CertificateCheck;
import com.prosysopc.ua.PkiFileBasedCertificateValidator.ValidationResult;
import com.prosysopc.ua.client.MonitoredItem;
import com.prosysopc.ua.client.ServerConnectionException;
import com.prosysopc.ua.client.Subscription;
import com.prosysopc.ua.client.UaClient;

import cern.c2mon.driver.opcua.OPCAddress;
import cern.c2mon.driver.opcua.connection.common.IGroupProvider;
import cern.c2mon.driver.opcua.connection.common.IItemDefinitionFactory;
import cern.c2mon.driver.opcua.connection.common.impl.OPCCommunicationException;
import cern.c2mon.driver.opcua.connection.common.impl.OPCCriticalException;
import cern.c2mon.driver.opcua.connection.common.impl.OPCEndpoint;
import cern.c2mon.driver.opcua.connection.common.impl.SubscriptionGroup;

/**
 * The OPCUA endpoint to connect to OPC UA servers.
 * 
 * @author Andreas Lang
 *
 */
public class UAEndpoint extends OPCEndpoint<UAItemDefintion>
        implements CertificateValidationListener {
    
    /**
     * Map of UA subscriptions
     */
    private Map<SubscriptionGroup<UAItemDefintion>, Subscription> subscrMap = 
        new HashMap<SubscriptionGroup<UAItemDefintion>, Subscription>();
    
    /**
     * An executor service which serves as ThreadPool.
     */
    private static final ExecutorService EXECUTOR_SERVICE =
        Executors.newCachedThreadPool();
    
    // TODO should be in configuration file.
    private static final String PRIVATE_KEY_PASSWORD = "password";

    private static final String PRODUCT_URI = "urn:cern.ch:UA:C2MON";

    private static final String APPLICATION_URI = "urn:localhost:UA:C2MON";
    
    /**
     * The application name. The certificate used should have the same name.
     */
    private static final String APP_NAME = "c2mon-opc-daq";

    /**
     * The base directory of the certificates.
     */
    private static final String CERTIFICATE_BASE_DIR = "PKI/CA";

    /**
     * Security mode of the application. The mode at the top is the preferred one.
     */
    private static final SecurityMode[] SECURITY_MODES = {
        SecurityMode.BASIC256_SIGN_ENCRYPT,
        SecurityMode.BASIC128RSA15_SIGN_ENCRYPT
    };

    /**
     * The UA client object of this endpoint.
     */
    private UaClient client;
    
    /**
     * Mapping between MonitoredItem ids and UAItemDefinitions.
     */
    private Map<UnsignedInteger, UAItemDefintion> definitionMap =
        new HashMap<UnsignedInteger, UAItemDefintion>();

    /**
     * Creates a new OPC UA endpoint.
     * 
     * @param itemDefinitionFactory The factory to create item definitions.
     * @param groupProvider The group provider to group definitions into 
     * subscriptions.
     */
    public UAEndpoint(
            final IItemDefinitionFactory<UAItemDefintion> itemDefinitionFactory,
            final IGroupProvider<UAItemDefintion> groupProvider) {
        super(itemDefinitionFactory, groupProvider);
    }
    
    /**
     * Initialization method.
     * 
     * @param opcAddress The address for this OPC UA endpoint.
     */
    @Override
    protected void onInit(final OPCAddress opcAddress) {
        String uri = opcAddress.getUriString();
        String userName = opcAddress.getUser();
        String password = opcAddress.getPassword();
        try {
            client = new UaClient(uri);
            setUpSecurity(userName, password);
            setUpApplication();
            client.connect();
            client.getServerStatus().getState();
        } catch (Exception e) {
            throw new OPCCommunicationException(e);
        }
    }

    /**
     * Sets up the security settings for the application. The highest available
     * security policy is set. If there are no matching policies a 
     * RuntimeException will be thrown.
     * 
     * @param password The password of the user.
     * @param userName The username to use.
     * @throws ServiceException Throws a service exception if the server is not
     * reachable.
     */
    private void setUpSecurity(
            final String userName, final String password) throws ServiceException {
        PkiFileBasedCertificateValidator validator =
            new PkiFileBasedCertificateValidator(CERTIFICATE_BASE_DIR);
        client.setCertificateValidator(validator);
        validator.setValidationListener(this);
        List<SecurityMode> serverModes = client.getSupportedSecurityModes();
        boolean found = false;
        for (SecurityMode clientMode : SECURITY_MODES) {
            if (serverModes.contains(clientMode)) {
                found = true;
                client.setSecurityMode(clientMode);
                break;
            }
        }
        if (!found) {
            throw new OPCCriticalException("No matching security modes for chosen" 
                    + "server. Cannot open connection.");
        }
        // TODO check how the siemens server works with that
        client.setUserIdentity(new UserIdentity(userName, password));
    }
    
    /**
     * Sets up the application setting to identify to the server.
     * 
     * @throws CertificateNotYetValidException Thrown if the certificate is
     * not yet valid.
     * @throws CertificateExpiredException Thrown if the certificate of the
     * application is expired.
     * @throws InvalidKeySpecException Thrown if the key is invalid.
     * @throws SecureIdentityException Throws a secure identity exception if the
     * certificate is invalid.
     * @throws IOException Throws an IO Exception if the certificate directory
     * is not accessible.
     */
    private void setUpApplication() throws CertificateNotYetValidException,
        CertificateExpiredException, InvalidKeySpecException,
        SecureIdentityException, IOException {
        ApplicationDescription appDescription = new ApplicationDescription();
        appDescription.setApplicationName(
                new LocalizedText(APP_NAME, Locale.ENGLISH));
        appDescription.setApplicationUri(APPLICATION_URI);
        appDescription.setProductUri(PRODUCT_URI);
        appDescription.setApplicationType(ApplicationType.Client);
        ApplicationIdentity identity = 
            ApplicationIdentity.loadOrCreateCertificate(
                appDescription, "CERN", PRIVATE_KEY_PASSWORD,
                new File(CERTIFICATE_BASE_DIR, "private"), false);
        client.setApplicationIdentity(identity);
    }

    /**
     * Subscribes the item definitions in the subscriptions to the OPC
     * UA server.
     * 
     * @param subscriptionGroups the subscription groups to subscribe to.
     */
    @Override
    protected void onSubscribe(
            final Collection<SubscriptionGroup<UAItemDefintion>> 
            subscriptionGroups) {
        for (SubscriptionGroup<UAItemDefintion> group : subscriptionGroups) {
            subscribe(group);
        }
    }

    /**
     * Subscribes this group o the OPC server.
     * 
     * @param group The group with the items to subscribe.
     */
    private void subscribe(final SubscriptionGroup<UAItemDefintion> group) {
        final Subscription subscription = UAObjectFactory.createSubscription();
        final float valueDeadband = group.getValueDeadband();
        final int timeDeadband = group.getTimeDeadband();
        try {
            for (UAItemDefintion definition : group.getDefintions()) {
                try {
                    processDefinition(
                            subscription, valueDeadband, timeDeadband,
                            definition);
                } catch (Exception e) {
                    notifyEndpointListenersItemError(definition.getId(), e);
                }
            }
            registerForNotifications(subscription);
            subscrMap.put(group, subscription);
            client.addSubscription(subscription);
        } catch (Exception e) {
            throw new OPCCommunicationException(e);
        }
    }

    /**
     * Processes a item defintion by adding it to a subscription and
     * setting its time and value deadband.
     * 
     * @param subscription The subscription to add to.
     * @param valueDeadband The value deadband for this item.
     * @param timeDeadband The time deadband for this item.
     * @param definition The item definition.
     * @throws ServiceException May throw a service exception.
     * @throws StatusException May throw a status exception.
     */
    private void processDefinition(
            final Subscription subscription,
            final float valueDeadband, final int timeDeadband, 
            final UAItemDefintion definition) 
                throws ServiceException, StatusException {
        MonitoredItem item = UAObjectFactory.createMonitoredItem(
                    definition.getAddress(), valueDeadband, 
                    timeDeadband);
        addToSubscription(subscription, definition, item);
//        System.out.println(definition.getAddress());
        if (definition.hasRedundantAddress()) {
            MonitoredItem redundantItem = UAObjectFactory.createMonitoredItem(
                    definition.getAddress(), valueDeadband, 
                    timeDeadband);
            addToSubscription(subscription, definition, redundantItem);
        }
    }

    /**
     * Adds a monitored item to a subscription and stores it in the
     * definition map.
     * 
     * @param subscription The subscription to add to.
     * @param definition The item definition.
     * @param item The item to add to the subscription.
     * @throws ServiceException May throw a service exception.
     * @throws StatusException May throw a status exception.
     */
    private void addToSubscription(final Subscription subscription,
            final UAItemDefintion definition, final MonitoredItem item) 
                throws ServiceException, StatusException {
        UnsignedInteger itemId = item.getClientHandle();
        definitionMap.put(itemId, definition);
        subscription.addItem(item);
    }

    /**
     * Registers for events on this subscription.
     * 
     * @param subscription The subscription to register on.
     */
    private void registerForNotifications(final Subscription subscription) {
        subscription.addNotificationListener(
            new SubscriptionNotificationAdapter() {
                @Override
                public void onDataChange(
                        final SubscriptionBase subscriptionBase,
                        final MonitoredItem item, final DataValue value) {
                    notifyEndpointsAboutMonitoredItemChange(item, value);
                }
                
                @Override
                public void onError(final SubscriptionBase subscriptionBase,
                        final Object object, final Exception exception) {
                    if (object instanceof MonitoredItemNotification) {
                        MonitoredItemNotification notification =
                            (MonitoredItemNotification) object;
                        notifyEndpointsAboutMonitoredItemError(
                                notification, exception);
                    }
                    notifyEndpointListenersSubscriptionFailed(exception);
                }
            });
    }
    
    /**
     * Notifies endpoints about a changed monitored item.
     * 
     * @param item The item which changed.
     * @param value The new value of the item.
     */
    private void notifyEndpointsAboutMonitoredItemChange(
            final MonitoredItem item, final DataValue value) {
        EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                long itemdefintionId = definitionMap.get(
                        item.getClientHandle()).getId();
                if (!checkError(itemdefintionId, value)) {
                notifyEndpointListenersValueChange(
                        itemdefintionId, 
                        value.getSourceTimestamp().getTimeInMillis(),
                        value.getValue().getValue());
                } 
            }
        });
    }
    
    /**
     * Notifies endpoints about a changed monitored item.
     * 
     * @param item The item which changed.
     * @param exception The exception which caused the error.
     */
    private void notifyEndpointsAboutMonitoredItemError(
            final MonitoredItemNotification item, final Throwable exception) {
        long itemdefintionId = definitionMap.get(
                item.getClientHandle()).getId();
        notifyEndpointListenersItemError(itemdefintionId, exception);
    }

    /**
     * Certificate validation method. Accepts a certificate if it is valid, the
     * signature is correct and it is either not self signed or the selfsigned
     * certificate is explicitly trusted.
     * 
     * @param certificate The certificate received.
     * @param description The application description received.
     * @param compliedCertificateChecks Set of all complied certificate checks.
     * @return The result of the validation see: {@link ValidationResult}.
     */
    @Override
    public ValidationResult onValidate(final Cert certificate,
            final ApplicationDescription description,
            final EnumSet<CertificateCheck> compliedCertificateChecks) {
        if (compliedCertificateChecks.contains(CertificateCheck.Validity)
              && compliedCertificateChecks.contains(CertificateCheck.Signature)
              && isNotSelfSignedOrSpecificlyTrusted(compliedCertificateChecks))
            return ValidationResult.AcceptOnce;
        else
            return ValidationResult.Reject; 
    }

    /**
     * Checks if he provided checks either don't contain 'SelfSigned' or the
     * checks describe a specifically trusted certificate.
     * 
     * @param compliedCertificateChecks The checks a certificate complies to.
     * @return True if the certificate the checks belong to is either not
     * self signed or is specifically trusted else false.
     */
    private boolean isNotSelfSignedOrSpecificlyTrusted(
            final EnumSet<CertificateCheck> compliedCertificateChecks) {
        return !compliedCertificateChecks.contains(CertificateCheck.SelfSigned)
                  || compliedCertificateChecks.contains(CertificateCheck.Trusted);
    }

    /**
     * Refreshes the values of a collection of item definitions.
     * 
     * @param itemDefintions The item definitions to refresh.
     */
    @Override
    protected void onRefresh(final Collection<UAItemDefintion> itemDefintions) {
        for (UAItemDefintion definition : itemDefintions) {
        	ArrayList<NodeId> items = 
                    new ArrayList<NodeId>(itemDefintions.size());
            items.add(definition.getAddress());
            if (definition.hasRedundantAddress()) {
                items.add(definition.getRedundantAddress());
            }
            NodeId[] nodeIdArray = items.toArray(new NodeId[items.size()]);
            try {
                DataValue[] values = client.readValues(
                        nodeIdArray, TimestampsToReturn.Both);
                for (DataValue value : values) {
                	if (!checkError(definition.getId(), value)) {
	                    DateTime timestamp = value.getSourceTimestamp();
	                    if (timestamp == null) {
	                    	timestamp = value.getServerTimestamp();
	                    	if (timestamp == null) {
	                    		timestamp = new DateTime();
	                    	}
	                    }
						long timeInMillis = timestamp.getTimeInMillis();
						notifyEndpointListenersValueChange(
	                            definition.getId(),
	                            timeInMillis,
	                            value.getValue().getValue());
                	}
                }
            } catch (ServiceException e) {
                throw new OPCCommunicationException(e);
            }
        }
    }

    private boolean checkError(long itemdefintionId, DataValue value) {
    	boolean error = false;
		if (value.getStatusCode().getValue().intValue() != 0) {
			notifyEndpointListenersItemError(
					itemdefintionId, new OPCCommunicationException(value.getStatusCode().toString()));
			error = true;
		}
		return error;
	}

	/**
     * Method call for OPC UA.
     * 
     * @param itemDefintion The item definiton object which identifies the
     * method to call.
     * @param values The values used as parameters of the method.
     */
    @Override
    protected void onCallMethod(final UAItemDefintion itemDefintion, 
            final Object... values) {
        NodeId objectId = itemDefintion.getAddress();
        NodeId methodId = itemDefintion.getRedundantAddress();
        Variant[] arguments = new Variant[values.length];
        for (int i = 0; i < values.length; i++) {
            arguments[i] = new Variant(values[i]);
        }
        CallMethodRequest callMethodRequest = new CallMethodRequest(
                objectId, methodId, arguments);
        try {
            client.call(callMethodRequest);
        } catch (ServiceException e) {
            throw new OPCCommunicationException(e);
        }
        
    }

    /**
     * Writes to a value in the OPC UA server identified by the item definiton.
     * 
     * @param itemDefintion The item definition which decribes the value in the
     * OPCServer to write to.
     * @param value The value to write to.
     */
    @Override
    protected void onWrite(final UAItemDefintion itemDefintion, final Object value) {
        NodeId nodeId = itemDefintion.getAddress();
        try {
            client.writeValue(nodeId, value);
        } catch (ServiceException e) {
            throw new OPCCommunicationException(e);
        } catch (StatusException e) {
            throw new OPCCommunicationException(e);
        }
    }

    /**
     * Stops this endpoint and clears its configuration.
     */
    @Override
    protected void onStop() {
        client.disconnect();
        definitionMap.clear();
        client = null;
    }

    /**
     * Called when an item definition which was in the provided subscription group
     * is removed.
     * 
     * @param subscriptionGroup The group which contained the item definition.
     * @param removedDefinition The definition which was removed.
     */
    @Override
    protected synchronized void onRemove(
            final SubscriptionGroup<UAItemDefintion> subscriptionGroup,
            final UAItemDefintion removedDefinition) {
        if (subscrMap.containsKey(subscriptionGroup)) {
            try {
                if (subscriptionGroup.size() < 1) {
                    Subscription subscription =
                        subscrMap.remove(subscriptionGroup);
                    client.removeSubscription(subscription);
                }
                else {
                    Subscription subscription =
                        subscrMap.get(subscriptionGroup);
                    for (MonitoredItemBase item : subscription.getItems()) {
                        NodeId monitoredId = item.getNodeId();
                        if (isPartOfDefinition(
                                removedDefinition, monitoredId)) {
                            subscription.removeItem(item);
                        }
                    }
                }
            } catch (ServiceException e) {
                throw new OPCCommunicationException(e);
            } catch (StatusException e) {
                throw new OPCCommunicationException(e);
            }
        }
    }
    
    /**
     * Called when a subscription group should be subscribed at the endpoint.
     * 
     * @param subscriptionGroup The group to subscribe at the endpoint.
     */
    @Override
    protected synchronized void onSubscribe(
            final SubscriptionGroup<UAItemDefintion> subscriptionGroup) {
        if (subscrMap.containsKey(subscriptionGroup)) {
            Subscription subscription = subscrMap.get(subscriptionGroup);
            for (UAItemDefintion definition 
                    : subscriptionGroup.getUnsubscribedDefinitions()) {
                float valueDeadband = subscriptionGroup.getValueDeadband();
                int timeDeadband = subscriptionGroup.getTimeDeadband();
                try {
                    processDefinition(
                            subscription, valueDeadband, timeDeadband, definition);
                } catch (ServiceException e) {
                    throw new OPCCommunicationException(e);
                } catch (StatusException e) {
                    throw new OPCCommunicationException(e);
                }
            }
        }
        else {
            subscribe(subscriptionGroup);
        }
        
    }

    /**
     * Returns true if the provided node id is part of the provided item definition.
     * 
     * @param definition The item definition to check.
     * @param monitoredId The NodeId which could be inside the definition
     * @return True if the NodeId is part of the definition else false.
     */
    private boolean isPartOfDefinition(
            final UAItemDefintion definition, final NodeId monitoredId) {
        return monitoredId.equals(definition.getAddress())
                || (definition.hasRedundantAddress() 
                        && monitoredId.equals(
                                definition.getRedundantAddress()));
    }
    
    /**
     * Checks the status of the enpoint. It will throw an exception if something
     * is wrong.
     * 
     * @throws OPCCommunicationException Thrown if the connection is not
     * reachable but might be back later on.
     * @throws OPCCriticalException Thrown if the connection is not
     * reachable and can most likely not be restored.
     */
    @Override
    protected void checkStatus() {
        try {
            ServerState state = client.getServerStatus().getState();
            if (!state.equals(ServerState.Running)) {
                throw new OPCCommunicationException("OPC server not running.");
            }
        } catch (ServerConnectionException e) {
            throw new OPCCommunicationException(e);
        } catch (StatusException e) {
            throw new OPCCommunicationException(e);
        }
    }

}

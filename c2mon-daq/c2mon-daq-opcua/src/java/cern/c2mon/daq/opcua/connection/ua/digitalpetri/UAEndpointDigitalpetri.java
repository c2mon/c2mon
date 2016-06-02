/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.daq.opcua.connection.ua.digitalpetri;

import static com.digitalpetri.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import static com.google.common.collect.Lists.newArrayList;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.opcfoundation.ua.core.MonitoredItemNotification;
import org.opcfoundation.ua.transport.security.SecurityMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitalpetri.opcua.sdk.client.OpcUaClient;
import com.digitalpetri.opcua.sdk.client.SessionActivityListener;
import com.digitalpetri.opcua.sdk.client.api.UaSession;
import com.digitalpetri.opcua.sdk.client.api.config.OpcUaClientConfig;
import com.digitalpetri.opcua.sdk.client.api.identity.AnonymousProvider;
import com.digitalpetri.opcua.sdk.client.api.identity.IdentityProvider;
import com.digitalpetri.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import com.digitalpetri.opcua.sdk.client.api.subscriptions.UaSubscription;
import com.digitalpetri.opcua.stack.client.UaTcpStackClient;
import com.digitalpetri.opcua.stack.core.AttributeId;
import com.digitalpetri.opcua.stack.core.security.SecurityPolicy;
import com.digitalpetri.opcua.stack.core.types.builtin.DataValue;
import com.digitalpetri.opcua.stack.core.types.builtin.LocalizedText;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.QualifiedName;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UInteger;
import com.digitalpetri.opcua.stack.core.types.enumerated.MonitoringMode;
import com.digitalpetri.opcua.stack.core.types.enumerated.TimestampsToReturn;
import com.digitalpetri.opcua.stack.core.types.structured.EndpointDescription;
import com.digitalpetri.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import com.digitalpetri.opcua.stack.core.types.structured.MonitoringParameters;
import com.digitalpetri.opcua.stack.core.types.structured.ReadValueId;

import cern.c2mon.daq.opcua.connection.common.AbstractOPCUAAddress;
import cern.c2mon.daq.opcua.connection.common.IGroupProvider;
import cern.c2mon.daq.opcua.connection.common.IItemDefinitionFactory;
import cern.c2mon.daq.opcua.connection.common.impl.OPCCommunicationException;
import cern.c2mon.daq.opcua.connection.common.impl.OPCCriticalException;
import cern.c2mon.daq.opcua.connection.common.impl.OPCEndpoint;
import cern.c2mon.daq.opcua.connection.common.impl.SubscriptionGroup;

/**
 * The OPCUA endpoint to connect to OPC UA servers.
 *
 * @author Andreas Lang
 *
 */
public class UAEndpointDigitalpetri extends OPCEndpoint<UAItemDefintionDigitalpetri> {

  /**
   * logger of this class.
   */
  private final static Logger LOG = LoggerFactory.getLogger(UAEndpointDigitalpetri.class);

  /**
   * Map of UA subscriptions
   */
  private Map<SubscriptionGroup<UAItemDefintionDigitalpetri>, UaSubscription> subscrMap = new HashMap<>();

  /**
   * An executor service which serves as ThreadPool.
   */
  private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

  private static final String PRODUCT_URI = "urn:cern.ch:UA:C2MON";

  private static final String APPLICATION_URI = "urn:localhost:UA:C2MON";

  /**
   * The application name. The certificate used should have the same name.
   */
  private static final String APP_NAME = "c2mon-opc-daq";

  /**
   * Security mode of the application. The mode at the top is the preferred one.
   */
  private static final SecurityMode[] SECURITY_MODES = { SecurityMode.BASIC256_SIGN_ENCRYPT, SecurityMode.BASIC128RSA15_SIGN_ENCRYPT, };

  /**
   * The UA client object of this endpoint.
   */
  private OpcUaClient client;

  private KeyStoreLoader keyStoreLoader = new KeyStoreLoader();

  AtomicInteger clientHandleCounter = new AtomicInteger(0);

  /**
   * Mapping between MonitoredItem ids and UAItemDefinitions.
   */
  private Map<UInteger, UAItemDefintionDigitalpetri> definitionMap = new HashMap<>();

  SessionActivityListener opcUasessionActivityListener = new SessionActivityListener() {
  };

  public boolean opcSessionDisconnected;

  /**
   * Creates a new OPC UA endpoint.
   *
   * @param itemDefinitionFactory
   *          The factory to create item definitions.
   * @param groupProvider
   *          The group provider to group definitions into subscriptions.
   */
  public UAEndpointDigitalpetri(final IItemDefinitionFactory<UAItemDefintionDigitalpetri> itemDefinitionFactory, final IGroupProvider<UAItemDefintionDigitalpetri> groupProvider) {
    super(itemDefinitionFactory, groupProvider);
  }

  /**
   * Initialization method.
   *
   * @param opcAddress
   *          The address for this OPC UA endpoint.
   */
  @Override
  protected void onInit(final AbstractOPCUAAddress opcAddress) {
    String uri = opcAddress.getUriString();
    String userName = opcAddress.getUser();
    String password = opcAddress.getPassword();
    SecurityPolicy sp = SecurityPolicy.None;

    try {
      EndpointDescription[] endpoints = UaTcpStackClient.getEndpoints(uri).get();

      EndpointDescription endpoint = Arrays.stream(endpoints).filter(e -> e.getSecurityPolicyUri().equals(sp.getSecurityPolicyUri())).findFirst()
          .orElseThrow(() -> new Exception("no desired endpoints returned"));

      IdentityProvider identityProvider = new AnonymousProvider();
      LOG.info("Using endpoint: {} [{}]", endpoint.getEndpointUrl(), sp);

      keyStoreLoader.load();
      OpcUaClientConfig config = OpcUaClientConfig.builder().setApplicationName(LocalizedText.english(APP_NAME)).setApplicationUri(APPLICATION_URI)
          .setProductUri(PRODUCT_URI).setCertificate(keyStoreLoader.getClientCertificate()).setKeyPair(keyStoreLoader.getClientKeyPair()).setEndpoint(endpoint)
          .setIdentityProvider(identityProvider).setRequestTimeout(uint(5000)).build();
      client = new OpcUaClient(config);
      client.connect().get();

//      client.addFaultListener(new ServiceFaultListener() {
//
//        @Override
//        public void onServiceFault(ServiceFault fault) {
//          LOG.warn("Received OPC-UA service fault code : {}", fault.getResponseHeader().getServiceResult());
//        }
//      });

//      client.addSessionActivityListener(opcUasessionActivityListener);
    } catch (Exception e) {
      throw new OPCCommunicationException(e);
    }
  }

  /**
   * Subscribes the item definitions in the subscriptions to the OPC UA server.
   *
   * @param subscriptionGroups
   *          the subscription groups to subscribe to.
   */
  @Override
  protected void onSubscribe(final Collection<SubscriptionGroup<UAItemDefintionDigitalpetri>> subscriptionGroups) {
    for (SubscriptionGroup<UAItemDefintionDigitalpetri> group : subscriptionGroups) {
      subscribe(group);
    }
  }

  /**
   * Subscribes this group o the OPC server.
   *
   * @param group
   *          The group with the items to subscribe.
   */
  private void subscribe(final SubscriptionGroup<UAItemDefintionDigitalpetri> group) {
    try {
      final float valueDeadband = group.getValueDeadband();
      final int timeDeadband = group.getTimeDeadband();
      final UaSubscription subscription = client.getSubscriptionManager().createSubscription(new Double(timeDeadband)).get();

      for (UAItemDefintionDigitalpetri definition : group.getDefintions()) {
        try {
          final UAItemDefintionDigitalpetri definition1 = definition;

          processDefinition(subscription, valueDeadband, timeDeadband, definition1);

          definition1.setSubscribed(true);
          subscrMap.put(group, subscription);
        } catch (Exception e) {
          notifyEndpointListenersItemError(definition.getId(), e);
        }
      }

      subscrMap.put(group, subscription);
    } catch (Exception e) {
      throw new OPCCommunicationException(e);
    }
  }

  /**
   * Notifies endpoints about a changed monitored item.
   *
   * @param item
   *          The item which changed.
   * @param value
   *          The new value of the item.
   */
  private void notifyEndpointsAboutMonitoredItemChange(final UaMonitoredItem item, final DataValue value) {
    EXECUTOR_SERVICE.execute(new Runnable() {
      @Override
      public void run() {
        long itemdefinitionId = definitionMap.get(item.getClientHandle()).getId();
        if (!checkError(itemdefinitionId, value)) {
          notifyEndpointListenersValueChange(itemdefinitionId, value.getSourceTime().getUtcTime(), value.getValue().getValue());
        }
      }
    });
  }

  /**
   * Notifies endpoints about a changed monitored item.
   *
   * @param item
   *          The item which changed.
   * @param exception
   *          The exception which caused the error.
   */
  private void notifyEndpointsAboutMonitoredItemError(final MonitoredItemNotification item, final Throwable exception) {
    long itemdefintionId = definitionMap.get(item.getClientHandle()).getId();
    notifyEndpointListenersItemError(itemdefintionId, exception);
  }

  // /**
  // * Certificate validation method. Accepts a certificate if it is valid, the
  // * signature is correct and it is either not self signed or the selfsigned
  // * certificate is explicitly trusted.
  // *
  // * @param certificate
  // * The certificate received.
  // * @param description
  // * The application description received.
  // * @param compliedCertificateChecks
  // * Set of all complied certificate checks.
  // * @return The result of the validation see: {@link ValidationResult}.
  // */
  // @Override
  // public ValidationResult onValidate(final Cert certificate, final
  // ApplicationDescription description,
  // final EnumSet<CertificateCheck> compliedCertificateChecks) {
  // if (compliedCertificateChecks.contains(CertificateCheck.Validity) &&
  // compliedCertificateChecks.contains(CertificateCheck.Signature)
  // && isNotSelfSignedOrSpecificlyTrusted(compliedCertificateChecks))
  // return ValidationResult.AcceptOnce;
  // else
  // return ValidationResult.Reject;
  // }

  // /**
  // * Checks if he provided checks either don't contain 'SelfSigned' or the
  // * checks describe a specifically trusted certificate.
  // *
  // * @param compliedCertificateChecks
  // * The checks a certificate complies to.
  // * @return True if the certificate the checks belong to is either not self
  // * signed or is specifically trusted else false.
  // */
  // private boolean isNotSelfSignedOrSpecificlyTrusted(final
  // EnumSet<CertificateCheck> compliedCertificateChecks) {
  // return !compliedCertificateChecks.contains(CertificateCheck.SelfSigned) ||
  // compliedCertificateChecks.contains(CertificateCheck.Trusted);
  // }

  /**
   * Refreshes the values of a collection of item definitions.
   *
   * @param itemDefintions
   *          The item definitions to refresh.
   */
  @Override
  protected void onRefresh(final Collection<UAItemDefintionDigitalpetri> itemDefintions) {
    // TODO
    // for (UAItemDefintion definition : itemDefintions) {
    // ArrayList<NodeId> items = new ArrayList<NodeId>(itemDefintions.size());
    // items.add(definition.getAddress());
    // if (definition.hasRedundantAddress()) {
    // items.add(definition.getRedundantAddress());
    // }
    // NodeId[] nodeIdArray = items.toArray(new NodeId[items.size()]);
    // try {
    // DataValue[] values = client.readValues(nodeIdArray,
    // TimestampsToReturn.Both);
    // for (DataValue value : values) {
    // if (!checkError(definition.getId(), value)) {
    // DateTime timestamp = value.getSourceTimestamp();
    // if (timestamp == null) {
    // timestamp = value.getServerTimestamp();
    // if (timestamp == null) {
    // timestamp = new DateTime();
    // }
    // }
    // long timeInMillis = timestamp.getTimeInMillis();
    // notifyEndpointListenersValueChange(definition.getId(), timeInMillis,
    // value.getValue().getValue());
    // }
    // }
    // } catch (ServiceException e) {
    // throw new OPCCommunicationException(e);
    // }
    // }
  }

  private boolean checkError(long itemdefintionId, DataValue value) {
    boolean error = false;
    if (value.getStatusCode().getValue() != 0) {
      notifyEndpointListenersItemError(itemdefintionId, new OPCCommunicationException(value.getStatusCode().toString()));
      error = true;
    }
    return error;
  }

  /**
   * Method call for OPC UA.
   *
   * @param itemDefintion
   *          The item definiton object which identifies the method to call.
   * @param values
   *          The values used as parameters of the method.
   */
  @Override
  protected void onCallMethod(final UAItemDefintionDigitalpetri itemDefintion, final Object... values) {
    // TODO
    // NodeId objectId = itemDefintion.getAddress();
    // NodeId methodId = itemDefintion.getRedundantAddress();
    // Variant[] arguments = new Variant[values.length];
    // for (int i = 0; i < values.length; i++) {
    // arguments[i] = new Variant(values[i]);
    // }
    // CallMethodRequest callMethodRequest = new CallMethodRequest(objectId,
    // methodId, arguments);
    // try {
    // client.call(callMethodRequest);
    // } catch (ServiceException e) {
    // throw new OPCCommunicationException(e);
    // }
    throw new UnsupportedOperationException("Not supported by right now by Digital Petry");
  }

  /**
   * Writes to a value in the OPC UA server identified by the item definiton.
   *
   * @param itemDefintion
   *          The item definition which decribes the value in the OPCServer to
   *          write to.
   * @param value
   *          The value to write to.
   */
  @Override
  protected void onWrite(final UAItemDefintionDigitalpetri itemDefintion, final Object value) {
    // TODO
    // NodeId nodeId = itemDefintion.getAddress();
    // try {
    // client.writeValue(nodeId, value);
    // } catch (ServiceException e) {
    // throw new OPCCommunicationException(e);
    // } catch (StatusException e) {
    // throw new OPCCommunicationException(e);
    // }
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
   * @param subscriptionGroup
   *          The group which contained the item definition.
   * @param removedDefinition
   *          The definition which was removed.
   */
  @Override
  protected synchronized void onRemove(final SubscriptionGroup<UAItemDefintionDigitalpetri> subscriptionGroup, final UAItemDefintionDigitalpetri removedDefinition) {
    if (subscrMap.containsKey(subscriptionGroup)) {
      // TODO
      // try {
      // if (subscriptionGroup.size() < 1) {
      // Subscription subscription = subscrMap.remove(subscriptionGroup);
      // client.removeSubscription(subscription);
      // } else {
      // Subscription subscription = subscrMap.get(subscriptionGroup);
      // for (MonitoredItemBase item : subscription.getItems()) {
      // NodeId monitoredId = item.getNodeId();
      // if (isPartOfDefinition(removedDefinition, monitoredId)) {
      // subscription.removeItem(item);
      // }
      // }
      // }
      // } catch (ServiceException e) {
      // throw new OPCCommunicationException(e);
      // } catch (StatusException e) {
      // throw new OPCCommunicationException(e);
      // }
    }
  }

  /**
   * Called when a subscription group should be subscribed at the endpoint.
   *
   * @param subscriptionGroup
   *          The group to subscribe at the endpoint.
   */
  @Override
  protected synchronized void onSubscribe(final SubscriptionGroup<UAItemDefintionDigitalpetri> subscriptionGroup) {
    if (subscrMap.containsKey(subscriptionGroup)) {
      UaSubscription subscription = subscrMap.get(subscriptionGroup);
      for (UAItemDefintionDigitalpetri definition : subscriptionGroup.getUnsubscribedDefinitions()) {
        float valueDeadband = subscriptionGroup.getValueDeadband();
        int timeDeadband = subscriptionGroup.getTimeDeadband();
        try {
          processDefinition(subscription, valueDeadband, timeDeadband, definition);
        } catch (Exception e) {
          throw new OPCCommunicationException(e);
        }
      }
    } else {
      subscribe(subscriptionGroup);
    }

  }

  private void processDefinition(UaSubscription subscription, float valueDeadband, int timeDeadband, UAItemDefintionDigitalpetri definition) throws Exception {
    UInteger clientHandle = uint(clientHandleCounter.getAndIncrement());

    // TODO : Use the valueDeadband by creating a filter
    MonitoringParameters parameters = new MonitoringParameters(clientHandle, new Double(timeDeadband), // sampling
                                                                                                       // interval
        null, // filter, null means use default
        uint(10), // queue size
        true); // discard oldest
    UInteger itemId = clientHandle;
    definitionMap.put(itemId, definition);

    ReadValueId readValueId = new ReadValueId(definition.getAddress(), AttributeId.Value.uid(), null, QualifiedName.NULL_VALUE);

    MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(readValueId, MonitoringMode.Reporting, parameters);
    List<UaMonitoredItem> newMonitoredItems = subscription.createMonitoredItems(TimestampsToReturn.Both, newArrayList(request)).get();

    for (UaMonitoredItem item : newMonitoredItems) {
      item.setValueConsumer(v -> {
        // LOG.info("value received: {}", v.getValue());
        notifyEndpointsAboutMonitoredItemChange(item, v);
      });

      // TODO : Add an error consumer if it exists ??
      // @Override
      // public void onError(final SubscriptionBase subscriptionBase,
      // final
      // Object
      // object, final Exception exception) {
      // if (object instanceof MonitoredItemNotification) {
      // MonitoredItemNotification notification =
      // (MonitoredItemNotification)
      // object;
      // notifyEndpointsAboutMonitoredItemError(notification, exception);
      // }
      // notifyEndpointListenersSubscriptionFailed(exception);
      // }
    }

  }

  /**
   * Returns true if the provided node id is part of the provided item
   * definition.
   *
   * @param definition
   *          The item definition to check.
   * @param monitoredId
   *          The NodeId which could be inside the definition
   * @return True if the NodeId is part of the definition else false.
   */
  private boolean isPartOfDefinition(final UAItemDefintionDigitalpetri definition, final NodeId monitoredId) {
    return monitoredId.equals(definition.getAddress()) || (definition.hasRedundantAddress() && monitoredId.equals(definition.getRedundantAddress()));
  }

  /**
   * Checks the status of the enpoint. It will throw an exception if something
   * is wrong.
   *
   * @throws OPCCommunicationException
   *           Thrown if the connection is not reachable but might be back later
   *           on.
   * @throws OPCCriticalException
   *           Thrown if the connection is not reachable and can most likely not
   *           be restored.
   */
  @Override
  protected void checkStatus() {
    if (opcSessionDisconnected) {
      throw new OPCCommunicationException("OPC-UA endpoint is not connected");
    }
    // try {
    // ServerState state = client.getServerStatus().getState();
    // if (!state.equals(ServerState.Running)) {
    // throw new OPCCommunicationException("OPC server not running.");
    // }
    // } catch (ServerConnectionException e) {
    // throw new OPCCommunicationException(e);
    // } catch (StatusException e) {
    // throw new OPCCommunicationException(e);
    // }
  }

  private final class OpcUaSessionStateListener implements SessionActivityListener {

    @Override
    public void onSessionInactive(UaSession session) {
      opcSessionDisconnected = true;

      // TODO : Add more details about the problem that occurred
      throw new OPCCommunicationException("OPC-UA endpoint connection lost");
    }

    @Override
    public void onSessionActive(UaSession session) {
      // TODO Auto-generated method stub
      SessionActivityListener.super.onSessionActive(session);
      opcSessionDisconnected = false;
    }

  }

}

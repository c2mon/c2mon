package cern.c2mon.daq.opcua.connection.common.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import cern.c2mon.daq.opcua.OPCUAAddress;
import cern.c2mon.daq.opcua.connection.common.IGroupProvider;
import cern.c2mon.daq.opcua.connection.common.IItemDefinitionFactory;
import cern.c2mon.daq.opcua.connection.common.IOPCEndpoint;
import cern.c2mon.daq.opcua.connection.common.IOPCEndpointListener;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.datatag.address.OPCHardwareAddress;
import cern.c2mon.shared.common.type.TypeConverter;
import cern.c2mon.shared.daq.command.ISourceCommandTag;
import cern.c2mon.shared.daq.command.SourceCommandTagValue;
import cern.c2mon.shared.daq.datatag.ISourceDataTag;

/**
 * The abstract OPC endpoint.
 * 
 * @author Andreas Lang
 *
 * @param <ID> An extension of the {@link ItemDefinition} object. The extension
 * depends on the connection used.
 */
public abstract class OPCEndpoint<ID extends ItemDefinition< ? > > 
        implements IOPCEndpoint {
      
    /**
     * logger of this class.
     */
    private final static Logger LOG = Logger.getLogger(OPCEndpoint.class);
  
    /**
     * Collection of endpoint listeners registered at this endpoint.
     */
    private final Collection<IOPCEndpointListener> listeners =
        new ConcurrentLinkedQueue<IOPCEndpointListener>();
    
    /**
     * Maps item defintion ids to data tags.
     */
    private final Map<Long, ISourceDataTag> itemDefintionIdsToDataTags =
        new ConcurrentHashMap<Long, ISourceDataTag>();
    
    /**
     * Maps item definiton ids to item definitions.
     */
    private final Map<Long, ID> tagIdsToItemDefinitions = new HashMap<Long, ID>();
    
    /**
     * The Item definition factory.
     */
    private final IItemDefinitionFactory<ID> itemDefinitionFactory;
    
    /**
     * The group provider.
     */
    private final IGroupProvider<ID> groupProvider;
    
    /**
     * The current state of the endpoint.
     */
    private STATE currentState = STATE.NOT_INITIALIZED;
    
    /**
     * logger of this class.
     */
    private final static Logger logger = Logger.getLogger(OPCEndpoint.class);

    /**
     * Creates a new OPCEndpoint.
     * 
     * @param itemAddressFactory The item address factory to use to create the
     * items.
     * @param groupProvider The group provider to use to create the groups.
     */
    public OPCEndpoint(
            final IItemDefinitionFactory<ID> itemAddressFactory, 
            final IGroupProvider<ID> groupProvider) {
        this.itemDefinitionFactory = itemAddressFactory;
        this.groupProvider = groupProvider;
    }
    
    /**
     * Returns the current state of the endpoint.
     * 
     * @return The current state of the endpoint.
     */
    @Override
    public synchronized STATE getState() {
        return currentState;
    }
    
    @Override
    public synchronized final void setStateOperational() {
      currentState = STATE.OPERATIONAL;
    }

    /**
     * Adds the provided command tags to this endpoint.
     * 
     * @param commandTags The tags to add.
     */
    @Override
    public void addCommandTags(
            final Collection<ISourceCommandTag> commandTags) {
        for (ISourceCommandTag commandTag : commandTags) {
            addCommandTag(commandTag);
        }
    }
    
    /**
     * Adds the provided command tag to this endpoint.
     * 
     * @param commandTag The tag to add.
     */
    @Override
    public void addCommandTag(final ISourceCommandTag commandTag) {
        HardwareAddress hardwareAddress = commandTag.getHardwareAddress();
        ID address = itemDefinitionFactory.createItemDefinition(
                commandTag.getId(), hardwareAddress);
        if (address != null)
            tagIdsToItemDefinitions.put(commandTag.getId(), address);
        
    }
    
    /**
     * Removes he command tag from this endpoint.
     * 
     * @param commandTag The command tag to remove.
     */
    @Override
    public void removeCommandTag(final ISourceCommandTag commandTag) {
        tagIdsToItemDefinitions.remove(commandTag.getId());
    }

    /**
     * Adds the provided data tags to the endpoint.
     * 
     * @param dataTags The tags to add.
     */
    @Override
    public synchronized void addDataTags(
            final Collection<ISourceDataTag> dataTags) {
        requireState(STATE.INITIALIZED);
        final Collection<SubscriptionGroup<ID>> subscriptionGroups = 
            new HashSet<SubscriptionGroup<ID>>();
        for (ISourceDataTag dataTag : dataTags) {
            SubscriptionGroup<ID> subscriptionGroup = processTag(dataTag);
            if (subscriptionGroup != null)
                subscriptionGroups.add(subscriptionGroup);
        }
        onSubscribe(subscriptionGroups);
    }

    /**
     * Adds a tag to a subscription group and returns that group.
     * 
     * @param dataTag The tag to add.
     * @return The subscription group of this tag.
     */
    private SubscriptionGroup<ID> processTag(final ISourceDataTag dataTag) {
        HardwareAddress hardwareAddress = dataTag.getHardwareAddress();
        ID definition = itemDefinitionFactory.createItemDefinition(
                dataTag.getId(), hardwareAddress);
        SubscriptionGroup<ID> subscriptionGroup = null;
        if (definition != null) {
            subscriptionGroup = 
                groupProvider.getOrCreateGroup(dataTag);
            subscriptionGroup.addDefintion(definition);
            itemDefintionIdsToDataTags.put(definition.getId(), dataTag);
            tagIdsToItemDefinitions.put(dataTag.getId(), definition);
        }
        else {
          logger.warn("processTag() - itemDefinitionFactory returned no item definition -> No subscription to data tag " + dataTag.getId() + " possible!");
        }
        return subscriptionGroup;
    }
    
    /**
     * Called when a data tag should be added to this endpoint.
     * 
     * @param sourceDataTag The data tag to add.
     */
    @Override
    public synchronized void addDataTag(final ISourceDataTag sourceDataTag) {
    	requireState(STATE.INITIALIZED, STATE.OPERATIONAL);
        SubscriptionGroup<ID> subscriptionGroup = processTag(sourceDataTag);
        onSubscribe(subscriptionGroup);
    }
    
    /**
     * Called when a data tag should be removed from this endpoint.
     * 
     * @param dataTag The data tag to remove.
     */
    @Override
    public synchronized void removeDataTag(final ISourceDataTag dataTag) {
    	requireState(STATE.OPERATIONAL);
        ID definition = tagIdsToItemDefinitions.remove(dataTag.getId());
        itemDefintionIdsToDataTags.remove(definition.getId());
        if (definition != null) {
            SubscriptionGroup<ID> subscriptionGroup = 
                groupProvider.getOrCreateGroup(dataTag);
            subscriptionGroup.removeDefintion(definition);
            onRemove(subscriptionGroup, definition);
        }
    }
    /**
     * Refreshes the values for the provided data tags.
     * 
     * @param dataTags The data tags whose values shall be refreshed.
     */
    @Override
    public synchronized void refreshDataTags(
            final Collection<ISourceDataTag> dataTags) {
        requireState(STATE.OPERATIONAL);
        final Collection<ID> itemDefintions = new ArrayList<ID>(dataTags.size());
        for (ISourceDataTag dataTag : dataTags) {
            ID itemDefinition = tagIdsToItemDefinitions.get(dataTag.getId());
            if (itemDefinition != null)
                itemDefintions.add(itemDefinition);
        }
        onRefresh(itemDefintions);
    }

    /**
     * Executes a command.
     * 
     * @param hardwareAddress The configuration of the command to execute.
     * @param command The command to execute.
     */
    @Override
    public synchronized void executeCommand(
            final OPCHardwareAddress hardwareAddress,
            final SourceCommandTagValue command) {
        requireState(STATE.OPERATIONAL);
        ID itemDefintion = 
            tagIdsToItemDefinitions.get(command.getId());
        if (itemDefintion != null) {
            Object value = TypeConverter.cast(
                    command.getValue().toString(), command.getDataType());
            if (value != null)
                switch (hardwareAddress.getCommandType()) {
                case METHOD:
                    onCallMethod(itemDefintion, 
                            command.getValue());
                    break;
                case CLASSIC:
                    int pulseLength = hardwareAddress.getCommandPulseLength();
                    if (pulseLength > 0)
                        writeRewrite(itemDefintion, pulseLength , value);
                    else {
                        onWrite(itemDefintion, value);
                    }
                    break;
                default:
                    throw new OPCCriticalException("Provided command type "
                            + "is unknown.");
                }
            else {
                throw new OPCCriticalException("Provided command value could "
                        + "not be processed. Check data type and value.");
            }
        }
        else {
            throw new OPCCriticalException("Provided command could "
                    + "not be processed.");
        }
    }

    /**
     * Checks the current state and throws an exception if it does not match the
     * argument.
     * 
     * @param requiredState The state which is required at this point.
     */
    private void requireState(STATE... requiredStates) {
      boolean hasState = false;
      for (STATE requiredState : requiredStates) {
        if (currentState == requiredState) {
          hasState = true;
        }
      } 
      
      if (!hasState)
        throw new OPCCriticalException("Endpoint has wrong state!"
                + " Should have at least one of the follwing states: " + Arrays.toString(requiredStates));
    }

    /**
     * Registers an endpoint listener.
     * 
     * @param endpointListener The listener to add.
     */
    @Override
    public void registerEndpointListener(
            final IOPCEndpointListener endpointListener) {
        listeners.add(endpointListener);
    }
    
    /**
     * Unregisters an endpoint listener.
     * 
     * @param endpointListener The endpoint listener to remove.
     */
    @Override
    public void unRegisterEndpointListener(
            final IOPCEndpointListener endpointListener) {
        listeners.remove(endpointListener);
    }
    
    /**
     * Initializes the endpoint. If it is already initialized it will stop,
     * clear all configuration and listeners and reinitialize.
     * 
     * @param address The address to use to initialize the endpoint.
     */
    @Override
    public synchronized void initialize(final OPCUAAddress address) {
        if (currentState == STATE.INITIALIZED) { 
            reset();
        }
        onInit(address);
        currentState = STATE.INITIALIZED;
    }
    
    /**
     * Stops and resets the endpoint completely.
     */
    @Override
    public synchronized void reset() {
        if (currentState != STATE.NOT_INITIALIZED) {
            try {
              onStop();
            }
            catch (Exception ex) {
              logger.error("Exception while stopping endpoint", ex);
            }
            listeners.clear();
            itemDefintionIdsToDataTags.clear();
            tagIdsToItemDefinitions.clear();
            currentState = STATE.NOT_INITIALIZED;
        }
    }
    
    /**
     * Notifies all endpoint listeners about a value change.
     * 
     * @param itemdefintionId The id of the item definition whose value changed.
     * @param timestamp The timestamp of the changed value.
     * @param value The value which changed.
     */
    public void notifyEndpointListenersValueChange(
            final long itemdefintionId,
            final long timestamp, final Object value) {
        ISourceDataTag dataTag = itemDefintionIdsToDataTags.get(itemdefintionId);
        if (dataTag != null) {
          if (!listeners.isEmpty()) {
            for (IOPCEndpointListener listener : listeners) {
              listener.onNewTagValue(dataTag, timestamp, value);
            }
          }
          else {
            LOG.warn("notifyEndpointListenersValueChange() - No endpoint listeners registerd! Nobody got informed about update for datatag " + dataTag.getId());
          }
        }
    }
    
    /**
     * Notifies all endpoint listeners about an error connected to an OPCItem.
     * 
     * @param itemdefintionId The id of the item defintion which caused the 
     * error.
     * @param ex The exception thrown in the endpoint.
     */
    public void notifyEndpointListenersItemError(
            final long itemdefintionId, final Throwable ex) {
        
      ISourceDataTag dataTag = itemDefintionIdsToDataTags.get(itemdefintionId);
      if (dataTag != null) {
        if (!listeners.isEmpty()) {
          for (IOPCEndpointListener listener : listeners) {
            listener.onTagInvalidException(dataTag, ex);
          }
        }
        else {
          LOG.warn("notifyEndpointListenersItemError() - No endpoint listeners registerd! Nobody got informed about invalidation of datatag " + dataTag.getId());
        }
      }
    }
    
    /**
     * Notifies all endpoint listeners about an error connected to 
     * a subscription.
     * 
     * @param ex The exception thrown in the endpoint.
     */
    public void notifyEndpointListenersSubscriptionFailed(
            final Throwable ex) {
      if (!listeners.isEmpty()) {
        for (IOPCEndpointListener listener : listeners) {
            listener.onSubscriptionException(ex);
        }
      }
      else {
        LOG.warn("notifyEndpointListenersSubscriptionFailed() - No endpoint listeners registerd! Nobody gets informed about exeption:", ex);
      }
    }
    
    /**
     * Writes a value to an item and rewrites it after a provided pulse length.
     * 
     * @param itemDefintion The item definition which defines where to write.
     * @param pulseLength The pulse length after which the value should be
     * rewritten.
     * @param value The value to write.
     */
    private void writeRewrite(final ID itemDefintion,
            final int pulseLength, final Object value) {
        onWrite(itemDefintion, Boolean.valueOf(value.toString()));
        try {
            Thread.sleep(pulseLength);
        } catch (InterruptedException e) {
            throw new OPCCriticalException("Sleep Interrupted.");
        }
        finally {
            onWrite(itemDefintion, !Boolean.valueOf(value.toString()));
        }
    }
    
    /**
     * Writes a value to the OPC server.
     * 
     * @param address The address which defines where to write.
     * @param value The value to write.
     */
    @Override
    public synchronized void write(
            final OPCHardwareAddress address, final Object value) {
        requireState(STATE.OPERATIONAL);
        ID itemDefinition = 
            itemDefinitionFactory.createItemDefinition(1L, address);
        if (itemDefinition != null) {
            onWrite(itemDefinition, value);
        }
    }
    
    @Override
    public synchronized void checkConnection() {
        requireState(STATE.INITIALIZED, STATE.OPERATIONAL);
        checkStatus();
    }
    /**
     * Checks the status of the endpoint.
     */
    protected abstract void checkStatus();
    
    /**
     * This method should be the first to be called. It gives the endpoint
     * all properties which are specific to the endpoint type.
     * 
     * @param address The properties specific to this endpoint.
     */
    protected abstract void onInit(OPCUAAddress address);
    
    /**
     * Stops the endpoint and clears all configuration states.
     */
    protected abstract void onStop();

    /**
     * Writes to an item defined by the item definition.
     * 
     * @param itemDefinition The item definition which defines where to write.
     * @param value The value to write.
     */
    protected abstract void onWrite(final ID itemDefinition, final Object value);

    /**
     * Subscribes a collection of subscription groups.
     * 
     * @param subscriptionGroups The subscription groups to subscribe to.
     */
    protected abstract void onSubscribe(
            Collection<SubscriptionGroup<ID>> subscriptionGroups);
    
    /**
     * Subscribes for a subscription group.
     * 
     * @param subscriptionGroup The subscription group to subscribe.
     */
    protected abstract void onSubscribe(SubscriptionGroup<ID> subscriptionGroup);
    
    /**
     * Called when a item is removed from a subscription.
     * 
     * @param subscriptionGroup The group where the item was removed.
     * @param removedDefinition The description which was removed.
     */
    protected abstract void onRemove(
            final SubscriptionGroup<ID> subscriptionGroup,
            final ID removedDefinition);
    
    /**
     * Refreshes the values of a collection of item definitions.
     * 
     * @param itemDefintions The item definitions to refresh.
     */
    protected abstract void onRefresh(Collection<ID> itemDefintions);
    
    /**
     * Executes the command for the provided item definition.
     * 
     * @param itemDefintion The item definition for the command.
     * @param value The values used as parameters for the command.
     */
    protected abstract void onCallMethod(ID itemDefintion, Object ...value);

}

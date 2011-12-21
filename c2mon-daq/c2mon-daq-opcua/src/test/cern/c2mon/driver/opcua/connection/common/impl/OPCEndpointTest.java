package cern.c2mon.driver.opcua.connection.common.impl;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

import org.easymock.Capture;
import org.easymock.classextension.ConstructorArgs;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import cern.c2mon.driver.opcua.OPCAddress;
import cern.c2mon.driver.opcua.connection.common.IGroupProvider;
import cern.c2mon.driver.opcua.connection.common.IItemDefinitionFactory;
import cern.c2mon.driver.opcua.connection.common.IOPCEndpoint;
import cern.c2mon.driver.opcua.connection.common.IOPCEndpointListener;
import cern.c2mon.driver.opcua.connection.common.impl.ItemDefinition;
import cern.c2mon.driver.opcua.connection.common.impl.OPCCriticalException;
import cern.c2mon.driver.opcua.connection.common.impl.OPCEndpoint;
import cern.c2mon.driver.opcua.connection.common.impl.SubscriptionGroup;
import cern.tim.shared.common.ConfigurationException;
import cern.tim.shared.common.datatag.DataTagAddress;
import cern.tim.shared.common.datatag.address.HardwareAddress;
import cern.tim.shared.common.datatag.address.OPCCommandHardwareAddress.COMMAND_TYPE;
import cern.tim.shared.daq.command.ISourceCommandTag;
import cern.tim.shared.daq.command.SourceCommandTag;
import cern.tim.shared.daq.command.SourceCommandTagValue;
import cern.tim.shared.daq.datatag.ISourceDataTag;
import cern.tim.shared.daq.datatag.SourceDataTag;
import ch.cern.tim.shared.datatag.address.impl.OPCHardwareAddressImpl;
import static org.easymock.classextension.EasyMock.*;

public class OPCEndpointTest {

    private IItemDefinitionFactory<ItemDefinition<String>> factory = 
        createMock(IItemDefinitionFactory.class);

    private IGroupProvider provider = createMock(IGroupProvider.class);


    private OPCEndpoint<ItemDefinition<String>> endpoint;

    private OPCAddress address;
        
    @Before
    public void setUp() throws SecurityException, NoSuchMethodException, URISyntaxException {
        endpoint = createMock(OPCEndpoint.class,
                new ConstructorArgs(
                        OPCEndpoint.class.getConstructor(
                                IItemDefinitionFactory.class,
                                IGroupProvider.class), factory, provider),
                OPCEndpoint.class.getDeclaredMethod("onInit", OPCAddress.class),
                OPCEndpoint.class.getDeclaredMethod("onSubscribe", Collection.class),
                OPCEndpoint.class.getDeclaredMethod(
                        "onWrite", ItemDefinition.class, Object.class),
                OPCEndpoint.class.getDeclaredMethod(
                        "onCallMethod", ItemDefinition.class, Object[].class),
                OPCEndpoint.class.getDeclaredMethod("onRefresh", Collection.class));
        
        address =
            new OPCAddress.Builder("http://host/path", 100, 1000)
                .build();
        endpoint.initialize(address);
        reset(endpoint);
    }
    
    @Test
    public void testAddDataTagsEmpty() {
        Collection<ISourceDataTag> dataTags = new ArrayList<ISourceDataTag>();
        
        endpoint.onSubscribe(isA(Collection.class));

        replay(endpoint, factory, provider);
        endpoint.addDataTags(dataTags);
        verify(endpoint, factory, provider);
    }

    @Test
    public void testAddDataTags() throws ConfigurationException {
        Collection<ISourceDataTag> dataTags = new ArrayList<ISourceDataTag>();
        OPCHardwareAddressImpl hwimpl = new OPCHardwareAddressImpl("asd");
        DataTagAddress address = new DataTagAddress(hwimpl);
        SourceDataTag dataTag1 = new SourceDataTag(1L, "asd", false);
        dataTag1.setAddress(address);
        dataTags.add(dataTag1);
        SourceDataTag dataTag2 = new SourceDataTag(2L, "asd", false);
        dataTag2.setAddress(address);
        dataTags.add(dataTag2);

        ItemDefinition<String> itemAddress = new ItemDefinition<String>(1L, "asd");
        expect(factory.createItemDefinition(1L, hwimpl)).andReturn(itemAddress);
        expect(factory.createItemDefinition(2L, hwimpl)).andReturn(itemAddress);
        expect(provider.getOrCreateGroup(dataTag1)).andReturn(new SubscriptionGroup<ItemDefinition<?>>(0, 0.1F));
        expect(provider.getOrCreateGroup(dataTag2)).andReturn(new SubscriptionGroup<ItemDefinition<?>>(0, 0.1F));
        endpoint.onSubscribe(isA(Collection.class));
        
        replay(endpoint, factory, provider);
        endpoint.addDataTags(dataTags);
        verify(endpoint, factory, provider);
    }
    
    @Test
    public void testAddCommandTagsEmpty() {
        Collection<ISourceCommandTag> commandTags = new ArrayList<ISourceCommandTag>();

        replay(endpoint, factory, provider);
        endpoint.addCommandTags(commandTags);
        verify(endpoint, factory, provider);
    }

    @Test
    public void testAddCommandTags() throws ConfigurationException {
        Collection<ISourceCommandTag> commandTags = new ArrayList<ISourceCommandTag>();
        OPCHardwareAddressImpl address = new OPCHardwareAddressImpl("asd");
        SourceCommandTag commandTag1 = new SourceCommandTag(1L, "asd");
        commandTag1.setHardwareAddress(address);
        commandTags.add(commandTag1);
        SourceCommandTag commandTag2 = new SourceCommandTag(2L, "asd");
        commandTag2.setHardwareAddress(address);
        commandTags.add(commandTag2);

        ItemDefinition<String> itemAddress = new ItemDefinition<String>(1L, "asd");
        expect(factory.createItemDefinition(1L, address)).andReturn(itemAddress);
        expect(factory.createItemDefinition(2L, address)).andReturn(itemAddress);

        replay(endpoint, factory, provider);
        endpoint.addCommandTags(commandTags);
        verify(endpoint, factory, provider);
    }
    
    @Test
    public void testNotifyEndPointListenersValueChange() throws ConfigurationException {
        IOPCEndpointListener endpointListener = 
            createMock(IOPCEndpointListener.class);
        Collection<ISourceDataTag> dataTags = 
            new ArrayList<ISourceDataTag>();
        OPCHardwareAddressImpl hwimpl = new OPCHardwareAddressImpl("asd");
        DataTagAddress address = new DataTagAddress(hwimpl);
        SourceDataTag dataTag = new SourceDataTag(1L, "asd", false);
        dataTag.setAddress(address);
        dataTags.add(dataTag);
        
        ItemDefinition<String> itemAddress = new ItemDefinition<String>(1L, "asd");
        expect(factory.createItemDefinition(1L, hwimpl)).andReturn(itemAddress);
        expect(provider.getOrCreateGroup(dataTag)).andReturn(
                new SubscriptionGroup<ItemDefinition<?>>(0, 0.1F));
        // expected once
        endpointListener.onNewTagValue(dataTag, 2L, "value");
        endpoint.onSubscribe(isA(Collection.class));
        
        replay(endpoint, endpointListener, factory, provider);
        endpoint.addDataTags(dataTags);
        endpoint.registerEndpointListener(endpointListener);
        endpoint.notifyEndpointListenersValueChange(1L, 2L, "value");
        endpoint.unRegisterEndpointListener(endpointListener);
        endpoint.notifyEndpointListenersValueChange(1L, 2L, "value");
        verify(endpoint, endpointListener, factory, provider);
    }
    
    @Test
    public void testNotifyEndPointListenersItemError() throws ConfigurationException {
        IOPCEndpointListener endpointListener = 
            createMock(IOPCEndpointListener.class);
        Collection<ISourceDataTag> dataTags = 
            new ArrayList<ISourceDataTag>();
        OPCHardwareAddressImpl hwimpl = new OPCHardwareAddressImpl("asd");
        DataTagAddress address = new DataTagAddress(hwimpl);
        SourceDataTag dataTag = new SourceDataTag(1L, "asd", false);
        dataTag.setAddress(address);
        dataTags.add(dataTag);
        
        ItemDefinition<String> itemAddress = new ItemDefinition<String>(1L, "asd");
        expect(factory.createItemDefinition(1L, hwimpl)).andReturn(itemAddress);
        expect(provider.getOrCreateGroup(dataTag)).andReturn(
                new SubscriptionGroup<ItemDefinition<?>>(0, 0.1F));
        // expected once
        Exception ex = new Exception();
        endpointListener.onTagInvalidException(dataTag, ex);
        endpoint.onSubscribe(isA(Collection.class));
        
        replay(endpoint, endpointListener, factory, provider);
        endpoint.addDataTags(dataTags);
        endpoint.registerEndpointListener(endpointListener);
        endpoint.notifyEndpointListenersItemError(1L, ex);
        endpoint.unRegisterEndpointListener(endpointListener);
        endpoint.notifyEndpointListenersItemError(1L, ex);
        verify(endpoint, endpointListener, factory, provider);
    }
    
    @Test
    public void testNotifyEndPointListenersSubscriptionError() throws ConfigurationException {
        IOPCEndpointListener endpointListener = 
            createMock(IOPCEndpointListener.class);
        
        // expected once
        Exception ex = new Exception();
        endpointListener.onSubscriptionException(ex);
        
        replay(endpointListener);
        endpoint.registerEndpointListener(endpointListener);
        endpoint.notifyEndpointListenersSubscriptionFailed(ex);
        endpoint.unRegisterEndpointListener(endpointListener);
        endpoint.notifyEndpointListenersSubscriptionFailed(ex);
        verify(endpointListener);
    }
    
    @Test
    public void testRefresh() throws ConfigurationException {
        Collection<ISourceDataTag> dataTags = new ArrayList<ISourceDataTag>();
        OPCHardwareAddressImpl hwimpl = new OPCHardwareAddressImpl("asd");
        DataTagAddress address = new DataTagAddress(hwimpl);
        SourceDataTag dataTag1 = new SourceDataTag(1L, "asd", false);
        dataTag1.setAddress(address);
        dataTags.add(dataTag1);
        SourceDataTag dataTag2 = new SourceDataTag(2L, "asd", false);
        dataTag2.setAddress(address);
        dataTags.add(dataTag2);
        
        ItemDefinition<String> itemAddress = new ItemDefinition<String>(1L, "asd");
        expect(factory.createItemDefinition(1L, hwimpl)).andReturn(itemAddress);
        expect(factory.createItemDefinition(2L, hwimpl)).andReturn(itemAddress);
        expect(provider.getOrCreateGroup(dataTag1)).andReturn(new SubscriptionGroup<ItemDefinition<?>>(0, 0.1F));
        expect(provider.getOrCreateGroup(dataTag2)).andReturn(new SubscriptionGroup<ItemDefinition<?>>(0, 0.1F));
        endpoint.onSubscribe(isA(Collection.class));
        Capture<Collection<ItemDefinition<String>>> capture =
            new Capture<Collection<ItemDefinition<String>>>();
        endpoint.onRefresh(capture(capture));
        
        replay(endpoint, factory, provider);
        endpoint.addDataTags(dataTags);
        endpoint.setStateOperational();
        endpoint.refreshDataTags(dataTags);
        verify(endpoint, factory, provider);
        assertEquals(2, capture.getValue().size());
    }
    
    @Test
    public void testRefreshUnknownTags() throws ConfigurationException {
        Collection<ISourceDataTag> dataTags = new ArrayList<ISourceDataTag>();
        OPCHardwareAddressImpl hwimpl = new OPCHardwareAddressImpl("asd");
        DataTagAddress address = new DataTagAddress(hwimpl);
        SourceDataTag dataTag1 = new SourceDataTag(1L, "asd", false);
        dataTag1.setAddress(address);
        dataTags.add(dataTag1);
        SourceDataTag dataTag2 = new SourceDataTag(2L, "asd", false);
        dataTag2.setAddress(address);
        dataTags.add(dataTag2);
        
        Capture<Collection<ItemDefinition<String>>> capture =
            new Capture<Collection<ItemDefinition<String>>>();
        endpoint.onRefresh(capture(capture));
        
        replay(endpoint, factory, provider);
        endpoint.setStateOperational();
        endpoint.refreshDataTags(dataTags);
        verify(endpoint, factory, provider);
        assertEquals(0, capture.getValue().size());
    }
    
    @Test
    public void testExecuteCommandWrite() throws ConfigurationException {
        OPCHardwareAddressImpl address = new OPCHardwareAddressImpl("asd");
        address.setCommandType(COMMAND_TYPE.CLASSIC);
        Object value = "value";
        SourceCommandTagValue sctValue =
            new SourceCommandTagValue(1L, "asd", 1L, (short) 0, value , "String");
        ISourceCommandTag commandTag =
            new SourceCommandTag(1L, "asd", 100, 1000, address );
        
        expect(factory.createItemDefinition(1L, address))
            .andReturn(new ItemDefinition<String>(1L, "asd"));
        endpoint.onWrite(isA(ItemDefinition.class), eq(value));
        
        replay(endpoint, factory);
        endpoint.addCommandTag(commandTag);
        endpoint.setStateOperational();
        endpoint.executeCommand(address, sctValue);
        verify(endpoint, factory);
    }
    
    @Test
    public void testExecuteCommandWriteReWrite() throws ConfigurationException, InterruptedException {
        OPCHardwareAddressImpl address = new OPCHardwareAddressImpl("asd", 1);
        address.setCommandType(COMMAND_TYPE.CLASSIC);
        Object value = true;
        SourceCommandTagValue sctValue =
            new SourceCommandTagValue(1L, "asd", 1L, (short) 0, value , "Boolean");
        ISourceCommandTag commandTag =
            new SourceCommandTag(1L, "asd", 100, 1000, address );
        
        expect(factory.createItemDefinition(1L, address))
            .andReturn(new ItemDefinition<String>(1L, "asd"));
        endpoint.onWrite(isA(ItemDefinition.class), eq(true));
        endpoint.onWrite(isA(ItemDefinition.class), eq(false));
        
        replay(endpoint, factory);
        endpoint.addCommandTag(commandTag);
        endpoint.setStateOperational();
        endpoint.executeCommand(address, sctValue);
        Thread.sleep(5);
        verify(endpoint, factory);
    }
    
    @Test
    public void testExecuteCommandMethod() throws ConfigurationException, InterruptedException {
        OPCHardwareAddressImpl address = new OPCHardwareAddressImpl("asd", 1);
        address.setCommandType(COMMAND_TYPE.METHOD);
        Object value = true;
        SourceCommandTagValue sctValue =
            new SourceCommandTagValue(1L, "asd", 1L, (short) 0, value , "Boolean");
        ISourceCommandTag commandTag =
            new SourceCommandTag(1L, "asd", 100, 1000, address );
        
        expect(factory.createItemDefinition(1L, address))
            .andReturn(new ItemDefinition<String>(1L, "asd"));
        endpoint.onCallMethod(isA(ItemDefinition.class), eq(true));
        
        replay(endpoint, factory);
        endpoint.addCommandTag(commandTag);
        endpoint.setStateOperational();
        endpoint.executeCommand(address, sctValue);
        verify(endpoint, factory);
    }
    
    @Test(expected=OPCCriticalException.class)
    public void testExecuteCommandParseError() throws ConfigurationException {
        OPCHardwareAddressImpl address = new OPCHardwareAddressImpl("asd", 1);
        address.setCommandType(COMMAND_TYPE.METHOD);
        Object value = true;
        SourceCommandTagValue sctValue =
            new SourceCommandTagValue(1L, "asd", 1L, (short) 0, value , "asd");
        ISourceCommandTag commandTag =
            new SourceCommandTag(1L, "asd", 100, 1000, address );
        
        expect(factory.createItemDefinition(1L, address))
            .andReturn(new ItemDefinition<String>(1L, "asd"));
        
        replay(factory);
        endpoint.addCommandTag(commandTag);
        verify(factory);
        endpoint.executeCommand(address, sctValue);
        
    }
    
    @Test(expected=OPCCriticalException.class)
    public void testExecuteCommandUnknown() throws ConfigurationException {
        OPCHardwareAddressImpl address = new OPCHardwareAddressImpl("asd", 1);
        address.setCommandType(COMMAND_TYPE.METHOD);
        Object value = true;
        SourceCommandTagValue sctValue =
            new SourceCommandTagValue(1L, "asd", 1L, (short) 0, value , "Boolean");
        
        endpoint.executeCommand(address, sctValue);
    }
    
    @Test(expected=OPCCriticalException.class)
    public void testState() {
        assertEquals(endpoint.getState(), IOPCEndpoint.STATE.INITIALIZED);
        endpoint.onStop();
        endpoint.onInit(address);
        replay(endpoint);
        endpoint.initialize(address);
        assertEquals(endpoint.getState(), IOPCEndpoint.STATE.INITIALIZED);
        verify(endpoint);
        reset(endpoint);
        endpoint.reset();
        assertEquals(endpoint.getState(), IOPCEndpoint.STATE.NOT_INITIALIZED);
        // critical exception (wrong state)
        endpoint.executeCommand(null, null);
    }
    
    @Test(expected=OPCCriticalException.class)
    public void testAddRemoveCommandTag() throws ConfigurationException {
        OPCHardwareAddressImpl address = new OPCHardwareAddressImpl("asd", 1);
        address.setCommandType(COMMAND_TYPE.METHOD);
        Object value = true;
        SourceCommandTagValue sctValue =
            new SourceCommandTagValue(1L, "asd", 1L, (short) 0, value , "Boolean");
        ISourceCommandTag commandTag =
            new SourceCommandTag(1L, "asd", 100, 1000, address );
        
        expect(factory.createItemDefinition(1L, address))
            .andReturn(new ItemDefinition<String>(1L, "asd"));
        endpoint.onCallMethod(isA(ItemDefinition.class), eq(true));
        
        replay(endpoint, factory);
        endpoint.addCommandTag(commandTag);
        endpoint.executeCommand(address, sctValue);
        verify(endpoint, factory);
        
        endpoint.removeCommandTag(commandTag);
        // should now fail
        endpoint.executeCommand(address, sctValue);
    }
    
    @Test
    public void testWrite() throws ConfigurationException {
        OPCHardwareAddressImpl hwimpl = new OPCHardwareAddressImpl("asd");
        
        ItemDefinition<String> itemAddress = new ItemDefinition<String>(1L, "asd");
        expect(factory.createItemDefinition(1L, hwimpl)).andReturn(itemAddress);
        endpoint.onWrite(isA(ItemDefinition.class), eq(true));
        
        replay(factory, endpoint);
        endpoint.setStateOperational();
        endpoint.write(hwimpl, true);
        verify(factory, endpoint);
    }
}

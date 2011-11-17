package cern.c2mon.driver.opcua;

import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.driver.opcua.EndpointController;
import cern.c2mon.driver.opcua.EndpointEquipmentLogListener;
import cern.c2mon.driver.opcua.EndpointTypesUnknownException;
import cern.c2mon.driver.opcua.OPCAddress;
import cern.c2mon.driver.opcua.connection.common.IOPCEndpoint;
import cern.c2mon.driver.opcua.connection.common.IOPCEndpointFactory;
import cern.c2mon.driver.opcua.connection.common.IOPCEndpoint.STATE;
import cern.tim.driver.common.EquipmentLogger;
import cern.tim.driver.common.EquipmentLoggerFactory;
import cern.tim.driver.common.IEquipmentMessageSender;
import cern.tim.driver.common.conf.equipment.IEquipmentConfiguration;
import cern.tim.shared.common.ConfigurationException;
import cern.tim.shared.common.datatag.DataTagAddress;
import cern.tim.shared.common.datatag.DataTagQuality;
import cern.tim.shared.common.datatag.address.HardwareAddress;
import cern.tim.shared.common.datatag.address.OPCHardwareAddress;
import cern.tim.shared.daq.command.ISourceCommandTag;
import cern.tim.shared.daq.command.SourceCommandTag;
import cern.tim.shared.daq.command.SourceCommandTagValue;
import cern.tim.shared.daq.config.ChangeReport;
import cern.tim.shared.daq.config.ChangeReport.CHANGE_STATE;
import cern.tim.shared.daq.datatag.ISourceDataTag;
import cern.tim.shared.daq.datatag.SourceDataQuality;
import cern.tim.shared.daq.datatag.SourceDataTag;
import ch.cern.tim.shared.datatag.address.impl.OPCHardwareAddressImpl;

public class EndpointControllerTest {
    
    private IOPCEndpointFactory factory = createMock(IOPCEndpointFactory.class);
    
    private IOPCEndpoint endpoint = createMock(IOPCEndpoint.class);
    
    private IEquipmentMessageSender sender =
        createMock(IEquipmentMessageSender.class);
    
    private IEquipmentConfiguration conf =
        createMock(IEquipmentConfiguration.class);

    private OPCAddress opcAddress;

    private EndpointController controller;

    private Map<Long, ISourceDataTag> sourceDataTags;

    private Map<Long, ISourceCommandTag> sourceCommandTags;
    
    private volatile Throwable error;

    private ArrayList<OPCAddress> addresses;
    
    @Before
    public void setUp() throws URISyntaxException {
        opcAddress = new OPCAddress.Builder(
                "test://somhost/somepath", 1000, 1000)
                .build();
        addresses = new ArrayList<OPCAddress>();
        addresses.add(opcAddress);
        sourceDataTags =
            new HashMap<Long, ISourceDataTag>();
        sourceCommandTags =
            new HashMap<Long, ISourceCommandTag>();
        controller = new EndpointController(
                factory, sender, new EquipmentLoggerFactory("asd", 1L, "asd", "asd", false, false),
                addresses, conf);
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                error = e;
            }
        });
//        reset(endpoint);
    }
    
    @After
    public void tearDown() throws Throwable {
        if (error != null)
            throw error;
        try {
        	reset(endpoint);
        	endpoint.reset();
        }
        catch (Exception e) {
        	
        }
    }
    
    @Test
    public void testStart() throws URISyntaxException {
        expect(factory.createEndpoint("test")).andReturn(endpoint);
        expect(conf.getSourceDataTags()).andReturn(sourceDataTags).anyTimes();
        expect(conf.getSourceCommandTags()).andReturn(sourceCommandTags).anyTimes();
        expect(conf.getAliveTagId()).andReturn(1L);
        expect(conf.getSourceDataTag(1L)).andReturn(null);
        endpoint.initialize(opcAddress);
        endpoint.addDataTags(sourceDataTags.values());
        endpoint.addCommandTags(sourceCommandTags.values());
        endpoint.registerEndpointListener(controller);
        endpoint.registerEndpointListener(
                isA(EndpointEquipmentLogListener.class));
        
        replay(factory, endpoint, conf);
        controller.startEndpoint();
        verify(factory, endpoint, conf);
    }
    
    @Test(expected=EndpointTypesUnknownException.class)
    public void testUnknownEndpoint() throws URISyntaxException {
        expect(factory.createEndpoint("test")).andReturn(null);
        
        controller.startEndpoint();
    }
    
    @Test
    public void testOnNewTagValue() {
        DataTagAddress address = new DataTagAddress();
        ISourceDataTag currentTag = new SourceDataTag(1L, "", false, (short) 0, "String", address );
        Object tagValue = "asd";
        long milisecTimestamp = 1L;
        expect(sender.sendTagFiltered(currentTag, tagValue, milisecTimestamp))
            .andReturn(true);
        
        replay(sender);
        controller.onNewTagValue(currentTag, milisecTimestamp, tagValue);
        verify(sender);
    }

    @Test
    public void testOnSubscriptionException() throws InterruptedException {
        initializeEndpoint();
        
        //stop
        expect(endpoint.getState()).andReturn(STATE.NOT_INITIALIZED);
        endpoint.reset();
        sender.confirmEquipmentStateIncorrect();
        
        // restart
        expect(endpoint.getState()).andReturn(STATE.NOT_INITIALIZED);
        expect(factory.createEndpoint("test")).andReturn(endpoint);
        expect(conf.getSourceDataTags()).andReturn(sourceDataTags).anyTimes();
        expect(conf.getSourceCommandTags()).andReturn(sourceCommandTags).anyTimes();
        expect(conf.getAliveTagId()).andReturn(1L);
        expect(conf.getSourceDataTag(1L)).andReturn(null);
        endpoint.initialize(opcAddress);
        endpoint.addDataTags(sourceDataTags.values());
        endpoint.addCommandTags(sourceCommandTags.values());
        endpoint.registerEndpointListener(controller);
        endpoint.registerEndpointListener(
                isA(EndpointEquipmentLogListener.class));
        sender.confirmEquipmentStateOK();
        endpoint.checkConnection();
        expectLastCall().anyTimes();
        //refresh
        expect(endpoint.getState()).andReturn(STATE.INITIALIZED);
        endpoint.refreshDataTags(sourceDataTags.values());
        
        // next in while loop
        expect(endpoint.getState()).andReturn(STATE.INITIALIZED);
        
        replay(endpoint, sender, factory, conf);
        controller.onSubscriptionException(new Exception());
        Thread.sleep(100L + 1000L);
        verify(endpoint, sender, factory, conf);
    }
    
    @Test
    public void testFirstEndpointInvalid() throws URISyntaxException {
        addresses.add(
                new OPCAddress.Builder(
                        "http://host/somepath", 100, 1000).build());
        expect(conf.getSourceDataTags()).andReturn(sourceDataTags).anyTimes();
        expect(conf.getSourceCommandTags()).andReturn(sourceCommandTags).anyTimes();
        expect(conf.getAliveTagId()).andReturn(1L);
        expect(conf.getSourceDataTag(1L)).andReturn(null);
        expect(factory.createEndpoint("test")).andReturn(null);
        expect(factory.createEndpoint("http")).andReturn(endpoint);
        
        replay(factory, conf);
        controller.startEndpoint();
        verify(factory, conf);
    }

    /**
     * 
     */
    private void initializeEndpoint() {
        // just to get it to initialized state
        expect(factory.createEndpoint("test")).andReturn(endpoint);
        expect(conf.getSourceDataTags()).andReturn(sourceDataTags).anyTimes();
        expect(conf.getSourceCommandTags()).andReturn(sourceCommandTags).anyTimes();
        expect(conf.getAliveTagId()).andReturn(1L);
        expect(conf.getSourceDataTag(1L)).andReturn(null);
        replay(factory, conf);
        controller.startEndpoint();
        controller.stopStatusChecker();
        controller.stopAliveTimer();
        verify(factory, conf);
        reset(endpoint, sender, factory, conf);
    }
    
    @Test
    public void testOnTagInvalidException() {
        ISourceDataTag dataTag = new SourceDataTag(1L, "asd", false);
        Throwable cause = new Exception("The cause of the problem");
        
        sender.sendInvalidTag(eq(dataTag), eq((short) SourceDataQuality.DATA_UNAVAILABLE),
                eq(cause.getMessage()));
        
        replay(sender);
        controller.onTagInvalidException(dataTag, cause);
        verify(sender);
    }
    
    @Test
    public void testRefresh() {
        initializeEndpoint();
        
        expect(endpoint.getState()).andReturn(STATE.INITIALIZED);
        endpoint.refreshDataTags(sourceDataTags.values());
        expect(conf.getSourceDataTags()).andReturn(sourceDataTags);
        
        replay(endpoint, conf);
        controller.refresh();
        verify(endpoint, conf);
    }
    
    @Test
    public void testRefreshTag() {
        initializeEndpoint();
        
        expect(endpoint.getState()).andReturn(STATE.INITIALIZED);
        Collection<ISourceDataTag> tags = new ArrayList<ISourceDataTag>(1);
        ISourceDataTag tag = new SourceDataTag(1L, "asd", false);
        tags.add(tag);
        endpoint.refreshDataTags(tags);
        
//        endpoint.checkConnection();
//        expectLastCall().anyTimes();
        
        replay(endpoint);
        controller.refresh(tag);
        verify(endpoint);
    }
    
    @Test
    public void testRunCommand() throws ConfigurationException {
        initializeEndpoint();
        
        OPCHardwareAddress hardwareAddress = new OPCHardwareAddressImpl("sd");
        ISourceCommandTag commandTag =
            new SourceCommandTag(1L, "asd", 100, 1000, hardwareAddress);
        SourceCommandTagValue sourceCommandTagValue = new SourceCommandTagValue();
        
        expect(endpoint.getState()).andReturn(STATE.INITIALIZED);
        endpoint.executeCommand(hardwareAddress, sourceCommandTagValue);
        
//        endpoint.checkConnection();
//        expectLastCall().anyTimes();
        
        replay(endpoint);
        controller.runCommand(commandTag, sourceCommandTagValue);
        verify(endpoint);
    }
    
    @Test
    public void testStartStopAliveTimer() {
        initializeEndpoint();
        
        expect(conf.getAliveTagId()).andReturn(1L);
        expect(conf.getSourceDataTag(1L)).andReturn(
                new SourceDataTag(1L, "asd", true));
        expect(conf.getAliveTagInterval()).andReturn(1000L);
        
        replay(conf);
        controller.startAliveTimer();
        controller.stopAliveTimer();
        verify(conf);
    }
    
    @Test
    public void testOnAddCommandTag() {
        initializeEndpoint();
        
        expect(endpoint.getState()).andReturn(STATE.INITIALIZED);
        ISourceCommandTag commandTag = new SourceCommandTag(1L, "asd");
        endpoint.addCommandTag(commandTag);
        
//        endpoint.checkConnection();
//        expectLastCall().anyTimes();
        
        replay(endpoint);
        ChangeReport changeReport = new ChangeReport(1L);
        controller.onAddCommandTag(commandTag, changeReport);
        assertEquals(changeReport.getState(), CHANGE_STATE.SUCCESS);
        verify(endpoint);
    }
    
    @Test
    public void testOnRemoveCommandTag() {
        initializeEndpoint();
        
        expect(endpoint.getState()).andReturn(STATE.INITIALIZED);
        ISourceCommandTag commandTag = new SourceCommandTag(1L, "asd");
        endpoint.removeCommandTag(commandTag);
        
        endpoint.checkConnection();
        expectLastCall().anyTimes();
        
        replay(endpoint);
        ChangeReport changeReport = new ChangeReport(1L);
        controller.onRemoveCommandTag(commandTag, changeReport);
        assertEquals(changeReport.getState(), CHANGE_STATE.SUCCESS);
        verify(endpoint);
    }
    
    @Test
    public void testOnUpdateCommandTagHardwareAddressChanged() throws ConfigurationException {
        initializeEndpoint();
        
        expect(endpoint.getState()).andReturn(STATE.INITIALIZED);
        HardwareAddress address = new OPCHardwareAddressImpl("asd");
        ISourceCommandTag oldTag = new SourceCommandTag(1L, "asd", 100, 1000, address);
        HardwareAddress address2 = new OPCHardwareAddressImpl("asd2");
        ISourceCommandTag newTag = new SourceCommandTag(1L, "asd", 100, 1000, address2);
        endpoint.removeCommandTag(oldTag);
        endpoint.addCommandTag(newTag);
        
        replay(endpoint);
        ChangeReport changeReport = new ChangeReport(1L);
        controller.onUpdateCommandTag(newTag, oldTag, changeReport);
        assertEquals(changeReport.getState(), CHANGE_STATE.SUCCESS);
        verify(endpoint);
    }
    
    @Test
    public void testOnUpdateCommandTagNothingChanged() throws ConfigurationException {
        initializeEndpoint();
        
        expect(endpoint.getState()).andReturn(STATE.INITIALIZED);
        HardwareAddress address = new OPCHardwareAddressImpl("asd");
        ISourceCommandTag oldTag = new SourceCommandTag(1L, "asd", 100, 1000, address);
        ISourceCommandTag newTag = new SourceCommandTag(1L, "asd", 100, 1000, address);
        
        endpoint.checkConnection();
        expectLastCall().anyTimes();
        
        replay(endpoint);
        ChangeReport changeReport = new ChangeReport(1L);
        controller.onUpdateCommandTag(newTag, oldTag, changeReport);
        assertEquals(changeReport.getState(), CHANGE_STATE.SUCCESS);
        verify(endpoint);
    }
    
    @Test
    public void testOnAddDataTag() {
        initializeEndpoint();
        
        expect(endpoint.getState()).andReturn(STATE.INITIALIZED).anyTimes();
        ISourceDataTag dataTag = new SourceDataTag(1L, "asd", false);
        endpoint.addDataTag(dataTag);
        endpoint.refreshDataTags(isA(Collection.class));
        
        replay(endpoint);
        ChangeReport changeReport = new ChangeReport(1L);
        controller.onAddDataTag(dataTag, changeReport);
        assertEquals(changeReport.getState(), CHANGE_STATE.SUCCESS);
        verify(endpoint);
    }
    
    @Test
    public void testOnRemoveDataTag() {
        initializeEndpoint();
        
        expect(endpoint.getState()).andReturn(STATE.INITIALIZED);
        ISourceDataTag dataTag = new SourceDataTag(1L, "asd", false);
        endpoint.removeDataTag(dataTag);
        
        replay(endpoint);
        ChangeReport changeReport = new ChangeReport(1L);
        controller.onRemoveDataTag(dataTag, changeReport);
        assertEquals(changeReport.getState(), CHANGE_STATE.SUCCESS);
        verify(endpoint);
    }
    
    @Test
    public void testOnUpdateDataTagHardwareAddressChanged() throws ConfigurationException {
        initializeEndpoint();
        
        expect(endpoint.getState()).andReturn(STATE.INITIALIZED);
        HardwareAddress address = new OPCHardwareAddressImpl("asd");
        SourceDataTag oldTag = new SourceDataTag(1L, "asd", false);
        oldTag.setAddress(new DataTagAddress(address));
        HardwareAddress address2 = new OPCHardwareAddressImpl("asd2");
        SourceDataTag newTag = new SourceDataTag(1L, "asd", false);
        newTag.setAddress(new DataTagAddress(address2));
        endpoint.removeDataTag(oldTag);
        endpoint.addDataTag(newTag);
        
        replay(endpoint);
        ChangeReport changeReport = new ChangeReport(1L);
        controller.onUpdateDataTag(newTag, oldTag, changeReport);
        assertEquals(changeReport.getState(), CHANGE_STATE.SUCCESS);
        verify(endpoint);
    }
    
    @Test
    public void testOnUpdateDataTagNothingChanged() throws ConfigurationException {
        initializeEndpoint();
        
        expect(endpoint.getState()).andReturn(STATE.INITIALIZED);
        HardwareAddress address = new OPCHardwareAddressImpl("asd");
        SourceDataTag oldTag = new SourceDataTag(1L, "asd", false);
        oldTag.setAddress(new DataTagAddress(address));
        SourceDataTag newTag = new SourceDataTag(1L, "asd", false);
        newTag.setAddress(new DataTagAddress(address));
        
        replay(endpoint);
        ChangeReport changeReport = new ChangeReport(1L);
        controller.onUpdateDataTag(newTag, oldTag, changeReport);
        assertEquals(changeReport.getState(), CHANGE_STATE.SUCCESS);
        verify(endpoint);
    }
    
    @Test
    public void testStop() throws ConfigurationException {
        initializeEndpoint();
        
        endpoint.reset();
        
        replay(endpoint);
        controller.stop();
        verify(endpoint);
    }
}

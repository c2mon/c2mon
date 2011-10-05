package cern.c2mon.driver.opcua.connection.soap;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.easymock.Capture;
import org.junit.Test;
import org.opcfoundation.xmlda.OPCXML_DataAccess;
import org.opcfoundation.xmlda.OPCXML_DataAccessStub;
import org.opcfoundation.xmlda.Subscribe;
import org.opcfoundation.xmlda.SubscribeResponse;

import cern.c2mon.driver.opcua.OPCAddress;
import cern.c2mon.driver.opcua.connection.common.IGroupProvider;
import cern.c2mon.driver.opcua.connection.common.IItemDefinitionFactory;
import cern.c2mon.driver.opcua.connection.common.impl.OPCCommunicationException;
import cern.c2mon.driver.opcua.connection.common.impl.OPCCriticalException;
import cern.c2mon.driver.opcua.connection.common.impl.SubscriptionGroup;
import cern.c2mon.driver.opcua.connection.soap.DASoapEndpoint;
import cern.c2mon.driver.opcua.connection.soap.DASoapItemDefintion;

public class DASoapEndpointTest {
    
    private IItemDefinitionFactory<DASoapItemDefintion> itemAddressFactory =
        createMock(IItemDefinitionFactory.class);
    private IGroupProvider groupProvider = createMock(IGroupProvider.class);
    
    private OPCXML_DataAccess soapAccess = 
        createMock(OPCXML_DataAccess.class);
    
    private DASoapEndpoint endpoint = 
        new DASoapEndpoint(itemAddressFactory, groupProvider);
    
//    @Test
//    public void testSubscribe() throws RemoteException, URISyntaxException {
//        Collection<SubscriptionGroup<DASoapItemDefintion>> subscriptionGroups =
//            new ArrayList<SubscriptionGroup<DASoapItemDefintion>>();
//        SubscriptionGroup<DASoapItemDefintion> subscriptionGroup =
//            new SubscriptionGroup<DASoapItemDefintion>(1000, 1.0f);
//        subscriptionGroup.addDefintion(new DASoapItemDefintion(1L, "asda", "red"));
//        subscriptionGroup.addDefintion(new DASoapItemDefintion(2L, "asda"));
//        subscriptionGroups.add(subscriptionGroup);
//        OPCAddress address = new OPCAddress.Builder(
//                "http://somehost/wsdl", 100, 1000)
//                .build();
//        endpoint.initialize(address);
//        endpoint.setDataAccess(soapAccess);
//        
//        Capture<Subscribe> subscribeCapture = new Capture<Subscribe>();
//        expect(soapAccess.subscribe(capture(subscribeCapture)))
//            .andReturn(new SubscribeResponse());
//        
//        replay(soapAccess);
//        endpoint.onSubscribe(subscriptionGroups);
//        verify(soapAccess);
//        Subscribe subscribe = subscribeCapture.getValue();
//        assertEquals(3, subscribe.getItemList().getItems().length);
//    }
    
    @Test(expected=OPCCriticalException.class)
    public void testMalformedURL() throws URISyntaxException {
        OPCAddress address = new OPCAddress.Builder(
                "dcom://asd/asd", 100, 1000)
                .build();
        endpoint.initialize(address);
    }

}

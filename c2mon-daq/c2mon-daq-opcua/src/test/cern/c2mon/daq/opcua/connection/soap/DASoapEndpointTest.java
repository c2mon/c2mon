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
package cern.c2mon.daq.opcua.connection.soap;

import static org.easymock.EasyMock.createMock;

import java.net.URISyntaxException;

import org.junit.Test;
import org.opcfoundation.xmlda.OPCXML_DataAccess;

import cern.c2mon.daq.opcua.connection.common.IGroupProvider;
import cern.c2mon.daq.opcua.connection.common.IItemDefinitionFactory;
import cern.c2mon.daq.opcua.connection.common.impl.OPCCriticalException;
import cern.c2mon.daq.opcua.connection.common.impl.OPCUADefaultAddress;

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
        OPCUADefaultAddress address = new OPCUADefaultAddress.DefaultBuilder(
                "dcom://asd/asd", 100, 1000)
                .build();
        endpoint.initialize(address);
    }

}

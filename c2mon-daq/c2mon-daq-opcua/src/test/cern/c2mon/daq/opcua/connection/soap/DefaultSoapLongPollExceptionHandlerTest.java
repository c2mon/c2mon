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

import static org.easymock.classextension.EasyMock.*;

import java.lang.Thread.UncaughtExceptionHandler;
import java.rmi.RemoteException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.daq.opcua.connection.common.impl.SubscriptionGroup;
import cern.c2mon.daq.opcua.connection.soap.DASoapEndpoint;
import cern.c2mon.daq.opcua.connection.soap.DefaultSoapLongPollExceptionHandler;
import cern.c2mon.daq.opcua.connection.soap.SoapLongPoll;

public class DefaultSoapLongPollExceptionHandlerTest {
    
    private static final long SLEEP_TIME = 100L;

    private DASoapEndpoint soapEndpoint =
        createMock(DASoapEndpoint.class);
    
    private SoapLongPoll poll = createMock(SoapLongPoll.class);
    
    private SubscriptionGroup<DASoapItemDefintion> group =
        new SubscriptionGroup<DASoapItemDefintion>(100, 1000);
    
    private DefaultSoapLongPollExceptionHandler handler =
        new DefaultSoapLongPollExceptionHandler(soapEndpoint, group, 10L);
    
    private volatile Throwable error;
    
    @Before
    public void setUp() {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                error = e;
            }
        });
    }
    
    @After
    public void tearDown() throws Throwable {
        if (error != null)
            throw error;
    }
    
    @Test
    public void testRestartPolling() 
        throws RemoteException, InterruptedException {
        Throwable exception = new Exception();
        
        soapEndpoint.onSubscribe(group);
        
        replay(poll);
        handler.onConnectionException(exception, poll);
        Thread.sleep(SLEEP_TIME);
        verify(poll);
    }
    
    @Test
    public void testRestartPollingToMuchErrors() 
        throws RemoteException, InterruptedException {
        Throwable exception = new Exception();
        
        soapEndpoint.notifyEndpointListenersSubscriptionFailed(exception);
        
        replay(poll);
        handler.onConnectionException(exception, poll);
        handler.onConnectionException(exception, poll);
        handler.onConnectionException(exception, poll);
        Thread.sleep(SLEEP_TIME);
        verify(poll);
    }

}

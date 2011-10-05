package cern.c2mon.driver.opcua.connection.soap;

import static org.easymock.classextension.EasyMock.*;

import java.lang.Thread.UncaughtExceptionHandler;
import java.rmi.RemoteException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.driver.opcua.connection.common.impl.SubscriptionGroup;
import cern.c2mon.driver.opcua.connection.soap.DASoapEndpoint;
import cern.c2mon.driver.opcua.connection.soap.DefaultSoapLongPollExceptionHandler;
import cern.c2mon.driver.opcua.connection.soap.SoapLongPoll;

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

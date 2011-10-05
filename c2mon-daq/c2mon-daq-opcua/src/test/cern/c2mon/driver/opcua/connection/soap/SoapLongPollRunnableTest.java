package cern.c2mon.driver.opcua.connection.soap;

import static org.easymock.classextension.EasyMock.*;

import java.lang.Thread.UncaughtExceptionHandler;
import java.rmi.RemoteException;
import java.util.GregorianCalendar;

import org.easymock.classextension.ConstructorArgs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opcfoundation.xmlda.GetStatus;
import org.opcfoundation.xmlda.GetStatusResponse;
import org.opcfoundation.xmlda.ItemValue;
import org.opcfoundation.xmlda.OPCError;
import org.opcfoundation.xmlda.OPCXML_DataAccessStub;
import org.opcfoundation.xmlda.ReplyBase;
import org.opcfoundation.xmlda.SubscribePolledRefreshReplyItemList;
import org.opcfoundation.xmlda.SubscriptionPolledRefresh;
import org.opcfoundation.xmlda.SubscriptionPolledRefreshResponse;

import cern.c2mon.driver.opcua.connection.common.impl.OPCCommunicationException;
import cern.c2mon.driver.opcua.connection.soap.SoapLongPollRunnable;

public class SoapLongPollRunnableTest {
    
    private static final String SUBHANDLE = "subhandle";

    private static final long SLEEP_TIME = 500L;

    private SoapLongPollRunnable poll;

    private OPCXML_DataAccessStub access = createMock(OPCXML_DataAccessStub.class);
    
    private volatile Throwable error;
    
    @Before
    public void setUp() throws SecurityException, NoSuchMethodException {
        poll = createMock(SoapLongPollRunnable.class,
                new ConstructorArgs(
                        SoapLongPollRunnable.class.getConstructor(
                                Integer.TYPE, Integer.TYPE, String.class,
                                OPCXML_DataAccessStub.class),
                                100, 1000, "asd", access ),
                SoapLongPollRunnable.class.getMethod(
                        "newItemValues", SubscribePolledRefreshReplyItemList[].class),
                SoapLongPollRunnable.class.getMethod(
                        "onError", Throwable.class));
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
    
//    @Test
//    public void testStartAndStop() throws RemoteException, InterruptedException {
//        GetStatusResponse statusResponse = new GetStatusResponse();
//        ReplyBase replyBase = new ReplyBase();
//        replyBase.setReplyTime(new GregorianCalendar());
//        statusResponse.setGetStatusResult(replyBase);
//        expect(access.getStatus(isA(GetStatus.class))).andReturn(statusResponse);
//        SubscriptionPolledRefreshResponse response = new SubscriptionPolledRefreshResponse();
//        response.setSubscriptionPolledRefreshResult(replyBase);
//        expect(access.subscriptionPolledRefresh(
//                isA(SubscriptionPolledRefresh.class))).andReturn(
//                        response ).atLeastOnce();
//        poll.newItemValues(null);
//        expectLastCall().atLeastOnce();
//        
//        replay(access, poll);
//        new Thread(poll).start();
//        Thread.sleep(SLEEP_TIME);
//        poll.stop();
//        verify(access, poll);
//        reset(access, poll);
//        replay(access, poll);
//        Thread.sleep(SLEEP_TIME);
//        // no more calls
//        verify(access, poll);
//    }
//    
//    @Test
//    public void testOPCErrors() throws RemoteException {
//        GetStatusResponse statusResponse = new GetStatusResponse();
//        ReplyBase replyBase = new ReplyBase();
//        replyBase.setReplyTime(new GregorianCalendar());
//        statusResponse.setGetStatusResult(replyBase);
//        expect(access.getStatus(isA(GetStatus.class))).andReturn(statusResponse);
//        OPCError error = new OPCError();
//        error.setText("asd");
//        OPCError[] errors = { error };
//        SubscriptionPolledRefreshResponse response = new SubscriptionPolledRefreshResponse();
//        response.setSubscriptionPolledRefreshResult(replyBase);
//        response.setErrors(errors);
//        expect(access.subscriptionPolledRefresh(
//                isA(SubscriptionPolledRefresh.class))).andReturn(
//                        response);
//        poll.onError(isA(OPCCommunicationException.class));
//        replay(access, poll);
//        poll.run();
//        verify(access, poll);
//    }

    @Test
    public void testStartPollingWithException() throws RemoteException, InterruptedException {
        GetStatusResponse statusResponse = new GetStatusResponse();
        statusResponse.setGetStatusResult(new ReplyBase());
        RuntimeException runtimeException = new RuntimeException();
        expect(access.getStatus(isA(GetStatus.class))).andThrow(runtimeException);
        poll.onError(runtimeException);

        replay(access, poll);
        poll.run();
        verify(access, poll);
    }
    
}

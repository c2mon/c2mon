package cern.c2mon.driver.opcua.connection.soap;

import java.util.Calendar;

import org.opcfoundation.xmlda.GetStatus;
import org.opcfoundation.xmlda.GetStatusResponse;
import org.opcfoundation.xmlda.OPCXML_DataAccessStub;
import org.opcfoundation.xmlda.ReplyBase;
import org.opcfoundation.xmlda.SubscribePolledRefreshReplyItemList;
import org.opcfoundation.xmlda.SubscriptionPolledRefresh;
import org.opcfoundation.xmlda.SubscriptionPolledRefreshResponse;

import cern.c2mon.driver.opcua.connection.common.impl.OPCCommunicationException;

/**
 * The runnable which implements the long poll. If an error happens or 
 * this runnable is stopped the thread will be disposed.
 * 
 * @author Andreas Lang
 *
 */
public abstract class SoapLongPollRunnable implements Runnable {

    /**
     * Flag to stop this thread. If it is set the thread will finish and
     * never run again.
     */
    private volatile boolean stop = false;
    
    /**
     * Time difference to the server. Since most of the OPCServers run
     * on Windows machines which are not relyable in their time.
     */
    private long timeDiff = 0;
    
    /**
     * Hold time: Server has to wait at least that time.
     */
    private int holdTime;

    /**
     * Additional time on top of hold time to wait if there are no updates.
     */
    private int waitTime;

    /**
     * The server subscription handle.
     */
    private String serverSubscriptionHandle;

    /**
     * The access stub
     */
    private OPCXML_DataAccessStub access;
    
    /**
     * Creates a new SoapLongPollRunnable.
     * 
     * @param holdTime The hold time (see {@link SoapLongPoll})
     * @param waitTime The wait time (see {@link SoapLongPoll})
     * @param serverSubscriptionHandle The handle of the subscription to poll.
     * @param access The connection to use.
     */
    public SoapLongPollRunnable(
            final int holdTime, final int waitTime, 
            final String serverSubscriptionHandle, 
            final OPCXML_DataAccessStub access) {
        super();
        this.holdTime = holdTime;
        this.waitTime = waitTime;
        this.serverSubscriptionHandle = serverSubscriptionHandle;
        this.access = access;
    }

    /**
     * Run method for the SoapLongPoll. It loop till it is stopped or 
     * till an exception is thrown.
     */
    @Override
    public void run() {
        SubscriptionPolledRefresh subscriptionPolledRefresh = 
            SoapObjectFactory.createSubscriptionPolledRefresh(
                    serverSubscriptionHandle, waitTime);
        Calendar holdTimeCalendar = subscriptionPolledRefresh.getHoldTime();
        try {
            GetStatusResponse status = 
                access.getStatus(new GetStatus());
            updateTimeDiff(status.getGetStatusResult());
            while (!stop) {
                updateHoldTime(holdTimeCalendar);
                SubscriptionPolledRefreshResponse response = 
                    access.subscriptionPolledRefresh(
                            subscriptionPolledRefresh);
                if (!stop) {
                    if (response.getErrors() == null 
                            || response.getErrors().length == 0) {
                        newItemValues(response.getRItemList());
                        updateTimeDiff(
                                response.getSubscriptionPolledRefreshResult());
                    }
                    else {
                        onError(
                                new OPCCommunicationException(
                                        "OPC error for subscription: " 
                                        + response.getErrors()[0].getText()));
                        stop();
                    }
                }
            }
        } catch (Throwable e) {
            onError(e);
        }
    }

    /**
     * Updates the time difference of the server.
     * 
     * @param response The reply base to extract the time.
     */
    private void updateTimeDiff(final ReplyBase response) {
        Calendar replyTime = response.getReplyTime();
        timeDiff =
            System.currentTimeMillis() - replyTime.getTimeInMillis();
    }
    
    /**
     * Updates the hold time relativ to the current time.
     * 
     * @param calendar The calendar object to update.
     */
    private void updateHoldTime(final Calendar calendar) {
        calendar.setTimeInMillis(System.currentTimeMillis() 
                + holdTime - timeDiff);
    }
    
    /**
     * Stops this runnable.
     */
    public void stop() {
        stop = true;
    }
    
    /**
     * Called in case of new item values.
     * 
     * @param subscribePolledRefreshReplyItemLists The new values.
     */
    public abstract void newItemValues(SubscribePolledRefreshReplyItemList[] subscribePolledRefreshReplyItemLists);

    /**
     * Called if the polling thread fails.
     * 
     * @param e The exception which caused the thread to fail.
     */
    public abstract void onError(Throwable e);
    
}

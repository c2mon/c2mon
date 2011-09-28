package cern.c2mon.driver.opcua.connection.soap;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import cern.c2mon.driver.opcua.connection.common.impl.OPCCommunicationException;
/**
 * Default way to handle excpetions in the long poll mechanism. It just restarts
 * it after a certain time.
 * 
 * @author Andreas Lang
 *
 */
public class DefaultSoapLongPollExceptionHandler 
        implements ISoapLongPollExceptionHandler {

    /**
     * The default delay before the restart is done in ms.
     */
    public static final long DEFAULT_RESTART_DELAY = 2000L;
    
    /**
     * The maximum errors in ERROR_ALIVE_FACTOR * restart delay
     */
    private static final float MAX_ERRORS = 1.5f;
    
    /**
     * The time an error is kept in the counter.
     */
    private static final long ERROR_ALIVE_FACTOR = 3;

    /**
     * The timer used to schedule the restart.
     */
    private final Timer timer = new Timer(true);
    
    /**
     * The endpoint this handler belongs to.
     */
    private final DASoapEndpoint endpoint;

    /**
     * The delay for the restart.
     */
    private long restartDelay;
    
    /**
     * Counter for errors.
     * 
     * Errors stay in the counter for ERROR_ALIVE_FACTOR * restartDelay.
     */
    private AtomicInteger errorCounter = new AtomicInteger(0);

    /**
     * Creates a new DefaultLongPollExceptionHandler
     * with the provided restart delay.
     * 
     * @param endpoint The endpoint of this handler.
     * @param restartDelay The restart delay.
     */
    public DefaultSoapLongPollExceptionHandler(
            final DASoapEndpoint endpoint, final long restartDelay) {
        this.restartDelay = restartDelay;
        this.endpoint = endpoint;
    }

    /**
     * Creates a new DefaultLongPollExceptionHandler
     * with the default restart delay. See DEFAULT_RESTART_DELAY.
     * 
     * @param endpoint The endpoint of this handler.
     */
    public DefaultSoapLongPollExceptionHandler(
            final DASoapEndpoint endpoint) {
        this(endpoint, DEFAULT_RESTART_DELAY);
    }

    /**
     * Handles the connection exception by just restarting the poll.
     * 
     * @param t The occurred exception.
     * @param poll The poll in which the exception happened.
     */
    @Override
    public synchronized void onConnectionException(
            final Throwable t, final SoapLongPoll poll) {
        errorCounter.incrementAndGet();
        if (!toMuchErrors()) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    poll.startPolling();
                }
            }, restartDelay);
            // decrease error counter
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    errorCounter.decrementAndGet();
                }
            }, ERROR_ALIVE_FACTOR * restartDelay);
        }
        else {
            timer.cancel();
            endpoint.notifyEndpointListenersSubscriptionFailed(
                    new OPCCommunicationException("There are too many subscription"
                            + "related exceptions."));
        }
    }

    /**
     * Checks if there are too much errors.
     * 
     * @return True if the amount of errors exceeds the normal rate else false.
     */
    private boolean toMuchErrors() {
        return (errorCounter.get() > MAX_ERRORS);
    }

}

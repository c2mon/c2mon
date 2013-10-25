package cern.c2mon.driver.testhandler;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import cern.c2mon.driver.common.EquipmentMessageHandler;

/**
 * This class is responsible for generating the alive messages for the
 * simulated equipment.
 * 
 * @author mbrightw
 *
 */
public class EquipmentAliveTimer extends Timer {
    
    /**
     * The time before the timer starts sending alive messages.
     */
    private static final long INITIAL_DELAY = 2000;

    /**
     * The standard java timer
     */
    private Timer timer;

    /**
     * A reference to the EquipmentMessageHandler's class
     */
    private EquipmentMessageHandler equipmentMessageHandler;

    /**
     * A reference to the static driver's logger
     */
    private Logger logger;

    /**
     * The AliveTimer constructor
     * @param pEquipmentMessageHandler the EquipmentMessageHandler the timer is associated to
     */
    public EquipmentAliveTimer(final EquipmentMessageHandler pEquipmentMessageHandler) {
        logger = Logger.getLogger(EquipmentAliveTimer.class);
        this.equipmentMessageHandler = pEquipmentMessageHandler;
        // start the timer as a 'deamon'
        timer = new Timer("EquipmentAliveTimer");
    }

    /**
     * This method sets the timer's 'tick' interval
     * @param milisecondsInterval the time between ticks
     */
    public final void setInterval(final long milisecondsInterval) {
        timer.schedule(new SendAliveTask(), INITIAL_DELAY, milisecondsInterval);
    }

    /**
     * This method is used for timer's termination
     */
    public final void terminateTimer() {
        // Terminate the timer thread
        timer.cancel();
    }

    /**
     * This class models the action/task that is taken each timer's 'tick'
     */
    class SendAliveTask extends TimerTask {
        /**
         * Sends the alive message at every timer tick.
         */
        public void run() {
            logger.debug("sending equipment status ok message...");
            equipmentMessageHandler.getEquipmentMessageSender().sendSupervisionAlive();
            logger.debug("equipment status ok sent");
        }
    }
}
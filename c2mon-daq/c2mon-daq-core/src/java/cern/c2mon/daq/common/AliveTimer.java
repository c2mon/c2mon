/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005 - 2011 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.daq.common;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import cern.c2mon.daq.common.messaging.IProcessMessageSender;

/**
 * This is a timer used for sending alive tags notification
 */
public class AliveTimer {
    /**
     * Flag indicating if the timer is currently running
     */
    private boolean running;
    /**
     * The standard java timer
     */
    private Timer timer;

    /**
     * A reference to the ProcessMessageSender's class
     */
    private IProcessMessageSender processMessageSender;
    /**
     * The task which actually sends the alive.
     */
    private SendAliveTask sendTask;

    /**
     * A reference to the static driver's logger
     */
    private static final Logger LOGGER = Logger.getLogger(AliveTimer.class);

    /**
     * This class models the action/task that is taken each timer's 'tick'
     */
    class SendAliveTask extends TimerTask {
        /**
         * The run method of the timer task.
         */
        public void run() {
            processMessageSender.sendAlive();
            LOGGER.debug("Alive sent.");
        }
    }

    /**
     * The AliveTimer constructor
     * @param processMessageSender The process message sender to send the 
     * alive tags to.
     */
    public AliveTimer(final IProcessMessageSender processMessageSender) {
        this.processMessageSender = processMessageSender;
        // start the timer as a 'deamon'
        timer = new Timer("ProcessAliveTimer", true);
    }

    /**
     * This method sets the timer's 'tick' interval and starts the timer.
     * @param milisecondsInterval The interval to send the alive tag.
     */
    public void setInterval(final long milisecondsInterval) {
        if (running) {
            sendTask.cancel();
            timer.purge();
        }
        sendTask = new SendAliveTask();
        timer.schedule(sendTask, 0, milisecondsInterval);
        running = true;
    }

    /**
     * This method is used for timer's termination
     */
    public void terminateTimer() {
        timer.cancel();
        running = false;
    }
    
    /**
     * Checks if the Alive Timer is actually running and sending.
     * @return True if the timer sends else false.
     */
    public boolean isRunning() {
        return running;
    }
    
}

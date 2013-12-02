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
package cern.c2mon.driver.jec;

import java.util.Timer;
import java.util.TimerTask;

import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.driver.jec.plc.StdConstants;

/**
 * This is a implementation of a IJECRestarter which delays the restart a little
 * bit. If its called again during a restart is scheduled it will reset the delay.
 * 
 * @author Andreas Lang
 *
 */
public class TimedJECRestarter implements IJECRestarter {
    /**
     * The basic delay time which will be (re)set when a restart is triggered.
     */
    public static final int RESTART_DELAY_TIME = 5000;
    /**
     * Waiting time between connect and disconnect.
     */
    public static final long RESTART_WAIT_TIME = StdConstants.reconnectTimeout;
    /**
     * The message handler to restart.
     */
    private EquipmentMessageHandler messageHandler;
    /**
     * The used timer.
     */
    private Timer timer = new Timer();
    /**
     * The restart task.
     */
    private RestartTask restartTask;
    /**
     * The current restart wait time. Defaults to a constant.
     * This is the time the restarter will wait after a disconnect
     * to reconnect.
     */
    private long restartWaitTime = RESTART_WAIT_TIME;
    /**
     * The current restart delay time. Defaults to a constant.
     * This is the time the restart will be delayed every time
     * trigger restart is called.
     */
    private long restartDelayTime = RESTART_WAIT_TIME;
    
    /**
     * Task to restart the message handler.
     * 
     * @author Andreas Lang
     *
     */
    private class RestartTask extends TimerTask {
        /**
         * Run method which is executed when a triggered restart finishes its delay.
         */
        @Override
        public void run() {
            // Makes sure that one restart is finished before running another.
            synchronized (messageHandler) {
                try {
                    messageHandler.disconnectFromDataSource();
                    try {
                        Thread.sleep(restartWaitTime);
                    } catch (InterruptedException e) {
                        messageHandler.getEquipmentLogger(TimedJECRestarter.class).error("JECRestarter interrupted while waiting to restart it.");
                    }
                    messageHandler.connectToDataSource();
                } catch (EqIOException e) {
                    messageHandler.getEquipmentLogger().error("Error while restarting DAQ.", e);
                }
            }
        }
    }
    
    /**
     * Creates a new timed JEC restarter.
     * 
     * @param messageHandler The message handler to restart.
     */
    public TimedJECRestarter(final EquipmentMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }
    
    /**
     * Creates a new timed JEC restarter.
     * 
     * @param messageHandler The message handler to restart.
     * @param restartDelayTime This is the time the restart will be 
     * delayed every time trigger restart is called.
     * @param restartWaitTime This is the time the restarter will wait 
     * after a disconnect to reconnect.
     */
    public TimedJECRestarter(final EquipmentMessageHandler messageHandler,
            final long restartDelayTime, final long restartWaitTime) {
        this.messageHandler = messageHandler;
        this.restartWaitTime = restartWaitTime;
        this.restartDelayTime = restartDelayTime;
    }

    /**
     * Triggers a restart which means the delay time is (re)set.
     */
    @Override
    public synchronized void triggerRestart() {
        restart(restartDelayTime);
    }

    /**
     * Forces an immediate restart of the DAQ.
     */
    @Override
    public synchronized void forceImmediateRestart() {
        restart(0);
    }
    
    @Override
    public void shutdown() {
      timer.cancel();
    }
    
    /**
     * Restarts the DAQ after a provided time. If there is already a restart
     * scheduled it is canceled.
     * 
     * @param waitTime The time to the restart.
     */
    private void restart(final long waitTime) {
        if (restartTask != null)
            restartTask.cancel();
        
        restartTask = new RestartTask();
        timer.schedule(restartTask, waitTime);
    }
    
}

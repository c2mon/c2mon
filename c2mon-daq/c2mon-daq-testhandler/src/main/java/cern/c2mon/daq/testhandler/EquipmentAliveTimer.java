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
package cern.c2mon.daq.testhandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.shared.common.datatag.ISourceDataTag;

/**
 * This class is responsible for generating the alive messages for the simulated
 * equipment.
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
   * The timer for the Equipment alive tag
   */
  private Timer equipmentAliveTimer;

  /**
   * Map of SubEquipment alive tag IDs -> timers
   */
  private Map<Long, Timer> subEquipmentAliveTimers =  new HashMap<>();

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
   *
   * @param pEquipmentMessageHandler the EquipmentMessageHandler the timer is
   *          associated to
   */
  public EquipmentAliveTimer(final EquipmentMessageHandler pEquipmentMessageHandler) {
    logger = LoggerFactory.getLogger(EquipmentAliveTimer.class);
    this.equipmentMessageHandler = pEquipmentMessageHandler;
  }

  /**
   * This method sets the timer's 'tick' interval
   *
   * @param interval the time between ticks (in milliseconds)
   */
  public final void scheduleEquipmentAliveTimer(final long interval) {
    equipmentAliveTimer = new Timer("EquipmentAliveTimer");
    equipmentAliveTimer.schedule(new SendAliveTask(), INITIAL_DELAY, interval);
  }

  /**
   * This method sets the timer's 'tick' interval for a sub equipment alive tag
   *
   * @param interval the time between ticks (in milliseconds)
   */
  public final void scheduleSubEquipmentAliveTimer(Long aliveTagId, final long interval) {
    Timer subEquipmentAliveTimer = new Timer("SubEquipmentAliveTimer:" + aliveTagId);
    subEquipmentAliveTimer.schedule(new SendSubEquipmentAliveTask(aliveTagId), INITIAL_DELAY, interval);

    // Keep a reference to the timers so we can cancel them later
    subEquipmentAliveTimers.put(aliveTagId, subEquipmentAliveTimer);
  }

  /**
   * This method is used for timer's termination
   */
  public final void terminateEquipmentAliveTimer() {
//    for (Timer timer : subEquipmentAliveTimers.values()) {
//      timer.cancel();
//    }
    equipmentAliveTimer.cancel();
  }

  /**
   * Terminate the alive timer for a particular SubEquipment.
   *
   * @param aliveTagId the ID of the SubEquipment alive tag
   */
  public final void terminateSubEquipmentAliveTimer(Long aliveTagId) {
    Timer timer = subEquipmentAliveTimers.get(aliveTagId);
    if (timer != null) {
      timer.cancel();
    } else {
      logger.debug("Alive timer not found for tag " + aliveTagId);
    }
  }

  /**
   * This class models the action/task that is taken each timer's 'tick'
   */
  class SendAliveTask extends TimerTask {

    /**
     * Sends the alive message at every timer tick.
     */
    @Override
    public void run() {
      logger.debug("sending equipment status ok message...");
      equipmentMessageHandler.getEquipmentMessageSender().sendSupervisionAlive();
      logger.debug("equipment status ok sent");
    }
  }

  /**
   * This class models the action/task that is taken each timer's 'tick'
   *
   * @author Justin Lewis Salmon
   */
  class SendSubEquipmentAliveTask extends TimerTask {

    private Long aliveTagId;

    public SendSubEquipmentAliveTask(Long aliveTagId) {
      this.aliveTagId = aliveTagId;
    }

    /**
     * Sends the alive message at every timer tick.
     */
    @Override
    public void run() {
      logger.debug("sending sub equipment status ok message...");
      ISourceDataTag aliveTag = equipmentMessageHandler.getEquipmentConfiguration().getSourceDataTag(aliveTagId);
      Long timestamp = System.currentTimeMillis();
      equipmentMessageHandler.getEquipmentMessageSender().sendTagFiltered(aliveTag, timestamp, timestamp, "SubEquipment is alive");
      logger.debug("sub equipment status ok sent");
    }
  }
}

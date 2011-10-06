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
package ch.cern.tim.driver.jec;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import cern.tim.driver.common.EquipmentLogger;
import ch.cern.tim.jec.JECPFrames;
import ch.cern.tim.jec.StdConstants;

/**
 * Synchronization timer keeps the PLC time in synchronization with the time of the
 * DAQ system.
 */
public class SynchronizationTimer extends TimerTask {
    /**
     * Time the synchronization timer will check if he needs to resynchronize.
     */
    private static final int SYNCHRONIZATION_SCHEDULE_TIME = 30000;
    /**
     * The calendar used by this timer.
     */
    private Calendar calendar;
    /**
     * TimeZone of the calendar.
     */
    private TimeZone timezone;
    /**
     * Checks if the TimeZone is in daylight saving.
     */
    private boolean inDST;
    /**
     * The timer to schedule the checks.
     */
    private Timer timer;
    /**
     * The send frame to communicate changes to the PLC.
     */
    private JECPFrames sendFrame;
    /**
     * The object factory to create objects to communicate with the PLC.
     */
    private PLCObjectFactory plcFactory;
    /**
     * Flag to pause execution.
     */
    private boolean pause = false;
    /**
     * The equipment logger for this class.
     */
    private EquipmentLogger equipmentLogger;

    /**
     * Creates and schedules the timer task for regular execution.
     * 
     * @param equipmentLogger The logger for this object.
     * @param plcFactory The PLC object factory to create frames and to send via
     * the driver.
     */
    public SynchronizationTimer(final EquipmentLogger equipmentLogger,
            final PLCObjectFactory plcFactory) {
        this.plcFactory = plcFactory;
        this.sendFrame = plcFactory.getRawSendFrame();
        this.equipmentLogger = equipmentLogger;
        // Create local time zone
        timezone = TimeZone.getTimeZone("Europe/Zurich");
        // Create a calendar for the local time zone, initialised with the
        // current time
        calendar = new GregorianCalendar(timezone);
        // Set the date to the next full minute (plus some milliseconds to
        // be on the safe side)
        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + 1);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 15);
        // Check whether we are currently in daylight savings time
        inDST = timezone.inDaylightTime(calendar.getTime());

        // Create a timer for periodic time checks
        timer = new Timer(true);
        // Defines a fixes interruption for this time check - 30sec
        timer.scheduleAtFixedRate(this, calendar.getTime(), SYNCHRONIZATION_SCHEDULE_TIME);

        // If daylight saving is detected
        if (inDST) {
            equipmentLogger.debug("Started in Daylight Savings Time");
        } else {
            equipmentLogger.debug("Started in Winter Time");
        }
        equipmentLogger.debug("First check will be done at: " + calendar.getTime());
    }

    /**
     * The run() method, which is periodically called by the TimerTask.
     */
    public void run() {
        // If not paused.
        if (!isPause()) {
            equipmentLogger.debug("run() : Checking for daylight synchronisation");
            testDaylightSavingTime(new Date());
        }
    }

    /**
     * This method tests if at the provided date daylight saving time is 
     * activated in the timezone and compares it with the state of the PLC. 
     * If the states do not match the time will be adjusted.
     * 
     * @param dateToCheck The current date.
     */
    public synchronized void testDaylightSavingTime(final Date dateToCheck) {
        if (inDST != timezone.inDaylightTime(dateToCheck)) {
            // Block JEC normal operation to send new TIME message
            // Inverts the daylight saving variable
            inDST = !inDST;
            if (inDST) {
                equipmentLogger.info("Switching to Summer Time");
            }
            else {
                equipmentLogger.info("Switching to Winter Time");
            }
            sendFrame.SetMessageIdentifier(StdConstants.SET_TIME_MSG);
            sendFrame.SetSequenceNumber((byte) (sendFrame.GetSequenceNumber() + 0x01));
            // Assigns the actual host time to the JEC frame
            sendFrame.JECSynchronize();
            equipmentLogger.debug("DayLight Saving Detected: Sending TIME MESSAGE to PLC...");
            // If there was a problem during send, exits reporting error
            if (plcFactory.getPLCDriver().Send(sendFrame) == StdConstants.ERROR) {
                equipmentLogger.error("Error while trying to send TIME ZONE CHANGED message to PLC...");
            }
        } else {
            equipmentLogger.debug("No summer/winter time switch necessary");
        }
    }

    /**
     * @param pause the pause to set
     */
    public synchronized void setPause(final boolean pause) {
        this.pause = pause;
    }

    /**
     * @return the pause
     */
    public synchronized boolean isPause() {
        return pause;
    }

}

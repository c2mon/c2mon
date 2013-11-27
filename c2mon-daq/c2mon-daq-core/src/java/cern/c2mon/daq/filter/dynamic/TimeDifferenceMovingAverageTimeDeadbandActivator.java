/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005 - 2010 CERN This program is free software; you can
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
package cern.c2mon.daq.filter.dynamic;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.daq.datatag.SourceDataTag;

/**
 * This class implements an activator of dynamic time deadband filtering.
 * It uses an average over the last incoming tags determining if a time 
 * deadband filtering is necessary or not.
 * 
 * @author alang
 *
 */
public class TimeDifferenceMovingAverageTimeDeadbandActivator implements IDynamicTimeDeadbandFilterActivator {
    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(TimeDifferenceMovingAverageTimeDeadbandActivator.class);
    
    /**
     * The table with the data tags managed by this class.
     */
    private Map<Long, SourceDataTag> dataTagTable = new ConcurrentHashMap<Long, SourceDataTag>();
    
    /**
     * Table with the moving averages used to determine if time deadband filtering
     * should be activated.
     */
    private Map<Long, DiffMovingAverage> movingAverages = new HashMap<Long, DiffMovingAverage>();

    /**
     * The number of the values used to calculate the average.
     */
    private int windowSize;

    /**
     * The minimumDifference between two values. (If lower dynamic time deadband
     * filtering will be activated)
     */
    private long minDiff;

    /**
     * The difference to deactivate dynamic time deadband filtering.
     */
    private long deactivationDiff;

    /**
     * The time the time deadband will be set if activated.
     */
    private int timeDeadbandTime;
    
    /**
     * Creates a new DiffTimeDeadbandActivatior.
     * @param windowSize The window size to use. (Number of differences to 
     * calculate the average.
     * @param minDiff The minimum difference between two tags in ms.
     * If the difference is lower dynamic time deadband filtering will 
     * be activated. The deactivation value is twice of this value.
     * @param timeDeadbandTime The time the time deadband will be set if activated.
     */
    public TimeDifferenceMovingAverageTimeDeadbandActivator(final int windowSize, final long minDiff,
            final int timeDeadbandTime) {
        this(windowSize, minDiff, minDiff * 2, timeDeadbandTime);
    }
    
    /**
     * Creates a new DiffTimeDeadbandActivatior.
     * @param windowSize The window size to use. (Number of differences to 
     * calculate the average.
     * @param minDiff The minimum difference between two tags in ms.
     * If the difference is lower dynamic time deadband filtering will 
     * be activated.
     * @param deactivationDiff If the difference of time [ms] between two tags
     * is higher than this dynamic time deadband filtering will be deactivated.
     * @param timeDeadbandTime The time the time deadband will be set if activated.
     */
    public TimeDifferenceMovingAverageTimeDeadbandActivator(final int windowSize, final long minDiff, 
            final long deactivationDiff, final int timeDeadbandTime) {
        super();
        this.windowSize = windowSize;
        this.minDiff = minDiff;
        this.deactivationDiff = deactivationDiff;
        this.timeDeadbandTime = timeDeadbandTime;
    }

    /**
     * Updates the time between this tag and the last with the same id and 
     * calculates the new average.
     * @param tagID The tag id of the param to be send to the server or would
     * be send to the server if time deadband filtering is deactivated.
     */
    @Override
    public void newTagValueSent(final long tagID) {
        if (dataTagTable == null) {
            LOGGER.error("Tried to add a new tag while field dataTagTable was null.");
            return;
        }
        SourceDataTag tag = dataTagTable.get(tagID);
        if (tag == null) {
            LOGGER.warn("Tried to add a tag not controlled by this class. (Tag-ID: '" + tagID + "')");
        }
        else {
            DiffMovingAverage diffMovingAverage = movingAverages.get(tagID);
            if (diffMovingAverage == null) {
                diffMovingAverage = new DiffMovingAverage(windowSize);
                movingAverages.put(tagID, diffMovingAverage);
            }
            diffMovingAverage.recordTimestamp();
            checkTag(tag, diffMovingAverage.getCurrentAverage());
        }
    }

    /**
     * Checks if the source data tag should have time deadband filtering activated.
     * @param tag The tag to be checked.
     * @param currentAverage The current average of time between to data tags.
     */
    private void checkTag(final SourceDataTag tag, final long currentAverage) {
        DataTagAddress address = tag.getAddress(); 
        LOGGER.debug("Tag: '" + tag.getId() + "' - Current average: " + currentAverage + "ms");
        if (address != null) {
            if (address.isTimeDeadbandEnabled()) {
                if (currentAverage >= deactivationDiff) {
                    LOGGER.info("Tag: '" + tag.getId() + "' - Removing tag from time deadband filtering.");
                    address.setTimeDeadband(0);
                }
            }
            else {
                if (currentAverage <= minDiff && currentAverage != -1) {
                    LOGGER.info("Tag: '" + tag.getId() + "' - Adding tag to time deadband filtering.");
                    address.setTimeDeadband(timeDeadbandTime);
                }
            }
        }
    }

    /**
     * Returns the currently used data table.
     * @return The currently used data tag table. May be null.
     */
    @Override
    public Map<Long, SourceDataTag> getDataTagMap() {
        return dataTagTable;
    }

    /**
     * Adds a data tag to be controlled by this activator.
     * @param sourceDataTag The data tag that should be controlled 
     * by this activator.
     */
    @Override
    public void addDataTag(final SourceDataTag sourceDataTag) {
        dataTagTable.put(sourceDataTag.getId(), sourceDataTag);
    }

    /**
     * Removes a data tag from the control of this activator.
     * Implementing classes should make sure time deadband filtering is 
     * deactivated when releasing a tag from their control.
     * @param sourceDataTag The data tag to remove from he control of this
     * activator.
     */
    @Override
    public void removeDataTag(final SourceDataTag sourceDataTag) {
        dataTagTable.remove(sourceDataTag.getId());
        sourceDataTag.getAddress().setTimeDeadband(0);
    }
    
    /**
     * Clears the data tags of this time deadband activator
     */
    @Override
    public void clearDataTags() {
        dataTagTable.clear();
    }
}

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

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.daq.datatag.SourceDataTag;
/**
 * Counter deadband activator which counts the tags in a provided time and 
 * activates/deactivates them if necessary. 
 * @author alang
 *
 */
public class CounterTimeDeadbandActivator extends TimerTask implements IDynamicTimeDeadbandFilterActivator {
    /**
     * The logger of this class.
     */
    private static final Logger LOGGER = Logger.getLogger(CounterTimeDeadbandActivator.class);
    /**
     * Table with the source data tags managed by this class.
     */
    private Map<Long, SourceDataTag> sourceDataTags = new ConcurrentHashMap<Long, SourceDataTag>();
    /**
     * Table with the Counter Time deadbands.
     */
    private Map<Long, CounterMovingAverage> movingAverages = new ConcurrentHashMap<Long, CounterMovingAverage>();
    /**
     * The number of counters used per tag.
     */
    private int numberOfCounters;
    /**
     * The maximum tags that should occurre per checkInterval (averaged over
     * all counters)
     */
    private int maxTagsPerTime;
    /**
     * If a tag with activated time deadband filtering
     * goes under this value time deadband filtering will be deactivated.
     */
    private int deactivationNumberOfTags;
    /**
     * The timer used to schedule the checks of tags.
     */
    private Timer timer;
    /**
     * The time deadband which is set if a timedeadband is activated.
     */
    private int timeDeadbandTime;

    /**
     * Creates a new CounterMovingAverage.
     * @param numberOfCounters The number of counters used per tag.
     * @param checkInterval The time in which the average number of tasks is checked and 
     * the next counter is used. [ms]
     * @param maxTagsPerTime The maximum number of tags per check interval averaged
     * over the counters. If there are more tags time deadband filtering is enabled
     * for this tag. The value to deactivate time deadband filtering is 0.5 times 
     * this value.
     * @param timeDeadbandTime The time deadband which is set if a timedeadband is activated.
     */
    public CounterTimeDeadbandActivator(final int numberOfCounters, final long checkInterval, 
            final int maxTagsPerTime, final int timeDeadbandTime) {
        this(numberOfCounters, checkInterval, maxTagsPerTime, maxTagsPerTime / 2, timeDeadbandTime);
    }
    
    /**
     * Creates a new CounterMovingAverage.
     * @param numberOfCounters The number of counters used per tag.
     * @param checkInterval The time in which the average number of tasks is checked and 
     * the next counter is used. [ms]
     * @param maxTagsPerTime The maximum number of tags per check interval averaged
     * over the counters. If there are more tags time deadband filtering is enabled
     * for this tag.
     * @param deactivationNumberOfTags If a tag with activated time deadband filtering
     * goes under this value time deadband filtering will be deactivated.
     * @param timeDeadbandTime The time deadband which is set if a timedeadband is activated.
     */
    public CounterTimeDeadbandActivator(final int numberOfCounters, final long checkInterval, 
            final int maxTagsPerTime, final int deactivationNumberOfTags, final int timeDeadbandTime) {
        this.numberOfCounters = numberOfCounters;
        this.maxTagsPerTime = maxTagsPerTime;
        this.deactivationNumberOfTags = deactivationNumberOfTags;
        this.timeDeadbandTime = timeDeadbandTime;
        timer = new Timer("CounterTimeDeadbandTimer", true);
        timer.scheduleAtFixedRate(this, 0, checkInterval);
    }

    /**
     * The run method of the timer task.
     */
    @Override
    public void run() {
        if (sourceDataTags == null) {
            LOGGER.error("Tried to add a new tag while field dataTagTable was null.");
            return;
        }
        LOGGER.debug("Printing current Tag stats.");
        for (Entry<Long, SourceDataTag> entry : getDataTagMap().entrySet()) {
            SourceDataTag tag = entry.getValue();
            DataTagAddress address = tag.getAddress();
            long tagID = tag.getId();
            CounterMovingAverage counterMovingAverage = movingAverages.get(tagID);
            if (counterMovingAverage == null) {
                counterMovingAverage = new CounterMovingAverage(numberOfCounters);
                movingAverages.put(tagID, counterMovingAverage);
            }
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Tag '" + tag.getId() + "' average incomming tags: "
                        + counterMovingAverage.getCurrentAverage() + " - Array: "
                        + counterMovingAverage);
            if (address.isTimeDeadbandEnabled()) {
                if (counterMovingAverage.getCurrentAverage() < deactivationNumberOfTags) {
                    address.setTimeDeadband(0);
                    LOGGER.info("Tag '" + tag.getId() + "' removed from dynamic timedeadband filter.");
                }
            } else {
                if (counterMovingAverage.getCurrentAverage() > maxTagsPerTime) {
                    LOGGER.info("Tag '" + tag.getId() + "' added to dynamic timedeadband filter.");
                    tag.getAddress().setTimeDeadband(timeDeadbandTime);
                }
            }
            counterMovingAverage.switchCurrentCounter();
        }
        LOGGER.debug("Finished printing current Tag stats.\n");
    }

    /**
     * If the tag with the provided id is controlled by this counter 
     * activator it increases the current counter of this tag.
     * 
     * @param tagID The id of the tag that occurred.
     */
    @Override
    public void newTagValueSent(final long tagID) {
        if (sourceDataTags == null) {
            LOGGER.error("Tried to add a new tag while field sourceDataTags was null.");
            return;
        }
        SourceDataTag tag = sourceDataTags.get(tagID);
        if (tag == null) {
            LOGGER.warn("Tried to count a tag not controlled by this class. (Tag-ID: '" + tagID + "')");
        }
        else {
            CounterMovingAverage average  = movingAverages.get(tagID);
            if (average == null) {
                average = new CounterMovingAverage(numberOfCounters);
                movingAverages.put(tagID, average);
            }
            average.increaseCurrentCounter();
        }
    }
    
    /**
     * Returns the source data tags controlled by this activator.
     * @return The source data tags controlled by this activator.
     */
    @Override
    public Map<Long, SourceDataTag> getDataTagMap() {
        return sourceDataTags;
    }

    /**
     * Adds a data tag to be controlled by this activator.
     * @param sourceDataTag The data tag that should be controlled 
     * by this activator.
     */
    @Override
    public void addDataTag(final SourceDataTag sourceDataTag) {
        sourceDataTags.put(sourceDataTag.getId(), sourceDataTag);
    }

    /**
     * Removes a data tag from the control of this activator.
     * @param sourceDataTag The data tag to remove from he control of this
     * activator.
     */
    @Override
    public void removeDataTag(final SourceDataTag sourceDataTag) {
        sourceDataTags.remove(sourceDataTag.getId());
        sourceDataTag.getAddress().setTimeDeadband(0);
        movingAverages.remove(sourceDataTag.getId());
    }

    /**
     * Clears the data tags of this time deadband activator
     */
    @Override
    public void clearDataTags() {
        sourceDataTags.clear();
        movingAverages.clear();
    }

}

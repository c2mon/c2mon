/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2009 CERN This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/

package cern.c2mon.statistics.consumer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import cern.c2mon.pmanager.IAlarmListener;
import cern.c2mon.pmanager.IDBPersistenceHandler;
import cern.c2mon.pmanager.IFallback;
import cern.c2mon.pmanager.persistence.IPersistenceManager;
import cern.c2mon.pmanager.persistence.impl.TimPersistenceManager;
import cern.tim.shared.daq.filter.FilteredDataTagValue;
import cern.tim.util.buffer.PullEvent;
import cern.tim.util.buffer.SynchroBuffer;
import cern.tim.util.buffer.SynchroBufferListener;

/**
 * A wrapper class for the SynchroBuffer containing the tag values. The class
 * uses the SqlMapper class to write the values in the buffer to the database
 * (the values are written when a PullEvent is registered on the SynchroBuffer).
 * 
 * @author mbrightw
 * 
 */
public class DataTagValueBuffer implements SynchroBufferListener {

 
    /**
     * The synchro buffer containing the SourceDataTagValue objects.
     */
    private SynchroBuffer tagValueBuffer;

    /**
     * The local logger.
     */
    private final Logger logger = Logger.getLogger(DataTagValueBuffer.class);;

    /**
     * The class managing the fallback mechanism.
     */
    private IPersistenceManager persistenceManager;
    
    /**
     * Parameters for buffer initialization.
     */
    private int bufferMinWindow;
    private int bufferMaxWindow;
    private int bufferWindowGrowth;
    private int bufferCapacity;
    
    /**
     * Fallback file name.
     */
    private String fallbackFile;
    
    @PostConstruct
    public void init() {
    
        //create an alarm listener (does nothing so far)
        IAlarmListener alarmListener = new AlarmListener();
        //create a persistence handler
        IDBPersistenceHandler dbPersistenceHandler = new DBPersistenceHandler();
        
        //create the persistence manager (need to pass an instance of FilterPersistenceObject here)
        persistenceManager = new TimPersistenceManager(
                                          dbPersistenceHandler, 
                                          fallbackFile, 
                                          alarmListener, 
                                          (IFallback) new FilterPersistenceObject(new FilteredDataTagValue(new Long(0), "class")));
        
        //initialize the SynchroBuffer
        tagValueBuffer = new SynchroBuffer(bufferMinWindow, bufferMaxWindow, 
                                        bufferWindowGrowth, SynchroBuffer.DUPLICATE_OK, 
                                        bufferCapacity);
        tagValueBuffer.setSynchroBufferListener(this);
        tagValueBuffer.enable();
    }

    /**
     * Add one FilteredDataTagValue to the buffer.
     * 
     * @param dataTagValue the FilteredDataTagValue to add
     */
    public void addValue(final FilteredDataTagValue dataTagValue) {
        logger.debug("adding data tag value to buffer");
        tagValueBuffer.push(dataTagValue);
    }

    /**
     * Adds a collection of FilteredDataTagValue's to the buffer,
     * in the form of FilterPersistenceObjects.
     * FilteredDataTagValue objects are coming from the received XML messages
     * and are here added as FilterPersistenceObject's to the SynchroBuffer
     * (FilterPersistenceObject objects are needed for the fallback manager).
     * 
     * @param tagValueCollection a collection of FilteredDataTagValue's
     *            
     */
    public void addValues(final Collection<FilteredDataTagValue> tagValueCollection) {
        logger.debug("adding data tag values to buffer");
        Iterator<FilteredDataTagValue> it = tagValueCollection.iterator();
        while (it.hasNext()) {
            tagValueBuffer.push(new FilterPersistenceObject((FilteredDataTagValue) it.next()));
        }
        //tagValueBuffer.push(tagValueCollection);
    }

    /**
     * Closes the buffer (used when the kernel is shut down).
     */
    @PreDestroy
    public void closeTagBuffer() {
        tagValueBuffer.disable();
        tagValueBuffer.close();
    }

    /**
     * The action to take when a pull event happens on the SynchroBuffer. The
     * contents of the buffer are then written to the database.
     * @param event the event that triggers the pull
     */

    public final void pull(final PullEvent event) {
        if (logger.isDebugEnabled()) {
            logger.debug("entering filter server SynchroBuffer pull()...");
            logger.debug("\t Number of pulled objects : " + event.getPulled().size());
        }

        // write the pulled objects to the database
        if (logger.isDebugEnabled()) {
            logger.debug("writing the collection to the database");
        }
        // switch from Collection to List (List is needed by fallback manager)
        List<FilterPersistenceObject> pulledList = new ArrayList<FilterPersistenceObject>(event.getPulled());
        persistenceManager.storeData(pulledList);

        if (logger.isDebugEnabled()) {
            logger.debug("leaving filter server SynchroBuffer pull()...");
        }

    }

    /**
     * Setter method.
     * @param bufferMinWindow the bufferMinWindow to set
     */
    public void setBufferMinWindow(final int bufferMinWindow) {
      this.bufferMinWindow = bufferMinWindow;
    }

    /**
     * Setter method.
     * @param bufferMaxWindow the bufferMaxWindow to set
     */
    public void setBufferMaxWindow(final int bufferMaxWindow) {
      this.bufferMaxWindow = bufferMaxWindow;
    }

    /**
     * Setter method.
     * @param bufferWindowGrowth the bufferWindowGrowth to set
     */
    public void setBufferWindowGrowth(final int bufferWindowGrowth) {
      this.bufferWindowGrowth = bufferWindowGrowth;
    }

    /**
     * Setter method.
     * @param bufferCapacity the bufferCapacity to set
     */
    public void setBufferCapacity(final int bufferCapacity) {
      this.bufferCapacity = bufferCapacity;
    }

    /**
     * Setter method.
     * @param fallbackFile the fallbackFile to set
     */
    public void setFallbackFile(final String fallbackFile) {
      this.fallbackFile = fallbackFile;
    }

}

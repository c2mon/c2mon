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
package cern.c2mon.client.ext.simulator;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.cache.BasicCacheHandler;
import cern.c2mon.client.core.tag.ClientDataTagImpl;
import cern.c2mon.shared.common.datatag.TagQualityStatus;

/**
 * This class implements the {@link C2monTagSimulator} interface. For more
 * information please read the interface description.
 * <p>
 * Internally the functionalities are realized by making use of the history 
 * cache which is provided by the {@link BasicCacheHandler}. It is therefore
 * not possible to use the simulator whilst being in history mode.
 *
 * @author Matthias Braeger
 */
@Service
class TagSimulatorImpl implements C2monTagSimulator {
  
  /** Log4j instance */
  private static final Logger LOG = LoggerFactory.getLogger(TagSimulatorImpl.class);
  
  /** Reference to the <code>ClientDataTagCache</code> */
  private final BasicCacheHandler cache;
  
  /** Flag to remember, if the simulation is on or off */
  private boolean simulationModeOn = false;

  /**
   * Default Constructor
   * @param pCache Reference to the basic cache handler
   */
  @Autowired
  protected TagSimulatorImpl(final BasicCacheHandler pCache) {
    this.cache = pCache;
  }
  
  @Override
  public synchronized boolean startSimulationMode() {
    if (simulationModeOn) {
      return simulationModeOn;
    }
    
    if (!simulationModeOn && !cache.isHistoryModeEnabled()) {
      cache.setHistoryMode(true);
      simulationModeOn = true;
      return simulationModeOn;
    }
    
    return false;
  }

  @Override
  public synchronized void stopSimulationMode() {
    if (simulationModeOn) {
      cache.setHistoryMode(false);
      simulationModeOn = false;
    }
  }

  @Override
  public boolean isSimulationModeEnabled() {
    return simulationModeOn;
  }

  @Override
  public boolean changeValue(final Long tagId, final Object value) throws ClassCastException {
    boolean retval = false;
    synchronized (cache.getHistoryModeSyncLock()) {
      if (isSimulationModeEnabled()) {
        Tag cdt = cache.get(tagId);
        if (cdt != null) {
          SimulatedTagValueUpdate update = new SimulatedTagValueUpdate(cdt);
          update.setValue(value);
          ((ClientDataTagImpl) cdt).update(update);
          retval = true;
        }
      }
    }
    
    return retval;
  }

  @Override
  public boolean changeValues(final Map<Long, Object> tagValues) {
    boolean allOk = true;
    synchronized (cache.getHistoryModeSyncLock()) {
      if (isSimulationModeEnabled()) {
        Tag cdt;
        for (Long tagId : tagValues.keySet()) {
          cdt = cache.get(tagId);
          if (cdt != null) {
            try {
              SimulatedTagValueUpdate update = new SimulatedTagValueUpdate(cdt);
              update.setValue(tagValues.get(tagId));
              ((ClientDataTagImpl) cdt).update(update);
            }
            catch (Exception ex) {
              LOG.error("changeValues() - A problem occured whilst updating tag " + tagId, ex);
              allOk = false;
            }
          }
          else {
            allOk = false;
          }
        } // end for loop
      }
      else {
        allOk = false;
      }
    }
    
    return allOk;
  }

  @Override
  public boolean invalidateTag(final  Long tagId, final TagQualityStatus status) {
    boolean retval = false;
    
    synchronized (cache.getHistoryModeSyncLock()) {
      if (isSimulationModeEnabled()) {
        Tag cdt = cache.get(tagId);
        if (cdt != null) {
          SimulatedTagValueUpdate update = new SimulatedTagValueUpdate(cdt);
          update.invalidateTag(status);
          ((ClientDataTagImpl) cdt).update(update);
          retval = true;
        }
      }
    }
    
    return retval;
  }

  @Override
  public boolean invalidateTags(final Set<Long> tagIds, final TagQualityStatus status) {
    boolean allOk = true;
    synchronized (cache.getHistoryModeSyncLock()) {
      if (isSimulationModeEnabled()) {
        Tag cdt;
        for (Long tagId : tagIds) {
          cdt = cache.get(tagId);
          if (cdt != null) {
            try {
              SimulatedTagValueUpdate update = new SimulatedTagValueUpdate(cdt);
              update.invalidateTag(status);
              ((ClientDataTagImpl) cdt).update(update);
            }
            catch (Exception ex) {
              LOG.error("invalidateTags() - A problem occured whilst updating tag " + tagId, ex);
              allOk = false;
            }
          }
          else {
            allOk = false;
          }
        } // end for loop
      }
      else {
        allOk = false;
      }
    }
    
    return allOk;
  }

}

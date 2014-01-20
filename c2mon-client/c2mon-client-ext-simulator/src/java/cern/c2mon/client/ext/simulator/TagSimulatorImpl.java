/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2013 CERN. This program is free software; you can
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
 ******************************************************************************/
package cern.c2mon.client.ext.simulator;

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.tag.ClientDataTag;
import cern.c2mon.client.core.cache.BasicCacheHandler;
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
  private static final Logger LOG = Logger.getLogger(TagSimulatorImpl.class);
  
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
        ClientDataTag cdt = cache.get(tagId);
        if (cdt != null) {
          try {
            SimulatedTagValueUpdate update = new SimulatedTagValueUpdate(cdt);
            update.setValue(value);
            cdt.update(update);
            retval = true;
          }
          catch (CloneNotSupportedException e) {
            LOG.error("changeValue() - Could not do simulated update for tag " + tagId + ". ClientDataTag not clonable.", e);
          }
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
        ClientDataTag cdt;
        for (Long tagId : tagValues.keySet()) {
          cdt = cache.get(tagId);
          if (cdt != null) {
            try {
              SimulatedTagValueUpdate update = new SimulatedTagValueUpdate(cdt);
              update.setValue(tagValues.get(tagId));
              cdt.update(update);
            }
            catch (CloneNotSupportedException e) {
              LOG.error("changeValues() - Could not do simulated update for tag " + tagId + ". ClientDataTag not clonable.", e);
              allOk = false;
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
        ClientDataTag cdt = cache.get(tagId);
        if (cdt != null) {
          try {
            SimulatedTagValueUpdate update = new SimulatedTagValueUpdate(cdt);
            update.invalidateTag(status);
            cdt.update(update);
            retval = true;
          }
          catch (CloneNotSupportedException e) {
            LOG.error("invalidateTag() - Could not do simulated update for tag " + tagId + ". ClientDataTag not clonable.", e);
          }
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
        ClientDataTag cdt;
        for (Long tagId : tagIds) {
          cdt = cache.get(tagId);
          if (cdt != null) {
            try {
              SimulatedTagValueUpdate update = new SimulatedTagValueUpdate(cdt);
              update.invalidateTag(status);
              cdt.update(update);
            }
            catch (CloneNotSupportedException e) {
              LOG.error("invalidateTags() - Could not do simulated update for tag " + tagId + ". ClientDataTag not clonable.", e);
              allOk = false;
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

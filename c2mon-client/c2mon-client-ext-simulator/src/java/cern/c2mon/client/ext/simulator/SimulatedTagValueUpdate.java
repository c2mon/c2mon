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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

import cern.c2mon.client.common.tag.ClientDataTag;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.tim.shared.common.datatag.DataTagQuality;
import cern.tim.shared.common.datatag.DataTagQualityImpl;
import cern.tim.shared.common.datatag.TagQualityStatus;

/**
 * This class is used internally by {@link TagSimulatorImpl} making a cache 
 * update. When setting the simulated value this class creates a new timestamp.
 * Source-, DAQ- and Server timestamp are then the same.
 *
 * @author Matthias Braeger
 */
class SimulatedTagValueUpdate implements TagValueUpdate {
  
  /** The client data tag clone */
  private ClientDataTag tag;
  
  /** The simulated quality object */
  private final DataTagQualityImpl quality = new DataTagQualityImpl();
  
  /** The new value */
  private Object simulatedValue = null;
  
  /** The simulated source timestamp */
  private Timestamp simulatedTimestamp = null;
  
  /**
   * Copy constructor
   * 
   * @param cdt The tag that will receive a simulated value
   * @throws CloneNotSupportedException In case that the ClientDataTag instance is not 
   *         clonable.
   */
  protected SimulatedTagValueUpdate(final ClientDataTag cdt) throws CloneNotSupportedException {
    this.tag = cdt.clone();
    quality.validate();
  }
  
  /**
   * Sets the simulated value and generates at the same time a new source timestamp
   * @param value The simulated value
   * @exception UnsupportedOperationException In case the value update object is not from the same
   *            type as the tag.
   * @see ClientDataTagValue#getType()
   */
  void setValue(final Object value) throws ClassCastException {
    boolean setValue = false;
    
    if (tag.getDataTagQuality().isInitialised()) {
      if (tag.getType().isInstance(value)) {
        setValue = true;
      }
      else {
        throw new ClassCastException(
            "The simulated value update object for tag " + tag.getId() + " is not of type " + tag.getType().toString());
      }
    }
    else {
      setValue = true;
    }
    
    if (setValue) {
      this.simulatedValue = value;
      quality.validate();
      simulatedTimestamp = new Timestamp(System.currentTimeMillis());
    }
  }
  
  void invalidateTag(final TagQualityStatus status) {
    quality.addInvalidStatus(status);
    simulatedTimestamp = new Timestamp(System.currentTimeMillis());
  }

  @Override
  public Long getId() {
    return tag.getId();
  }

  @Override
  public DataTagQuality getDataTagQuality() {
    if (simulatedValue == null && quality.isValid()) { // allows overwriting initial quality flag
      return tag.getDataTagQuality();
    }
    else {
      return quality;
    }
  }

  @Override
  public Object getValue() {
    if (simulatedValue == null) {
      return tag.getValue();
    }
    else {
      return simulatedValue;
    }
  }

  @Override
  public Timestamp getSourceTimestamp() {
    if (simulatedTimestamp == null) {
      return tag.getTimestamp();
    }
    else {
      return simulatedTimestamp;
    }
  }

  @Override
  public Timestamp getDaqTimestamp() {
    if (simulatedTimestamp == null) {
      return tag.getDaqTimestamp();
    }
    else {
      return simulatedTimestamp;
    }
  }

  @Override
  public Timestamp getServerTimestamp() {
    if (simulatedTimestamp == null) {
      return tag.getServerTimestamp();
    }
    else {
      return simulatedTimestamp;
    }
  }

  @Override
  public String getDescription() {
    return tag.getDescription();
  }

  @Override
  public Collection<AlarmValue> getAlarms() {
    return new ArrayList<AlarmValue>();
  }

  @Override
  public TagMode getMode() {
    return tag.getMode();
  }

  @Override
  public boolean isSimulated() {
    return true;
  }

  @Override
  public String getValueDescription() {
    return tag.getValueDescription();
  }
}

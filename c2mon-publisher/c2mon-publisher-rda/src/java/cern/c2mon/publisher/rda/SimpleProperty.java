/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2012 CERN. This program is free software; you can
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
package cern.c2mon.publisher.rda;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.cmw.BadParameter;
import cern.cmw.Data;
import cern.cmw.IOError;
import cern.cmw.rda.server.DeviceServerBase;
import cern.cmw.rda.server.IOPoint;
import cern.cmw.rda.server.ValueChangeListener;

/**
 * This class represents a RDA property. For each tag which is published
 * via RDA an instance of this class is created. It handles the registration
 * of the listeners and is responsible of notifiying all subscribers about
 * value updates.
 *
 * @author Matthias Braeger
 */
final class SimpleProperty implements DataTagUpdateListener {
  
  /** Log4j logger instance */
  private static final Logger LOG = Logger.getLogger(SimpleProperty.class);
  
  /** The current tag value of the device */
  private Data currentValue = null;

  /** List of registered listeners which are interested in this value */
  private final List<ValueChangeListener> listeners;
  
  /** The RDA property name for which this class has been instanciated for */
  private final String rdaPropertyName;

  /**
   * Default Constructor
   */
  SimpleProperty(final String pRdaPropertyName) {
    this.rdaPropertyName = pRdaPropertyName;
    listeners = Collections.synchronizedList(new ArrayList<ValueChangeListener>());
  }

  /**
   * @see DeviceServerBase#get(IOPoint, Data)
   */
  synchronized Data get(final IOPoint iop, final Data context) throws BadParameter, IOError {
    if (currentValue == null) {
      throw new IOError("C2MON-RDA", 0, "Data is uninitialized");
    }
    return currentValue;
  }

  /**
   * @see DeviceServerBase#set(IOPoint, Data, Data)
   */
  synchronized void set(final IOPoint iop, final Data value, final Data context) throws BadParameter, IOError {
    throw new IOError("C2MON-RDA", 0, "SET is not supported by the server");
  }

  /**
   * @see DeviceServerBase#monitorOn(IOPoint, ValueChangeListener)
   */
  synchronized void monitorOn(final IOPoint iop, final ValueChangeListener listener) throws BadParameter {
    listeners.add(listener);
    listener.valueUpdated(null, currentValue, (currentValue != null));
  }

  /**
   * @see DeviceServerBase#monitorOff(IOPoint, ValueChangeListener)
   */
  synchronized void monitorOff(final IOPoint iop, final ValueChangeListener listener) {
    listeners.remove(listener);
  }

  /**
   * Creates a CMW Data object of the {@link ClientDataTagValue} object
   * @param cdt The {@link ClientDataTagValue} object that shall be packed as CMW Data
   * @return The representation of the {@link ClientDataTagValue} object
   */
  private static Data pack(final ClientDataTagValue cdt) {
    Data data = new Data();
    
    switch (cdt.getTypeNumeric()) {
      case TYPE_BOOLEAN: 
        data.insert((Boolean) cdt.getValue());
        break;
      case TYPE_BYTE:
        data.insert((Byte) cdt.getValue());
        break;
      case TYPE_DOUBLE:
        data.insert((Double) cdt.getValue());
        break;
      case TYPE_FLOAT:
        data.insert((Float) cdt.getValue());
        break;
      case TYPE_INTEGER:
        data.insert((Integer) cdt.getValue());
        break;
      case TYPE_LONG:
        data.insert((Long) cdt.getValue());
        break;
      case TYPE_SHORT:
        data.insert((Short) cdt.getValue());
        break;
      case TYPE_STRING:
        data.insert((String) cdt.getValue());
        break;
      default:
        LOG.warn("The data value of tag " + cdt.getId() + " is uninitialized");
        return null;
    }
    
    data.insert("id", cdt.getId());
    data.insert("valid", cdt.isValid());
    data.insert("simulated", cdt.isSimulated());
    data.insert("valueDescription", cdt.getValueDescription());
    data.insert("description", cdt.getDescription());
    data.insert("unit", cdt.getUnit());
    data.insert("quality", cdt.getDataTagQuality().toString());
    data.insert("qualityDescription", cdt.getDataTagQuality().getDescription());
    data.insert("name", cdt.getName());
    data.insert("mode", cdt.getMode().toString());
    data.insert("timestamp", Long.valueOf(cdt.getServerTimestamp().getTime()).doubleValue());
    data.insert("sourceTimestamp", cdt.getTimestamp().getTime());
    return data;
  }

  /**
   * Generates a new {@link Data} object from the received tag update and
   * propagates it to all the listeners.
   */
  @Override
  public synchronized void onUpdate(final ClientDataTagValue cdt) {
    Data newValue = pack(cdt);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Value update received for RDA property " + rdaPropertyName + " : " + newValue);
    }
    
    synchronized (listeners) {
      for (ValueChangeListener l : listeners) {
        l.valueUpdated(currentValue, newValue, true);
      }
    }
    currentValue = newValue;
  }
}

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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.cmw.BadParameter;
import cern.cmw.Data;
import cern.cmw.IOError;
import cern.cmw.InternalException;
import cern.cmw.rda.examples.SimpleServer;
import cern.cmw.rda.server.DeviceServerBase;
import cern.cmw.rda.server.IOPoint;
import cern.cmw.rda.server.ValueChangeListener;

/**
 * This class is based on the {@link SimpleServer} that is provided with the RDA package.
 * It creates and registeres a new RDA server and is able to publish data tags as RDA
 * data. The property name corresponds the name of the data tag.  
 *
 * @author Matthias Braeger
 */
public final class RdaPublisher extends DeviceServerBase implements DataTagUpdateListener {

  /** Log4j logger instance */
  private static final Logger LOG = Logger.getLogger(RdaPublisher.class);
  
  /**
   * Maps the RDA properties to the tag names. For each tag subscription there is
   * exactly one entry registered in this map.
   */
  private final Map<String, SimpleProperty> properties = new HashMap<String, SimpleProperty>();
  
  /**
   * The RDA device name that shall be used by the clients to access all published properties.
   */
  private final String deviceName;

  /**
   * Default constructor
   * @param serverName The device name that is used for creating the RDA server
   * @param device The device name that shall be used for by the clients to access the properties
   * @throws InternalException In case of a problem at creation time of the RDA server
   */
  public RdaPublisher(final String serverName, final String device) throws InternalException {
    super(serverName);
    this.deviceName = device;
    LOG.info("Registered RDA device " + device + " for server: " + serverName);
  }
  
  /**
   * This method has to be called in order to start the RDA publisher
   */
  public void start() {
    Thread daemonThread = new Thread(new Runnable() {
      public void run() {
        try {
          LOG.info("Starting RDA device server");
          runServer();
        }
        catch (Exception e) {
          LOG.fatal("A major problem occured while running the RDA server. Stopping publisher!", e);
          System.exit(1);
        }
      }
    });
    daemonThread.start();
  }

  /**
   * Private method to find a property by its property name
   * @param name The name of the device which corresponds to the data tag name
   * @return A reference to the device
   * @throws BadParameter In case the device is not known.
   */
  private SimpleProperty findProperty(final String name) throws BadParameter {
    SimpleProperty property = properties.get(name);
    if (property == null) {
      throw new BadParameter("Property '" + name + "' not found");
    }
    return property;
  }

  @Override
  public Data get(final IOPoint iop, final Data context) throws BadParameter, IOError {
    if (!iop.getDeviceName().equalsIgnoreCase(deviceName)) {
      LOG.warn("GET - Rejected request for unknown device " + iop.getDeviceName());
      throw new BadParameter("Device name " + iop.getDeviceName() + " unknown by server " + getServerName());
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("GET - " + iop.getDeviceName() + "/" + iop.getPropertyName());
    }
    SimpleProperty property = findProperty(iop.getPropertyName());
    return property.get(iop, context);
  }

  @Override
  public void set(final IOPoint iop, final Data value, final Data context) throws BadParameter, IOError {
    if (!iop.getDeviceName().equalsIgnoreCase(deviceName)) {
      LOG.warn("SET - Rejected request for unknown device " + iop.getDeviceName());
      throw new BadParameter("Device name " + iop.getDeviceName() + " unknown by server " + getServerName());
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("SET - " + iop.getDeviceName() + "/" + iop.getPropertyName());
    }
    SimpleProperty property = findProperty(iop.getPropertyName());
    property.set(iop, value, context);
  }

  @Override
  public void monitorOn(final IOPoint iop, final ValueChangeListener listener) throws BadParameter {
    SimpleProperty property = findProperty(iop.getPropertyName());
    property.monitorOn(iop, listener);
  }

  @Override
  public void monitorOff(final IOPoint iop, final ValueChangeListener listener) {
    try {
      SimpleProperty property = findProperty(iop.getPropertyName());
      property.monitorOff(iop, listener);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }


  /**
   * Updates the corresponding {@link SimpleProperty} instance about the value
   * update. In case of a new (yet) unknown tag a new {@link SimpleProperty} instance
   * is first of all created.
   */
  @Override
  public void onUpdate(final ClientDataTagValue cdt) {
    if (cdt.getDataTagQuality().isExistingTag()) {
      String propertyName = cdt.getName();
      if (!properties.containsKey(cdt.getName())) {
        SimpleProperty property = new SimpleProperty();
        properties.put(propertyName, property);
      }
      
      properties.get(propertyName).onUpdate(cdt);
    }
  }
}

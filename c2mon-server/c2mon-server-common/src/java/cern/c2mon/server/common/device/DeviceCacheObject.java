/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2014 CERN. This program is free software; you can
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
package cern.c2mon.server.common.device;

import java.util.Map;

/**
 * This class implements the <code>Device</code> interface and resides in the
 * server Device cache.
 *
 * @author Justin Lewis Salmon
 */
public class DeviceCacheObject implements Device {

  /**
   * The unique ID of this device.
   */
  private final Long id;

  /**
   * The name of this device.
   */
  private final String name;

  /**
   * The unique ID of the class to which this device belongs.
   */
  private final Long deviceClassId;

  /**
   * The property mapping (property name : tag ID) of this device.
   */
  private Map<String, Long> propertyValues;

  /**
   * The command mapping (command name : tag ID) of this device.
   */
  private Map<String, Long> commandValues;

  /**
   * Default constructor.
   *
   * @param id the unique ID of this device
   * @param name the name of this device
   * @param deviceClassId the ID of the class to which this device belongs
   */
  public DeviceCacheObject(final Long id, final String name, final Long deviceClassId) {
    this.id = id;
    this.name = name;
    this.deviceClassId = deviceClassId;
  }

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Long getDeviceClassId() {
    return deviceClassId;
  }

  @Override
  public Map<String, Long> getPropertyValues() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, Long> getCommandValues() {
    // TODO Auto-generated method stub
    return null;
  }

}

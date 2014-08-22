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
package cern.c2mon.server.cache.loading.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.dbaccess.DeviceClassMapper;
import cern.c2mon.server.cache.loading.DeviceClassDAO;
import cern.c2mon.server.cache.loading.common.AbstractBatchLoaderDAO;
import cern.c2mon.server.common.device.DeviceClass;

/**
 * DeviceClass loader DAO implementation.
 *
 * @author Justin Lewis Salmon
 */
@Service("deviceClassDAO")
public class DeviceClassDAOImpl extends AbstractBatchLoaderDAO<DeviceClass> implements DeviceClassDAO {

  /**
   * Reference to the DeviceClass MyBatis loader.
   */
  private DeviceClassMapper deviceClassMapper;

  @Autowired
  public DeviceClassDAOImpl(final DeviceClassMapper deviceClassMapper) {
    super(deviceClassMapper);
    this.deviceClassMapper = deviceClassMapper;
  }

  @Override
  public DeviceClass getItem(Object id) {
    return deviceClassMapper.getItem(id);
  }

  @Override
  protected DeviceClass doPostDbLoading(DeviceClass item) {
    return item;
  }
}

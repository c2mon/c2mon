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
package cern.c2mon.server.cache.loader.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.dbaccess.AliveTimerMapper;
import cern.c2mon.server.cache.loader.AliveTimerDAO;
import cern.c2mon.server.cache.loader.common.AbstractDefaultLoaderDAO;
import cern.c2mon.server.common.alive.AliveTimer;

/**
 * DAO for AliveTimer cache loading.
 *
 * @author Mark Brightwell
 */
//TODO: refer a name
@Service("aliveTimerDAORef")
public class AliveTimerDAOImpl extends AbstractDefaultLoaderDAO<AliveTimer> implements AliveTimerDAO {

  /**
   * Reference to mapper.
   */
  private AliveTimerMapper aliveTimerMapper;

  @Autowired
  public AliveTimerDAOImpl(AliveTimerMapper aliveTimerMapper) {
    super(1000, aliveTimerMapper);
    this.aliveTimerMapper = aliveTimerMapper;
  }

  @Override
  protected AliveTimer doPostDbLoading(AliveTimer item) {
    //do nothing for this cache
    return item;
  }

}

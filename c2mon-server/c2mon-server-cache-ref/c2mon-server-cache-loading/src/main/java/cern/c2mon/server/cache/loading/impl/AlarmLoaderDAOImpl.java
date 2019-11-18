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
package cern.c2mon.server.cache.loading.impl;

import cern.c2mon.server.cache.dbaccess.AlarmMapper;
import cern.c2mon.server.cache.loading.AlarmLoaderDAO;
import cern.c2mon.server.cache.loading.CacheLoaderName;
import cern.c2mon.server.cache.loading.common.AbstractBatchLoaderDAO;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * The DAO for loading Alarms into the cache from the database.
 *
 * @author Mark Brightwell
 */
@Slf4j
@Service(CacheLoaderName.Names.ALARM)
public class AlarmLoaderDAOImpl extends AbstractBatchLoaderDAO<Long, Alarm> implements AlarmLoaderDAO {

  /**
   * Reference to the required mapper.
   */
  private AlarmMapper alarmMapper;

  @Inject
  public AlarmLoaderDAOImpl(AlarmMapper alarmMapper) {
    super(alarmMapper);
    this.alarmMapper = alarmMapper;
  }

  @Override
  public void deleteItem(Long id) {
    alarmMapper.deleteAlarm(id);
  }

  @Override
  public void insert(Alarm alarm) {
    // TODO Auto-generated method stub
    alarmMapper.insertAlarm((AlarmCacheObject) alarm);
  }

  @Override
  public void updateConfig(Alarm alarm) {
    alarmMapper.updateConfig(alarm);
  }

  @Override
  protected Alarm doPostDbLoading(Alarm item) {
    log.trace(item.toString(true));
    return item;
  }

}
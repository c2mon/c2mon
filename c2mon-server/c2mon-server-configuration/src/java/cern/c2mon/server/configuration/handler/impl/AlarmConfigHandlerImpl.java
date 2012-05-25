/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.configuration.handler.impl;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;

import cern.c2mon.server.configuration.handler.AlarmConfigHandler;
import cern.c2mon.server.configuration.handler.transacted.AlarmConfigTransacted;
import cern.tim.server.cache.AlarmCache;
import cern.tim.server.cache.AlarmFacade;
import cern.tim.server.common.alarm.Alarm;
import cern.tim.shared.client.configuration.ConfigurationElement;
import cern.tim.shared.client.configuration.ConfigurationElementReport;

/**
 * See interface documentation.
 * 
 * @author Mark Brightwell
 *
 */
@Service
public class AlarmConfigHandlerImpl implements AlarmConfigHandler {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(AlarmConfigHandlerImpl.class);  
  
  /**
   * Transacted bean.
   */
  @Autowired
  private AlarmConfigTransacted alarmConfigTransacted;
  
  /**
   * Cache.
   */
  private AlarmCache alarmCache;
  
  private AlarmFacade alarmFacade;
  
  @Autowired
  public AlarmConfigHandlerImpl(AlarmCache alarmCache, 
                                AlarmFacade alarmFacade) {
    super();
    this.alarmCache = alarmCache;
    this.alarmFacade = alarmFacade;
  }

  /**
   * Removes the alarm from the system (including datatag reference to it).
   * 
   * <p>In more detail, removes the reference to the alarm in the associated
   * tag, removes the alarm from the DB and removes the alarm form the cache,
   * in that order.
   * 
   * @param alarmId the id of the alarm to remove
   * @param alarmReport the configuration report for the alarm removal
   */
  @Override
  public void removeAlarm(final Long alarmId, final ConfigurationElementReport alarmReport) {
    alarmConfigTransacted.doRemoveAlarm(alarmId, alarmReport);    
    alarmCache.remove(alarmId); //will be skipped if rollback exception thrown in do method    
  }

  @Override
  public void createAlarm(ConfigurationElement element) throws IllegalAccessException {
    alarmConfigTransacted.doCreateAlarm(element);
    alarmFacade.evaluateAlarm(element.getEntityId());
    Alarm alarm = alarmCache.get(element.getEntityId());
    alarmCache.lockAndNotifyListeners(alarm);
  }

  @Override
  public void updateAlarm(Long alarmId, Properties properties) {
    try {
      alarmConfigTransacted.doUpdateAlarm(alarmId, properties);
      alarmFacade.evaluateAlarm(alarmId);
    } catch (UnexpectedRollbackException e) {
      LOGGER.error("Rolling back Alarm update in cache");
      alarmCache.remove(alarmId);
      alarmCache.loadFromDb(alarmId);
      throw e;      
    }    
  }
  
  

}
